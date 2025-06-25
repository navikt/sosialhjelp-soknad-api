package no.nav.sosialhjelp.soknad.vedlegg.virusscan.dto

import com.fasterxml.jackson.annotation.JsonAlias

data class ScanResult(
    @param:JsonAlias("Filename")
    val filename: String?,
    @param:JsonAlias("Result")
    val result: Result,
)

enum class Result {
    FOUND,
    OK,
    ERROR,
}
