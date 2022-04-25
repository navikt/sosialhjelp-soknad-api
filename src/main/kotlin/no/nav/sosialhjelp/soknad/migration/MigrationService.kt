package no.nav.sosialhjelp.soknad.migration

import no.nav.sosialhjelp.kotlin.utils.logger
import no.nav.sosialhjelp.soknad.db.repositories.oppgave.OppgaveRepository
import no.nav.sosialhjelp.soknad.migration.dto.OppgaveDto
import no.nav.sosialhjelp.soknad.migration.dto.ReplicationDto
import no.nav.sosialhjelp.soknad.migration.dto.SendtSoknadDto
import no.nav.sosialhjelp.soknad.migration.dto.SoknadUnderArbeidDto
import no.nav.sosialhjelp.soknad.migration.repo.OpplastetVedleggMigrationRepository
import no.nav.sosialhjelp.soknad.migration.repo.SendtSoknadMigrationRepository
import no.nav.sosialhjelp.soknad.migration.repo.SoknadMetadataMigrationRepository
import no.nav.sosialhjelp.soknad.migration.repo.SoknadUnderArbeidMigrationRepository
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class MigrationService(
    private val soknadMetadataMigrationRepository: SoknadMetadataMigrationRepository,
    private val soknadUnderArbeidMigrationRepository: SoknadUnderArbeidMigrationRepository,
    private val opplastetVedleggMigrationRepository: OpplastetVedleggMigrationRepository,
    private val sendtSoknadMigrationRepository: SendtSoknadMigrationRepository,
    private val oppgaveRepository: OppgaveRepository
) {

    fun getNext(sistEndretTidspunkt: LocalDateTime): ReplicationDto? {
        log.info("Henter dto for migrering, neste soknadMetadata med sistEndretDato nyere enn $sistEndretTidspunkt")

        val soknadMetadata = soknadMetadataMigrationRepository.getNextSoknadMetadataAfter(sistEndretTidspunkt)
        if (soknadMetadata == null) {
            log.info("Ingen SoknadMetadata funnet med sistEndretTidspunkt nyere enn $sistEndretTidspunkt")
            return null
        }
        val behandlingsId = soknadMetadata.behandlingsId
        return ReplicationDto(
            behandlingsId = behandlingsId,
            soknadMetadata = soknadMetadata.toDto(),
            soknadUnderArbeid = getSoknadUnderArbeid(behandlingsId),
            sendtSoknad = getSendtSoknad(behandlingsId),
            oppgave = getOppgave(behandlingsId)
        )
    }

    private fun getSoknadUnderArbeid(behandlingsId: String): SoknadUnderArbeidDto? {
        return soknadUnderArbeidMigrationRepository.getSoknadUnderArbeid(behandlingsId)
            ?.let {
                val vedlegg = opplastetVedleggMigrationRepository.getOpplastetVedlegg(it.soknadId)
                it.toDto(vedlegg)
            }
    }

    private fun getSendtSoknad(behandlingsId: String): SendtSoknadDto? {
        return sendtSoknadMigrationRepository.getSendtSoknad(behandlingsId)?.toDto()
    }

    private fun getOppgave(behandlingsId: String): OppgaveDto? {
        return oppgaveRepository.hentOppgave(behandlingsId)?.toDto()
    }

    companion object {
        private val log by logger()
    }
}
