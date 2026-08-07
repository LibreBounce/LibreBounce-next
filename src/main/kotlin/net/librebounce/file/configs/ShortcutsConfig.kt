/*
 * LibreBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LibreBounce/
 */
package net.librebounce.file.configs

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import net.librebounce.LibreBounce.commandManager
import net.librebounce.features.command.Command
import net.librebounce.features.command.shortcuts.Shortcut
import net.librebounce.file.FileConfig
import net.librebounce.file.FileManager.PRETTY_GSON
import net.librebounce.utils.io.readJson
import java.io.File
import java.io.IOException

class ShortcutsConfig(file: File) : FileConfig(file) {

    /**
     * Load config from file
     *
     * @throws IOException
     */
    override fun loadConfig() {
        val json = file.readJson() as? JsonArray ?: return

        for (shortcutJson in json) {
            if (shortcutJson !is JsonObject)
                continue

            val name = shortcutJson["name"]?.asString ?: continue
            val scriptJson = shortcutJson["script"]?.asJsonArray ?: continue

            val script = mutableListOf<Pair<Command, Array<String>>>()

            for (scriptCommand in scriptJson) {
                if (scriptCommand !is JsonObject)
                    continue

                val commandName = scriptCommand["name"]?.asString ?: continue
                val arguments = scriptCommand["arguments"]?.asJsonArray ?: continue

                val command = commandManager.getCommand(commandName) ?: continue

                script += command to arguments.map { it.asString }.toTypedArray()
            }

            commandManager.registerCommand(Shortcut(name, script))
        }
    }

    /**
     * Save config to file
     *
     * @throws IOException
     */
    override fun saveConfig() {
        val jsonArray = JsonArray()

        for (command in commandManager.commands) {
            if (command !is Shortcut)
                continue

            val jsonCommand = JsonObject()
            jsonCommand.addProperty("name", command.command)

            val scriptArray = JsonArray()

            for (pair in command.script) {
                val pairObject = JsonObject()

                pairObject.addProperty("name", pair.first.command)

                val argumentsObject = JsonArray()
                /*for (argument in pair.second) {
                    // argumentsObject.add(argument)
                }*/

                pairObject.add("arguments", argumentsObject)

                scriptArray.add(pairObject)
            }

            jsonCommand.add("script", scriptArray)

            jsonArray.add(jsonCommand)
        }

        file.writeText(PRETTY_GSON.toJson(jsonArray))
    }

}
