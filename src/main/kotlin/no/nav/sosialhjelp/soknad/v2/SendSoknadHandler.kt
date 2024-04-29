package no.nav.sosialhjelp.soknad.v2

import no.nav.sbl.soknadsosialhjelp.json.JsonSosialhjelpObjectMapper
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.app.exceptions.FeilVedSendingTilFiksException
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.innsending.digisosapi.DigisosApiV2Client
import no.nav.sosialhjelp.soknad.innsending.digisosapi.JsonTilleggsinformasjon
import no.nav.sosialhjelp.soknad.innsending.digisosapi.dto.FilMetadata
import no.nav.sosialhjelp.soknad.innsending.digisosapi.dto.FilOpplasting
import no.nav.sosialhjelp.soknad.metrics.VedleggskravStatistikkUtil
import no.nav.sosialhjelp.soknad.pdf.SosialhjelpPdfGenerator
import no.nav.sosialhjelp.soknad.v2.json.generate.JsonInternalSoknadGenerator
import no.nav.sosialhjelp.soknad.v2.soknad.Soknad
import no.nav.sosialhjelp.soknad.vedlegg.filedetection.MimeTypes
import org.springframework.stereotype.Component
import java.io.ByteArrayInputStream
import java.util.UUID
import no.nav.sosialhjelp.soknad.v2.kontakt.NavEnhetService

@Component
class SendSoknadHandler(
    private val digisosApiV2Client: DigisosApiV2Client,
    private val sosialhjelpPdfGenerator: SosialhjelpPdfGenerator,
    private val jsonGenerator: JsonInternalSoknadGenerator,
    private val navEnhetService: NavEnhetService,
) {
    private val objectMapper = JsonSosialhjelpObjectMapper.createObjectMapper()

    fun doSendAndReturnDigisosId(soknad: Soknad): UUID {
        val json = jsonGenerator.createJsonInternalSoknad(soknad.id)

        val mottaker = navEnhetService.findMottaker(soknad.id)

        mottaker?.let {
            log.info(
                "Starter kryptering av filer for ${soknad.id}, " +
                    "skal sende til kommune ${it.kommunenummer}) med " +
                    "enhetsnummer ${it.enhetsnummer} og navenhetsnavn ${it.enhetsnavn}",
            )
        }

        val digisosId: UUID =
            try {
                // TODO Verdt å kikke litt på digisosApiV2Clienten
                digisosApiV2Client.krypterOgLastOppFiler(
                    soknadJson = objectMapper.writeValueAsString(json.soknad),
                    tilleggsinformasjonJson =
                        objectMapper.writeValueAsString(
                            JsonTilleggsinformasjon(mottaker?.enhetsnummer),
                        ),
                    vedleggJson = objectMapper.writeValueAsString(json.vedlegg),
                    dokumenter = getFilOpplastingList(json),
                    kommunenr = json.soknad.mottaker.kommunenummer,
                    navEksternRefId = soknad.id.toString(),
                    token = SubjectHandlerUtils.getToken(),
                ).let { UUID.fromString(it) }
            } catch (e: Exception) {
                throw FeilVedSendingTilFiksException("Feil ved sending til fiks", e, soknad.id.toString())
            }
        VedleggskravStatistikkUtil.genererVedleggskravStatistikk(json)

        return digisosId
    }

    private fun getFilOpplastingList(json: JsonInternalSoknad): List<FilOpplasting> {
        // TODO vi må ha en logikk som er sikker på at våre lokale referanser (og antall) stemmer..
        // TODO ...overens med antall vedlegg hos mellomlagring
//        val mellomlagredeVedlegg = mellomlagringService.getAllVedlegg(soknadUnderArbeid.behandlingsId)

        return listOf(
            lagDokumentForSaksbehandlerPdf(json),
            lagDokumentForJuridiskPdf(json),
            lagDokumentForBrukerkvitteringPdf(),
        ).also {
            log.info("Antall vedlegg: ${it.size}.")
            // TODO Antall mellomlastede vedlegg (filer!!) bør kunne utledes fra våre egne data
//            log.info("Antall vedlegg: ${it.size}. Antall mellomlagrede vedlegg: ${mellomlagredeVedlegg.size}")
        }
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

    private fun opprettFilOpplastingFraByteArray(
        filnavn: String,
        bytes: ByteArray,
    ): FilOpplasting {
        return FilOpplasting(
            metadata =
                FilMetadata(
                    filnavn = filnavn,
                    mimetype = MimeTypes.APPLICATION_PDF,
                    storrelse = bytes.size.toLong(),
                ),
            data = ByteArrayInputStream(bytes),
        )
    }

    companion object {
        private val log by logger()
    }
}
