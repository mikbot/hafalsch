package dev.schlaubi.hafalsch.marudor.entity

import kotlinx.serialization.Serializable

@Serializable
public data class HafasStation(
    val products: List<Product> = emptyList(),
    val coordinates: Coordinates,
    val title: String,
    val id: String
) {
    @Serializable
    public data class Product(
        val name: String,
        val line: String?,
        val number: String?,
        val type: String?,
        val operator: Operator,
        val admin: String
    )
}
