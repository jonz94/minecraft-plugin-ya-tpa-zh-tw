package dev.jonz94.yatpazhtw

import org.bukkit.plugin.PluginDescriptionFile
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.plugin.java.JavaPluginLoader
import java.io.File

@Suppress("unused")
open class Plugin : JavaPlugin {
    constructor() : super()

    // Constructor needed for tests.
    protected constructor(
        loader: JavaPluginLoader,
        description: PluginDescriptionFile,
        dataFolder: File,
        file: File
    ) : super(loader, description, dataFolder, file)

    override fun onEnable() {
        initializeCommands()
    }

    private fun initializeCommands() {
        val availableCommands = listOf("tpa", "tpaccept", "tpok", "tpdeny", "tpno", "tpcancel")
        val executor = Executor()

        availableCommands.forEach { getCommand(it)?.setExecutor(executor) }
    }
}
