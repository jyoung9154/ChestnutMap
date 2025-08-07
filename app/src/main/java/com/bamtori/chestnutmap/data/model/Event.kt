package com.bamtori.chestnutmap.data.model

data class Event(
    val id: String,
    val title: String,
    val date: java.time.LocalDate,
    val color: String // e.g., "#FF0000" for red
)
