package no.nav.sosialhjelp.soknad.health.selftest

import java.util.Optional

/*
Kopiert inn fra no.nav.sbl.dialogarena:common-web
Endringer gjort i no.nav.common:web gjør at vi heller benytter den fra det gamle artefaktet.
Kan mest sannsynlig oppgraderes, hvis vi får selftest til å fungere fra no.nav.common:web
*/
data class Selftest(
    val application: String? = null,
    val version: String? = null,
    val timestamp: String? = null,
    val aggregateResult: Int = 0,
    private val checks: List<SelftestEndpoint>? = null
) {
    fun getChecks(): List<SelftestEndpoint>? {
        return Optional.ofNullable(checks).orElseGet { emptyList() }
    }
}

data class SelftestEndpoint(
    val endpoint: String?,
    val description: String?,
    val errorMessage: String?,
    private val result: Int?,
    val responseTime: String?,
    val stacktrace: String?,
    val critical: Boolean = false,
) {
    fun harFeil(): Boolean {
        return this.result != SelftestService.STATUS_OK
    }

    fun getResult(): Int {
        return Optional.ofNullable(result).orElse(SelftestService.STATUS_ERROR)
    }
}
