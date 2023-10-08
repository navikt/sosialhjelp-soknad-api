package no.nav.sosialhjelp.soknad.migration

import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.repository.oppgave.OppgaveRepository
import no.nav.sosialhjelp.soknad.migration.Extensions.toDto
import no.nav.sosialhjelp.soknad.migration.dto.OppgaveDto
import no.nav.sosialhjelp.soknad.migration.dto.ReplicationDto
import no.nav.sosialhjelp.soknad.migration.dto.SjekksumDto
import no.nav.sosialhjelp.soknad.migration.dto.SoknadUnderArbeidDto
import no.nav.sosialhjelp.soknad.migration.repo.OpplastetVedleggMigrationRepository
import no.nav.sosialhjelp.soknad.migration.repo.SoknadMetadataMigrationRepository
import no.nav.sosialhjelp.soknad.migration.repo.SoknadUnderArbeidMigrationRepository
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class MigrationService(
    private val soknadMetadataMigrationRepository: SoknadMetadataMigrationRepository,
    private val soknadUnderArbeidMigrationRepository: SoknadUnderArbeidMigrationRepository,
    private val opplastetVedleggMigrationRepository: OpplastetVedleggMigrationRepository,
    private val oppgaveRepository: OppgaveRepository
) {

    fun getNext(sistEndretDato: LocalDateTime): ReplicationDto? {
        log.info("Henter dto for migrering, neste soknadMetadata med sistEndretDato nyere enn $sistEndretDato")

        val soknadMetadata = soknadMetadataMigrationRepository.getNextSoknadMetadataAfter(sistEndretDato)
        if (soknadMetadata == null) {
            log.info("Ingen SoknadMetadata funnet med sistEndretDato nyere enn $sistEndretDato")
            return null
        }
        val behandlingsId = soknadMetadata.behandlingsId
        return ReplicationDto(
            behandlingsId = behandlingsId,
            soknadMetadata = soknadMetadata.toDto(),
            soknadUnderArbeid = getSoknadUnderArbeid(behandlingsId),
            oppgave = getOppgave(behandlingsId)
        )
    }

    fun getSjekksum(): SjekksumDto {
        return SjekksumDto(
            soknadMetadataSum = soknadMetadataMigrationRepository.count(),
            soknadUnderArbeidSum = soknadUnderArbeidMigrationRepository.count(),
            opplastetVedleggSum = opplastetVedleggMigrationRepository.count(),
            oppgaveSum = oppgaveRepository.count()
        )
    }

    private fun getSoknadUnderArbeid(behandlingsId: String): SoknadUnderArbeidDto? {
        return soknadUnderArbeidMigrationRepository.getSoknadUnderArbeid(behandlingsId)
            ?.let {
                val vedlegg = opplastetVedleggMigrationRepository.getOpplastetVedlegg(it.soknadId)
                it.toDto(vedlegg)
            }
    }

    private fun getOppgave(behandlingsId: String): OppgaveDto? {
        return oppgaveRepository.hentOppgave(behandlingsId)?.toDto()
    }

    companion object {
        private val log by logger()
    }
}
