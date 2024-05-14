package no.nav.sosialhjelp.soknad.v2.shadow

import no.nav.sosialhjelp.soknad.v2.soknad.service.SoknadService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.util.UUID

@Service
@Transactional(propagation = Propagation.NESTED)
class SoknadV2AdapterService(
    private val soknadService: SoknadService,
) : V2AdapterService {
    private val log = LoggerFactory.getLogger(this::class.java)

    override fun createSoknad(
        behandlingsId: String,
        opprettetDato: LocalDateTime,
        eierId: String,
    ) {
        log.info("NyModell: Oppretter ny soknad for $behandlingsId")

        kotlin.runCatching {
            soknadService.createSoknad(
                soknadId = UUID.fromString(behandlingsId),
                opprettetDato = opprettetDato,
                eierId = eierId,
            )
        }
            .onFailure { log.warn("Ny modell: Feil ved oppretting av ny soknad i adapter", it) }
    }

    override fun setInnsendingstidspunkt(
        soknadId: String,
        innsendingsTidspunkt: String,
    ) {
        log.info("NyModell: Setter innsendingstidspunkt")

        kotlin.runCatching {
            val zonedDateTime = ZonedDateTime.parse(innsendingsTidspunkt)

            soknadService.setInnsendingstidspunkt(
                UUID.fromString(soknadId),
                zonedDateTime.toLocalDateTime(),
            )
        }
            .onFailure { log.warn("NyModell: Kunne ikke sette innsendingstidspunkt", it) }
    }

    override fun slettSoknad(behandlingsId: String) {
        log.info("NyModell: Sletter SoknadV2")

        kotlin.runCatching {
            soknadService.slettSoknad(UUID.fromString(behandlingsId))
        }
            .onFailure { log.warn("NyModell: Kunne ikke slette Soknad V2") }
    }
}
