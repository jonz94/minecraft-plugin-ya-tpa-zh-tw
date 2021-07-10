package dev.jonz94.yatpazhtw

import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.*

class Executor : CommandExecutor {
    private val records = mutableMapOf<UUID, UUID>()

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage("${ChatColor.RED}只有玩家才可以使用這個指令")

            return true
        }

        when (command.name) {
            "tpa" -> request(sender, args)
            "tpaccept", "tpok" -> accept(sender)
            "tpdeny", "tpno" -> deny(sender)
            "tpcancel" -> cancel(sender)
        }

        return true
    }

    private fun request(sender: CommandSender, args: Array<out String>) {
        val source = sender as Player
        if (args.size != 1) {
            source.sendMessage("${ChatColor.RED}指令的格式有誤！")

            return
        }

        val server = sender.server
        val target = server.getPlayer(args[0])

        if (target == null || !server.onlinePlayers.contains(target)) {
            source.sendMessage("${ChatColor.RED}找不到此玩家，你可能打錯名字或是他目前不在線上")

            return
        }

        if (source.uniqueId == target.uniqueId) {
            source.sendMessage("${ChatColor.RED}你為什麼想要發出傳送請求給你自己？？？")

            return
        }

        if (records.containsKey(source.uniqueId)) {
            val targetUniqueIdInRecord = records.getValue(source.uniqueId)
            val targetNameInRecord = server.getPlayer(targetUniqueIdInRecord)?.name ?: "某個人"
            source.sendMessage(
                """
                ${ChatColor.GOLD}你剛才已經向 ${ChatColor.RED}${targetNameInRecord}${ChatColor.GOLD} 發送過傳送請求了
                你可以使用指令 ${ChatColor.RED}/tpcancel${ChatColor.GOLD} 來取消此請求
                """.trimIndent()
            )

            return
        }

        records[source.uniqueId] = target.uniqueId

        source.sendMessage("${ChatColor.GOLD}已經向 ${ChatColor.RED}${target.name}${ChatColor.GOLD} 發出傳送請求")
        target.sendMessage(
            """
            ${ChatColor.RED}${source.name}${ChatColor.GOLD} 想要傳送到你目前所在的位置
            使用指令 ${ChatColor.RED}/tpok${ChatColor.GOLD} 或 ${ChatColor.RED}/tpaccept${ChatColor.GOLD} 可以接受此請求
            使用指令 ${ChatColor.RED}/tpno${ChatColor.GOLD} 或 ${ChatColor.RED}/tpdeny${ChatColor.GOLD} 可以拒絕此請求
            """.trimIndent()
        )
    }

    private fun accept(sender: CommandSender) {
        val target = sender as Player

        if (!records.containsValue(target.uniqueId)) {
            target.sendMessage("${ChatColor.RED}你目前沒有任何傳送請求可以接受")

            return
        }

        val server = sender.server
        val sourceUniqueIdInRecord = records.keys.first { target.uniqueId == records[it] }
        val source = server.getPlayer(sourceUniqueIdInRecord)

        if (source == null) {
            target.sendMessage("${ChatColor.RED}找不到發出此傳送請求的玩家，已自動忽略此請求")
        } else {
            server.dispatchCommand(server.consoleSender, "tp ${source.name} ${target.name}")
            source.sendMessage("${ChatColor.RED}${target.name}${ChatColor.GOLD} 接受了你的傳送請求")
            target.sendMessage("${ChatColor.GOLD}你已接受 ${ChatColor.RED}${source.name}${ChatColor.GOLD} 的傳送請求")
        }

        records.remove(sourceUniqueIdInRecord)
    }

    private fun deny(sender: CommandSender) {
        val target = sender as Player

        if (!records.containsValue(target.uniqueId)) {
            target.sendMessage("${ChatColor.RED}你目前沒有任何傳送請求可以拒絕")

            return
        }

        val server = sender.server
        val sourceUniqueIdInRecord = records.keys.first { target.uniqueId == records[it] }
        val source = server.getPlayer(sourceUniqueIdInRecord)

        if (source == null) {
            target.sendMessage("${ChatColor.RED}找不到發出此傳送請求的玩家，已自動忽略此請求")
        } else {
            source.sendMessage("${ChatColor.RED}${target.name}${ChatColor.GOLD} 拒絕了你的傳送請求")
            target.sendMessage("${ChatColor.GOLD}你已拒絕了 ${ChatColor.RED}${source.name}${ChatColor.GOLD} 的傳送請求")
        }

        records.remove(sourceUniqueIdInRecord)
    }

    private fun cancel(sender: CommandSender) {
        val source = sender as Player

        if (!records.containsKey(source.uniqueId)) {
            source.sendMessage("${ChatColor.RED}你目前還沒有發出過任何傳送請求")

            return
        }

        val server = sender.server
        val targetUniqueId = records.getValue(source.uniqueId)
        val target = server.getPlayer(targetUniqueId)

        if (target == null) {
            source.sendMessage("${ChatColor.GOLD}你已經取消了此傳送請求")
        } else {
            source.sendMessage("${ChatColor.GOLD}你已經取消了向 ${ChatColor.RED}${target.name}${ChatColor.GOLD} 的傳送請求")
            target.sendMessage("${ChatColor.RED}${source.name}${ChatColor.GOLD} 取消了傳送請求")
        }

        records.remove(source.uniqueId)
    }
}
