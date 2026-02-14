package com.example.interxactions.utils

fun titleCaseWithExceptions(
    text: String,
    exceptions: Set<String> = setOf("and", "or", "in", "of", "the", "a", "an")
): String {
    return text.lowercase().split(" ").mapIndexed { index, word ->
        if (index == 0 || word !in exceptions) {
            word.replaceFirstChar { it.titlecase() }
        } else {
            word
        }
    }.joinToString(" ")
}
