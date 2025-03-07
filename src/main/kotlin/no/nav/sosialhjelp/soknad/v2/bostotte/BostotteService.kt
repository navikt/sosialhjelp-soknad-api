package no.nav.sosialhjelp.soknad.v2.bostotte

import no.nav.sosialhjelp.soknad.app.exceptions.SosialhjelpSoknadApiException
import no.nav.sosialhjelp.soknad.v2.dokumentasjon.DokumentasjonService
import no.nav.sosialhjelp.soknad.v2.okonomi.Bekreftelse
import no.nav.sosialhjelp.soknad.v2.okonomi.BekreftelseType.BOSTOTTE
import no.nav.sosialhjelp.soknad.v2.okonomi.BekreftelseType.BOSTOTTE_SAMTYKKE
import no.nav.sosialhjelp.soknad.v2.okonomi.BostotteSak
import no.nav.sosialhjelp.soknad.v2.okonomi.OkonomiService
import no.nav.sosialhjelp.soknad.v2.okonomi.inntekt.Inntekt
import no.nav.sosialhjelp.soknad.v2.okonomi.inntekt.InntektType
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

interface BostotteService {
    fun getBostotteInfo(soknadId: UUID): BostotteInfo

    fun updateBostotte(
        soknadId: UUID,
        hasBostotte: Boolean,
    )

    fun updateSamtykke(
        soknadId: UUID,
        hasSamtykke: Boolean,
    )

    fun addForventetDokumentasjon(soknadId: UUID)

    fun saveDataFromHusbanken(
        soknadId: UUID,
        saker: List<BostotteSak>,
        utbetalinger: Inntekt?,
    )
}

@Service
class BostotteServiceImpl(
    private val okonomiService: OkonomiService,
    private val dokumentasjonService: DokumentasjonService,
) : BostotteService {
    @Transactional(readOnly = true)
    override fun getBostotteInfo(soknadId: UUID): BostotteInfo {
        return getBekreftelseAndSamtykke(okonomiService.getBekreftelser(soknadId))
            .let { (bostotte, samtykke) ->
                BostotteInfo(
                    bostotte = bostotte,
                    samtykke = samtykke,
                    saker = okonomiService.getBostotteSaker(soknadId),
                    utbetalinger = okonomiService.getInntekter(soknadId).filter { it.type == InntektType.UTBETALING_HUSBANKEN },
                    fetchHusbankenFeilet = null,
                )
            }
    }

    @Transactional
    override fun updateBostotte(
        soknadId: UUID,
        hasBostotte: Boolean,
    ) {
        resetBostotte(soknadId)

        okonomiService.updateBekreftelse(soknadId, BOSTOTTE, hasBostotte)
        if (hasBostotte) {
            dokumentasjonService.opprettDokumentasjon(soknadId, InntektType.UTBETALING_HUSBANKEN)
            okonomiService.addElementToOkonomi(soknadId, InntektType.UTBETALING_HUSBANKEN)
        }
    }

    @Transactional
    override fun updateSamtykke(
        soknadId: UUID,
        hasSamtykke: Boolean,
    ) {
        if (!hasBostotte(soknadId)) error("HasBostotte er null eller false ved oppdatering av samtykke")

        okonomiService.updateBekreftelse(soknadId, BOSTOTTE_SAMTYKKE, hasSamtykke)
        if (hasSamtykke) {
            dokumentasjonService.fjernForventetDokumentasjon(soknadId, InntektType.UTBETALING_HUSBANKEN)
            okonomiService.removeElementFromOkonomi(soknadId, InntektType.UTBETALING_HUSBANKEN)
        } else {
            okonomiService.removeBostotteSaker(soknadId)
            // fjerner eventuelt tidligere lagrede utbetalinger
            okonomiService.removeElementFromOkonomi(soknadId, InntektType.UTBETALING_HUSBANKEN)
            // ved bostotte = ja og samtykke = nei, skal det fortsatt være en mulighet for bruker å legge til
            okonomiService.addElementToOkonomi(soknadId, InntektType.UTBETALING_HUSBANKEN)
        }
    }

    override fun saveDataFromHusbanken(
        soknadId: UUID,
        saker: List<BostotteSak>,
        utbetalinger: Inntekt?,
    ) {
        if (saker.isNotEmpty()) okonomiService.addBostotteSaker(soknadId, saker)
        utbetalinger?.let { okonomiService.addElementToOkonomi(soknadId, it) }
    }

    @Transactional
    override fun addForventetDokumentasjon(soknadId: UUID) {
        okonomiService.addElementToOkonomi(soknadId, InntektType.UTBETALING_HUSBANKEN)
        dokumentasjonService.opprettDokumentasjon(soknadId, InntektType.UTBETALING_HUSBANKEN)
    }

    private fun hasBostotte(soknadId: UUID): Boolean =
        okonomiService.getBekreftelser(soknadId)
            .find { it.type == BOSTOTTE }?.verdi == true

    private fun resetBostotte(soknadId: UUID) {
        okonomiService.deleteBekreftelse(soknadId, BOSTOTTE)
        okonomiService.deleteBekreftelse(soknadId, BOSTOTTE_SAMTYKKE)
        okonomiService.removeBostotteSaker(soknadId)
        okonomiService.removeElementFromOkonomi(soknadId, InntektType.UTBETALING_HUSBANKEN)
        dokumentasjonService.fjernForventetDokumentasjon(soknadId, InntektType.UTBETALING_HUSBANKEN)
    }
}

private fun getBekreftelseAndSamtykke(bekreftelser: Set<Bekreftelse>): Pair<Bekreftelse?, Bekreftelse?> {
    val bostotte = bekreftelser.find { it.type == BOSTOTTE }
    val samtykke =
        bostotte?.let {
            if (bostotte.verdi) {
                bekreftelser.find { it.type == BOSTOTTE_SAMTYKKE }
            } else {
                null
            }
        }
    return Pair(bostotte, samtykke)
}

data class BostotteInfo(
    val bostotte: Bekreftelse?,
    val samtykke: Bekreftelse?,
    val saker: List<BostotteSak>,
    val utbetalinger: List<Inntekt>,
    val fetchHusbankenFeilet: Boolean?,
)

data class UpdateBostotteException(
    override val message: String?,
    val soknadId: UUID? = null,
) : SosialhjelpSoknadApiException(
        message = message,
        cause = null,
        id = soknadId?.toString(),
    )
