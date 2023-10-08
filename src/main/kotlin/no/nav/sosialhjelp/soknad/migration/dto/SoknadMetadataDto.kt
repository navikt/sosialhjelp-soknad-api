package no.nav.sosialhjelp.soknad.migration.dto

import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedlegg
import no.nav.sosialhjelp.soknad.repository.soknadmetadata.SoknadMetadataInnsendingStatus
import no.nav.sosialhjelp.soknad.repository.soknadmetadata.SoknadMetadataType
import no.nav.sosialhjelp.soknad.repository.soknadmetadata.Vedleggstatus
import java.time.LocalDateTime

data class SoknadMetadataDto(
    val id: Long,
    val behandlingsId: String,
    val tilknyttetBehandlingsId: String?,
    val fnr: String,
    val skjema: String?,
    val orgnr: String?,
    val navEnhet: String?,
    val fiksForsendelseId: String?,
    val vedlegg: VedleggMetadataListeDto?,
    val type: SoknadMetadataType?,
    val status: SoknadMetadataInnsendingStatus?,
    val opprettetDato: LocalDateTime,
    val sistEndretDato: LocalDateTime,
    val innsendtDato: LocalDateTime?,
    val lest: Boolean
)

data class VedleggMetadataListeDto(
    val vedleggListe: List<VedleggMetadataDto>
)

data class VedleggMetadataDto(
    val filUuid: String?,
    val filnavn: String?,
    val mimeType: String?,
    val filStorrelse: String?,
    val status: Vedleggstatus?,
    val skjema: String?,
    val tillegg: String?,
    val hendelseType: JsonVedlegg.HendelseType?,
    val hendelseReferanse: String?
)
