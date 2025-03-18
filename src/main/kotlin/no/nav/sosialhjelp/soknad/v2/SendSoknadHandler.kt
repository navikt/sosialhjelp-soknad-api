package no.nav.sosialhjelp.soknad.v2

import no.nav.sbl.soknadsosialhjelp.json.JsonSosialhjelpObjectMapper
import no.nav.sbl.soknadsosialhjelp.json.JsonSosialhjelpValidator
import no.nav.sbl.soknadsosialhjelp.soknad.JsonData.Soknadstype
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.app.MiljoUtils
import no.nav.sosialhjelp.soknad.app.exceptions.FeilVedSendingTilFiksException
import no.nav.sosialhjelp.soknad.innsending.digisosapi.DigisosApiV2Client
import no.nav.sosialhjelp.soknad.innsending.digisosapi.JsonTilleggsinformasjon
import no.nav.sosialhjelp.soknad.innsending.digisosapi.dto.FilMetadata
import no.nav.sosialhjelp.soknad.innsending.digisosapi.dto.FilOpplasting
import no.nav.sosialhjelp.soknad.metrics.VedleggskravStatistikkUtil
import no.nav.sosialhjelp.soknad.nowWithMillis
import no.nav.sosialhjelp.soknad.pdf.SosialhjelpPdfGenerator
import no.nav.sosialhjelp.soknad.v2.json.generate.JsonInternalSoknadGenerator
import no.nav.sosialhjelp.soknad.v2.kontakt.NavEnhet
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadMetadataService
import no.nav.sosialhjelp.soknad.v2.soknad.SoknadService
import no.nav.sosialhjelp.soknad.vedlegg.filedetection.MimeTypes
import org.springframework.stereotype.Component
import java.io.ByteArrayInputStream
import java.time.LocalDateTime
import java.util.UUID

// TODO Ble mange argumenter til denne klassen - og ganske mye logikk. Refaktor?

@Component
class SendSoknadHandler(
    private val digisosApiV2Client: DigisosApiV2Client,
    private val sosialhjelpPdfGenerator: SosialhjelpPdfGenerator,
    private val jsonGenerator: JsonInternalSoknadGenerator,
    private val soknadValidator: SoknadValidator,
    private val soknadService: SoknadService,
    private val soknadMetadataService: SoknadMetadataService,
) {
    private val objectMapper = JsonSosialhjelpObjectMapper.createObjectMapper()

    fun doSendAndReturnInfo(
        soknadId: UUID,
        token: String?,
    ): SoknadSendtInfo {
        val innsendingTidspunkt = soknadMetadataService.setInnsendingstidspunkt(soknadId, nowWithMillis())

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
                        token = token,
                    )
                    .let { UUID.fromString(it) }
            }
                .onSuccess { digisosId ->
                    mottaker.kommunenummer
                        ?.also {
                            soknadMetadataService.updateSoknadSendt(
                                soknadId = soknadId,
                                kommunenummer = mottaker.kommunenummer,
                                digisosId = digisosId,
                            )
                        }
                        ?: error("NavMottaker mangler kommunenummer")
                }
                .onFailure {
                    soknadMetadataService.updateSendingFeilet(soknadId)
                    logger.error("Feil ved sending av soknad til FIKS", it)
                    throw FeilVedSendingTilFiksException("Feil ved sending til fiks", it, soknadId.toString())
                }
                .getOrThrow()

        logger.info("Sendt ${json.soknad.data.soknadstype.value()} søknad til FIKS med DigisosId: $digisosId")

        VedleggskravStatistikkUtil.genererVedleggskravStatistikk(json)

        return SoknadSendtInfo(
            digisosId = digisosId,
            navEnhet = mottaker,
            isKortSoknad = soknadService.erKortSoknad(soknadId),
            innsendingTidspunkt = innsendingTidspunkt,
        )
            .also {
                // TODO Logger ut json så data kan sjekkes
                if (MiljoUtils.isNonProduction()) {
                    logger.info(
                        "Følgende JsonInternalSoknad sendt: \n\n" +
                            JsonSosialhjelpObjectMapper.createObjectMapper().writeValueAsString(json),
                    )
                }
            }
    }

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
