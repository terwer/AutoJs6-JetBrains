package org.autojs.autojs6.jetbrains

import org.autojs.autojs6.jetbrains.adb.AdbService
import org.autojs.autojs6.jetbrains.device.AutoJs6Frame
import org.autojs.autojs6.jetbrains.device.FrameCodec
import org.autojs.autojs6.jetbrains.device.JsonCodec
import org.autojs.autojs6.jetbrains.project.AutoJs6ProjectTemplateService
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
class MvpUnitTest {
    @Test fun frameCodecUsesBigEndianLengthAndType() {
        val codec = FrameCodec()
        val encoded = codec.encode(AutoJs6Constants.TYPE_JSON, "{}".toByteArray())
        assertEquals(0, encoded[0].toInt())
        assertEquals(2, encoded[3].toInt())
        assertEquals(1, encoded[7].toInt())
        val frames = codec.feed(encoded)
        assertEquals(1, frames.size)
        assertTrue(frames.single() is AutoJs6Frame.Json)
    }

    @Test fun jsonRoundTripPreservesCommandPayload() {
        val payload = mapOf("id" to 1, "type" to "command", "data" to mapOf("command" to "run", "script" to "toast('ok')"))
        val decoded = JsonCodec.decode(JsonCodec.encode(payload))
        assertEquals("command", decoded.string("type"))
        assertEquals("run", decoded.obj("data")?.string("command"))
    }

    @Test fun parsesAdbDevicesOutput() {
        val devices = AdbService.parseDevices("List of devices attached\nabc123 device product:sdk model:Pixel_8 device:emu\nxyz offline\n")
        assertEquals(1, devices.size)
        assertEquals("abc123", devices[0].id)
        assertEquals("Pixel_8", devices[0].model)
    }

    @Test fun packageSuffixIsPackageSafe() {
        assertEquals("app_123_foo", AutoJs6ProjectTemplateService.normalizePackageSuffix("123 Foo!"))
        assertTrue(AutoJs6ProjectTemplateService.normalizePackageSuffix("中文项目").matches(Regex("[a-z_][a-z0-9_]*")))
    }
    @Test fun deviceHandshakeAndScriptCommandsUseRealFrames() {
        val server = ServerSocket(0)
        val attached = CountDownLatch(1)
        lateinit var ideDevice: org.autojs.autojs6.jetbrains.device.AutoJs6Device
        val acceptThread = Thread {
            val accepted = server.accept()
            ideDevice = org.autojs.autojs6.jetbrains.device.AutoJs6Device(
                accepted,
                { attached.countDown() },
                {},
                null
            )
        }
        acceptThread.isDaemon = true
        acceptThread.start()

        val client = Socket("127.0.0.1", server.localPort)
        val codec = FrameCodec()
        fun sendClientJson(value: Map<String, Any?>) {
            client.getOutputStream().write(codec.encode(AutoJs6Constants.TYPE_JSON, JsonCodec.encode(value)))
            client.getOutputStream().flush()
        }
        fun readClientJson(): org.autojs.autojs6.jetbrains.device.JsonPayload {
            val header = client.getInputStream().readNBytes(8)
            val payloadLen = java.nio.ByteBuffer.wrap(header).int
            java.nio.ByteBuffer.wrap(header).int
            return JsonCodec.decode(client.getInputStream().readNBytes(payloadLen))
        }

        sendClientJson(mapOf("id" to 1, "type" to "hello", "data" to mapOf(
            "device_name" to "ReplayDevice",
            "app_version" to "6.7.0",
            "app_version_code" to "3591",
            "device_id" to "replay-1"
        )))
        assertTrue(attached.await(3, TimeUnit.SECONDS))
        assertEquals("hello", readClientJson().string("type"))

        // wait beyond the old handshake timeout to prove attached sockets stay alive
        Thread.sleep(5500)

        ideDevice.sendCommand("run", mapOf("id" to "main.js", "name" to "main.js", "script" to "toast('run')"))
        assertEquals("run", readClientJson().obj("data")?.string("command"))
        ideDevice.sendCommand("save", mapOf("id" to "main.js", "name" to "main.js", "script" to "toast('save')"))
        assertEquals("save", readClientJson().obj("data")?.string("command"))
        ideDevice.sendCommand("stop", mapOf("id" to "main.js"))
        assertEquals("stop", readClientJson().obj("data")?.string("command"))
        ideDevice.sendCommand("stopAll")
        assertEquals("stopAll", readClientJson().obj("data")?.string("command"))

        ideDevice.close()
        client.close()
        server.close()
    }
    @Test fun activeIpConnectionReplayCanHandshakeAndCloseSocket() {
        val server = ServerSocket(0)
        val attached = CountDownLatch(1)
        lateinit var ideDevice: org.autojs.autojs6.jetbrains.device.AutoJs6Device
        val clientSocket = Socket("127.0.0.1", server.localPort)
        val serverSide = server.accept()
        ideDevice = org.autojs.autojs6.jetbrains.device.AutoJs6Device(clientSocket, { attached.countDown() }, {}, null)
        val codec = FrameCodec()
        serverSide.getOutputStream().write(codec.encode(AutoJs6Constants.TYPE_JSON, JsonCodec.encode(mapOf("id" to 1, "type" to "hello", "data" to mapOf(
            "device_name" to "ServerReplay",
            "app_version" to "6.7.0",
            "app_version_code" to "3591",
            "device_id" to "server-replay-1"
        )))))
        serverSide.getOutputStream().flush()
        assertTrue(attached.await(3, TimeUnit.SECONDS))
        ideDevice.close()
        Thread.sleep(100)
        assertTrue(clientSocket.isClosed)
        serverSide.close()
        server.close()
    }
    @Test fun createsProjectFromBundledTemplate() {
        val parent = java.nio.file.Files.createTempDirectory("autojs6-template-test")
        val target = parent.resolve("My AutoJs6 App")
        try {
            AutoJs6ProjectTemplateService(null).createProject(target)
            val projectJson = java.nio.file.Files.readString(target.resolve("project.json"))
            assertTrue(java.nio.file.Files.exists(target.resolve("main.js")))
            assertTrue(projectJson.contains("\"name\": \"My AutoJs6 App\""))
            assertTrue(projectJson.contains("org.autojs.autojs6.my_autojs6_app"))
        } finally {
            target.toFile().deleteRecursively()
            parent.toFile().deleteRecursively()
        }
    }
}


