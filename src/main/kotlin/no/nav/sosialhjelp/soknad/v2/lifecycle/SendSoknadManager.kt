package no.nav.sosialhjelp.soknad.v2.lifecycle

import no.nav.sbl.soknadsosialhjelp.json.JsonSosialhjelpObjectMapper
import no.nav.sbl.soknadsosialhjelp.json.JsonSosialhjelpValidator
import no.nav.sbl.soknadsosialhjelp.soknad.JsonData.Soknadstype
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sosialhjelp.soknad.innsending.digisosapi.DigisosApiV2Client
import no.nav.sosialhjelp.soknad.innsending.digisosapi.JsonTilleggsinformasjon
import no.nav.sosialhjelp.soknad.innsending.digisosapi.dto.FilMetadata
import no.nav.sosialhjelp.soknad.innsending.digisosapi.dto.FilOpplasting
import no.nav.sosialhjelp.soknad.pdf.SosialhjelpPdfGenerator
import no.nav.sosialhjelp.soknad.v2.kontakt.service.AdresseService
import no.nav.sosialhjelp.soknad.v2.lifecycle.SendSoknadHandler.Companion.logger
import no.nav.sosialhjelp.soknad.vedlegg.filedetection.MimeTypes
import org.springframework.stereotype.Component
import java.io.ByteArrayInputStream
import java.util.UUID

@Component
class SendSoknadManager(
    private val digisosApiV2Client: DigisosApiV2Client,
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
        return doKrypterAndSend(
            navEksternRefId = soknadId,
            soknadJson = json.toSoknadJson(),
            vedleggJson = json.toVedleggJson(),
            tilleggsinformasjon = json.createTilleggsinformasjonJson(),
            pdfDokumenter = getFilOpplastingList(json),
            kommunenummer = kommunenummer,
        )
    }

    private fun doKrypterAndSend(
        navEksternRefId: UUID,
        soknadJson: String,
        vedleggJson: String,
        pdfDokumenter: List<FilOpplasting>,
        tilleggsinformasjon: String,
        kommunenummer: String,
    ): UUID {
        return digisosApiV2Client.krypterOgLastOppFiler(
            soknadJson = soknadJson,
            tilleggsinformasjonJson = tilleggsinformasjon,
            vedleggJson = vedleggJson,
            pdfDokumenter = pdfDokumenter,
            kommunenr = kommunenummer,
            navEksternRefId = navEksternRefId,
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

    private fun getFilOpplastingList(json: JsonInternalSoknad): List<FilOpplasting> {
        return listOf(
            lagDokumentForSaksbehandlerPdf(json),
            lagDokumentForJuridiskPdf(json),
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
