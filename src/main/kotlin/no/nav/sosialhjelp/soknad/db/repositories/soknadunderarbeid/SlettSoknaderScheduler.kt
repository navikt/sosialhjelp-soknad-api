package no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid

import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.SoknadMetadataInnsendingStatus
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.SoknadMetadataType
import no.nav.sosialhjelp.soknad.scheduled.leaderelection.LeaderElection
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class SlettSoknaderScheduler(
    private val jdbcTemplate: JdbcTemplate,
    private val leaderElection: LeaderElection
) {
    private val log by logger()

    @Scheduled(cron = "@hourly")
    fun slettAlleSoknaderUnderArbeid1Jan() {

        if (leaderElection.isLeader()) {
            log.info("Starter sletting av alle soknader under arbeid")

            try {
                jdbcTemplate.update(
                    "DELETE FROM SOKNADMETADATA WHERE innsendingstatus = ? AND soknadtype = ?",
                    SoknadMetadataInnsendingStatus.UNDER_ARBEID.name,
                    SoknadMetadataType.SEND_SOKNAD_KOMMUNAL.name
                ).also {
                    log.info("Slettet $it rader fra SOKNADMETADATA")
                }

                jdbcTemplate.update("DELETE FROM SOKNAD_UNDER_ARBEID").also {
                    log.info("Slettet $it rader fra SOKNAD_UNDER_ARBEID")
                }
            } catch (e: RuntimeException) {
                log.error("Sletting av Soknader under arbeid feilet", e)
            }
        }
    }
}
