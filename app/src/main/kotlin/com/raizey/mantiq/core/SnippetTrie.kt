package com.raizey.mantiq.core

/**
 * A compact in-memory exact-match trie. It is built once and then read without locks
 * on the keyboard's hot path.
 */
class SnippetTrie(snippets: Iterable<Snippet>) {
    private class Node {
        val children = HashMap<Char, Node>()
        var snippet: Snippet? = null
    }

    private val root = Node()

    init {
        snippets.forEach(::insert)
    }

    fun findExact(trigger: String): Snippet? {
        var node = root
        for (character in trigger) {
            node = node.children[character] ?: return null
        }
        return node.snippet
    }

    private fun insert(snippet: Snippet) {
        require(snippet.trigger.isNotBlank()) { "Snippet trigger cannot be blank" }

        var node = root
        for (character in snippet.trigger) {
            node = node.children.getOrPut(character, ::Node)
        }
        node.snippet = snippet
    }
}
