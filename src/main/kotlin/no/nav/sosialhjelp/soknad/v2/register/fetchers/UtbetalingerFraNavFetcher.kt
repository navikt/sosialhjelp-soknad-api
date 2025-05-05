package no.nav.sosialhjelp.soknad.v2.register.fetchers

import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils.getUserIdFromToken
import no.nav.sosialhjelp.soknad.inntekt.navutbetalinger.UtbetalingerFraNavService
import no.nav.sosialhjelp.soknad.v2.okonomi.Inntekt
import no.nav.sosialhjelp.soknad.v2.okonomi.InntektType
import no.nav.sosialhjelp.soknad.v2.okonomi.OkonomiDetaljer
import no.nav.sosialhjelp.soknad.v2.okonomi.OkonomiService
import no.nav.sosialhjelp.soknad.v2.okonomi.UtbetalingMedKomponent
import no.nav.sosialhjelp.soknad.v2.register.RegisterDataFetcher
import no.nav.sosialhjelp.soknad.v2.soknad.IntegrasjonStatusService
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class UtbetalingerFraNavFetcher(
    private val navUtbetalingerService: UtbetalingerFraNavService,
    private val okonomiService: OkonomiService,
    private val integrasjonStatusService: IntegrasjonStatusService,
) : RegisterDataFetcher {
    private val logger by logger()

    override fun fetchAndSave(soknadId: UUID) {
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
        utbetalinger: List<UtbetalingMedKomponent>,
    ) {
        logger.info("Hentet ${utbetalinger.size} utbetalinger fra NAV")

        if (utbetalinger.isEmpty()) return

        Inntekt(
            type = InntektType.UTBETALING_NAVYTELSE,
            inntektDetaljer = OkonomiDetaljer(detaljer = utbetalinger),
        )
            .also { okonomiService.addElementToOkonomi(soknadId = soknadId, opplysning = it) }
    }
}
