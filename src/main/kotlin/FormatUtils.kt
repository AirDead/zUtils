package me.airdead.zutils

fun String.formatWithMap(map: Map<String, Any>): String {
    val p = Regex("\\{([\\w.]+)\\}")
    val result = StringBuffer()
    val m = p.toPattern().matcher(this)

    fun findByPath(instance: Any?, pathParts: List<String>): Any? {
        if (instance == null || pathParts.isEmpty()) return instance
        val fieldName = pathParts.first()
        return try {
            val field = instance::class.java.getDeclaredField(fieldName)
            field.isAccessible = true
            val nextInstance = field[instance]
            findByPath(nextInstance, pathParts.drop(1))
        } catch (e: Exception) {
            // If field not found, return null
            null
        }
    }

    while (m.find()) {
        val fullPath = m.group(1)
        if (fullPath != null) {
            val pathParts = fullPath.split(".")
            val className = pathParts.first()
            val value = map[className]?.let { instance ->
                findByPath(instance, pathParts.drop(1))
            } ?: m.group()

            m.appendReplacement(result, value.toString())
        }
    }
    m.appendTail(result)
    return result.toString()
}
