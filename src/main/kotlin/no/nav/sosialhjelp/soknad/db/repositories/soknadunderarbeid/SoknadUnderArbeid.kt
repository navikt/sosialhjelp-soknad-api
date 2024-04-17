package no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid

import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonPersonalia
import no.nav.sosialhjelp.soknad.v2.eier.Eier
import no.nav.sosialhjelp.soknad.v2.eier.Kontonummer
import no.nav.sosialhjelp.soknad.v2.navn.Navn
import java.time.LocalDateTime
import java.util.*

data class SoknadUnderArbeid(
    var soknadId: Long = 0L, // dummy verdi, settes i DB
    var versjon: Long,
    var behandlingsId: String,
    private val tilknyttetBehandlingsId: String? = null,
    var eier: String,
    var jsonInternalSoknad: JsonInternalSoknad?,
    var status: SoknadUnderArbeidStatus,
    var opprettetDato: LocalDateTime,
    var sistEndretDato: LocalDateTime
)

enum class SoknadUnderArbeidStatus {
    UNDER_ARBEID, LAAST
}

fun SoknadUnderArbeid.toV2Eier(): Eier? {
    return jsonInternalSoknad?.soknad?.data?.personalia?.toV2Eier(UUID.fromString(this.behandlingsId))
}

private fun JsonPersonalia.toV2Eier(soknadId: UUID): Eier {
    return Eier(
        soknadId = soknadId,
        statsborgerskap = statsborgerskap?.verdi,
        nordiskBorger = nordiskBorger?.verdi,
        kontonummer = kontonummer?.let { Kontonummer(fraRegister = it.verdi) },
        navn = Navn(
            fornavn = navn.fornavn,
            mellomnavn = navn.mellomnavn,
            etternavn = navn.etternavn
        )
    )
}
