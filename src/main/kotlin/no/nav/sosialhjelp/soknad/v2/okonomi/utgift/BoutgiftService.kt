package no.nav.sosialhjelp.soknad.v2.okonomi.utgift

import no.nav.sosialhjelp.soknad.v2.okonomi.BekreftelseType
import no.nav.sosialhjelp.soknad.v2.okonomi.OkonomiService
import no.nav.sosialhjelp.soknad.v2.okonomi.inntekt.InntektType
import no.nav.sosialhjelp.soknad.v2.soknad.IntegrasjonstatusRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.util.UUID

interface BoutgiftService {
    fun getBoutgifter(soknadId: UUID): Set<Utgift>?

    fun removeBoutgifter(soknadId: UUID)

    fun updateBoutgifter(
        soknadId: UUID,
        input: HarBoutgifterInput,
    )

    fun skalViseInfoVedBekreftelse(soknadId: UUID): Boolean
}

@Service
class BoutgiftServiceImpl(
    private val okonomiService: OkonomiService,
    // TODO Bruk integrasjonService (ligger i okonomi.inntekt - så den kan merges først)
    private val integrasjonstatusRepository: IntegrasjonstatusRepository,
) : BoutgiftService {
    override fun getBoutgifter(soknadId: UUID): Set<Utgift>? {
        return okonomiService.getUtgifter(soknadId)
            ?.filter { boutgiftTyper.contains(it.type) }
            ?.toSet()
    }

    override fun removeBoutgifter(soknadId: UUID) {
        okonomiService.updateBekreftelse(soknadId, BekreftelseType.BEKREFTELSE_BOUTGIFTER, verdi = false)

        okonomiService.getUtgifter(soknadId)
            ?.filter { boutgiftTyper.contains(it.type) }
            ?.forEach { okonomiService.removeElementFromOkonomi(soknadId, it.type) }
    }

    override fun updateBoutgifter(
        soknadId: UUID,
        input: HarBoutgifterInput,
    ) {
        TODO("Not yet implemented")
    }

    override fun skalViseInfoVedBekreftelse(soknadId: UUID): Boolean {
        val hasBostotteBekreftelse =
            okonomiService.getBekreftelser(soknadId)
                ?.any { it.type == BekreftelseType.BOSTOTTE }
        val hasInntektHusbanken =
            okonomiService.getInntekter(soknadId)
                ?.any { it.type == InntektType.UTBETALING_HUSBANKEN } ?: false
        val hasBostotteSaker =
            okonomiService.getBostotteSaker(soknadId)
                ?.isNotEmpty() ?: false
        val fetchBostotteFailed = integrasjonstatusRepository.findByIdOrNull(soknadId)?.feilStotteHusbanken == true
        val missingBostotteSamtykke =
            okonomiService.getBekreftelser(soknadId)
                ?.none { it.type == BekreftelseType.BOSTOTTE_SAMTYKKE && it.verdi } ?: true

        // IF henting av bostotte har feilet ELLER vi mangler samtykke
        // -> sjekk om vi har noen bekreftelser -> hvis ikke returner false
        // -> sjekk om vi har bekreftelse BOSTOTTE -> hvis ikke returner true
        // -> sjekk at verdien ikke er null OG at den er false -> hvis det inntreffer, returner true
        // ELSE ingen husbanken-saker OG ingen husbanken-utbetalinger

        return if (fetchBostotteFailed || missingBostotteSamtykke) {
            okonomiService.getBekreftelser(soknadId)
                .let { bekreftelser ->
                    if (bekreftelser == null) {
                        return false
                    } else {
                        bekreftelser.find { bekreftelse -> bekreftelse.type == BekreftelseType.BOSTOTTE }
                    }
                }
                .let { bekreftelse -> if (bekreftelse == null) true else !bekreftelse.verdi }
        } else {
            !hasInntektHusbanken && !hasBostotteSaker
        }
    }

    companion object {
        private val boutgiftTyper: List<UtgiftType> =
            listOf(
                UtgiftType.UTGIFTER_HUSLEIE,
                UtgiftType.UTGIFTER_STROM,
                UtgiftType.UTGIFTER_KOMMUNAL_AVGIFT,
                UtgiftType.UTGIFTER_OPPVARMING,
                UtgiftType.UTGIFTER_BOLIGLAN_AVDRAG,
                UtgiftType.UTGIFTER_BOLIGLAN_RENTER,
                UtgiftType.UTGIFTER_ANNET_BO,
            )
    }
}
