package com.raizey.mantiq.core

data class Snippet(
    val trigger: String,
    val template: String,
)

data class SnippetExpansion(
    val trigger: String,
    val deleteCharacters: Int,
    val replacement: String,
)

