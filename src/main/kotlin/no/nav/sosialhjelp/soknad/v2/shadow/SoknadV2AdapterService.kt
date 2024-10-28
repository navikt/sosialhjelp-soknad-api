package no.nav.sosialhjelp.soknad.v2.shadow

import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.v2.dokumentasjon.AnnenDokumentasjonType
import no.nav.sosialhjelp.soknad.v2.dokumentasjon.DokumentasjonService
import no.nav.sosialhjelp.soknad.v2.json.generate.TimestampConverter
import no.nav.sosialhjelp.soknad.v2.okonomi.utgift.UtgiftType
import no.nav.sosialhjelp.soknad.v2.soknad.SoknadService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID

@Service
@Transactional(propagation = Propagation.NESTED)
class SoknadV2AdapterService(
    private val soknadService: SoknadService,
    private val dokumentasjonService: DokumentasjonService,
) : V2AdapterService {
    private val logger by logger()

    override fun createSoknad(
        behandlingsId: String,
        opprettetDato: LocalDateTime,
        eierId: String,
        kortSoknad: Boolean,
    ) {
        logger.info("NyModell: Oppretter ny soknad for $behandlingsId")

        kotlin
            .runCatching {
                soknadService.createSoknad(
                    soknadId = UUID.fromString(behandlingsId),
                    opprettetDato = opprettetDato,
                    eierId = eierId,
                    kortSoknad = kortSoknad,
                )

                opprettForventetDokumentasjon(behandlingsId, kortSoknad)
            }.onFailure { logger.warn("Ny modell: Feil ved oppretting av ny soknad i adapter", it) }

        kotlin
            .runCatching {
                soknadService
                    .findOrError(UUID.fromString(behandlingsId))
                    .also { logger.info("NyModell: Opprettet soknad: ${it.tidspunkt.opprettet}") }
            }.onFailure { logger.warn("NyModell: Fant ikke ny soknad i databasen", it) }
    }

    override fun setInnsendingstidspunkt(
        soknadId: String,
        innsendingsTidspunkt: String,
    ) {
        logger.info("NyModell: Setter innsendingstidspunkt fra timestamp: $innsendingsTidspunkt")

        kotlin
            .runCatching {
                TimestampConverter
                    .parseFromUTCString(innsendingsTidspunkt)
                    .also {
                        soknadService.setInnsendingstidspunkt(
                            soknadId = UUID.fromString(soknadId),
                            innsendingsTidspunkt = it,
                        )
                    }
            }.onFailure { logger.warn("NyModell: Kunne ikke sette innsendingstidspunkt", it) }
    }

    override fun slettSoknad(behandlingsId: String) {
        logger.info("NyModell: Sletter SoknadV2")

        kotlin
            .runCatching {
                soknadService.deleteSoknad(UUID.fromString(behandlingsId))
            }.onFailure { logger.warn("NyModell: Kunne ikke slette Soknad V2") }
    }

    override fun createInnsendtSoknadMetadata(
        behandlingsId: String,
        eierId: String,
        sendtInnDato: LocalDateTime?,
        opprettetDato: LocalDateTime,
    ) {
        TODO("Not yet implemented")
    }

    private fun opprettForventetDokumentasjon(
        behandlingsId: String,
        kortSoknad: Boolean,
    ) {
        if (kortSoknad) {
            // oppretter dokumentasjon kort|behov
            dokumentasjonService.opprettDokumentasjon(
                soknadId = UUID.fromString(behandlingsId),
                opplysningType = AnnenDokumentasjonType.BEHOV,
            )
        } else {
            // oppretter dokumentasjon skattemelding
            dokumentasjonService.opprettDokumentasjon(
                soknadId = UUID.fromString(behandlingsId),
                opplysningType = AnnenDokumentasjonType.SKATTEMELDING,
            )
        }
        // oppretter utgift annet annet (og den oppretter forventet dokumentasjon)
        dokumentasjonService.opprettDokumentasjon(
            soknadId = UUID.fromString(behandlingsId),
            opplysningType = UtgiftType.UTGIFTER_ANDRE_UTGIFTER,
        )
    }
}
