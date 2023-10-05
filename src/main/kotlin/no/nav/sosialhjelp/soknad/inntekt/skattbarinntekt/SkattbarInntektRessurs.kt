package no.nav.sosialhjelp.soknad.inntekt.skattbarinntekt

import jakarta.validation.Valid
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTBETALING_SKATTEETATEN
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTBETALING_SKATTEETATEN_SAMTYKKE
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtbetaling
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOrganisasjon
import no.nav.sosialhjelp.soknad.app.annotation.ProtectionSelvbetjeningHigh
import no.nav.sosialhjelp.soknad.app.mapper.OkonomiMapper.removeBekreftelserIfPresent
import no.nav.sosialhjelp.soknad.app.mapper.OkonomiMapper.setBekreftelse
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeid
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.inntekt.skattbarinntekt.dto.Organisasjon
import no.nav.sosialhjelp.soknad.inntekt.skattbarinntekt.dto.SkattbarInntektFrontend
import no.nav.sosialhjelp.soknad.inntekt.skattbarinntekt.dto.SkattbarInntektInputDTO
import no.nav.sosialhjelp.soknad.inntekt.skattbarinntekt.dto.SkattbarInntektOgForskuddstrekk
import no.nav.sosialhjelp.soknad.inntekt.skattbarinntekt.dto.Utbetaling
import no.nav.sosialhjelp.soknad.tekster.TextService
import no.nav.sosialhjelp.soknad.tilgangskontroll.Tilgangskontroll
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils.getUserIdFromToken as getBrukerPid

@RestController
@ProtectionSelvbetjeningHigh
@RequestMapping("/soknader/{behandlingsId}/inntekt/skattbarinntektogforskuddstrekk", produces = [MediaType.APPLICATION_JSON_VALUE])
class SkattbarInntektRessurs(
    private val tilgangskontroll: Tilgangskontroll,
    private val soknadUnderArbeidRepository: SoknadUnderArbeidRepository,
    private val skatteetatenSystemdata: SkatteetatenSystemdata,
    private val textService: TextService
) {
    @GetMapping
    fun hentSkattbareInntekter(@PathVariable("behandlingsId") behandlingsId: String): SkattbarInntektFrontend {
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
        val soknad = internalFraSoknad(fetchSoknad(behandlingsId))

        val inntektFraSkatteetaten = skatteetatUtbetalingerFraSoknad(soknad)
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
            inntektFraSkatteetatenFeilet = soknad.soknad.driftsinformasjon.inntektFraSkatteetatenFeilet,
            samtykke = samtykkeFraSoknad(soknad),
            samtykkeTidspunkt = samtykkeDatoFraSoknad(soknad)
        )
    }

    /** Mapper JsonOkonomiOpplysningUtbetaling til SkattbarInntektOgForskuddstrekk for frontend. */
    private fun List<JsonOkonomiOpplysningUtbetaling>.transformToFrontend() =
        SkattbarInntektOgForskuddstrekk(
            organisasjoner = this.groupBy { it.organisasjon }.map { (organisasjon, utbetalinger) ->
                mapTilOrganisasjon(
                    utbetalinger = mapTilUtbetalinger(utbetalinger),
                    organisasjon = organisasjon,
                    periode = utbetalinger.first().periode()
                )
            }
        )

    private fun JsonOkonomiOpplysningUtbetaling.periode(): Pair<String, String> = Pair(periodeFom, periodeTom)

    private fun setSamtykkeIfChanged(behandlingsId: String, samtykke: Boolean) {
        val soknad = fetchSoknad(behandlingsId)
        val internal = internalFraSoknad(soknad)

        if (samtykkeFraSoknad(internal) != samtykke) {
            removeBekreftelserIfPresent(
                internal.soknad.data.okonomi.opplysninger, UTBETALING_SKATTEETATEN_SAMTYKKE
            )
            setBekreftelse(
                internal.soknad.data.okonomi.opplysninger,
                UTBETALING_SKATTEETATEN_SAMTYKKE,
                samtykke,
                textService.getJsonOkonomiTittel("utbetalinger.skattbar.samtykke")
            )
            skatteetatenSystemdata.updateSystemdataIn(soknad)
            soknadUnderArbeidRepository.oppdaterSoknadsdata(soknad, getBrukerPid())
        }
    }

    /** Snarvei for SoknadUnderArbeid fra behandlingsId. Sjekker aktiv brukers eierskap. */
    private fun fetchSoknad(behandlingsId: String): SoknadUnderArbeid = soknadUnderArbeidRepository.hentSoknad(behandlingsId, getBrukerPid())

    /** Snarvei for å hente it jsonInternalSoknad fra SoknadUnderArbeid og bekrefte at den ikke er null */
    private fun internalFraSoknad(soknad: SoknadUnderArbeid): JsonInternalSoknad =
        soknad.jsonInternalSoknad ?: throw IllegalStateException("Kan ikke oppdatere søknaddata hvis SoknadUnderArbeid.jsonInternalSoknad er null")

    /**
     * Henter utbetalinger fra Skatteetaten fra søknaden.
     * Vet ikke hvorfor vi sjekker "tittel" og "type" her, men er vel best å ikke røre ¯\_(ツ)_/¯
     */
    private fun skatteetatUtbetalingerFraSoknad(soknad: JsonInternalSoknad): List<JsonOkonomiOpplysningUtbetaling> =
        soknad.soknad.data.okonomi.opplysninger.utbetaling.filter { it.tittel != null && it.type != null && it.type == UTBETALING_SKATTEETATEN }

    /**
     * Henter verdien av samtykke for Skatteetaten fra søknaden.
     * @return true hvis samtykke er gitt, false hvis samtykke er trukket tilbake, null hvis intet samtykke
     */
    private fun samtykkeFraSoknad(soknad: JsonInternalSoknad): Boolean? =
        soknad.soknad.data.okonomi.opplysninger.bekreftelse.firstOrNull { it.type == UTBETALING_SKATTEETATEN_SAMTYKKE }?.verdi

    /**
     * Henter dato for sist samtykke ble gitt for å hente fra Skatteetaten
     */
    private fun samtykkeDatoFraSoknad(soknad: JsonInternalSoknad): String? =
        soknad.soknad.data.okonomi.opplysninger.bekreftelse.filter { it.type == UTBETALING_SKATTEETATEN_SAMTYKKE && it.verdi }.map { it.bekreftelsesDato }
            .sortedByDescending { it }.firstOrNull()

    private fun mapTilUtbetalinger(
        utbetalinger: List<JsonOkonomiOpplysningUtbetaling>
    ) = utbetalinger.map { Utbetaling(it.brutto, it.skattetrekk, it.tittel) }

    private fun mapTilOrganisasjon(
        utbetalinger: List<Utbetaling>,
        organisasjon: JsonOrganisasjon?,
        periode: Pair<String, String>
    ) = Organisasjon(
        utbetalinger, organisasjon?.navn ?: "Uten organisasjonsnummer", organisasjon?.organisasjonsnummer ?: "", periode.first, periode.second
    )
}
