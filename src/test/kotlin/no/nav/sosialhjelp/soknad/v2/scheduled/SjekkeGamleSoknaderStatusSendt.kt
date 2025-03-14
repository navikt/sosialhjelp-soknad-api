package no.nav.sosialhjelp.soknad.v2.scheduled

import no.nav.sosialhjelp.soknad.v2.metadata.SoknadMetadataService
import no.nav.sosialhjelp.soknad.v2.scheduled.SjekkeGamleSoknaderStatusSendt.Companion.NUMBER_OF_DAYS
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class SjekkeGamleSoknaderStatusSendt(
    private val metadataService: SoknadMetadataService,
) {
    @Scheduled(cron = HVER_TIME)
    fun sjekkeGamleSoknaderStatusSendt() {
        metadataService
            .getMetadatasStatusSendt()
            .filter { it.tidspunkt.isOlderThan(NUMBER_OF_DAYS) }
            .also { metadatas ->
                if (metadatas.isNotEmpty()) throw SoknadIkkeMottattException(NUMBER_OF_DAYS, metadatas.map { it.soknadId })
            }
    }

    companion object {
        private const val HVER_TIME = "0 0 * * * *"
        const val NUMBER_OF_DAYS = 1L
    }
}

data class SoknadIkkeMottattException(
    val antallDager: Long,
    val soknadIds: List<UUID>,
) : IllegalStateException(
        "OBS!!! Soknad (ids= ${soknadIds.joinToString(", ")}) har status sendt, " +
            "men er over $NUMBER_OF_DAYS dag(er) gammel og er ikke mottatt av Fagsystem.",
    )
