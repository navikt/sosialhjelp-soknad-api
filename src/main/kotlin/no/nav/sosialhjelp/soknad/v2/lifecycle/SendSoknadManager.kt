package no.nav.sosialhjelp.soknad.v2.lifecycle

import no.nav.sbl.soknadsosialhjelp.json.JsonSosialhjelpObjectMapper
import no.nav.sbl.soknadsosialhjelp.json.JsonSosialhjelpValidator
import no.nav.sbl.soknadsosialhjelp.soknad.JsonData.Soknadstype
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sosialhjelp.soknad.innsending.digisosapi.AlleredeMottattException
import no.nav.sosialhjelp.soknad.innsending.digisosapi.DigisosApiV2Client
import no.nav.sosialhjelp.soknad.innsending.digisosapi.DokumentlagerClient
import no.nav.sosialhjelp.soknad.innsending.digisosapi.JsonTilleggsinformasjon
import no.nav.sosialhjelp.soknad.innsending.digisosapi.KrypteringService
import no.nav.sosialhjelp.soknad.innsending.digisosapi.KrypteringService.Companion.waitForFutures
import no.nav.sosialhjelp.soknad.innsending.digisosapi.SendSoknadResponse
import no.nav.sosialhjelp.soknad.innsending.digisosapi.Utils
import no.nav.sosialhjelp.soknad.innsending.digisosapi.dto.FilMetadata
import no.nav.sosialhjelp.soknad.innsending.digisosapi.dto.FilOpplasting
import no.nav.sosialhjelp.soknad.pdf.SosialhjelpPdfGenerator
import no.nav.sosialhjelp.soknad.v2.kontakt.service.AdresseService
import no.nav.sosialhjelp.soknad.v2.lifecycle.SendSoknadHandler.Companion.logger
import no.nav.sosialhjelp.soknad.vedlegg.filedetection.MimeTypes
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClientResponseException
import java.io.ByteArrayInputStream
import java.util.Collections
import java.util.UUID
import java.util.concurrent.Future

@Component
class SendSoknadManager(
    private val digisosApiV2Client: DigisosApiV2Client,
    private val sosialhjelpPdfGenerator: SosialhjelpPdfGenerator,
    private val adresseService: AdresseService,
    private val krypteringService: KrypteringService,
    private val dokumentlagerClient: DokumentlagerClient,
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
        return krypterOgLastOppFiler(
            navEksternRefId = soknadId,
            soknadJson = json.toSoknadJson(),
            vedleggJson = json.toVedleggJson(),
            tilleggsinformasjonJson = json.createTilleggsinformasjonJson(),
            pdfDokumenter = json.getFilOpplastingList(),
            kommunenr = kommunenummer,
        )
    }

    private fun krypterOgLastOppFiler(
        tilleggsinformasjonJson: String,
        vedleggJson: String,
        pdfDokumenter: List<FilOpplasting>,
        kommunenr: String,
        navEksternRefId: UUID,
    ): UUID {
        val krypteringFutureList = Collections.synchronizedList(ArrayList<Future<Void>>(pdfDokumenter.size))
        val response: SendSoknadResponse
        val startTime = System.currentTimeMillis()
        try {
            // TODO soknadJson og vedleggJson bør også krypteres
            response =
                digisosApiV2Client.lastOppFiler(
                    soknadJson,
                    tilleggsinformasjonJson,
                    vedleggJson,
                    pdfDokumenter.map { dokument: FilOpplasting ->
                        FilOpplasting(
                            metadata = dokument.metadata,
                            data = krypteringService.krypter(dokument.data, krypteringFutureList, fiksX509Certificate),
                        )
                    },
                    kommunenr,
                    navEksternRefId,
                )
            waitForFutures(krypteringFutureList)
        } finally {
            krypteringFutureList
                .filter { !it.isDone && !it.isCancelled }
                .forEach { it.cancel(true) }
        }
        return when (response) {
            is SendSoknadResponse.Success -> response.digisosId
            is SendSoknadResponse.Error -> throw IllegalStateException("Opplasting av $navEksternRefId til fiks-digisos-api feilet", response.e)
            is SendSoknadResponse.ResponseError -> handleResponseError(navEksternRefId, startTime, response.e)
        }
    }

    private fun handleResponseError(
        soknadId: UUID,
        startTime: Long,
        e: WebClientResponseException,
    ): UUID {
        val errorResponse = e.responseBodyAsString
        val digisosId = Utils.getDigisosIdFromResponse(errorResponse, soknadId)

        when {
            digisosId != null && e is WebClientResponseException.BadRequest -> handleAlleredeMottatt(digisosId, soknadId, errorResponse)
            else -> throw IllegalStateException(
                "Opplasting av $soknadId til fiks-digisos-api feilet etter ${System.currentTimeMillis() - startTime} " +
                    "ms med status ${e.statusCode} og response: $errorResponse",
            )
        }
    }

    private fun handleAlleredeMottatt(
        digisosId: UUID,
        soknadId: UUID,
        errorResponse: String,
    ): Nothing {
        logger.warn(
            "Søknad $soknadId er allerede sendt med id $digisosId. " +
                "Returner exception med digisos-id så brukeren blir rutet til innsyn. " +
                "ErrorResponse var: $errorResponse",
        )
        throw AlleredeMottattException(
            digisosId = digisosId,
            message = "Søknad $soknadId er allerede sendt med id $digisosId. ErrorResponse var: $errorResponse",
        )
    }

    private fun JsonInternalSoknad.toSoknadJson(): String =
        objectMapper.writeValueAsString(soknad)
            .also { JsonSosialhjelpValidator.ensureValidSoknad(it) }

    private fun JsonInternalSoknad.toVedleggJson(): String {
        /* I en kort søknad må man ha et vedleggobjekt for å kunne vise fram opplastingsboksen på frontend,
           men det er ikke riktig at de skal ha status VedleggKreves og dermed vises som vedleggskrav på innsyn.
           Fjerner derfor alle vedlegg som ikke har filer her.
         */
        if (soknad.data.soknadstype == Soknadstype.KORT) {
            logger.info("Søknadstype er KORT, fjerner alle vedlegg som ikke har filer")
            vedlegg.vedlegg = vedlegg.vedlegg.filter { it.filer.isNotEmpty() }
        }

        return objectMapper
            .writeValueAsString(vedlegg)
            .also { JsonSosialhjelpValidator.ensureValidVedlegg(it) }
    }

    private fun JsonInternalSoknad.createTilleggsinformasjonJson(): String {
        return objectMapper.writeValueAsString(JsonTilleggsinformasjon(soknad.mottaker.enhetsnummer))
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

    private val fiksX509Certificate get() = dokumentlagerClient.getDokumentlagerPublicKeyX509Certificate()
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
