package no.nav.sosialhjelp.soknad.v2.lifecycle

import no.nav.sbl.soknadsosialhjelp.json.JsonSosialhjelpObjectMapper
import no.nav.sbl.soknadsosialhjelp.json.JsonSosialhjelpValidator
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sosialhjelp.api.fiks.exceptions.FiksException
import no.nav.sosialhjelp.soknad.app.exceptions.BrokenSoknadException
import no.nav.sosialhjelp.soknad.app.exceptions.SosialhjelpSoknadApiException
import no.nav.sosialhjelp.soknad.innsending.digisosapi.DigisosApiService
import no.nav.sosialhjelp.soknad.innsending.digisosapi.FiksUtils
import no.nav.sosialhjelp.soknad.innsending.digisosapi.JsonTilleggsinformasjon
import no.nav.sosialhjelp.soknad.innsending.digisosapi.SendSoknadResponse
import no.nav.sosialhjelp.soknad.innsending.digisosapi.dto.FilMetadata
import no.nav.sosialhjelp.soknad.innsending.digisosapi.dto.FilOpplasting
import no.nav.sosialhjelp.soknad.pdf.SosialhjelpPdfGenerator
import no.nav.sosialhjelp.soknad.v2.HandledException
import no.nav.sosialhjelp.soknad.v2.kontakt.service.AdresseService
import no.nav.sosialhjelp.soknad.v2.lifecycle.SendSoknadHandler.Companion.logger
import no.nav.sosialhjelp.soknad.vedlegg.filedetection.MimeTypes
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClientResponseException.BadRequest
import java.io.ByteArrayInputStream
import java.util.UUID

@Component
class SendSoknadManager(
    private val digisosApiService: DigisosApiService,
    private val sosialhjelpPdfGenerator: SosialhjelpPdfGenerator,
    private val adresseService: AdresseService,
) {
    private val objectMapper = JsonSosialhjelpObjectMapper.createObjectMapper()

    fun getNavEnhetForSending(soknadId: UUID): NavEnhetForSending {
        return adresseService.findMottaker(soknadId)
            ?.let { navEnhet ->
                NavEnhetForSending(
                    kommunenummer = navEnhet.kommunenummer ?: error("NavEnhet mangler kommunenummer"),
                    enhetsnavn = navEnhet.enhetsnavn,
                )
            }
            ?: error("Søknad mangler NavEnhet")
    }

    fun doSendSoknad(
        soknadId: UUID,
        json: JsonInternalSoknad,
        kommunenummer: String,
    ): UUID {
        // lager nødvendige filer
        return digisosApiService.sendSoknad(
            soknadId = soknadId,
            soknadJson = json.toSoknadJson(),
            vedleggJson = json.toVedleggJson(),
            tilleggsinformasjonJson = json.createTilleggsinformasjonJson(),
            pdfDokumenter = json.getFilOpplastingList(),
            kommunenummer = kommunenummer,
        )
            .let { response ->
                when (response) {
                    is SendSoknadResponse.Success -> response.digisosId
                    else -> SendSoknadErrorHandler.handleInnsendingError(soknadId, response)
                }
            }
    }

    private fun JsonInternalSoknad.toSoknadJson(): String =
        objectMapper.writeValueAsString(soknad)
            .also { JsonSosialhjelpValidator.ensureValidSoknad(it) }

    private fun JsonInternalSoknad.toVedleggJson(): String {
        val vedlegg = checkNotNull(vedlegg) { "Vedlegg mangler" }
        return objectMapper
            .writeValueAsString(vedlegg)
            .also { JsonSosialhjelpValidator.ensureValidVedlegg(it) }
    }

    private fun JsonInternalSoknad.createTilleggsinformasjonJson(): String {
        return objectMapper.writeValueAsString(JsonTilleggsinformasjon(requireNotNull(soknad).mottaker.enhetsnummer))
    }

    private fun JsonInternalSoknad.getFilOpplastingList(): List<FilOpplasting> {
        return listOf(
            lagDokumentForSaksbehandlerPdf(this),
            lagDokumentForJuridiskPdf(this),
            lagDokumentForBrukerkvitteringPdf(),
        )
    }

    private fun lagDokumentForSaksbehandlerPdf(jsonInternalSoknad: JsonInternalSoknad): FilOpplasting {
        val filnavn = "Soknad.pdf"
        val soknadPdf = sosialhjelpPdfGenerator.generate(jsonInternalSoknad, false)
        return opprettFilOpplastingFraByteArray(filnavn, soknadPdf)
    }

    private fun lagDokumentForBrukerkvitteringPdf(): FilOpplasting {
        val filnavn = "Brukerkvittering.pdf"
        val pdf = sosialhjelpPdfGenerator.generateBrukerkvitteringPdf()
        return opprettFilOpplastingFraByteArray(filnavn, pdf)
    }

    private fun lagDokumentForJuridiskPdf(internalSoknad: JsonInternalSoknad): FilOpplasting {
        val filnavn = "Soknad-juridisk.pdf"
        val pdf = sosialhjelpPdfGenerator.generate(internalSoknad, true)
        return opprettFilOpplastingFraByteArray(filnavn, pdf)
    }

}

private fun opprettFilOpplastingFraByteArray(
    filnavn: String,
    bytes: ByteArray,
): FilOpplasting =
    FilOpplasting(
        metadata =
            FilMetadata(
                filnavn = filnavn,
                mimetype = MimeTypes.APPLICATION_PDF,
                storrelse = bytes.size.toLong(),
            ),
        data = ByteArrayInputStream(bytes),
    )

class AlleredeMottattException(
    val digisosId: UUID,
    message: String,
) : SosialhjelpSoknadApiException(message)

class MottakPabegyntException(
    message: String,
) : SosialhjelpSoknadApiException(message), HandledException
