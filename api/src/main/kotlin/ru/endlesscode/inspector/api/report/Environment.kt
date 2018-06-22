package ru.endlesscode.inspector.api.report

interface Environment {
    val title: String
    val fields: List<Pair<String, String>>
}
