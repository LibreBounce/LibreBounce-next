/*
 * LibreBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LibreBounce/
 */
package net.librebounce

import com.formdev.flatlaf.themes.FlatMacLightLaf
import kotlinx.coroutines.launch
import net.fabricmc.loader.api.FabricLoader
import net.librebounce.api.ClientUpdate.gitInfo
//import net.librebounce.api.loadSettings
import net.librebounce.event.ClientShutdownEvent
import net.librebounce.event.EventManager
import net.librebounce.event.StartupEvent
import net.librebounce.features.command.CommandManager
import net.librebounce.features.command.CommandManager.registerCommands
import net.librebounce.features.module.ModuleManager
import net.librebounce.features.module.ModuleManager.registerModules
import net.librebounce.features.ui.elements.clickgui.dropdown.Dropdown
import net.librebounce.file.FileManager
import net.librebounce.file.FileManager.loadAllConfigs
import net.librebounce.file.FileManager.saveAllConfigs
//import net.librebounce.file.configs.models.ClientConfiguration.updateClientWindow
import net.librebounce.lang.LanguageManager.loadLanguages
import net.librebounce.utils.attack.CombatUtils
/*import net.librebounce.ui.client.clickgui.ClickGui
import net.librebounce.ui.client.clickgui.style.styles.panel.PanelStyle
import net.librebounce.ui.client.hud.HUD
import net.librebounce.ui.font.Fonts*/
//import net.librebounce.utils.client.BlinkUtils
import net.librebounce.utils.client.ClientUtils.LOGGER
//import net.librebounce.utils.client.MinecraftInstance
import net.librebounce.utils.client.PacketUtils
//import net.librebounce.utils.inventory.InventoryManager
import net.librebounce.utils.inventory.InventoryUtils
import net.librebounce.utils.inventory.SilentHotbar
/*import net.librebounce.utils.io.MiscUtils
import net.librebounce.utils.io.MiscUtils.showErrorPopup*/
import net.librebounce.utils.kotlin.SharedScopes
import net.librebounce.utils.movement.BPSUtils
import net.librebounce.utils.movement.MovementUtils
import net.librebounce.utils.movement.TimerBalanceUtils
import net.librebounce.utils.timing.TickedActions
import net.librebounce.utils.timing.WaitTickUtils
//import net.librebounce.utils.render.MiniMapRegister
//import net.librebounce.utils.render.shader.Background
import net.librebounce.utils.rotation.RotationUtils
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future
import javax.swing.UIManager

object LibreBounce {

    /**
     * Client Information
     *
     * This has all the basic information.
     */
    const val CLIENT_NAME = "LibreBounce"
    const val CLIENT_AUTHOR = "CCBlueX"
    const val CLIENT_CLOUD = "https://cloud.liquidbounce.net/LibreBounce"
    const val CLIENT_WEBSITE = "https://github.com/LibreBounce/LibreBounce"
    const val CLIENT_GITHUB = "https://github.com/LibreBounce/LibreBounce"

    const val MINECRAFT_VERSION = "1.8.9"

    var clientVersionText: String = "unknown"//gitInfo["git.build.version"]?.toString() ?: "unknown"
    val clientVersionNumber = clientVersionText.substring(1).toIntOrNull() ?: 0 // version format: "v<MAJOR.MINOR.PATCH>"
    val clientCommit = gitInfo["git.commit.id.abbrev"]?.let { "git-$it" } ?: "unknown"
    val clientBranch = gitInfo["git.branch"]?.toString() ?: "unknown"

    /**
     * Defines if the client is in development mode.
     * This will enable update checking on commit time instead of regular versioning.
     */
    const val IN_DEV = true

    val nightlyText = if (IN_DEV) " (Nightly) " else " "

    // Perhaps the client commit number should be omitted? Not certain
    val clientTitle = CLIENT_NAME + " " + clientVersionText + nightlyText + clientCommit + " | " + MINECRAFT_VERSION

    var isStarting = true

    val moduleManager = ModuleManager
    val commandManager = CommandManager
    val eventManager = EventManager
    val fileManager = FileManager

    // HUD & ClickGUI
    val clickGui = Dropdown
    /*val hud = HUD

    val clickGui = ClickGui
    val panelGui = PanelStyle*/

    // Menu Background
    //var background: Background? = null

    // Discord RPC
    //val clientRichPresence = ClientRichPresence

    /**
     * Start IO tasks
     */
    fun initClient(): Future<*> {
        // Change theme of Swing
        UIManager.setLookAndFeel(FlatMacLightLaf())

        val future = CompletableFuture<Unit>()

        SharedScopes.IO.launch {
            try {
                LOGGER.info("Starting preload tasks of $CLIENT_NAME")

                //Fonts.downloadFonts()

                loadLanguages()

                // Load alt generators
                //loadActiveGenerators()

                // Load SRG file
                //loadSrg()

                LOGGER.info("Preload tasks of $CLIENT_NAME are completed!")

                future.complete(Unit)
            } catch (e: Exception) {
                future.completeExceptionally(e)
            }
        }

        return future
    }

    /**
     * Execute if client will be started
     */
    fun init() {
        isStarting = true

        val loader: FabricLoader = FabricLoader.getInstance()
        clientVersionText = loader.getModContainer("librebounce").orElseThrow().metadata.version.toString()

        LOGGER.info("Starting $CLIENT_NAME $clientVersionText $clientCommit, by $CLIENT_AUTHOR")

        try {
            //Fonts.loadFonts()

            // Register listeners
            /*RotationUtils
            ClientFixes
            BungeeCordSpoof
            CapeService
            InventoryUtils
            InventoryManager
            MiniMapRegister
            TickedActions
            MovementUtils
            PacketUtils
            TimerBalanceUtils
            BPSUtils
            WaitTickUtils
            SilentHotbar
            BlinkUtils*/

            RotationUtils
            InventoryUtils
            TimerBalanceUtils
            BPSUtils
            CombatUtils
            MovementUtils
            PacketUtils
            SilentHotbar
            TickedActions
            WaitTickUtils

            // Load online settings
            /*loadSettings(false) {
                LOGGER.info("Successfully loaded ${it.size} settings.")
            }*/

            registerCommands()
            registerModules()

            /*runCatching {
                // Remapper
                loadSrg()

                if (!Remapper.mappingsLoaded) {
                    error("Failed to load SRG mappings.")
                }

                // ScriptManager
                loadScripts()
                enableScripts()
            }.onFailure {
                LOGGER.error("Failed to load scripts.", it)
            }*/

            // Load configs
            loadAllConfigs()

            // Update client window
            //updateClientWindow()

            // Setup Discord RPC
            /*if (showRPCValue) {
                SharedScopes.IO.launch {
                    try {
                        clientRichPresence.setup()
                    } catch (throwable: Throwable) {
                        LOGGER.error("Failed to setup Discord RPC.", throwable)
                    }
                }
            }

            // Login into known token if not empty
            if (CapeService.knownToken.isNotBlank()) {
                SharedScopes.IO.launch {
                    runCatching {
                        CapeService.login(CapeService.knownToken)
                    }.onFailure {
                        LOGGER.error("Failed to login into known cape token.", it)
                    }.onSuccess {
                        LOGGER.info("Successfully logged in into known cape token.")
                    }
                }
            }

            // Refresh cape service
            CapeService.refreshCapeCarriers {
                LOGGER.info("Successfully loaded ${it.size} cape carriers.")
            }*/

            // Load background
            //FileManager.loadBackground()
        } catch (e: Exception) {
            LOGGER.error("Failed to start client: ${e.message}")
            //e.showErrorPopup()
        } finally {
            // Set is starting status
            isStarting = false

            /*if (!FileManager.firstStart && FileManager.backedup) {
                SharedScopes.IO.launch {
                    MiscUtils.showMessageDialog("Warning: backup triggered", "Client update detected! Please check the config folder.")
                }
            }*/

            EventManager.call(StartupEvent)
            LOGGER.info("Successfully started client")
        }
    }

    /**
     * Execute if client will be stopped
     */
    fun exit() {
        // Call client shutdown
        EventManager.call(ClientShutdownEvent)

        // Stop all CoroutineScopes
        SharedScopes.stop()

        // Save all available configs
        saveAllConfigs()
    }
}
