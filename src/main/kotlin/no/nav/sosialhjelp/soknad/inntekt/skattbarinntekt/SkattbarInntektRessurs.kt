package no.nav.sosialhjelp.soknad.inntekt.skattbarinntekt

import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTBETALING_SKATTEETATEN
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTBETALING_SKATTEETATEN_SAMTYKKE
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtbetaling
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOrganisasjon
import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.sosialhjelp.soknad.common.Constants
import no.nav.sosialhjelp.soknad.common.mapper.OkonomiMapper.removeBekreftelserIfPresent
import no.nav.sosialhjelp.soknad.common.mapper.OkonomiMapper.setBekreftelse
import no.nav.sosialhjelp.soknad.common.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.tekster.TextService
import no.nav.sosialhjelp.soknad.tilgangskontroll.Tilgangskontroll
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Controller
import javax.ws.rs.GET
import javax.ws.rs.HeaderParam
import javax.ws.rs.POST
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType

@Controller
@ProtectedWithClaims(issuer = Constants.SELVBETJENING, claimMap = [Constants.CLAIM_ACR_LEVEL_4])
@Path("/soknader/{behandlingsId}/inntekt/skattbarinntektogforskuddstrekk")
@Produces(MediaType.APPLICATION_JSON)
open class SkattbarInntektRessurs(
    private val tilgangskontroll: Tilgangskontroll,
    private val soknadUnderArbeidRepository: SoknadUnderArbeidRepository,
    private val skatteetatenSystemdata: SkatteetatenSystemdata,
    private val textService: TextService
) {
    @GET
    open fun hentSkattbareInntekter(@PathParam("behandlingsId") behandlingsId: String): SkattbarInntektFrontend {
        tilgangskontroll.verifiserAtBrukerHarTilgang()
        val eier = SubjectHandlerUtils.getUserIdFromToken()
        val utbetalinger: List<JsonOkonomiOpplysningUtbetaling>
        val soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier).jsonInternalSoknad
            ?: throw IllegalStateException("Kan ikke hente søknaddata hvis SoknadUnderArbeid.jsonInternalSoknad er null")
        utbetalinger = soknad.soknad.data.okonomi.opplysninger.utbetaling
        val skatteopplysninger = utbetalinger
            .filter { it.tittel != null }
            .filter { it.type != null && it.type == UTBETALING_SKATTEETATEN }
        return SkattbarInntektFrontend(
            organiserSkattOgForskuddstrekkEtterMaanedOgOrganisasjon(skatteopplysninger),
            soknad.soknad.driftsinformasjon.inntektFraSkatteetatenFeilet,
            hentSamtykkeBooleanFraSoknad(soknad),
            hentSamtykkeDatoFraSoknad(soknad)
        )
    }

    @POST
    @Path(value = "/samtykke")
    open fun updateSamtykke(
        @PathParam("behandlingsId") behandlingsId: String,
        samtykke: Boolean,
        @HeaderParam(value = HttpHeaders.AUTHORIZATION) token: String?
    ) {
        tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId)
        val eier = SubjectHandlerUtils.getUserIdFromToken()
        val soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier)
        val jsonInternalSoknad = soknad.jsonInternalSoknad
            ?: throw IllegalStateException("Kan ikke oppdatere søknaddata hvis SoknadUnderArbeid.jsonInternalSoknad er null")
        val opplysninger = jsonInternalSoknad.soknad.data.okonomi.opplysninger
        val lagretSamtykke = hentSamtykkeBooleanFraSoknad(jsonInternalSoknad)
        var skalLagre = samtykke
        if (lagretSamtykke != samtykke) {
            skalLagre = true
            removeBekreftelserIfPresent(opplysninger, UTBETALING_SKATTEETATEN_SAMTYKKE)
            setBekreftelse(
                opplysninger,
                UTBETALING_SKATTEETATEN_SAMTYKKE,
                samtykke,
                textService.getJsonOkonomiTittel("utbetalinger.skattbar.samtykke")
            )
        }
        if (skalLagre) {
            skatteetatenSystemdata.updateSystemdataIn(soknad)
            soknadUnderArbeidRepository.oppdaterSoknadsdata(soknad, eier)
        }
    }

    private fun hentSamtykkeBooleanFraSoknad(soknad: JsonInternalSoknad): Boolean {
        return soknad.soknad.data.okonomi.opplysninger.bekreftelse
            .filter { it.type == UTBETALING_SKATTEETATEN_SAMTYKKE }
            .any { it.verdi }
    }

    private fun hentSamtykkeDatoFraSoknad(soknad: JsonInternalSoknad): String? {
        return soknad.soknad.data.okonomi.opplysninger.bekreftelse
            .filter { it.type == UTBETALING_SKATTEETATEN_SAMTYKKE }
            .filter { it.verdi }
            .firstOrNull()
            ?.bekreftelsesDato
    }

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

    private fun mapTilUtbetaling(jsonOkonomiOpplysningUtbetaling: JsonOkonomiOpplysningUtbetaling): Utbetaling {
        return Utbetaling(
            jsonOkonomiOpplysningUtbetaling.brutto,
            jsonOkonomiOpplysningUtbetaling.skattetrekk,
            jsonOkonomiOpplysningUtbetaling.tittel
        )
    }

    private fun mapTilOrganisasjon(
        utbetalingListe: List<Utbetaling>,
        jsonOrganisasjon: JsonOrganisasjon,
        utbetaling: JsonOkonomiOpplysningUtbetaling
    ): Organisasjon {
        return Organisasjon(
            utbetalingListe,
            jsonOrganisasjon.navn,
            jsonOrganisasjon.organisasjonsnummer,
            utbetaling.periodeFom,
            utbetaling.periodeTom
        )
    }

    data class SkattbarInntektOgForskuddstrekk(
        val organisasjoner: List<Organisasjon>?
    )

    data class Organisasjon(
        val utbetalinger: List<Utbetaling>?,
        val organisasjonsnavn: String?,
        val orgnr: String?,
        val fom: String?,
        val tom: String?
    )

    data class Utbetaling(
        val brutto: Double?,
        val forskuddstrekk: Double?,
        val tittel: String?
    )

    data class SkattbarInntektFrontend(
        val inntektFraSkatteetaten: List<SkattbarInntektOgForskuddstrekk>?,
        val inntektFraSkatteetatenFeilet: Boolean?,
        val samtykke: Boolean?,
        val samtykkeTidspunkt: String?
    )
}
