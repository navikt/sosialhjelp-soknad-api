package no.nav.sosialhjelp.soknad.migration.dto

import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sosialhjelp.soknad.repository.soknadunderarbeid.SoknadUnderArbeidStatus
import java.time.LocalDateTime

data class SoknadUnderArbeidDto(
    val soknadId: Long,
    val versjon: Long,
    val behandlingsId: String,
    val tilknyttetBehandlingsId: String?,
    val eier: String,
    val jsonInternalSoknad: JsonInternalSoknad?,
    val status: SoknadUnderArbeidStatus,
    val opprettetDato: LocalDateTime,
    val sistEndretDato: LocalDateTime,
    val opplastetVedleggList: List<OpplastetVedleggDto>
)
