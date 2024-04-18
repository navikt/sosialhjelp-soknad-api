package no.nav.sosialhjelp.soknad.migration.dto

data class ReplicationDto(
    val behandlingsId: String,
    val soknadMetadata: SoknadMetadataDto,
    val soknadUnderArbeid: SoknadUnderArbeidDto?,
    val oppgave: OppgaveDto?,
)
