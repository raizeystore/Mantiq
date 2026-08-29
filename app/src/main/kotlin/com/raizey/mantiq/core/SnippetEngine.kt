package com.raizey.mantiq.core

class SnippetEngine(
    snippets: Iterable<Snippet>,
    private val templateEngine: TemplateEngine = TemplateEngine(),
) {
    private val trie = SnippetTrie(snippets)

    fun expandTrigger(
        trigger: String,
        context: TemplateContext = TemplateContext(),
    ): String? = trie.findExact(trigger)?.let { templateEngine.render(it.template, context) }

    fun expandBeforeDelimiter(
        textBeforeCursor: String,
        context: TemplateContext = TemplateContext(),
    ): SnippetExpansion? {
        val trigger = trailingToken(textBeforeCursor)
        if (trigger.isEmpty()) return null

        val replacement = expandTrigger(trigger, context) ?: return null
        return SnippetExpansion(
            trigger = trigger,
            deleteCharacters = trigger.length,
            replacement = replacement,
        )
    }

    private fun trailingToken(text: String): String {
        var start = text.length
        while (start > 0 && !text[start - 1].isWhitespace()) {
            start--
        }
        return text.substring(start)
    }
}
