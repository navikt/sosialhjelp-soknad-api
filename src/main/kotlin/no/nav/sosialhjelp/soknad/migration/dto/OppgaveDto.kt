package no.nav.sosialhjelp.soknad.migration.dto

import no.nav.sosialhjelp.soknad.db.repositories.oppgave.Status
import java.time.LocalDateTime

data class OppgaveDto(
    val id: Long,
    val behandlingsId: String,
    val type: String?,
    val status: Status,
    val steg: Int,
    val oppgaveData: FiksDataDto?,
    val oppgaveResultat: FiksResultatDto?,
    val opprettet: LocalDateTime?,
    val sistKjort: LocalDateTime?,
    val nesteForsok: LocalDateTime?,
    val retries: Int
)

data class FiksDataDto(
    val behandlingsId: String?,
    val avsenderFodselsnummer: String?,
    val mottakerOrgNr: String?,
    val mottakerNavn: String?,
    val dokumentInfoer: List<DokumentInfoDto>?,
    val innsendtDato: LocalDateTime?,
    val ettersendelsePa: String?,
)

data class DokumentInfoDto(
    val uuid: String?,
    val filnavn: String?,
    val mimetype: String?,
    val ekskluderesFraPrint: Boolean?
)

data class FiksResultatDto(
    val fiksForsendelsesId: String?,
    val feilmelding: String?
)
