package no.nav.sosialhjelp.soknad.v2

import no.nav.sbl.soknadsosialhjelp.json.JsonSosialhjelpObjectMapper
import no.nav.sbl.soknadsosialhjelp.json.JsonSosialhjelpValidator
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
import no.nav.sosialhjelp.soknad.v2.kontakt.NavEnhet
import no.nav.sosialhjelp.soknad.v2.soknad.SoknadService
import no.nav.sosialhjelp.soknad.vedlegg.filedetection.MimeTypes
import org.springframework.stereotype.Component
import java.io.ByteArrayInputStream
import java.time.LocalDateTime
import java.util.UUID

@Component
class SendSoknadHandler(
    private val digisosApiV2Client: DigisosApiV2Client,
    private val sosialhjelpPdfGenerator: SosialhjelpPdfGenerator,
    private val jsonGenerator: JsonInternalSoknadGenerator,
    private val soknadValidator: SoknadValidator,
    private val soknadService: SoknadService,
) {
    private val objectMapper = JsonSosialhjelpObjectMapper.createObjectMapper()

    fun doSendAndReturnInfo(soknadId: UUID): SoknadSendtInfo {
        val json = jsonGenerator.createJsonInternalSoknad(soknadId)

        val mottaker = soknadValidator.validateAndReturnMottaker(soknadId)

        val digisosId: UUID =
            runCatching {
                digisosApiV2Client
                    .krypterOgLastOppFiler(
                        soknadJson = objectMapper.writeValueAsString(json.soknad),
                        tilleggsinformasjonJson =
                            objectMapper.writeValueAsString(
                                JsonTilleggsinformasjon(mottaker.enhetsnummer),
                            ),
                        vedleggJson = json.toVedleggJson(),
                        dokumenter = getFilOpplastingList(json),
                        kommunenr = json.soknad.mottaker.kommunenummer,
                        navEksternRefId = soknadId.toString(),
                        token = SubjectHandlerUtils.getToken(),
                    ).let { UUID.fromString(it) }
            }.onFailure {
                logger.error("Feil ved sending av soknad til FIKS", it)
                throw FeilVedSendingTilFiksException("Feil ved sending til fiks", it, soknadId.toString())
            }.getOrThrow()

        logger.info("Sendt ${json.soknad.data.soknadstype.value()} søknad til FIKS")

        VedleggskravStatistikkUtil.genererVedleggskravStatistikk(json)

        val innsendingTidspunkt = LocalDateTime.now().also { soknadService.setInnsendingstidspunkt(soknadId, it) }

        return SoknadSendtInfo(
            // TODO Dette er vel ikke soknadId
            digisosId = digisosId,
            navEnhet = mottaker,
            isKortSoknad = soknadService.erKortSoknad(soknadId),
            innsendingTidspunkt = innsendingTidspunkt,
        )
    }

    private fun JsonInternalSoknad.toVedleggJson(): String =
        objectMapper
            .writeValueAsString(vedlegg)
            .also { JsonSosialhjelpValidator.ensureValidVedlegg(it) }

    private fun getFilOpplastingList(json: JsonInternalSoknad): List<FilOpplasting> {
        // TODO vi må ha en logikk som er sikker på at våre lokale referanser (og antall) stemmer..
        // TODO ...overens med antall vedlegg hos mellomlagring
//        val mellomlagredeVedlegg = mellomlagringService.getAllVedlegg(soknadUnderArbeid.behandlingsId)

        return listOf(
            lagDokumentForSaksbehandlerPdf(json),
            lagDokumentForJuridiskPdf(json),
            lagDokumentForBrukerkvitteringPdf(),
        ).also {
            logger.info("Antall vedlegg: ${it.size}.")
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

    companion object {
        private val logger by logger()
    }
}

data class SoknadSendtInfo(
    val digisosId: UUID,
    val navEnhet: NavEnhet,
    val isKortSoknad: Boolean,
    val innsendingTidspunkt: LocalDateTime,
)
