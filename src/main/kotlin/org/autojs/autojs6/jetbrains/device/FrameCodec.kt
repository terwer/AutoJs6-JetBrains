package org.autojs.autojs6.jetbrains.device

import org.autojs.autojs6.jetbrains.AutoJs6Constants
import java.nio.ByteBuffer
import java.nio.ByteOrder

sealed class AutoJs6Frame(val type: Int, val payload: ByteArray) {
    class Json(payload: ByteArray) : AutoJs6Frame(AutoJs6Constants.TYPE_JSON, payload)
    class Bytes(payload: ByteArray) : AutoJs6Frame(AutoJs6Constants.TYPE_BYTES, payload)
}

class FrameCodec {
    private var buffer = ByteArray(0)

    fun encode(type: Int, payload: ByteArray): ByteArray {
        require(type == AutoJs6Constants.TYPE_JSON || type == AutoJs6Constants.TYPE_BYTES) { "Unsupported frame type: $type" }
        require(payload.size <= AutoJs6Constants.MAX_FRAME_SIZE) { "Frame is too large: ${payload.size}" }
        val header = ByteBuffer.allocate(AutoJs6Constants.FRAME_HEADER_SIZE).order(ByteOrder.BIG_ENDIAN)
        header.putInt(payload.size)
        header.putInt(type)
        return header.array() + payload
    }

    fun feed(chunk: ByteArray): List<AutoJs6Frame> {
        if (chunk.isNotEmpty()) buffer += chunk
        val frames = mutableListOf<AutoJs6Frame>()
        while (buffer.size >= AutoJs6Constants.FRAME_HEADER_SIZE) {
            val header = ByteBuffer.wrap(buffer, 0, AutoJs6Constants.FRAME_HEADER_SIZE).order(ByteOrder.BIG_ENDIAN)
            val length = header.int
            val type = header.int
            if (length < 0 || length > AutoJs6Constants.MAX_FRAME_SIZE || (type != AutoJs6Constants.TYPE_JSON && type != AutoJs6Constants.TYPE_BYTES)) {
                buffer = ByteArray(0)
                throw IllegalArgumentException("Invalid AutoJs6 frame header: length=$length type=$type")
            }
            val total = AutoJs6Constants.FRAME_HEADER_SIZE + length
            if (buffer.size < total) break
            val payload = buffer.copyOfRange(AutoJs6Constants.FRAME_HEADER_SIZE, total)
            frames += if (type == AutoJs6Constants.TYPE_JSON) AutoJs6Frame.Json(payload) else AutoJs6Frame.Bytes(payload)
            buffer = buffer.copyOfRange(total, buffer.size)
        }
        return frames
    }
}
