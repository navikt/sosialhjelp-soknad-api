package no.nav.sosialhjelp.soknad.inntekt.verdi

import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.BEKREFTELSE_VERDI
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.VERDI_ANNET
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.VERDI_BOLIG
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.VERDI_CAMPINGVOGN
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.VERDI_FRITIDSEIENDOM
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.VERDI_KJORETOY
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKildeBruker
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomiopplysninger
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomioversikt
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomibeskrivelserAvAnnet
import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.sosialhjelp.soknad.app.Constants
import no.nav.sosialhjelp.soknad.app.mapper.OkonomiForventningService
import no.nav.sosialhjelp.soknad.app.mapper.OkonomiMapper.setBekreftelse
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.tekster.TextService
import no.nav.sosialhjelp.soknad.tilgangskontroll.Tilgangskontroll
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@ProtectedWithClaims(
    issuer = Constants.SELVBETJENING,
    claimMap = [Constants.CLAIM_ACR_LEVEL_4, Constants.CLAIM_ACR_LOA_HIGH],
    combineWithOr = true,
)
@RequestMapping("/soknader/{behandlingsId}/inntekt/verdier", produces = [MediaType.APPLICATION_JSON_VALUE])
class VerdiRessurs(
    private val tilgangskontroll: Tilgangskontroll,
    private val soknadUnderArbeidRepository: SoknadUnderArbeidRepository,
    private val okonomiForventningService: OkonomiForventningService,
    private val textService: TextService,
) {
    @GetMapping
    fun hentVerdier(
        @PathVariable("behandlingsId") behandlingsId: String,
    ): VerdierFrontend {
        tilgangskontroll.verifiserAtBrukerHarTilgang()
        val eier = SubjectHandlerUtils.getUserIdFromToken()
        val soknad =
            soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier).jsonInternalSoknad
                ?: throw IllegalStateException("Kan ikke hente søknaddata hvis SoknadUnderArbeid.jsonInternalSoknad er null")
        val okonomi = soknad.soknad.data.okonomi

        if (okonomi.opplysninger.bekreftelse == null) {
            return VerdierFrontend()
        }

        return VerdierFrontend(
            bekreftelse = getBekreftelse(okonomi.opplysninger),
            bolig = hasVerdiType(okonomi.oversikt, VERDI_BOLIG),
            campingvogn = hasVerdiType(okonomi.oversikt, VERDI_CAMPINGVOGN),
            kjoretoy = hasVerdiType(okonomi.oversikt, VERDI_KJORETOY),
            fritidseiendom = hasVerdiType(okonomi.oversikt, VERDI_FRITIDSEIENDOM),
            annet = hasVerdiType(okonomi.oversikt, VERDI_ANNET),
            beskrivelseAvAnnet = okonomi.opplysninger.beskrivelseAvAnnet?.verdi,
        )
    }

    @PutMapping
    fun updateVerdier(
        @PathVariable("behandlingsId") behandlingsId: String,
        @RequestBody verdierFrontend: VerdierFrontend,
    ) {
        tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId)
        val eier = SubjectHandlerUtils.getUserIdFromToken()
        val soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier)
        val jsonInternalSoknad =
            soknad.jsonInternalSoknad
                ?: throw IllegalStateException("Kan ikke oppdatere søknaddata hvis SoknadUnderArbeid.jsonInternalSoknad er null")
        val okonomi = jsonInternalSoknad.soknad.data.okonomi

        setBekreftelse(
            okonomi.opplysninger,
            BEKREFTELSE_VERDI,
            verdierFrontend.bekreftelse,
            textService.getJsonOkonomiTittel("inntekt.eierandeler"),
        )
        setVerdier(behandlingsId, okonomi.oversikt, verdierFrontend)
        setBeskrivelseAvAnnet(okonomi.opplysninger, verdierFrontend)
        soknadUnderArbeidRepository.oppdaterSoknadsdata(soknad, eier)
    }

    private fun setVerdier(
        behandlingsId: String,
        oversikt: JsonOkonomioversikt,
        verdierFrontend: VerdierFrontend,
    ) = mapOf(
        VERDI_BOLIG to verdierFrontend.bolig,
        VERDI_CAMPINGVOGN to verdierFrontend.campingvogn,
        VERDI_KJORETOY to verdierFrontend.kjoretoy,
        VERDI_FRITIDSEIENDOM to verdierFrontend.fritidseiendom,
        VERDI_ANNET to verdierFrontend.annet,
    ).forEach { (opplysningType, isChecked) -> okonomiForventningService.setOversiktFormue(behandlingsId, oversikt.formue, opplysningType, isChecked) }

    private fun setBeskrivelseAvAnnet(
        opplysninger: JsonOkonomiopplysninger,
        verdierFrontend: VerdierFrontend,
    ) {
        if (opplysninger.beskrivelseAvAnnet == null) {
            opplysninger.withBeskrivelseAvAnnet(
                JsonOkonomibeskrivelserAvAnnet()
                    .withKilde(JsonKildeBruker.BRUKER)
                    .withVerdi("")
                    .withSparing("")
                    .withUtbetaling("")
                    .withBoutgifter("")
                    .withBarneutgifter(""),
            )
        }
        opplysninger.beskrivelseAvAnnet.verdi = verdierFrontend.beskrivelseAvAnnet ?: ""
    }

    private fun getBekreftelse(opplysninger: JsonOkonomiopplysninger): Boolean? {
        return opplysninger.bekreftelse.firstOrNull { it.type == BEKREFTELSE_VERDI }?.verdi
    }

    private fun hasVerdiType(
        oversikt: JsonOkonomioversikt,
        type: String,
    ): Boolean {
        return oversikt.formue.any { it.type == type }
    }

    data class VerdierFrontend(
        var bekreftelse: Boolean? = null,
        var bolig: Boolean = false,
        var campingvogn: Boolean = false,
        var kjoretoy: Boolean = false,
        var fritidseiendom: Boolean = false,
        var annet: Boolean = false,
        var beskrivelseAvAnnet: String? = null,
    )
}
