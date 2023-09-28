package no.nav.sosialhjelp.soknad.inntekt.skattbarinntekt

import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTBETALING_SKATTEETATEN
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTBETALING_SKATTEETATEN_SAMTYKKE
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtbetaling
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOrganisasjon
import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.sosialhjelp.soknad.app.Constants
import no.nav.sosialhjelp.soknad.app.mapper.OkonomiMapper.removeBekreftelserIfPresent
import no.nav.sosialhjelp.soknad.app.mapper.OkonomiMapper.setBekreftelse
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeid
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.inntekt.skattbarinntekt.dto.Organisasjon
import no.nav.sosialhjelp.soknad.inntekt.skattbarinntekt.dto.SkattbarInntektFrontend
import no.nav.sosialhjelp.soknad.inntekt.skattbarinntekt.dto.SkattbarInntektOgForskuddstrekk
import no.nav.sosialhjelp.soknad.inntekt.skattbarinntekt.dto.Utbetaling
import no.nav.sosialhjelp.soknad.tekster.TextService
import no.nav.sosialhjelp.soknad.tilgangskontroll.Tilgangskontroll
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils.getUserIdFromToken as getBrukerPid

@RestController
@ProtectedWithClaims(issuer = Constants.SELVBETJENING, claimMap = [Constants.CLAIM_ACR_LEVEL_4, Constants.CLAIM_ACR_LOA_HIGH], combineWithOr = true)
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

        val soknad = internalFraSoknad(fetchSoknad(behandlingsId))

        val skatteopplysninger = soknad.soknad.data.okonomi.opplysninger.utbetaling
            .filter { it.tittel != null && it.type != null && it.type == UTBETALING_SKATTEETATEN }

        return SkattbarInntektFrontend(
            inntektFraSkatteetaten = organiserSkattOgForskuddstrekkEtterMaanedOgOrganisasjon(skatteopplysninger),
            inntektFraSkatteetatenFeilet = soknad.soknad.driftsinformasjon.inntektFraSkatteetatenFeilet,
            samtykke = samtykkeFraSoknad(soknad),
            samtykkeTidspunkt = samtykkeDatoFraSoknad(soknad)
        )
    }

    @PostMapping("/samtykke")
    fun updateSamtykke(
        @PathVariable("behandlingsId") behandlingsId: String,
        @RequestBody samtykke: Boolean,
        @RequestHeader(value = HttpHeaders.AUTHORIZATION) token: String?
    ) {
        tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId)
        val soknad = fetchSoknad(behandlingsId)
        val jsonInternalSoknad = internalFraSoknad(soknad)

        if (samtykkeFraSoknad(jsonInternalSoknad) != samtykke) {
            overwriteSamtykke(jsonInternalSoknad, samtykke)
            skatteetatenSystemdata.updateSystemdataIn(soknad)
            soknadUnderArbeidRepository.oppdaterSoknadsdata(soknad, getBrukerPid())
        }
    }

    private fun overwriteSamtykke(
        jsonInternalSoknad: JsonInternalSoknad,
        samtykke: Boolean
    ) {
        removeBekreftelserIfPresent(jsonInternalSoknad.soknad.data.okonomi.opplysninger, UTBETALING_SKATTEETATEN_SAMTYKKE)
        setBekreftelse(
            jsonInternalSoknad.soknad.data.okonomi.opplysninger,
            UTBETALING_SKATTEETATEN_SAMTYKKE,
            samtykke,
            textService.getJsonOkonomiTittel("utbetalinger.skattbar.samtykke")
        )
    }

    private fun fetchSoknad(behandlingsId: String): SoknadUnderArbeid = soknadUnderArbeidRepository.hentSoknad(behandlingsId, getBrukerPid())

    private fun internalFraSoknad(soknad: SoknadUnderArbeid): JsonInternalSoknad = soknad.jsonInternalSoknad
        ?: throw IllegalStateException("Kan ikke oppdatere søknaddata hvis SoknadUnderArbeid.jsonInternalSoknad er null")

    private fun samtykkeFraSoknad(soknad: JsonInternalSoknad): Boolean? =
        soknad.soknad.data.okonomi.opplysninger.bekreftelse
            .firstOrNull { it.type == UTBETALING_SKATTEETATEN_SAMTYKKE }
            ?.verdi

    private fun samtykkeDatoFraSoknad(soknad: JsonInternalSoknad): String? =
        soknad.soknad.data.okonomi.opplysninger.bekreftelse
            .firstOrNull { it.type == UTBETALING_SKATTEETATEN_SAMTYKKE && it.verdi }
            ?.bekreftelsesDato

    private fun organiserSkattOgForskuddstrekkEtterMaanedOgOrganisasjon(
        skatteopplysninger: List<JsonOkonomiOpplysningUtbetaling>
    ): List<SkattbarInntektOgForskuddstrekk> {
        // Skatteetaten returnerer opplysninger månedsvis, så objekter med samme PeriodeFom gjelder for samme periode
        val utbetalingerPerManedPerOrganisasjon: Map<String?, Map<JsonOrganisasjon?, List<JsonOkonomiOpplysningUtbetaling>>> =
            skatteopplysninger
                .groupBy { it.periodeFom }
                .mapValues { it.value.groupBy { utbetaling -> utbetaling.organisasjon } }

        val skattbarInntektOgForskuddstrekkListe = utbetalingerPerManedPerOrganisasjon.values
            .map {
                val organisasjoner = it
                    .map { (organisasjon: JsonOrganisasjon?, utbetalinger: List<JsonOkonomiOpplysningUtbetaling>) ->
                        val utbetalingListe: List<Utbetaling> = utbetalinger.map { u -> mapTilUtbetaling(u) }
                        val jsonOrganisasjon = organisasjon ?: JsonOrganisasjon().withNavn("Uten organisasjonsnummer")
                        mapTilOrganisasjon(utbetalingListe, jsonOrganisasjon, utbetalinger[0])
                    }
                SkattbarInntektOgForskuddstrekk(organisasjoner)
            }
            .reversed()

        return skattbarInntektOgForskuddstrekkListe
    }

    private fun mapTilUtbetaling(utbetaling: JsonOkonomiOpplysningUtbetaling): Utbetaling =
        Utbetaling(
            utbetaling.brutto,
            utbetaling.skattetrekk,
            utbetaling.tittel
        )

    private fun mapTilOrganisasjon(
        utbetalinger: List<Utbetaling>,
        organisasjon: JsonOrganisasjon,
        utbetaling: JsonOkonomiOpplysningUtbetaling
    ) = Organisasjon(
        utbetalinger,
        organisasjon.navn,
        organisasjon.organisasjonsnummer,
        utbetaling.periodeFom,
        utbetaling.periodeTom
    )
}
