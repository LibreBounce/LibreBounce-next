/*
 * LibreBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LibreBounce/
 */
package net.librebounce.file.configs

import com.google.gson.JsonObject
import net.librebounce.LibreBounce
import net.librebounce.LibreBounce.commandManager
import net.librebounce.LibreBounce.moduleManager
import net.librebounce.file.FileConfig
import net.librebounce.file.FileManager
import net.librebounce.file.FileManager.PRETTY_GSON
import net.librebounce.file.configs.models.ClientConfiguration
import net.librebounce.utils.io.readJson
import java.io.*

class ValuesConfig(file: File) : FileConfig(file) {

    /**
     * Load config from file
     *
     * @throws IOException
     */
    @Throws(IOException::class)
    override fun loadConfig() {
        val json = file.readJson() as? JsonObject ?: return

        val prevVersion = json["ClientVersion"]?.asString ?: "unknown"
        // Compare the versions
        if (prevVersion != LibreBounce.clientVersionText) {
            // Run backup
            FileManager.backupAllConfigs(prevVersion, LibreBounce.clientVersionText)
        }

        for ((key, value) in json.entrySet()) {
            when {
                key.equals("CommandPrefix", true) -> {
                    commandManager.prefix = value.asString
                }

                /*key.equals(ClientRichPresence.name, true) -> {
                    ClientRichPresence.fromJson(value)
                }

                key.equals(Targets.name, true) -> {
                    Targets.fromJson(value)
                }

                key.equals(ClientFixes.name, true) -> {
                    ClientFixes.fromJson(value)
                }


                key.equals("liquidchat", true) -> {
                    val jsonValue = value as JsonObject
                    if (jsonValue.has("token")) jwtToken = jsonValue["token"].asString
                }

                key.equals("DonatorCape", true) -> {
                    val jsonValue = value as JsonObject
                    if (jsonValue.has("TransferCode")) {
                        CapeService.knownToken = jsonValue["TransferCode"].asString
                    }
                }*/

                key.equals(ClientConfiguration.name, true) -> {
                    ClientConfiguration.fromJson(value)
                }

// Deprecated
// Compatibility with old versions
                key.equals("background", true) -> {
                    val jsonValue = value as JsonObject
                    if (jsonValue.has("Enabled")) ClientConfiguration.customBackground = jsonValue["Enabled"].asBoolean
                    if (jsonValue.has("Particles")) ClientConfiguration.particles = jsonValue["Particles"].asBoolean
                }

                /*key.equals("popup", true) -> {
                    val jsonValue = value as JsonObject
                    if (jsonValue.has("lastWarningTime")) TitleScreen.lastWarningTime =
                        jsonValue["lastWarningTime"].asLong
                }*/

                else -> {
                    val module = moduleManager[key] ?: continue

                    val jsonModule = value as JsonObject
                    for (moduleValue in module.values) {
                        val element = jsonModule[moduleValue.name]
                        if (element != null) moduleValue.fromJson(element)
                    }
                }
            }
        }
    }

    /**
     * Save config to file
     *
     * @throws IOException
     */
    @Throws(IOException::class)
    override fun saveConfig() {
        val jsonObject = JsonObject()
        jsonObject.run {
            addProperty("CommandPrefix", commandManager.prefix)
            addProperty("ClientVersion", LibreBounce.clientVersionText)
        }

        //jsonObject.add(Targets.name, Targets.toJson())

        jsonObject.add(ClientConfiguration.name, ClientConfiguration.toJson())

        for (module in moduleManager) {
            if (module.values.isEmpty()) continue

            val jsonModule = JsonObject()
            for (value in module.values) jsonModule.add(value.name, value.toJson())
            jsonObject.add(module.name, jsonModule)
        }

        val popupData = JsonObject()
        jsonObject.add("popup", popupData)

        file.writeText(PRETTY_GSON.toJson(jsonObject))
    }
}
