package no.nav.sosialhjelp.soknad.v2.register.fetchers

import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils.getUserIdFromToken
import no.nav.sosialhjelp.soknad.inntekt.navutbetalinger.NavUtbetalingerService
import no.nav.sosialhjelp.soknad.inntekt.navutbetalinger.domain.NavKomponent
import no.nav.sosialhjelp.soknad.inntekt.navutbetalinger.domain.NavUtbetaling
import no.nav.sosialhjelp.soknad.v2.okonomi.Komponent
import no.nav.sosialhjelp.soknad.v2.okonomi.OkonomiDetaljer
import no.nav.sosialhjelp.soknad.v2.okonomi.OkonomiService
import no.nav.sosialhjelp.soknad.v2.okonomi.Utbetaling
import no.nav.sosialhjelp.soknad.v2.okonomi.UtbetalingMedKomponent
import no.nav.sosialhjelp.soknad.v2.okonomi.inntekt.Inntekt
import no.nav.sosialhjelp.soknad.v2.okonomi.inntekt.InntektType
import no.nav.sosialhjelp.soknad.v2.register.RegisterDataFetcher
import no.nav.sosialhjelp.soknad.v2.soknad.IntegrasjonStatusService
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class UtbetalingerFraNavFetcher(
    private val navUtbetalingerService: NavUtbetalingerService,
    private val okonomiService: OkonomiService,
    private val integrasjonStatusService: IntegrasjonStatusService,
) : RegisterDataFetcher {
    private val logger by logger()

    override fun fetchAndSave(soknadId: UUID) {
        // TODO 1. Hvis vi allerede har utbetalinger - men kallet feiler - skal det gamle beholdes, eller fjernes?
        // TODO 2. Hvis vi har utbetalinger - men ny innhenting returnerer tom liste - skal det da være tomt?
        okonomiService.removeElementFromOkonomi(soknadId, type = InntektType.UTBETALING_NAVYTELSE)

        navUtbetalingerService.getUtbetalingerSiste40Dager(getUserIdFromToken())
            ?.also {
                saveUtbetalingerFraNav(soknadId, it)
                integrasjonStatusService.setUtbetalingerFraNavStatus(soknadId, feilet = false)
            }
            ?: integrasjonStatusService.setUtbetalingerFraNavStatus(soknadId, feilet = true)
    }

    private fun saveUtbetalingerFraNav(
        soknadId: UUID,
        utbetalinger: List<NavUtbetaling>,
    ) {
        logger.info("NyModell: Hentet ${utbetalinger.size} utbetalinger fra NAV")

        if (utbetalinger.isEmpty()) return

        Inntekt(
            type = InntektType.UTBETALING_NAVYTELSE,
            inntektDetaljer = OkonomiDetaljer(utbetalinger.map { it.toUtbetalingMedKomponent() }),
        )
            .also { okonomiService.addElementToOkonomi(soknadId = soknadId, element = it) }
    }
}

private fun NavUtbetaling.toUtbetalingMedKomponent() =
    UtbetalingMedKomponent(
        utbetaling =
            Utbetaling(
                brutto = brutto,
                netto = netto,
                skattetrekk = skattetrekk,
                andreTrekk = andreTrekk,
                utbetalingsdato = utbetalingsdato,
                periodeFom = periodeFom,
                periodeTom = periodeTom,
            ),
        komponenter = komponenter.map { it.toKomponent() },
    )

private fun NavKomponent.toKomponent() =
    Komponent(
        type = type,
        belop = belop,
        satsType = satsType,
        satsAntall = satsAntall,
        satsBelop = satsBelop,
    )
