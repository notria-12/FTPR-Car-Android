package com.example.myapitest.model

data class Item(
    val id: String,
    val name: String,
    val imageUrl: String,
    val year: String,
    val licence: String,
    val place: ItemPlace
)


data class ItemPlace(
    val lat: Double,
    val long: Double,
)
