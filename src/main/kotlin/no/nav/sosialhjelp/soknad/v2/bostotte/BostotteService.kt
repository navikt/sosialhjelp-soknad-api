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
import no.nav.sosialhjelp.soknad.v2.register.fetchers.BostotteHusbankenFetcher
import no.nav.sosialhjelp.soknad.v2.soknad.IntegrasjonStatusService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

interface BostotteService {
    fun getBostotteInfo(soknadId: UUID): BostotteInfo

    fun updateBostotte(
        soknadId: UUID,
        hasBostotte: Boolean?,
        hasSamtykke: Boolean?,
    )
}

@Service
@Transactional
class BostotteServiceImpl(
    private val okonomiService: OkonomiService,
    private val integrasjonStatusService: IntegrasjonStatusService,
    private val husbankenFetcher: BostotteHusbankenFetcher,
    private val dokumentasjonService: DokumentasjonService,
) : BostotteService {
    override fun getBostotteInfo(soknadId: UUID): BostotteInfo {
        return getBekreftelseAndSamtykke(okonomiService.getBekreftelser(soknadId))
            .let { (bostotte, samtykke) ->
                BostotteInfo(
                    bostotte = bostotte,
                    samtykke = samtykke,
                    saker = okonomiService.getBostotteSaker(soknadId),
                    utbetalinger = okonomiService.getInntekter(soknadId).filter { it.type == InntektType.UTBETALING_HUSBANKEN },
                    fetchHusbankenFeilet = integrasjonStatusService.hasFetchHusbankenFailed(soknadId),
                )
            }
    }

    override fun updateBostotte(
        soknadId: UUID,
        // TODO: Denne kan være non nullable når spor av gammelt API er fjernet
        hasBostotte: Boolean?,
        hasSamtykke: Boolean?,
    ) {
        // TODO: Fjern også denne når hasBostotte er non nullable
        val hasConfirmedBostotte = hasBostotte.takeIf { it != null } ?: okonomiService.getBekreftelser(soknadId).find { it.type == BOSTOTTE }?.verdi
        checkNotNull(hasConfirmedBostotte) { "Bruker har ikke oppdatert bostøttebekreftelse" }
        okonomiService.updateBekreftelse(soknadId, BOSTOTTE, hasConfirmedBostotte)
        if (hasSamtykke != null) {
            getBekreftelseAndSamtykke(okonomiService.getBekreftelser(soknadId))
                .let { (_, samtykke) -> samtykke?.verdi != hasSamtykke }
                .also { needsUpdate ->
                    if (needsUpdate) {
                        okonomiService.updateBekreftelse(soknadId, BOSTOTTE_SAMTYKKE, hasSamtykke)
                        syncInntektOgDokumentasjonsKrav(soknadId)
                        if (hasSamtykke) husbankenFetcher.fetchAndSave(soknadId)
                    }
                }
        }
        syncInntektOgDokumentasjonsKrav(soknadId)
    }

    private fun syncInntektOgDokumentasjonsKrav(soknadId: UUID) {
        val (bostotte, samtykke) = getBekreftelseAndSamtykke(okonomiService.getBekreftelser(soknadId))

        when {
            bostotte?.verdi != true -> cleanBostotte(soknadId)
            samtykke?.verdi == true -> {
                dokumentasjonService.fjernForventetDokumentasjon(soknadId, InntektType.UTBETALING_HUSBANKEN)
                okonomiService.removeElementFromOkonomi(soknadId, InntektType.UTBETALING_HUSBANKEN)
            }
            // utledet -> bostotte er true, og samtykke null eller false
            else -> {
                // fjern eventuelt tidligere lagrede inntekter og opprett ny tom
                okonomiService.removeElementFromOkonomi(soknadId, InntektType.UTBETALING_HUSBANKEN)
                okonomiService.addElementToOkonomi(soknadId, InntektType.UTBETALING_HUSBANKEN)

                okonomiService.removeBostotteSaker(soknadId)
                dokumentasjonService.opprettDokumentasjon(soknadId, InntektType.UTBETALING_HUSBANKEN)
            }
        }
    }

    private fun cleanBostotte(soknadId: UUID) {
        okonomiService.deleteBekreftelse(soknadId, BOSTOTTE_SAMTYKKE)
        okonomiService.removeElementFromOkonomi(soknadId, InntektType.UTBETALING_HUSBANKEN)
        okonomiService.removeBostotteSaker(soknadId)
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
