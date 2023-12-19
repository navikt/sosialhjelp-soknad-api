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

    @Scheduled(cron = "0 */15 * * * *")
    fun SlettAlleSoknaderUnderArbeid1Jan() {

        if (leaderElection.isLeader()) {
            var retries = 0
            while (retries < 5) {

                log.info("Forsøk ${retries + 1} av 5: Starter sletting av alle soknader under arbeid")

                try {
                    val antallRaderMetadata = jdbcTemplate.update(
                        "DELETE FROM SOKNADMETADATA WHERE innsendingstatus = ? AND soknadtype = ?",
                        SoknadMetadataInnsendingStatus.UNDER_ARBEID.name,
                        SoknadMetadataType.SEND_SOKNAD_KOMMUNAL.name
                    )
                    log.info("Slettet $antallRaderMetadata rader fra SOKNAD_UNDER_ARBEID")

                    val antallRaderSUA = jdbcTemplate.update("DELETE FROM SOKNAD_UNDER_ARBEID")
                    log.info("Slettet $antallRaderSUA rader fra SOKNAD_UNDER_ARBEID")
                    return
                } catch (e: RuntimeException) {
                    log.error("Sletting av Soknader under arbeid feilet", e)
                    retries++
                    Thread.sleep(5000) // vent 5 sekunder
                }
            }
        }
    }
}
