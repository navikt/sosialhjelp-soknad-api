package no.nav.sosialhjelp.soknad.v2

import no.nav.sosialhjelp.api.fiks.KommuneInfo
import no.nav.sosialhjelp.soknad.api.minesaker.MineSakerService
import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.app.exceptions.SendingTilKommuneErMidlertidigUtilgjengeligException
import no.nav.sosialhjelp.soknad.app.exceptions.SendingTilKommuneUtilgjengeligException
import no.nav.sosialhjelp.soknad.app.exceptions.SosialhjelpSoknadApiException
import no.nav.sosialhjelp.soknad.innsending.digisosapi.kommuneinfo.KommuneInfoService
import no.nav.sosialhjelp.soknad.v2.dokumentasjon.DokumentasjonRepository
import no.nav.sosialhjelp.soknad.v2.dokumentasjon.MellomlagerService
import no.nav.sosialhjelp.soknad.v2.dokumentasjon.removeDokumentFromDokumentasjon
import no.nav.sosialhjelp.soknad.v2.kontakt.NavEnhet
import no.nav.sosialhjelp.soknad.v2.kontakt.service.AdresseService
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.util.UUID

interface SoknadValidators {
    fun validate(soknadId: UUID)
}

@Component
class SoknadMottakerValidator(
    private val adresseService: AdresseService,
    private val kommuneInfoService: KommuneInfoService,
) : SoknadValidators {
    override fun validate(soknadId: UUID) {
        val mottaker = harSoknadMottaker(soknadId)
        kanKommuneMottaSoknad(mottaker)
    }

    private fun harSoknadMottaker(soknadId: UUID): NavEnhet =
        adresseService.findMottaker(soknadId)
            ?.also { if (it.kommunenummer == null) error("Mottaker Mangler kommunenummer") }
            ?: error("Søknad mangler NavEnhet")

    private fun kanKommuneMottaSoknad(mottaker: NavEnhet) {
        val kommunenummer = mottaker.kommunenummer ?: error("NavEnhet ${mottaker.enhetsnavn} mangler kommunenummer")
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
) : SoknadValidators {
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
class AntallSoknaderSendtValidator(private val mineSakerService: MineSakerService) : SoknadValidators {
    override fun validate(soknadId: UUID) {
        mineSakerService.hentInnsendteSoknaderSisteDogn()
            .also { (antall, innsendingTillattFra) ->
                if (antall >= 10) {
                    if (innsendingTillattFra == null) error("Soker har flere enn 10 soknader sendt siste 24 timer, men innsendingTillattFra er null")
                    throw AntallSoknaderSendtException(antall, soknadId, innsendingTillattFra)
                }
            }
    }
}

data class AntallSoknaderSendtException(
    val antall: Int,
    val soknadId: UUID,
    val innsendingTillattFra: LocalDateTime,
) : SosialhjelpSoknadApiException("$antall soknader sendt siste 24 timer", null, soknadId.toString())
