package no.nav.sosialhjelp.soknad.v2.shadow

import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.v2.soknad.service.SoknadService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.util.UUID

@Service
@Transactional(propagation = Propagation.NESTED)
class SoknadV2AdapterService(
    private val soknadService: SoknadService,
) : V2AdapterService {
    private val logger by logger()

    override fun createSoknad(
        behandlingsId: String,
        opprettetDato: LocalDateTime,
        eierId: String,
    ) {
        logger.info("NyModell: Oppretter ny soknad for $behandlingsId")

        kotlin.runCatching {
            soknadService.createSoknad(
                soknadId = UUID.fromString(behandlingsId),
                opprettetDato = opprettetDato,
                eierId = eierId,
            )
        }
            .onFailure { logger.warn("Ny modell: Feil ved oppretting av ny soknad i adapter", it) }

        kotlin.runCatching {
            soknadService.findOrError(UUID.fromString(behandlingsId))
                .also { logger.info("NyModell: Opprettet soknad: ${it.tidspunkt.opprettet}") }
        }
            .onFailure { logger.warn("NyModell: Fant ikke ny soknad i databasen", it) }
    }

    override fun setInnsendingstidspunkt(
        soknadId: String,
        innsendingsTidspunkt: String,
    ) {
        logger.info("NyModell: Setter innsendingstidspunkt fra timestamp: $innsendingsTidspunkt")

        kotlin.runCatching {
            OffsetDateTime.parse(innsendingsTidspunkt).let {
                soknadService.setInnsendingstidspunkt(
                    soknadId = UUID.fromString(soknadId),
                    innsendingsTidspunkt = it.toLocalDateTime(),
                )
            }
        }
            .onFailure { logger.warn("NyModell: Kunne ikke sette innsendingstidspunkt", it) }
    }

    override fun slettSoknad(behandlingsId: String) {
        logger.info("NyModell: Sletter SoknadV2")

        kotlin.runCatching {
            soknadService.slettSoknad(UUID.fromString(behandlingsId))
        }
            .onFailure { logger.warn("NyModell: Kunne ikke slette Soknad V2") }
    }
}
