package no.nav.sosialhjelp.soknad.v2.kvittering

import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.app.exceptions.IkkeFunnetException
import no.nav.sosialhjelp.soknad.app.subjecthandler.TokenUtils
import no.nav.sosialhjelp.soknad.innsending.digisosapi.FiksReadClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.UUID

@Service
class KvitteringService(
    private val fiksReadClient: FiksReadClient,
    @param:Value("\${fiks_dokumentlager_endpoint_url}") private val dokumentlagerUrl: String,
) {
    fun hentKvittering(digisosId: UUID): KvitteringDto {
        val digisosSak = fiksReadClient.getDigisosSak(digisosId)

        if (digisosSak.fiksDigisosId != digisosId.toString()) {
            log.warn("Fiks returnerte annen søknad enn forespurt. digisosId=$digisosId")
            throw IkkeFunnetException("Fant ikke søknad med digisosId=$digisosId")
        }

        if (digisosSak.sokerFnr != TokenUtils.getUserIdFromToken()) {
            log.warn("Søknad tilhører ikke innlogget bruker. digisosId=$digisosId")
            throw IkkeFunnetException("Fant ikke søknad med digisosId=$digisosId")
        }

        val original =
            digisosSak.originalSoknadNAV
                ?: throw IkkeFunnetException("Søknad er ikke innsendt. digisosId=$digisosId")

        return KvitteringDto(
            digisosId = digisosId.toString(),
            tidspunktSendt = LocalDateTime.ofInstant(Instant.ofEpochMilli(original.timestampSendt), OSLO),
            filnavn = original.soknadDokument.filnavn,
            storrelse = original.soknadDokument.storrelse,
            soknadPdfUrl = lastNedUrl(original.soknadDokument.dokumentlagerDokumentId),
        )
    }

    private fun lastNedUrl(dokumentlagerId: String) = "${dokumentlagerUrl.trimEnd('/')}/dokumentlager/nedlasting/niva4/$dokumentlagerId"

    companion object {
        private val log by logger()
        private val OSLO = ZoneId.of("Europe/Oslo")
    }
}
