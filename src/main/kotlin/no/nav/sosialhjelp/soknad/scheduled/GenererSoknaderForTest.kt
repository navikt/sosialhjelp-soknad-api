package no.nav.sosialhjelp.soknad.scheduled

import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.app.mdc.MdcOperations
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.app.systemdata.SystemdataUpdater
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.SoknadMetadata
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.SoknadMetadataInnsendingStatus
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.SoknadMetadataRepository
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.SoknadMetadataType
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeid
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidStatus
import no.nav.sosialhjelp.soknad.innsending.SenderUtils
import no.nav.sosialhjelp.soknad.innsending.SoknadService
import no.nav.sosialhjelp.soknad.scheduled.leaderelection.LeaderElection
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Clock
import java.time.LocalDateTime

@Component
class GenererSoknaderForTest(
    private val leaderElection: LeaderElection,
    private val soknadUnderArbeidRepository: SoknadUnderArbeidRepository,
    private val systemdataUpdater: SystemdataUpdater,
    private val soknadMetadataRepository: SoknadMetadataRepository,
    private val clock: Clock
) {

    private val log by logger()
    private val fnr = "05518232810"

    @Scheduled(cron = "0 */15 * * * *")
    fun fyllDbMedSoknader() {

        log.info("Test - oppretter soknad for test")
        try {
            val behandlingsId = startSoknad()
            log.info("Test - opprettet soknad $behandlingsId for $fnr")

        } catch (e: Exception) {
            log.error("Test - feil ved opprettelse av soknad", e)
        }

    }

    private fun startSoknad(): String {
        val eier = fnr
        val behandlingsId = opprettSoknadMetadata(eier)
        MdcOperations.putToMDC(MdcOperations.MDC_BEHANDLINGS_ID, behandlingsId)

        val soknadUnderArbeid = SoknadUnderArbeid(
            versjon = 1L,
            behandlingsId = behandlingsId,
            tilknyttetBehandlingsId = null,
            eier = eier,
            jsonInternalSoknad = SoknadService.createEmptyJsonInternalSoknad(eier),
            status = SoknadUnderArbeidStatus.UNDER_ARBEID,
            opprettetDato = LocalDateTime.now(),
            sistEndretDato = LocalDateTime.now()
        )

//        systemdataUpdater.update(soknadUnderArbeid)
        soknadUnderArbeidRepository.opprettSoknad(soknadUnderArbeid, eier)

        return behandlingsId
    }

    private fun opprettSoknadMetadata(fnr: String): String {
        log.info("Starter søknad")
        val id = soknadMetadataRepository.hentNesteId()
        val soknadMetadata = SoknadMetadata(
            id = id,
            behandlingsId = SenderUtils.lagBehandlingsId(id),
            fnr = fnr,
            skjema = SenderUtils.SKJEMANUMMER,
            type = SoknadMetadataType.SEND_SOKNAD_KOMMUNAL,
            status = SoknadMetadataInnsendingStatus.UNDER_ARBEID,
            opprettetDato = LocalDateTime.now(clock),
            sistEndretDato = LocalDateTime.now(clock)
        )
        soknadMetadataRepository.opprett(soknadMetadata)
        return soknadMetadata.behandlingsId
    }
}
