package org.autojs.autojs6.jetbrains

import org.autojs.autojs6.jetbrains.adb.AdbService
import org.autojs.autojs6.jetbrains.adb.AdbExecutableResolver
import org.autojs.autojs6.jetbrains.actions.AutoJs6CommandDispatcher
import org.autojs.autojs6.jetbrains.connection.AutoJs6NetworkInterfaces
import org.autojs.autojs6.jetbrains.device.AutoJs6Frame
import org.autojs.autojs6.jetbrains.device.AutoJs6DeviceSnapshot
import org.autojs.autojs6.jetbrains.device.FrameCodec
import org.autojs.autojs6.jetbrains.device.JsonCodec
import org.autojs.autojs6.jetbrains.project.AutoJs6ProjectSyncService
import org.autojs.autojs6.jetbrains.project.AutoJs6ProjectTemplateService
import org.autojs.autojs6.jetbrains.project.AUTOJS6_RUN_PROJECT_COMMAND
import org.autojs.autojs6.jetbrains.project.AUTOJS6_SAVE_PROJECT_COMMAND
import org.autojs.autojs6.jetbrains.remote.AutoJs6HttpBridgeService
import org.autojs.autojs6.jetbrains.run.AutoJs6ProjectConfigurationSerializer
import org.autojs.autojs6.jetbrains.run.AutoJs6ScriptConfigurationSerializer
import org.autojs.autojs6.jetbrains.run.formatAutoJs6RunLogText
import org.autojs.autojs6.jetbrains.script.AutoJs6ScriptCommand
import org.autojs.autojs6.jetbrains.statusbar.AutoJs6DeviceStatusText
import org.jdom.Element
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import java.net.ServerSocket
import java.net.Socket
import java.nio.file.Files
import java.nio.file.attribute.FileTime
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.zip.ZipInputStream
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

    @Test fun adbResolverIncludesMacAndroidStudioDefaultSdkPath() {
        val candidates = AdbExecutableResolver.commonUnixCandidates(
            envLookup = { null },
            userHome = "/Users/alice"
        ).map { it.path }

        assertTrue(candidates.contains("/Users/alice/Library/Android/sdk/platform-tools/adb"))
    }

    @Test fun adbResolverUsesAndroidSdkEnvironmentRoots() {
        val candidates = AdbExecutableResolver.commonUnixCandidates(
            envLookup = { key -> if (key == "ANDROID_HOME") "/opt/android-sdk" else null },
            userHome = "/Users/alice"
        ).map { it.path }

        assertEquals("/opt/android-sdk/platform-tools/adb", candidates.first())
    }

    @Test fun packageSuffixIsPackageSafe() {
        assertEquals("app_123_foo", AutoJs6ProjectTemplateService.normalizePackageSuffix("123 Foo!"))
        assertTrue(AutoJs6ProjectTemplateService.normalizePackageSuffix("中文项目").matches(Regex("[a-z_][a-z0-9_]*")))
    }

    @Test fun autoJs6ScriptConfigurationPersistsScriptPath() {
        val element = Element("configuration")
        val scriptPath = "/tmp/autojs6/main.js"
        AutoJs6ScriptConfigurationSerializer.writeScriptPath(element, scriptPath)
        assertEquals(scriptPath, AutoJs6ScriptConfigurationSerializer.readScriptPath(element))

        AutoJs6ScriptConfigurationSerializer.writeScriptPath(element, "")
        assertEquals("", AutoJs6ScriptConfigurationSerializer.readScriptPath(element))
    }

    @Test fun autoJs6ProjectConfigurationPersistsProjectRoot() {
        val element = Element("configuration")
        val projectRoot = "/tmp/autojs6/project"
        AutoJs6ProjectConfigurationSerializer.writeProjectRoot(element, projectRoot)
        assertEquals(projectRoot, AutoJs6ProjectConfigurationSerializer.readProjectRoot(element))

        AutoJs6ProjectConfigurationSerializer.writeProjectRoot(element, "")
        assertEquals("", AutoJs6ProjectConfigurationSerializer.readProjectRoot(element))
    }

    @Test fun runConsoleLogFormatterPrefixesDeviceForEachLine() {
        assertEquals(
            "[Pixel 127.0.0.1:7347] I/AutoJs6: first\n[Pixel 127.0.0.1:7347] second\n",
            formatAutoJs6RunLogText("Pixel", "127.0.0.1:7347", "I/AutoJs6: first\r\nsecond\n")
        )
    }

    @Test fun pluginXmlRegistersRunConfigurationTypeWithPlatformExtensionPoint() {
        val pluginXml = Files.readString(java.nio.file.Path.of("src/main/resources/META-INF/plugin.xml"))

        assertTrue(pluginXml.contains("<configurationType implementation=\"org.autojs.autojs6.jetbrains.run.AutoJs6ScriptConfigurationType\"/>"))
        assertTrue(pluginXml.contains("<configurationType implementation=\"org.autojs.autojs6.jetbrains.run.AutoJs6ProjectConfigurationType\"/>"))
        assertFalse(pluginXml.contains("<runConfigurationType"))
        assertTrue(pluginXml.contains("<runConfigurationProducer implementation=\"org.autojs.autojs6.jetbrains.run.AutoJs6ScriptConfigurationProducer\"/>"))
        assertTrue(pluginXml.contains("<runConfigurationProducer implementation=\"org.autojs.autojs6.jetbrains.run.AutoJs6ProjectConfigurationProducer\"/>"))
        assertTrue(pluginXml.contains("<statusBarWidgetFactory id=\"AutoJs6DeviceStatus\" implementation=\"org.autojs.autojs6.jetbrains.statusbar.AutoJs6DeviceStatusBarWidgetFactory\""))
    }

    @Test fun pluginXmlRegistersAllVscodeParityActionsAndToolWindow() {
        val pluginXml = Files.readString(java.nio.file.Path.of("src/main/resources/META-INF/plugin.xml"))
        val ids = listOf(
            "AutoJs6.ViewDocument",
            "AutoJs6.Connect",
            "AutoJs6.DisconnectAll",
            "AutoJs6.RunCurrentFile",
            "AutoJs6.RunWithoutArguments",
            "AutoJs6.CommandsHierarchy",
            "AutoJs6.RunOnDevice",
            "AutoJs6.StopCurrentScript",
            "AutoJs6.StopAllScripts",
            "AutoJs6.Rerun",
            "AutoJs6.SaveCurrentFile",
            "AutoJs6.SaveToDevice",
            "AutoJs6.NewUntitledFile",
            "AutoJs6.NewProject",
            "AutoJs6.SaveProject",
            "AutoJs6.SaveProjectWithoutArguments",
            "AutoJs6.RunProject",
            "AutoJs6.RunProjectWithoutArguments"
        )
        ids.forEach { id -> assertTrue(pluginXml.contains("id=\"$id\""), "missing action $id") }
        assertTrue(pluginXml.contains("factoryClass=\"org.autojs.autojs6.jetbrains.toolwindow.AutoJs6ToolWindowFactory\""))
        assertTrue(pluginXml.contains("group-id=\"EditorPopupMenu\""))
        assertTrue(pluginXml.contains("group-id=\"ProjectViewPopupMenu\""))
        assertTrue(pluginXml.contains("id=\"AutoJs6.RunProject\" class=\"org.autojs.autojs6.jetbrains.actions.RunProjectAction\""))
        assertTrue(pluginXml.contains("id=\"AutoJs6.SaveProject\" class=\"org.autojs.autojs6.jetbrains.actions.SaveProjectAction\""))
        assertTrue(pluginXml.substringAfter("id=\"AutoJs6.RunProject\"").substringBefore("</action>").contains("group-id=\"AutoJs6.ToolbarGroup\""))
        assertTrue(pluginXml.substringAfter("id=\"AutoJs6.SaveProject\"").substringBefore("</action>").contains("group-id=\"AutoJs6.ToolbarGroup\""))
        assertTrue(pluginXml.contains("first-keystroke=\"F6\""))
        assertTrue(pluginXml.contains("first-keystroke=\"F8\""))
    }

    @Test fun statusBarDeviceTextReflectsSelectionAndEmptyState() {
        val emulator = AutoJs6DeviceSnapshot(
            key = "emulator-5554",
            name = "Pixel_8",
            connectionType = "ADB",
            endpoint = "emulator-5554",
            status = "connected",
            version = "6.7.0 (3591)",
            adbDeviceId = "emulator-5554"
        )
        val phone = AutoJs6DeviceSnapshot(
            key = "phone-1",
            name = "本机",
            connectionType = "LAN server",
            endpoint = "192.168.1.8:7347",
            status = "connected",
            version = "6.7.0 (3591)",
            adbDeviceId = null
        )

        assertEquals("AutoJs6: 无设备", AutoJs6DeviceStatusText.selectedValue(emptyList(), null))
        assertEquals("AutoJs6: Pixel_8", AutoJs6DeviceStatusText.selectedValue(listOf(emulator, phone), emulator.key))
        assertEquals("AutoJs6: 本机", AutoJs6DeviceStatusText.selectedValue(listOf(emulator, phone), phone.key))
        assertEquals("AutoJs6: 2 设备", AutoJs6DeviceStatusText.selectedValue(listOf(emulator, phone), null))
        assertTrue(AutoJs6DeviceStatusText.tooltip(listOf(emulator, phone), phone.key).contains("192.168.1.8:7347"))
    }

    @Test fun autoJs6ScriptValidationAcceptsOnlyLocalJsFiles() {
        val dir = Files.createTempDirectory("autojs6-script-validation")
        try {
            val jsFile = dir.resolve("main.js")
            val textFile = dir.resolve("main.txt")
            val missingJsFile = dir.resolve("missing.js")
            Files.writeString(jsFile, "toast('ok')")
            Files.writeString(textFile, "not js")

            assertTrue(AutoJs6ScriptCommand.validateLocalJsPath(jsFile.toString()).valid)
            assertFalse(AutoJs6ScriptCommand.validateLocalJsPath(dir.toString()).valid)
            assertFalse(AutoJs6ScriptCommand.validateLocalJsPath(textFile.toString()).valid)
            assertFalse(AutoJs6ScriptCommand.validateLocalJsPath(missingJsFile.toString()).valid)
        } finally {
            dir.toFile().deleteRecursively()
        }
    }

    @Test fun recentHostRecordsPreserveTimestampsAndCanBeCleared() {
        val settings = AutoJs6SettingsService()
        settings.addRecentHost("192.168.1.7", 1000)
        settings.addRecentHost("192.168.1.8:7347", 2000)
        settings.addRecentHost("192.168.1.7", 3000)
        assertEquals(listOf("192.168.1.7", "192.168.1.8:7347"), settings.state.recentHostRecords.map { it.host })
        assertEquals(3000, settings.state.recentHostRecords.first().lastConnectedEpochMillis)
        assertEquals(2, settings.clearRecentHosts())
        assertTrue(settings.state.recentHostRecords.isEmpty())
    }

    @Test fun hostPortParserAcceptsExplicitPortAndRejectsInvalidInput() {
        assertEquals("192.168.1.2", AutoJs6NetworkInterfaces.parseHostPort("192.168.1.2")?.host)
        assertEquals(AutoJs6Constants.SERVER_PORT, AutoJs6NetworkInterfaces.parseHostPort("192.168.1.2")?.port)
        assertEquals(12345, AutoJs6NetworkInterfaces.parseHostPort("192.168.1.2:12345")?.port)
        assertEquals(null, AutoJs6NetworkInterfaces.parseHostPort("192.168.1.2:not-a-port"))
    }

    @Test fun commandWhitelistAndHttpGateRejectUnknownCommands() {
        assertTrue(AutoJs6CommandDispatcher.isWhitelisted("run"))
        assertTrue(AutoJs6CommandDispatcher.isWhitelisted("rerunProject"))
        assertFalse(AutoJs6CommandDispatcher.isWhitelisted("formatDisk"))
        val bridge = AutoJs6HttpBridgeService()
        assertEquals(400, bridge.handleExecForTest(null, null, null).first)
        assertEquals(400, bridge.handleExecForTest("formatDisk", null, null).first)
        assertEquals(400, bridge.handleExecForTest("run", "C:/fixture/main.js", null).first)
        assertEquals(400, bridge.handleExecForTest("rerunProject", "C:/fixture/AutoJs6Project", null).first)
    }

    @Test fun projectSyncBuildsZipMd5IgnoresAndTracksDeletedFiles() {
        val dir = Files.createTempDirectory("autojs6-project-sync")
        try {
            Files.writeString(dir.resolve("project.json"), """{"name":"Demo","ignore":["ignored","out.log"]}""")
            val main = dir.resolve("main.js")
            val ignoredDir = dir.resolve("ignored")
            Files.createDirectories(ignoredDir)
            Files.writeString(main, "toast('v1')")
            Files.writeString(ignoredDir.resolve("skip.js"), "toast('skip')")
            Files.writeString(dir.resolve("out.log"), "skip")

            val service = AutoJs6ProjectSyncService()
            val first = service.buildPayload(dir, "device-1")
            assertFalse(first.override)
            assertTrue(first.modifiedFiles.contains("project.json"))
            assertTrue(first.modifiedFiles.contains("main.js"))
            assertFalse(first.modifiedFiles.contains("ignored/skip.js"))
            assertFalse(first.modifiedFiles.contains("out.log"))
            assertEquals(AutoJs6ProjectSyncService.md5Hex(first.zipBytes), first.md5)
            assertTrue(zipEntries(first.zipBytes).contains("main.js"))

            Files.writeString(main, "toast('v2')")
            Files.setLastModifiedTime(main, FileTime.fromMillis(Files.getLastModifiedTime(main).toMillis() + 2000))
            val second = service.buildPayload(dir, "device-1")
            assertTrue(second.override)
            assertEquals(listOf("main.js"), second.modifiedFiles)

            Files.delete(main)
            val third = service.buildPayload(dir, "device-1")
            assertTrue(third.deletedFiles.contains("main.js"))
        } finally {
            dir.toFile().deleteRecursively()
        }
    }

    @Test fun projectSyncOverridePayloadIncludesFullCurrentProjectWhenOnlyNonEntryFileChanges() {
        val dir = Files.createTempDirectory("autojs6-project-sync-full-override")
        try {
            Files.writeString(dir.resolve("project.json"), """{"name":"Demo","ignore":[]}""")
            Files.writeString(dir.resolve("main.js"), "require('./lib/helper.js')")
            val lib = dir.resolve("lib")
            Files.createDirectories(lib)
            val helper = lib.resolve("helper.js")
            Files.writeString(helper, "toast('v1')")

            val service = AutoJs6ProjectSyncService()
            val first = service.buildPayload(dir, "device-1")
            assertFalse(first.override)
            assertTrue(zipEntries(first.zipBytes).contains("main.js"))

            Files.writeString(helper, "toast('v2')")
            Files.setLastModifiedTime(helper, FileTime.fromMillis(Files.getLastModifiedTime(helper).toMillis() + 2000))
            val second = service.buildPayload(dir, "device-1")

            assertTrue(second.override)
            assertEquals(listOf("lib/helper.js"), second.modifiedFiles)
            val entries = zipEntries(second.zipBytes)
            assertTrue(entries.contains("project.json"))
            assertTrue(entries.contains("main.js"))
            assertTrue(entries.contains("lib/helper.js"))
        } finally {
            dir.toFile().deleteRecursively()
        }
    }

    @Test fun projectSyncOverridePayloadDoesNotSendEmptyZipWhenNoFilesChanged() {
        val dir = Files.createTempDirectory("autojs6-project-sync-no-change")
        try {
            Files.writeString(dir.resolve("project.json"), """{"name":"Demo","ignore":[]}""")
            Files.writeString(dir.resolve("main.js"), "toast('project')")

            val service = AutoJs6ProjectSyncService()
            service.buildPayload(dir, "device-1")
            val second = service.buildPayload(dir, "device-1")

            assertTrue(second.override)
            assertEquals(emptyList(), second.modifiedFiles)
            val entries = zipEntries(second.zipBytes)
            assertTrue(entries.contains("project.json"))
            assertTrue(entries.contains("main.js"))
        } finally {
            dir.toFile().deleteRecursively()
        }
    }

    private fun zipEntries(bytes: ByteArray): List<String> {
        val names = mutableListOf<String>()
        ZipInputStream(bytes.inputStream()).use { zip ->
            while (true) {
                val entry = zip.nextEntry ?: break
                names += entry.name
            }
        }
        return names
    }

    @Test fun projectSyncSendsRealBytesCommandFramesForRunAndSave() {
        val dir = Files.createTempDirectory("autojs6-project-frames")
        val server = ServerSocket(0)
        val attached = CountDownLatch(1)
        lateinit var ideDevice: org.autojs.autojs6.jetbrains.device.AutoJs6Device
        try {
            Files.writeString(dir.resolve("project.json"), """{"name":"Demo","ignore":[]}""")
            Files.writeString(dir.resolve("main.js"), "toast('project')")

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
            fun readFrame(): Pair<Int, ByteArray> {
                val header = client.getInputStream().readNBytes(8)
                val buffer = java.nio.ByteBuffer.wrap(header)
                val payloadLen = buffer.int
                val type = buffer.int
                return type to client.getInputStream().readNBytes(payloadLen)
            }

            sendClientJson(mapOf("id" to 1, "type" to "hello", "data" to mapOf(
                "device_name" to "ProjectReplayDevice",
                "app_version" to "6.7.0",
                "app_version_code" to "3591",
                "device_id" to "replay-project"
            )))
            assertTrue(attached.await(3, TimeUnit.SECONDS))
            val hello = readFrame()
            assertEquals(AutoJs6Constants.TYPE_JSON, hello.first)
            assertEquals("hello", JsonCodec.decode(hello.second).string("type"))

            val service = AutoJs6ProjectSyncService()
            val saveResult = service.sendProjectCommand(dir, AUTOJS6_SAVE_PROJECT_COMMAND, listOf(ideDevice))
            assertEquals(1, saveResult.sent)
            val saveBytes = readFrame()
            val saveJson = readFrame()
            assertEquals(AutoJs6Constants.TYPE_BYTES, saveBytes.first)
            assertEquals(AutoJs6Constants.TYPE_JSON, saveJson.first)
            val savePayload = JsonCodec.decode(saveJson.second)
            assertEquals("bytes_command", savePayload.string("type"))
            assertEquals(AUTOJS6_SAVE_PROJECT_COMMAND, savePayload.obj("data")?.string("command"))
            assertEquals(AutoJs6ProjectSyncService.md5Hex(saveBytes.second), savePayload.string("md5"))

            val runResult = service.sendProjectCommand(dir, AUTOJS6_RUN_PROJECT_COMMAND, listOf(ideDevice))
            assertEquals(1, runResult.sent)
            val runBytes = readFrame()
            val runJson = readFrame()
            assertEquals(AutoJs6Constants.TYPE_BYTES, runBytes.first)
            assertEquals(AutoJs6Constants.TYPE_JSON, runJson.first)
            val runPayload = JsonCodec.decode(runJson.second)
            assertEquals("bytes_command", runPayload.string("type"))
            assertEquals(AUTOJS6_RUN_PROJECT_COMMAND, runPayload.obj("data")?.string("command"))
            assertEquals(AutoJs6ProjectSyncService.md5Hex(runBytes.second), runPayload.string("md5"))

            ideDevice.close()
            client.close()
        } finally {
            server.close()
            dir.toFile().deleteRecursively()
        }
    }

    @Test fun autoJs6ScriptRunConfigurationPayloadMatchesRunCurrentFilePayloadInReplay() {
        val dir = Files.createTempDirectory("autojs6-script-payload")
        val server = ServerSocket(0)
        val attached = CountDownLatch(1)
        lateinit var ideDevice: org.autojs.autojs6.jetbrains.device.AutoJs6Device
        try {
            val scriptFile = dir.resolve("main.js")
            val script = "toast('run config')"
            Files.writeString(scriptFile, script)
            val absolutePath = scriptFile.toAbsolutePath().toString()

            val runConfigurationPayload = AutoJs6ScriptCommand.readLocalJsPayload(absolutePath).toCommandData()
            val runCurrentFilePayload = AutoJs6ScriptCommand
                .createSingleFilePayload(absolutePath, "main.js", script)
                .toCommandData()
            assertEquals(runCurrentFilePayload, runConfigurationPayload)

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
                "device_id" to "replay-run-config"
            )))
            assertTrue(attached.await(3, TimeUnit.SECONDS))
            assertEquals("hello", readClientJson().string("type"))

            ideDevice.sendCommand("run", runConfigurationPayload)
            val replayed = readClientJson().obj("data")
            assertEquals("run", replayed?.string("command"))
            assertEquals(absolutePath, replayed?.string("id"))
            assertEquals("main.js", replayed?.string("name"))
            assertEquals(script, replayed?.string("script"))

            ideDevice.close()
            client.close()
        } finally {
            server.close()
            dir.toFile().deleteRecursively()
        }
    }


    @Test fun protocolReplayFixturesAreValidJson() {
        val dir = java.nio.file.Path.of("src/test/resources/protocol-fixtures")
        assertTrue(Files.isDirectory(dir))
        val jsonFiles = mutableListOf<java.nio.file.Path>()
        Files.walk(dir).use { stream ->
            stream
                .filter { Files.isRegularFile(it) && it.fileName.toString().endsWith(".json") }
                .forEach { jsonFiles.add(it) }
        }
        assertTrue(jsonFiles.size >= 8)
        val names = jsonFiles.map { it.fileName.toString() }.toSet()
        assertTrue(names.contains("manifest.json"))
        assertTrue(names.contains("command-run-file.json"))
        assertTrue(names.contains("bytes-frame.sample.json"))
        assertTrue(names.contains("bytes-command-run-project.json"))
        jsonFiles.forEach { fixture ->
            JsonCodec.decode(Files.readString(fixture).toByteArray())
        }
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
