package dev.jonz94.yatpazhtw

import be.seeseemelk.mockbukkit.MockBukkit
import be.seeseemelk.mockbukkit.ServerMock
import be.seeseemelk.mockbukkit.entity.PlayerMock
import be.seeseemelk.mockbukkit.entity.PlayerMockFactory
import org.bukkit.ChatColor
import org.bukkit.Location
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.random.Random

class ExecutorTests {
    private lateinit var server: ServerMock
    private lateinit var plugin: Plugin

    @BeforeEach
    fun setUp() {
        // Start the mock server
        server = MockBukkit.mock()

        // Load plugin
        plugin = MockBukkit.load(Plugin::class.java)
    }

    @AfterEach
    fun tearDown() {
        // Stop the mock server
        MockBukkit.unmock()
    }

    private fun assertSourcePlayerSendTeleportRequestToTargetPlayer(
        source: PlayerMock,
        target: PlayerMock
    ) {
        source.performCommand("tpa ${target.name}")

        val expectedMessageToSource = "${ChatColor.GOLD}已經向 ${ChatColor.RED}${target.name}${ChatColor.GOLD} 發出傳送請求"
        source.assertSaid(expectedMessageToSource)
        source.assertNoMoreSaid()

        val expectedMessageToTarget =
            "${ChatColor.RED}${source.name}${ChatColor.GOLD} 想要傳送到你目前所在的位置\n" +
                "使用指令 ${ChatColor.RED}/tpok${ChatColor.GOLD} 或 ${ChatColor.RED}/tpaccept${ChatColor.GOLD} 可以接受此請求\n" +
                "使用指令 ${ChatColor.RED}/tpno${ChatColor.GOLD} 或 ${ChatColor.RED}/tpdeny${ChatColor.GOLD} 可以拒絕此請求"
        target.assertSaid(expectedMessageToTarget)
        target.assertNoMoreSaid()
    }

    @Test
    fun sourcePlayerSendTeleportRequestToTargetPlayer() {
        val source = server.addPlayer()
        val target = server.addPlayer()

        assertSourcePlayerSendTeleportRequestToTargetPlayer(source, target)
    }

    @Test
    fun playerWillGetErrorMessageWhenUsingTpaCommandWithoutArguments() {
        val player = server.addPlayer()

        player.performCommand("tpa")

        val expectedMessage = "${ChatColor.RED}指令的格式有誤！"
        player.assertSaid(expectedMessage)
        player.assertNoMoreSaid()
    }

    @Test
    fun playerWillGetErrorMessageWhenUsingTpaCommandWithMoreThanOneArgument() {
        val player = server.addPlayer()
        val expectedMessage = "${ChatColor.RED}指令的格式有誤！"

        player.performCommand("tpa one two")
        player.assertSaid(expectedMessage)

        player.performCommand("tpa one two three")
        player.assertSaid(expectedMessage)

        player.performCommand("tpa one two three four")
        player.assertSaid(expectedMessage)

        player.assertNoMoreSaid()
    }

    @Test
    fun sourcePlayerWillGetErrorMessageIfTargetPlayerIsNotFound() {
        val source = server.addPlayer("a")
        source.performCommand("tpa player_not_exist")

        val expectedMessage = "${ChatColor.RED}找不到此玩家，你可能打錯名字或是他目前不在線上"
        source.assertSaid(expectedMessage)
        source.assertNoMoreSaid()
    }

    @Test
    fun sourcePlayerWillGetErrorMessageIfTargetPlayerIsOffline() {
        val playerFactory = PlayerMockFactory(server)
        val source = playerFactory.createRandomPlayer()
        val target = playerFactory.createRandomOfflinePlayer()

        source.performCommand("tpa ${target.name}")

        val expectedMessage = "${ChatColor.RED}找不到此玩家，你可能打錯名字或是他目前不在線上"
        source.assertSaid(expectedMessage)
        source.assertNoMoreSaid()
    }

    @Test
    fun playerWillGetErrorMessageWhenTryingSendTeleportRequestToHimself() {
        val player = server.addPlayer()

        player.performCommand("tpa ${player.name}")

        val expectedMessage = "${ChatColor.RED}你為什麼想要發出傳送請求給你自己？？？"
        player.assertSaid(expectedMessage)
        player.assertNoMoreSaid()
    }

    @Test
    fun playerWillGetMessageWhenSendingMoreThanOneTeleportRequest() {
        val source = server.addPlayer()
        val firstTarget = server.addPlayer()
        val secondTarget = server.addPlayer()

        source.performCommand("tpa ${firstTarget.name}")

        var expectedMessage = "${ChatColor.GOLD}已經向 ${ChatColor.RED}${firstTarget.name}${ChatColor.GOLD} 發出傳送請求"
        source.assertSaid(expectedMessage)
        source.assertNoMoreSaid()

        source.performCommand("tpa ${secondTarget.name}")

        expectedMessage =
            "${ChatColor.GOLD}你剛才已經向 ${ChatColor.RED}${firstTarget.name}${ChatColor.GOLD} 發送過傳送請求了\n" +
                "你可以使用指令 ${ChatColor.RED}/tpcancel${ChatColor.GOLD} 來取消此請求"
        source.assertSaid(expectedMessage)
        source.assertNoMoreSaid()
    }

    @Test
    fun sourcePlayerWillTeleportToTargetPlayerWhenTargetPlayerTypeTpokCommand() {
        val source = server.addPlayer()
        val target = server.addPlayer()
        val world = server.addSimpleWorld("test")

        assertSourcePlayerSendTeleportRequestToTargetPlayer(source, target)

        val randomNumbers = List(6) { Random.nextDouble() * 1_000_000 }
        val x1 = randomNumbers[0]
        val y1 = randomNumbers[1]
        val z1 = randomNumbers[2]
        val x2 = randomNumbers[3]
        val y2 = randomNumbers[4]
        val z2 = randomNumbers[5]
        source.simulatePlayerMove(Location(world, x1, y1, z1))
        target.simulatePlayerMove(Location(world, x2, y2, z2))

        // println(source.location)
        // println(target.location)

        target.performCommand("tpok")

        val expectedMessageToSource = "${ChatColor.RED}${target.name}${ChatColor.GOLD} 接受了你的傳送請求"
        source.assertSaid(expectedMessageToSource)
        source.assertNoMoreSaid()

        val expectedMessageToTarget = "${ChatColor.GOLD}你已接受 ${ChatColor.RED}${source.name}${ChatColor.GOLD} 的傳送請求"
        target.assertSaid(expectedMessageToTarget)
        target.assertNoMoreSaid()

        // println(source.location)
        // println(target.location)

        // FIXME: this will failed, because /tp command is not found when running testing
        // source.assertTeleported(target.location, 0.toDouble())
    }
}
