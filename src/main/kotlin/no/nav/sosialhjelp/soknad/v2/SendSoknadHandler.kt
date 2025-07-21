package no.nav.sosialhjelp.soknad.v2

import no.nav.sbl.soknadsosialhjelp.json.JsonSosialhjelpObjectMapper
import no.nav.sbl.soknadsosialhjelp.json.JsonSosialhjelpValidator
import no.nav.sbl.soknadsosialhjelp.soknad.JsonData.Soknadstype
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.app.exceptions.SoknadAlleredeSendtException
import no.nav.sosialhjelp.soknad.innsending.digisosapi.AlleredeMottattException
import no.nav.sosialhjelp.soknad.innsending.digisosapi.DigisosApiV2Client
import no.nav.sosialhjelp.soknad.innsending.digisosapi.JsonTilleggsinformasjon
import no.nav.sosialhjelp.soknad.innsending.digisosapi.dto.FilMetadata
import no.nav.sosialhjelp.soknad.innsending.digisosapi.dto.FilOpplasting
import no.nav.sosialhjelp.soknad.metrics.VedleggskravStatistikkUtil
import no.nav.sosialhjelp.soknad.pdf.SosialhjelpPdfGenerator
import no.nav.sosialhjelp.soknad.v2.json.generate.JsonInternalSoknadGenerator
import no.nav.sosialhjelp.soknad.v2.json.generate.TimestampUtil.nowWithMillis
import no.nav.sosialhjelp.soknad.v2.kontakt.NavEnhet
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadMetadataService
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadStatus
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
    private val metadataService: SoknadMetadataService,
) {
    private val objectMapper = JsonSosialhjelpObjectMapper.createObjectMapper()

    fun doSendAndReturnInfo(
        soknadId: UUID,
    ): SoknadSendtInfo {
        val innsendingTidspunkt = metadataService.setInnsendingstidspunkt(soknadId, nowWithMillis())

        val json = jsonGenerator.createJsonInternalSoknad(soknadId)
        val mottaker = soknadValidator.validateAndReturnMottaker(soknadId)

        val digisosId: UUID =
            runCatching {
                digisosApiV2Client.krypterOgLastOppFiler(
                    soknadJson = objectMapper.writeValueAsString(json.soknad),
                    tilleggsinformasjonJson =
                        objectMapper.writeValueAsString(JsonTilleggsinformasjon(mottaker.enhetsnummer)),
                    vedleggSpec = json.toVedleggJson(),
                    pdfDokumenter = getFilOpplastingList(json),
                    kommunenr = json.soknad.mottaker.kommunenummer,
                    navEksternRefId = soknadId,
                )
            }
                .onSuccess { digisosId ->
                    mottaker.kommunenummer?.also {
                        metadataService.updateSoknadSendt(
                            soknadId = soknadId,
                            kommunenummer = mottaker.kommunenummer,
                            digisosId = digisosId,
                        )
                    }
                        ?: error("NavMottaker mangler kommunenummer")
                }
                .onFailure { e ->
                    when (e) {
                        is AlleredeMottattException -> {
                            throw SoknadAlleredeSendtException(
                                sendtInfo =
                                    SoknadSendtInfo(
                                        e.digisosId,
                                        mottaker,
                                        soknadService.erKortSoknad(soknadId),
                                        innsendingTidspunkt,
                                    ),
                                message = "Søknad med ID $soknadId er allerede sendt.",
                            )
                        }
                        else -> {
                            metadataService.updateSendingFeilet(soknadId)
                            logger.error("Feil ved sending av soknad til FIKS", e)
                            throw e
                        }
                    }
                }
                .getOrThrow()

        val duplicates =
            json.soknad.data.okonomi.opplysninger.utbetaling
                .groupBy { listOf(it.tittel, it.utbetalingsdato, it.netto, it.brutto) }
                .filter { it.value.size > 1 }

        val totalDuplicatesCount = duplicates.values.sumOf { it.size }

        if (totalDuplicatesCount > 0) {
            logger.info(
                "Søknad sendt, ut av ${json.soknad.data.okonomi.opplysninger.utbetaling.size} " +
                    "utbetaling(er) så er det $totalDuplicatesCount som er identiske utbetaling(er)",
            )
        }

        logger.info("Sendt ${json.soknad.data.soknadstype.value()} søknad til FIKS med DigisosId: $digisosId")

        VedleggskravStatistikkUtil.genererVedleggskravStatistikk(json)

        return SoknadSendtInfo(
            digisosId = digisosId,
            navEnhet = mottaker,
            isKortSoknad = soknadService.erKortSoknad(soknadId),
            innsendingTidspunkt = innsendingTidspunkt,
        )
    }

    fun getDeletionDate(soknadId: UUID): LocalDateTime {
        return metadataService.getMetadataForSoknad(soknadId)
            .run {
                when (status) {
                    SoknadStatus.INNSENDING_FEILET -> tidspunkt.opprettet.plusDays(19)
                    else -> tidspunkt.opprettet.plusDays(14)
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
