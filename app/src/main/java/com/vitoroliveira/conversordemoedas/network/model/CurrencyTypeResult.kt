package com.vitoroliveira.conversordemoedas.network.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class CurrencyType(
    val name: String,
    val acronym: String,
    val symbol: String,
    @SerialName("country_flag_image_url")
    val countryFlagImageUrl: String
)

@Serializable
data class CurrencyTypesResult(
    val values: List<CurrencyType>
)