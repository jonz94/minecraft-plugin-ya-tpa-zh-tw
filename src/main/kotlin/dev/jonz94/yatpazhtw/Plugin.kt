package dev.jonz94.yatpazhtw

import org.bukkit.plugin.java.JavaPlugin

@Suppress("unused")
class Plugin : JavaPlugin() {
    override fun onEnable() {
        initializeCommands()
    }

    private fun initializeCommands() {
        val availableCommands = listOf("tpa", "tpaccept", "tpok", "tpdeny", "tpno", "tpcancel")
        val executor = Executor()

        availableCommands.forEach { getCommand(it)?.setExecutor(executor) }
    }
}
