package no.nav.sosialhjelp.soknad.client.virusscan.dto

data class ScanResult(
    val filename: String?,
    val result: Result
)

enum class Result {
    FOUND, OK
}
