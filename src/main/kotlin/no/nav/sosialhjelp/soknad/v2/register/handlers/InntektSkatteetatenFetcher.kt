package no.nav.sosialhjelp.soknad.v2.register.handlers

import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils.getUserIdFromToken
import no.nav.sosialhjelp.soknad.inntekt.skattbarinntekt.SkattbarInntektService
import no.nav.sosialhjelp.soknad.inntekt.skattbarinntekt.domain.Utbetaling
import no.nav.sosialhjelp.soknad.organisasjon.OrganisasjonService
import no.nav.sosialhjelp.soknad.v2.okonomi.BekreftelseType
import no.nav.sosialhjelp.soknad.v2.okonomi.OkonomiService
import no.nav.sosialhjelp.soknad.v2.okonomi.OkonomiskeDetaljer
import no.nav.sosialhjelp.soknad.v2.okonomi.Organisasjon
import no.nav.sosialhjelp.soknad.v2.okonomi.inntekt.Inntekt
import no.nav.sosialhjelp.soknad.v2.okonomi.inntekt.InntektType
import no.nav.sosialhjelp.soknad.v2.register.RegisterDataFetcher
import no.nav.sosialhjelp.soknad.v2.soknad.IntegrasjonStatusService
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class InntektSkatteetatenFetcher(
    private val okonomiService: OkonomiService,
    private val skattbarInntektService: SkattbarInntektService,
    private val integrasjonStatusService: IntegrasjonStatusService,
    private val organisasjonService: OrganisasjonService,
) : RegisterDataFetcher {
    override fun fetchAndSave(soknadId: UUID) {
        okonomiService.removeElementFromOkonomi(soknadId, InntektType.UTBETALING_SKATTEETATEN)

        okonomiService.getBekreftelser(soknadId)
            .find { it.type == BekreftelseType.UTBETALING_SKATTEETATEN_SAMTYKKE }
            ?.let { if (it.verdi) getSkattbarInntekt(soknadId) }
    }

    private fun getSkattbarInntekt(soknadId: UUID) {
        skattbarInntektService.hentUtbetalinger(getUserIdFromToken())?.let { utbetalinger ->
            setIntegrasjonStatus(soknadId, feilet = false)

            if (utbetalinger.isNotEmpty()) {
                saveUtbetalinger(soknadId, utbetalinger)
            }
        }
            ?: setIntegrasjonStatus(soknadId, feilet = true)
    }

    private fun saveUtbetalinger(
        soknadId: UUID,
        utbetalinger: List<Utbetaling>,
    ) {
        Inntekt(
            type = InntektType.UTBETALING_SKATTEETATEN,
            inntektDetaljer = OkonomiskeDetaljer(utbetalinger.map { it.toUtbetaling() }),
        )
            .also { okonomiService.addElementToOkonomi(soknadId, it) }
    }

    private fun setIntegrasjonStatus(
        soknadId: UUID,
        feilet: Boolean,
    ) {
        integrasjonStatusService.setInntektSkatteetatenStatus(soknadId, feilet)
    }

    private fun Utbetaling.toUtbetaling() =
        no.nav.sosialhjelp.soknad.v2.okonomi.Utbetaling(
            brutto = brutto,
            skattetrekk = skattetrekk,
            periodeFom = periodeFom,
            periodeTom = periodeTom,
            organisasjon =
                Organisasjon(
                    navn = organisasjonService.hentOrgNavn(orgnummer),
                    orgnummer = orgnummer,
                ),
        )
}
