package dev.schlaubi.hafalsch.traewelling.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class PaginatedResponse<T>(
    @SerialName("current_page")    
    val currentPage: Int,
    val data: List<T>
)
