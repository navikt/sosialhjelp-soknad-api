package no.nav.sosialhjelp.soknad.inntekt.skattbarinntekt

import jakarta.validation.Valid
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtbetaling
import no.nav.sosialhjelp.soknad.app.annotation.ProtectionSelvbetjeningHigh
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.inntekt.skattbarinntekt.dto.Organisasjon
import no.nav.sosialhjelp.soknad.inntekt.skattbarinntekt.dto.SkattbarInntektFrontend
import no.nav.sosialhjelp.soknad.inntekt.skattbarinntekt.dto.SkattbarInntektInputDTO
import no.nav.sosialhjelp.soknad.inntekt.skattbarinntekt.dto.SkattbarInntektOgForskuddstrekk
import no.nav.sosialhjelp.soknad.inntekt.skattbarinntekt.dto.Utbetaling
import no.nav.sosialhjelp.soknad.tekster.TextService
import no.nav.sosialhjelp.soknad.tilgangskontroll.Tilgangskontroll
import no.nav.sosialhjelp.soknad.v2.okonomi.MigrationToolkit.getBekreftelseVerdi
import no.nav.sosialhjelp.soknad.v2.okonomi.MigrationToolkit.getSamtykkeDato
import no.nav.sosialhjelp.soknad.v2.okonomi.MigrationToolkit.updateBekreftelse
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils.getUserIdFromToken as eier

@RestController
@ProtectionSelvbetjeningHigh
@RequestMapping("/soknader/{behandlingsId}/inntekt/skattbarinntektogforskuddstrekk", produces = [MediaType.APPLICATION_JSON_VALUE])
class SkattbarInntektRessurs(
    private val tilgangskontroll: Tilgangskontroll,
    private val soknadUnderArbeidRepository: SoknadUnderArbeidRepository,
    private val skatteetatenSystemdata: SkatteetatenSystemdata,
    private val textService: TextService,
) {
    @GetMapping
    fun hentSkattbareInntekter(
        @PathVariable("behandlingsId") behandlingsId: String,
    ): SkattbarInntektFrontend {
        tilgangskontroll.verifiserAtBrukerHarTilgang()
        return getSkattbarInntekt(behandlingsId)
    }

    @PostMapping("/samtykke")
    @Deprecated("POST skal ikke ha side effects; bruk PUT mot skattbarinntektogforskuddstrekk")
    fun updateSamtykke(
        @PathVariable("behandlingsId") behandlingsId: String,
        @RequestBody samtykke: Boolean,
    ) {
        tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId)
        setSamtykkeIfChanged(behandlingsId, samtykke)
    }

    @PutMapping
    fun putSkatteetatenSamtykke(
        @PathVariable("behandlingsId") behandlingsId: String,
        @RequestBody @Valid input: SkattbarInntektInputDTO,
    ): SkattbarInntektFrontend {
        tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId)
        setSamtykkeIfChanged(behandlingsId, input.samtykke)
        return getSkattbarInntekt(behandlingsId)
    }

    /** Henter SkattbarInntektFrontend for en gitt behandlingsId. */
    private fun getSkattbarInntekt(behandlingsId: String): SkattbarInntektFrontend {
        val soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier())
        val jsonInternalSoknad = soknad.jsonInternalSoknad ?: error("jsonInternalSoknad == null")

        val opplysninger = jsonInternalSoknad.soknad.data.okonomi.opplysninger
        val inntektFraSkatteetatenFeilet = jsonInternalSoknad.soknad.driftsinformasjon.inntektFraSkatteetatenFeilet

        // Hvorfor vi sjekker tittel != null vet jeg ikke
        val inntektFraSkatteetaten =
            opplysninger.utbetaling.filter { it.tittel != null && it.type == SoknadJsonTyper.UTBETALING_SKATTEETATEN }
                // Sort by periodeFom (String); newest first. groupBy retains ordering.
                .sortedByDescending { it.periodeFom }
                // Group transactions as Map<periodeFom, List<JsonOkonomiOpplysningUtbetaling>>
                .groupBy { it.periodeFom }
                // Collection<List<JsonOkonomiOpplysningUtbetaling>>
                .values
                // List<SkattbarInntektOgForskuddstrekk>
                .map { it.transformToFrontend() }

        return SkattbarInntektFrontend(
            inntektFraSkatteetaten = inntektFraSkatteetaten,
            inntektFraSkatteetatenFeilet = inntektFraSkatteetatenFeilet,
            samtykke = opplysninger.getBekreftelseVerdi(SoknadJsonTyper.UTBETALING_SKATTEETATEN_SAMTYKKE),
            samtykkeTidspunkt = opplysninger.getSamtykkeDato(SoknadJsonTyper.UTBETALING_SKATTEETATEN_SAMTYKKE),
        )
    }

    /** Mapper JsonOkonomiOpplysningUtbetaling til SkattbarInntektOgForskuddstrekk for frontend. */
    private fun List<JsonOkonomiOpplysningUtbetaling>.transformToFrontend() =
        SkattbarInntektOgForskuddstrekk(
            organisasjoner =
                this.groupBy { it.organisasjon }.map { (organisasjon, utbetalinger) ->
                    Organisasjon(
                        utbetalinger = utbetalinger.map { Utbetaling(it.brutto, it.skattetrekk, it.tittel) },
                        organisasjonsnavn = organisasjon?.navn ?: "Uten organisasjonsnummer",
                        orgnr = organisasjon?.organisasjonsnummer ?: "",
                        fom = utbetalinger.first().periodeFom,
                        tom = utbetalinger.first().periodeTom,
                    )
                },
        )

    private fun setSamtykkeIfChanged(
        behandlingsId: String,
        samtykke: Boolean,
    ) {
        val soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier())
        val jsonInternalSoknad = soknad.jsonInternalSoknad ?: error("jsonInternalSoknad == null")

        val opplysninger = jsonInternalSoknad.soknad.data.okonomi.opplysninger

        if (opplysninger.getBekreftelseVerdi(SoknadJsonTyper.UTBETALING_SKATTEETATEN_SAMTYKKE) != samtykke) {
            opplysninger.updateBekreftelse(SoknadJsonTyper.UTBETALING_SKATTEETATEN_SAMTYKKE, samtykke, textService.getJsonOkonomiTittel("utbetalinger.skattbar.samtykke"))
            skatteetatenSystemdata.updateSystemdataIn(soknad)
            soknadUnderArbeidRepository.oppdaterSoknadsdata(soknad, eier())
        }
    }
}
