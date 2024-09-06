package no.nav.sosialhjelp.soknad.v2.bostotte

import no.nav.sosialhjelp.soknad.v2.okonomi.Bekreftelse
import no.nav.sosialhjelp.soknad.v2.okonomi.BekreftelseType.BOSTOTTE
import no.nav.sosialhjelp.soknad.v2.okonomi.BekreftelseType.BOSTOTTE_SAMTYKKE
import no.nav.sosialhjelp.soknad.v2.okonomi.BostotteSak
import no.nav.sosialhjelp.soknad.v2.okonomi.OkonomiService
import no.nav.sosialhjelp.soknad.v2.okonomi.inntekt.Inntekt
import no.nav.sosialhjelp.soknad.v2.okonomi.inntekt.InntektType
import no.nav.sosialhjelp.soknad.v2.register.fetchers.BostotteHusbankenFetcher
import no.nav.sosialhjelp.soknad.v2.soknad.IntegrasjonStatusService
import org.springframework.stereotype.Service
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
        userToken: String?,
    )
}

@Service
class BostotteServiceImpl(
    private val okonomiService: OkonomiService,
    private val integrasjonStatusService: IntegrasjonStatusService,
    private val husbankenFetcher: BostotteHusbankenFetcher,
) : BostotteService {
    override fun getBostotteInfo(soknadId: UUID): BostotteInfo {
        return getBekreftelseAndSamtykke(okonomiService.getBekreftelser(soknadId))
            .let { (bostotte, samtykke) ->
                BostotteInfo(
                    bostotte = bostotte,
                    samtykke = samtykke,
                    saker = okonomiService.getBostotteSaker(soknadId),
                    utbetalinger = okonomiService.getInntekter(soknadId).filter { it.type == InntektType.UTBETALING_HUSBANKEN },
                    fetchHusbankenFeilet = integrasjonStatusService.hasHusbankenFailed(soknadId),
                )
            }
    }

    override fun updateBostotte(
        soknadId: UUID,
        hasBostotte: Boolean,
    ) {
        okonomiService.updateBekreftelse(soknadId, BOSTOTTE, hasBostotte)
        if (!hasBostotte) {
            okonomiService.deleteBekreftelse(soknadId, BOSTOTTE_SAMTYKKE)
        }
    }

    override fun updateSamtykke(
        soknadId: UUID,
        hasSamtykke: Boolean,
        userToken: String?,
    ) {
        okonomiService.getBekreftelser(soknadId)
            .find { it.type == BOSTOTTE }
            ?.also { bostotteBekreftelse ->
                if (bostotteBekreftelse.verdi) {
                    okonomiService.updateBekreftelse(soknadId, BOSTOTTE_SAMTYKKE, hasSamtykke)
                    if (hasSamtykke) {
                        userToken?.let { husbankenFetcher.fetchAndSave(soknadId, userToken) }
                    }
                }
            }
    }
}

private fun getBekreftelseAndSamtykke(bekreftelser: Set<Bekreftelse>): Pair<Bekreftelse?, Bekreftelse?> {
    val bostotte = bekreftelser.find { it.type == BOSTOTTE }
    val samtykke =
        if (bostotte != null && bostotte.verdi) {
            bekreftelser.find { it.type == BOSTOTTE_SAMTYKKE }
        } else {
            null
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
