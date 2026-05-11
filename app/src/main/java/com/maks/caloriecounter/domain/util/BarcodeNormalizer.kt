package com.maks.caloriecounter.domain.util

object BarcodeNormalizer {
    private val numericLengths = setOf(8, 12, 13, 14)
    private val productFormats = setOf("EAN_13", "EAN_8", "UPC_A", "UPC_E")
    private val gs1Ai01Regex = Regex("\\(?01\\)?(\\d{14})")

    fun candidates(rawValue: String, format: String?): List<String> {
        val raw = rawValue.trim()
        if (raw.isBlank()) return emptyList()

        val candidates = buildList {
            if (raw.all(Char::isDigit) && raw.length in numericLengths) add(raw)
            if (format?.let { it in productFormats } == true && raw.isNotBlank()) add(raw)
            if (format == "DATA_MATRIX" || raw.length > 14) {
                val gtin = gs1Ai01Regex.find(raw)?.groupValues?.getOrNull(1)
                if (!gtin.isNullOrBlank()) {
                    add(gtin)
                    if (gtin.startsWith('0')) add(gtin.drop(1))
                }
            }
        }

        return candidates.distinct()
    }
}
