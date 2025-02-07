package no.nav.sosialhjelp.soknad.v2.okonomi.utgift

import no.nav.sosialhjelp.soknad.v2.okonomi.Bekreftelse
import no.nav.sosialhjelp.soknad.v2.okonomi.BekreftelseType
import no.nav.sosialhjelp.soknad.v2.okonomi.OkonomiService
import no.nav.sosialhjelp.soknad.v2.okonomi.inntekt.InntektType
import no.nav.sosialhjelp.soknad.v2.soknad.IntegrasjonStatusService
import no.nav.sosialhjelp.soknad.v2.soknad.IntegrasjonstatusRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.util.UUID

interface BoutgiftService {
    fun getBoutgifter(soknadId: UUID): Set<Utgift>?

    fun removeBoutgifter(soknadId: UUID)

    fun updateBoutgifter(
        soknadId: UUID,
        existingBoutgifter: Set<UtgiftType>,
    )

    fun skalViseInfoVedBekreftelse(soknadId: UUID): Boolean
}

@Service
class BoutgiftServiceImpl(
    private val okonomiService: OkonomiService,
    private val integrasjonService: IntegrasjonStatusService,
    private val integrasjonstatusRepository: IntegrasjonstatusRepository,
) : BoutgiftService {
    override fun getBoutgifter(soknadId: UUID): Set<Utgift>? {
        return okonomiService.getBekreftelser(soknadId).find { it.type == BekreftelseType.BEKREFTELSE_BOUTGIFTER }
            ?.let { bostotteBekreftelse ->
                if (!bostotteBekreftelse.verdi) {
                    emptySet()
                } else {
                    okonomiService.getUtgifter(soknadId)
                        .filter { boutgiftTypes.contains(it.type) }
                        .toSet()
                }
            }
    }

    override fun removeBoutgifter(soknadId: UUID) {
        okonomiService.updateBekreftelse(soknadId, BekreftelseType.BEKREFTELSE_BOUTGIFTER, verdi = false)
        boutgiftTypes.forEach { okonomiService.removeElementFromOkonomi(soknadId, it) }
    }

    override fun updateBoutgifter(
        soknadId: UUID,
        existingBoutgifter: Set<UtgiftType>,
    ) {
        okonomiService.updateBekreftelse(soknadId, BekreftelseType.BEKREFTELSE_BOUTGIFTER, verdi = true)

        boutgiftTypes.forEach {
            if (existingBoutgifter.contains(it)) {
                okonomiService.addElementToOkonomi(soknadId, it)
            } else {
                okonomiService.removeElementFromOkonomi(soknadId, it)
            }
        }
    }

    // TODO "kopiering" og forsøk på enklere fremstilling av BoutgiftRessurs#getSkalViseInfoVedBekreftelse
    // TODO denne presiserer hvilke bekreftelser vi er interessert i - og ikke hva som helst som tidligere
    // TODO Skal backend egentlig eie denne logikken? Tore
    override fun skalViseInfoVedBekreftelse(soknadId: UUID): Boolean {
        return if (fetchBostotteFailedOrMissingSamtykke(soknadId)) {
            getBostotteRelatedBekreftelser(soknadId)
                // hvis bruker ikke har svart på noe relatert til bostotte - ikke vis
                .ifEmpty { return false }
                // hvis bruker har svart nei, eller ikke svart - vis info (returner true)
                .isBostotteBekreftelseFalseOrNull()
        } else {
            // ingen bostotte-utbetalinger eller saker - vis info (returnerer true)
            trueIfNoBostottesakerOrUtbetalinger(soknadId)
        }
    }

    private fun getBostotteRelatedBekreftelser(soknadId: UUID) =
        okonomiService.getBekreftelser(soknadId)
            .filter { bekreftelse -> boutgiftsRelevanteBekreftelser.contains(bekreftelse.type) }

    private fun fetchBostotteFailedOrMissingSamtykke(soknadId: UUID): Boolean {
        return integrasjonstatusRepository.findByIdOrNull(soknadId)?.feilStotteHusbanken == true ||
            okonomiService.getBekreftelser(soknadId).none {
                it.type == BekreftelseType.BOSTOTTE_SAMTYKKE && it.verdi
            }
    }

    private fun trueIfNoBostottesakerOrUtbetalinger(soknadId: UUID): Boolean {
        return okonomiService.getInntekter(soknadId).none { it.type == InntektType.UTBETALING_HUSBANKEN } &&
            okonomiService.getBostotteSaker(soknadId).isEmpty()
    }

    private fun List<Bekreftelse>.isBostotteBekreftelseFalseOrNull() =
        find { it.type == BekreftelseType.BOSTOTTE }
            ?.let { !it.verdi } ?: true

    companion object {
        private val boutgiftsRelevanteBekreftelser: List<BekreftelseType> =
            listOf(
                BekreftelseType.BEKREFTELSE_BOUTGIFTER,
                BekreftelseType.BOSTOTTE,
                BekreftelseType.BOSTOTTE_SAMTYKKE,
            )
        private val boutgiftTypes: Set<UtgiftType> =
            setOf(
                UtgiftType.UTGIFTER_HUSLEIE,
                UtgiftType.UTGIFTER_STROM,
                UtgiftType.UTGIFTER_KOMMUNAL_AVGIFT,
                UtgiftType.UTGIFTER_OPPVARMING,
                UtgiftType.UTGIFTER_BOLIGLAN,
                UtgiftType.UTGIFTER_ANNET_BO,
            )
    }
}
