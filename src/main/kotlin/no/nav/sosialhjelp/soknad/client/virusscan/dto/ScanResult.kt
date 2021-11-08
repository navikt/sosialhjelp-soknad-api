package no.nav.sosialhjelp.soknad.client.virusscan.dto

import com.fasterxml.jackson.annotation.JsonAlias

data class ScanResult(
    @JsonAlias("Filename")
    val filename: String?,
    @JsonAlias("Result")
    val result: Result
)

enum class Result {
    FOUND, OK
}
