package org.autojs.autojs6.jetbrains.device

class JsonPayload(private val map: Map<String, Any?>) {
    fun string(key: String): String? = map[key]?.toString()
    fun int(key: String): Int? = when (val value = map[key]) {
        is Number -> value.toInt()
        else -> value?.toString()?.toIntOrNull()
    }
    fun bool(key: String): Boolean? = when (val value = map[key]) {
        is Boolean -> value
        else -> value?.toString()?.toBooleanStrictOrNull()
    }
    @Suppress("UNCHECKED_CAST")
    fun obj(key: String): JsonPayload? = (map[key] as? Map<String, Any?>)?.let(::JsonPayload)
    @Suppress("UNCHECKED_CAST")
    fun list(key: String): List<Any?>? = map[key] as? List<Any?>
    fun any(key: String): Any? = map[key]
    fun asMap(): Map<String, Any?> = map
    override fun toString(): String = JsonCodec.toJson(map)
}

object JsonCodec {
    fun encode(value: Map<String, Any?>): ByteArray = toJson(value).toByteArray(Charsets.UTF_8)

    fun decode(bytes: ByteArray): JsonPayload = JsonPayload(parseObject(String(bytes, Charsets.UTF_8)))

    fun parse(text: String): Any? {
        val parser = Parser(text)
        val value = parser.parseValue()
        parser.ensureEnd()
        return value
    }

    fun parseObject(text: String): Map<String, Any?> {
        @Suppress("UNCHECKED_CAST")
        return parse(text) as? Map<String, Any?> ?: error("JSON root is not object")
    }

    fun toJson(value: Any?): String = when (value) {
        null -> "null"
        is String -> "\"" + value.flatMap { escape(it).asIterable() }.joinToString("") + "\""
        is Number, is Boolean -> value.toString()
        is Map<*, *> -> value.entries.joinToString(prefix = "{", postfix = "}") { toJson(it.key.toString()) + ":" + toJson(it.value) }
        is Iterable<*> -> value.joinToString(prefix = "[", postfix = "]") { toJson(it) }
        is Array<*> -> value.joinToString(prefix = "[", postfix = "]") { toJson(it) }
        else -> toJson(value.toString())
    }

    private fun escape(ch: Char): String = when (ch) {
        '\\' -> "\\\\"
        '"' -> "\\\""
        '\b' -> "\\b"
        '\u000C' -> "\\f"
        '\n' -> "\\n"
        '\r' -> "\\r"
        '\t' -> "\\t"
        else -> if (ch.code < 0x20) "\\u%04x".format(ch.code) else ch.toString()
    }

    private class Parser(private val s: String) {
        private var i = 0
        fun parseValue(): Any? {
            skipWs()
            return when (peek()) {
                '{' -> parseObj()
                '[' -> parseArray()
                '"' -> parseString()
                't' -> { expect("true"); true }
                'f' -> { expect("false"); false }
                'n' -> { expect("null"); null }
                else -> parseNumberOrWord()
            }
        }
        fun ensureEnd() {
            skipWs()
            if (i != s.length) error("Unexpected token at $i")
        }
        private fun parseObj(): Map<String, Any?> {
            take('{')
            val m = linkedMapOf<String, Any?>()
            skipWs()
            if (tryTake('}')) return m
            while (true) {
                val k = parseString()
                skipWs()
                take(':')
                m[k] = parseValue()
                skipWs()
                if (tryTake('}')) return m
                take(',')
            }
        }
        private fun parseArray(): List<Any?> {
            take('[')
            val a = mutableListOf<Any?>()
            skipWs()
            if (tryTake(']')) return a
            while (true) {
                a += parseValue()
                skipWs()
                if (tryTake(']')) return a
                take(',')
            }
        }
        private fun parseString(): String {
            take('"')
            val out = StringBuilder()
            while (i < s.length) {
                val c = s[i++]
                if (c == '"') return out.toString()
                if (c == '\\') {
                    val e = s[i++]
                    out.append(when (e) {
                        '"' -> '"'
                        '\\' -> '\\'
                        '/' -> '/'
                        'b' -> '\b'
                        'f' -> '\u000C'
                        'n' -> '\n'
                        'r' -> '\r'
                        't' -> '\t'
                        'u' -> {
                            val hex = s.substring(i, i + 4)
                            i += 4
                            hex.toInt(16).toChar()
                        }
                        else -> e
                    })
                } else {
                    out.append(c)
                }
            }
            error("Unterminated string")
        }
        private fun parseNumberOrWord(): Any {
            val start = i
            while (i < s.length && !s[i].isWhitespace() && s[i] !in charArrayOf(',', ']', '}')) i++
            val token = s.substring(start, i)
            return token.toLongOrNull() ?: token.toDoubleOrNull() ?: token
        }
        private fun skipWs() { while (i < s.length && s[i].isWhitespace()) i++ }
        private fun peek(): Char = s.getOrElse(i) { error("Unexpected end") }
        private fun take(c: Char) { skipWs(); if (peek() != c) error("Expected $c at $i"); i++ }
        private fun tryTake(c: Char): Boolean { skipWs(); return if (i < s.length && s[i] == c) { i++; true } else false }
        private fun expect(x: String) { if (!s.startsWith(x, i)) error("Expected $x at $i"); i += x.length }
    }
}
