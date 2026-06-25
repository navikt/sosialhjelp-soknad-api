package no.nav.sosialhjelp.soknad.v2

import no.nav.sosialhjelp.api.fiks.KommuneInfo
import no.nav.sosialhjelp.soknad.api.minesaker.MineSakerService
import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.app.exceptions.SendingTilKommuneErMidlertidigUtilgjengeligException
import no.nav.sosialhjelp.soknad.app.exceptions.SendingTilKommuneUtilgjengeligException
import no.nav.sosialhjelp.soknad.app.exceptions.SosialhjelpSoknadApiException
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils.getUserIdFromToken
import no.nav.sosialhjelp.soknad.innsending.digisosapi.kommuneinfo.KommuneInfoService
import no.nav.sosialhjelp.soknad.v2.dokumentasjon.DokumentasjonRepository
import no.nav.sosialhjelp.soknad.v2.dokumentasjon.MellomlagerService
import no.nav.sosialhjelp.soknad.v2.dokumentasjon.removeDokumentFromDokumentasjon
import no.nav.sosialhjelp.soknad.v2.json.generate.TimestampUtil.nowWithMillis
import no.nav.sosialhjelp.soknad.v2.kontakt.NavEnhet
import no.nav.sosialhjelp.soknad.v2.kontakt.service.AdresseService
import no.nav.sosialhjelp.soknad.v2.kontakt.service.ForMangeMottakereException
import no.nav.sosialhjelp.soknad.v2.kontakt.service.ForMangeMottakereInfo
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadMetadata
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadMetadataService
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.UUID

interface SoknadValidator {
    fun validate(soknadId: UUID)
}

@Component
class SoknadMottakerValidator(
    private val adresseService: AdresseService,
    private val kommuneInfoService: KommuneInfoService,
) : SoknadValidator {
    override fun validate(soknadId: UUID) {
        val mottaker = harSoknadMottaker(soknadId)
        kanKommuneMottaSoknad(mottaker)
    }

    private fun harSoknadMottaker(soknadId: UUID): NavEnhet =
        adresseService.findMottaker(soknadId) ?: error("Søknad mangler NavEnhet")

    private fun kanKommuneMottaSoknad(mottaker: NavEnhet) {
        val kommunenummer = mottaker.kommunenummer
        kommuneInfoService.hentAlleKommuneInfo()
            ?.let { kommuneInfoMap -> kommuneInfoMap[kommunenummer] }
            ?.also { kommuneInfo ->
                kommuneInfo.validateKanMottaSoknader()
                kommuneInfo.validateIsNotMidlertidigDeaktivert()
            }
            ?: throw SendingTilKommuneUtilgjengeligException("Fant ikke KommuneInfo for $kommunenummer")
    }

    private fun KommuneInfo.validateKanMottaSoknader() {
        if (!kanMottaSoknader) {
            throw SendingTilKommuneUtilgjengeligException(
                "Kommune $kommunenummer kan ikke ta imot søknader",
            )
        }
    }

    private fun KommuneInfo.validateIsNotMidlertidigDeaktivert() {
        if (harMidlertidigDeaktivertMottak) {
            throw SendingTilKommuneErMidlertidigUtilgjengeligException(
                "Kommune $kommunenummer har midlertidig deaktivert mottak av søknader",
            )
        }
    }
}

@Component
class DocumentValidator(
    private val dokumentasjonRepository: DokumentasjonRepository,
    private val mellomlagerService: MellomlagerService,
) : SoknadValidator {
    override fun validate(soknadId: UUID) {
        validateDocumentsExistsInMellomlager(soknadId)
    }

    private fun validateDocumentsExistsInMellomlager(soknadId: UUID) {
        val filIdsMellomlager = mellomlagerService.getAllDokumenterMetadata(soknadId).map { it.filId }

        runCatching {
            dokumentasjonRepository.findAllBySoknadId(soknadId)
                .forEach { dokumentasjon ->
                    dokumentasjon.dokumenter.forEach { dokument ->
                        when (filIdsMellomlager.find { it == dokument.dokumentId.toString() }) {
                            null -> {
                                logger.error(
                                    "Dokument(${dokument.dokumentId}) på dokumentasjon(type=${dokumentasjon.type}) " +
                                        "mangler i FIKS mellomlager. Sletter.",
                                )
                                dokumentasjonRepository.removeDokumentFromDokumentasjon(soknadId, dokument.dokumentId)
                            }
                        }
                    }
                }
        }
            .getOrElse { throw IllegalStateException("Feil ved validering av dokumenter hos mellomlager", it) }
    }

    companion object {
        private val logger by logger()
    }
}

@Component
class AntallSoknaderSendtValidator(private val mineSakerService: MineSakerService) : SoknadValidator {
    override fun validate(soknadId: UUID) {
        mineSakerService.hentInnsendteSoknaderSisteDogn()
            .also { (antall, innsendingTillattFra) ->
                if (antall >= MAX_ANTALL_SOKNADER) {
                    if (innsendingTillattFra == null) error("Soker har $MAX_ANTALL_SOKNADER eller flere soknader sendt siste 24 timer, men innsendingTillattFra er null")
                    throw AntallSoknaderSendtException(antall, soknadId, innsendingTillattFra)
                }
            }
    }

    companion object {
        const val MAX_ANTALL_SOKNADER = 2
    }
}

@Component
class BegrenseAntallMottakereValidator(
    private val metadataService: SoknadMetadataService,
    private val adresseService: AdresseService,
) : SoknadValidator {
    override fun validate(soknadId: UUID) {
        val kommunenummer =
            adresseService.findMottaker(soknadId)?.kommunenummer
                ?: error("Mangler mottaker ved validering: ${BegrenseAntallMottakereValidator::class.simpleName}")

        findRelevantMetadata().also { metadatas -> doValidate(metadatas, kommunenummer) }
    }

    fun validateMottaker(kommunenummer: String) {
        findRelevantMetadata().also { metadatas -> doValidate(metadatas, kommunenummer) }
    }

    private fun findRelevantMetadata(): List<SoknadMetadata> =
        metadataService.findMetadataForPersonSendtInnAfter(
            personId = getUserIdFromToken(),
            date = nowWithMillis().minusDays(ANTALL_DAGER_BEGRENSET),
        )

    private fun doValidate(
        metadatas: List<SoknadMetadata>,
        kommunenummer: String,
    ) {
        val listOfMottakere = metadatas.getMottakere()

        if (listOfMottakere.size < MAX_ANTALL_KOMMUNER) {
            return
        } else {
            if (listOfMottakere.none { it == kommunenummer }) {
                throw ForMangeMottakereException(
                    message =
                        "Du har sendt soknad til ${listOfMottakere.size} forskjellige kommuner de siste $ANTALL_DAGER_BEGRENSET dagene. " +
                            "Maks antall kommuner innenfor $ANTALL_DAGER_BEGRENSET dager er $MAX_ANTALL_KOMMUNER.",
                    info =
                        ForMangeMottakereInfo(
                            innsendingGyldigFra = metadatas.getInnsendingGyldigIfra(),
                            antallMottakere = listOfMottakere.size,
                            maksAntallMottakere = MAX_ANTALL_KOMMUNER,
                        ),
                )
            }
        }
    }

    companion object {
        // bruker skal kun få lov til å søke i x kommuner innenfor n dager
        const val ANTALL_DAGER_BEGRENSET = 7L
        const val MAX_ANTALL_KOMMUNER = 2

        private fun List<SoknadMetadata>.getMottakere(): List<String> = mapNotNull { it.mottakerKommunenummer }.distinct()

        private fun List<SoknadMetadata>.getInnsendingGyldigIfra(): LocalDateTime =
            mapNotNull { it.tidspunkt.sendtInn }.sortedByDescending { it }[MAX_ANTALL_KOMMUNER - 1]
                .plusDays(1)
                .plusMinutes(1)
                .truncatedTo(ChronoUnit.MINUTES)
    }
}

data class AntallSoknaderSendtException(
    val antall: Int,
    val soknadId: UUID,
    val innsendingTillattFra: LocalDateTime,
) : SosialhjelpSoknadApiException("$antall soknader sendt siste 24 timer", null, soknadId.toString())
