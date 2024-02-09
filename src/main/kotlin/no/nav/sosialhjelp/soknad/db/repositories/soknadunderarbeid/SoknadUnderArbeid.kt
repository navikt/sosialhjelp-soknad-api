package no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid

import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonPersonalia
import no.nav.sosialhjelp.soknad.v2.soknad.Eier
import no.nav.sosialhjelp.soknad.v2.soknad.Navn
import java.time.LocalDateTime

@Deprecated("Erstattes av no.nav.sosialhjelp.soknad.v2.soknad.Soknad")
data class SoknadUnderArbeid(
    var soknadId: Long = 0L, // dummy verdi, settes i DB
    var versjon: Long,
    var behandlingsId: String,
    var tilknyttetBehandlingsId: String? = null,
    var eier: String,
    var jsonInternalSoknad: JsonInternalSoknad?,
    var status: SoknadUnderArbeidStatus,
    var opprettetDato: LocalDateTime,
    var sistEndretDato: LocalDateTime
) {
    val erEttersendelse: Boolean get() = !tilknyttetBehandlingsId.isNullOrEmpty()
}

enum class SoknadUnderArbeidStatus {
    UNDER_ARBEID, LAAST
}

fun SoknadUnderArbeid.toV2Eier(): Eier? {
    return jsonInternalSoknad?.soknad?.data?.personalia?.toV2Eier()
}

private fun JsonPersonalia.toV2Eier(): Eier {
    return Eier(
        personId = personIdentifikator.verdi,
        statsborgerskap = statsborgerskap?.verdi,
        nordiskBorger = nordiskBorger?.verdi,
        kontonummer = kontonummer?.verdi,
        telefonnummer = telefonnummer?.verdi,
        navn = Navn(
            fornavn = navn.fornavn,
            mellomnavn = navn.mellomnavn,
            etternavn = navn.etternavn
        )
    )
}
