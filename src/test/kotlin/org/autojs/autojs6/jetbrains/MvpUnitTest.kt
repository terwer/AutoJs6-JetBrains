package org.autojs.autojs6.jetbrains

import org.autojs.autojs6.jetbrains.adb.AdbService
import org.autojs.autojs6.jetbrains.device.AutoJs6Constants
import org.autojs.autojs6.jetbrains.device.AutoJs6Frame
import org.autojs.autojs6.jetbrains.device.FrameCodec
import org.autojs.autojs6.jetbrains.device.JsonCodec
import org.autojs.autojs6.jetbrains.project.AutoJs6ProjectTemplateService
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

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
}
