package no.nav.sosialhjelp.soknad.inntekt.skattbarinntekt

import no.nav.sosialhjelp.soknad.inntekt.skattbarinntekt.dto.Organisasjon
import no.nav.sosialhjelp.soknad.inntekt.skattbarinntekt.dto.SkattbarInntektFrontend
import no.nav.sosialhjelp.soknad.inntekt.skattbarinntekt.dto.SkattbarInntektOgForskuddstrekk
import no.nav.sosialhjelp.soknad.inntekt.skattbarinntekt.dto.Utbetaling
import no.nav.sosialhjelp.soknad.v2.okonomi.inntekt.InntektFraOrganisasjonDto
import no.nav.sosialhjelp.soknad.v2.okonomi.inntekt.InntektSkattetatenController
import no.nav.sosialhjelp.soknad.v2.okonomi.inntekt.OrganisasjonDto
import no.nav.sosialhjelp.soknad.v2.okonomi.inntekt.SkattbarInntektDto
import no.nav.sosialhjelp.soknad.v2.okonomi.inntekt.UtbetalingFraSkatteetatenDto
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class SkattbarInntektProxy(private val inntektSkattetatenController: InntektSkattetatenController) {
    fun getSkattbarInntekt(soknadId: String): SkattbarInntektFrontend {
        return inntektSkattetatenController
            .getSkattbarInntekt(UUID.fromString(soknadId))
            .toSkattbarInntektFrontend()
    }

    fun updateSamtykkeSkatteetaten(
        behandlingsId: String,
        samtykke: Boolean,
    ): SkattbarInntektFrontend {
        inntektSkattetatenController.updateSamtykke(
            soknadId = UUID.fromString(behandlingsId),
            samtykke = samtykke,
        )

        return getSkattbarInntekt(behandlingsId)
    }
}

private fun SkattbarInntektDto.toSkattbarInntektFrontend() =
    SkattbarInntektFrontend(
        samtykke = samtykke?.verdi,
        samtykkeTidspunkt = samtykke?.samtykkeTidspunkt,
        inntektFraSkatteetatenFeilet = inntektFraSkatteetatenFeilet,
        inntektFraSkatteetaten = this.inntektSkatteetaten.map { it.toSkattbarInntektOgForskuddstrekk() },
    )

private fun InntektFraOrganisasjonDto.toSkattbarInntektOgForskuddstrekk(): SkattbarInntektOgForskuddstrekk {
    return SkattbarInntektOgForskuddstrekk(organisasjoner.map { it.toOrganisasjon() })
}

private fun OrganisasjonDto.toOrganisasjon(): Organisasjon {
    return Organisasjon(
        organisasjonsnavn = organisasjonsnavn,
        orgnr = orgnr,
        fom = fom ?: "",
        tom = tom ?: "",
        utbetalinger = utbetalinger.map { it.toUtbetaling() },
    )
}

private fun UtbetalingFraSkatteetatenDto.toUtbetaling(): Utbetaling {
    return Utbetaling(
        brutto = brutto,
        forskuddstrekk = forskuddstrekk,
        tittel = tittel,
    )
}
