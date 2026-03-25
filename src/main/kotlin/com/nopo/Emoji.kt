package com.nopo

data class Emojis(val emojis: List<Emoji>)

data class Emoji(val name: String, val alternatives: List<String>? = null) {
    fun isEmoji(part: String): Boolean {
        if (part == name || ":$part:" in getColonAlternatives()) return true
        return false
    }

    fun getColonAlternatives(): List<String> {
        val newList = mutableListOf<String>()
        if (alternatives == null) return emptyList()
        for (alt in alternatives) {
            newList.add(":$alt:")
        }
        return newList
    }

    fun getAll(): List<String> {
        return getColonAlternatives() + ":$name:"
    }
}
