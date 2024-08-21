package no.nav.sosialhjelp.soknad.v2.register.handlers

import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils.getUserIdFromToken
import no.nav.sosialhjelp.soknad.inntekt.navutbetalinger.NavUtbetalingerService
import no.nav.sosialhjelp.soknad.inntekt.navutbetalinger.domain.NavKomponent
import no.nav.sosialhjelp.soknad.inntekt.navutbetalinger.domain.NavUtbetaling
import no.nav.sosialhjelp.soknad.v2.okonomi.BekreftelseType
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
        okonomiService.getBekreftelser(soknadId)
            .find { it.type == BekreftelseType.BOSTOTTE_SAMTYKKE }
            ?.let { if (it.verdi) getAndSaveUtbetalingerFraNav(soknadId) else null }
            ?: okonomiService.removeElementFromOkonomi(soknadId, InntektType.UTBETALING_NAVYTELSE)
    }

    private fun getAndSaveUtbetalingerFraNav(soknadId: UUID) {
        navUtbetalingerService.getUtbetalingerSiste40Dager(getUserIdFromToken())
            ?.let { utbetalinger ->
                logger.info("NyModell: Hentet ${utbetalinger.size} utbetalinger fra NAV")
                integrasjonStatusService.setUtbetalingerFraNavStatus(soknadId, feilet = false)

                if (utbetalinger.isEmpty()) return

                val inntekt =
                    Inntekt(
                        type = InntektType.UTBETALING_NAVYTELSE,
                        inntektDetaljer = OkonomiDetaljer(utbetalinger.map { it.toUtbetalingMedKomponent() }),
                    )
                okonomiService.addElementToOkonomi(soknadId, inntekt)
            }
            ?: integrasjonStatusService.setUtbetalingerFraNavStatus(soknadId, feilet = true)
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
