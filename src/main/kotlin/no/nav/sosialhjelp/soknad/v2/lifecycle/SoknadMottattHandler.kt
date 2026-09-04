package no.nav.sosialhjelp.soknad.v2.lifecycle

import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.app.exceptions.SoknadAlleredeSendtException
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadMetadata
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadMetadataServiceImpl
import org.springframework.stereotype.Component
import java.util.UUID

/**
 * Det finnes eksempler hvor bruker forsøker å sende inn søknader som allerede er mottatt:
 * 1. Søknaden er mottatt og markert som det hos oss - men bruker får allikevel prøvd å sende igjen. Ruter bruker til innsyn.
 * 2. Søknaden feilet ved sending (timeout), men ble mottatt av Fiks. Lagre nødvendig data og ruter bruker til innsyn.
 */

@Component
class SoknadMottattHandler(private val metadataService: SoknadMetadataServiceImpl) {
    fun resolveSoknadMottatt(
        soknadId: UUID,
        navEnhet: NavEnhetForSending,
        digisosId: UUID,
    ): Nothing {
        val metadata = metadataService.getMetadataForSoknad(soknadId)
        if (metadata.digisosId == null) updateSoknadMetadata(metadata, navEnhet, digisosId)

        createSoknadAlleredeSendtException(soknadId, navEnhet.enhetsnavn)
    }

    private fun updateSoknadMetadata(
        metadata: SoknadMetadata,
        navEnhet: NavEnhetForSending,
        digisosId: UUID,
    ) {
        logger.info(
            "Soknad ${metadata.soknadId} feilet ved innsending, men ble mottatt av Fiks. " +
                "Oppdaterer metadata med digisosId: $digisosId, kommunenummer: ${navEnhet.kommunenummer}, enhetsnavn: ${navEnhet.enhetsnavn}. " +
                "Bruker ${metadata.tidspunkt.sistEndret} som innsendingsTidspunkt.",
        )

        metadataService.updateSoknadSendt(
            soknadId = metadata.soknadId,
            kommunenummer = navEnhet.kommunenummer,
            digisosId = digisosId,
            // antar at tidspunkt.sistEndret er når status ble satt til INNSENDING_FEILET (men ble mottatt av FIKS)
            innsendingsTidspunkt = metadata.tidspunkt.sistEndret,
        )
    }

    private fun createSoknadAlleredeSendtException(
        soknadId: UUID,
        navEnhetNavn: String?,
    ): Nothing {
        metadataService.getSoknadSendtInfo(soknadId)
            .also { info ->
                throw SoknadAlleredeSendtException(
                    sendtInfo = info.copy(navEnhetNavn = navEnhetNavn),
                    message = "Søknad med ID $soknadId er allerede sendt.",
                )
            }
    }

    companion object {
        private val logger by logger()
    }
}
