package com.willkamp.vial.example

data class PojoResponse @JvmOverloads constructor(
        val title: String,
        val description: String? = null
)
