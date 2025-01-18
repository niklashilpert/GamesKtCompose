package client.config

import java.io.File

const val CONFIG_DIR = "/home/niklas/Desktop/GamesKtCompose/"
val LINE_FORMAR = Regex("^([a-zA-Z-_. ]+)=(.+)$")

class ConfigFile(val name: String, defaultProperties: HashMap<String, String>) {
    val properties = defaultProperties

    private val file = File(CONFIG_DIR, "$name.properties")

    init {
        read()
        store()
    }

    fun getString(key: String): String {
        return properties[key]!!
    }
    fun getInt(key: String): Int {
        return properties[key]!!.toIntOrNull() ?: 0
    }
    fun getBoolean(key: String): Boolean {
        return properties[key]?.toBooleanStrictOrNull() ?: false
    }

    fun set(key: String, value: Any) {
        properties[key] = value.toString()
    }

    private fun read() {
        if (file.isFile) {
            for (line in file.readLines()) {
                val match = LINE_FORMAR.matchEntire(line) ?: continue
                if (match.groupValues.size != 3) continue

                val key = match.groupValues[1]
                val value = match.groupValues[2]
                properties[key] = value
            }
        }
    }

    fun store() {
        println("Storing $name")
        if (!file.isFile) {
            file.parentFile.mkdirs()
            file.createNewFile()
        }
        val result = StringBuilder()
        for ((key, value) in properties) {
            result.append(key).append("=").append(value).append("\n")
        }
        file.writeText(result.toString())
    }
}