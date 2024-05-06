package me.airdead.zutils

fun String.formatPlaceholders(map: Map<String, Any>) = replaceClassPlaceholders(map)

private fun String.replaceClassPlaceholders(map: Map<String, Any>): String {
    val pattern = Regex("\\{([\\w.]+)\\}")
    return pattern.replace(this) { matchResult ->
        val fullPath = matchResult.groupValues[1]
        val pathParts = fullPath.split(".")
        val className = pathParts.first()
        map[className]?.let { instance ->
            safelyGetValue(instance, pathParts.drop(1))?.toString()
        } ?: matchResult.value
    }
}

private fun safelyGetValue(instance: Any?, pathParts: List<String>) = try {
    pathParts.fold(instance) { currentInstance, part ->
        currentInstance?.getFieldValue(part)
    }
} catch (e: Exception) {
    e.printStackTrace()
    null
}

private fun Any?.getFieldValue(fieldName: String) = if (this == null) null else try {
    val field = this::class.java.getDeclaredField(fieldName)
    field.isAccessible = true
    field[this]?.toString()
} catch (e: Exception) {
    println("Error accessing field $fieldName: ${e.message}")
    null
}
