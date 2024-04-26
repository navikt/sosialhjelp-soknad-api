package no.nav.sosialhjelp.soknad.inntekt.verdi

import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomioversikt
import no.nav.sosialhjelp.soknad.app.annotation.ProtectionSelvbetjeningHigh
import no.nav.sosialhjelp.soknad.app.mapper.OkonomiMapper.setFormueInOversikt
import no.nav.sosialhjelp.soknad.app.mapper.TitleKeyMapper.soknadTypeToTitleKey
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.tekster.TextService
import no.nav.sosialhjelp.soknad.tilgangskontroll.Tilgangskontroll
import no.nav.sosialhjelp.soknad.v2.okonomi.MigrationToolkit.getBekreftelseVerdi
import no.nav.sosialhjelp.soknad.v2.okonomi.MigrationToolkit.hasFormue
import no.nav.sosialhjelp.soknad.v2.okonomi.MigrationToolkit.updateBekreftelse
import no.nav.sosialhjelp.soknad.v2.okonomi.MigrationToolkit.updateOrCreateBeskrivelseAvAnnet
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils.getUserIdFromToken as eier

@RestController
@ProtectionSelvbetjeningHigh
@RequestMapping("/soknader/{behandlingsId}/inntekt/verdier", produces = [MediaType.APPLICATION_JSON_VALUE])
class VerdiRessurs(
    private val tilgangskontroll: Tilgangskontroll,
    private val soknadUnderArbeidRepository: SoknadUnderArbeidRepository,
    private val textService: TextService,
) {
    @GetMapping
    fun hentVerdier(
        @PathVariable("behandlingsId") behandlingsId: String,
    ): VerdierFrontend {
        tilgangskontroll.verifiserAtBrukerHarTilgang()
        val soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier())
        val jsonInternalSoknad = soknad.jsonInternalSoknad ?: error("jsonInternalSoknad == null")

        val oversikt = jsonInternalSoknad.soknad.data.okonomi.oversikt
        val opplysninger = jsonInternalSoknad.soknad.data.okonomi.opplysninger

        return opplysninger.bekreftelse?.let {
            VerdierFrontend(
                bekreftelse = opplysninger.getBekreftelseVerdi(SoknadJsonTyper.BEKREFTELSE_VERDI),
                bolig = oversikt.hasFormue(SoknadJsonTyper.VERDI_BOLIG),
                campingvogn = oversikt.hasFormue(SoknadJsonTyper.VERDI_CAMPINGVOGN),
                kjoretoy = oversikt.hasFormue(SoknadJsonTyper.VERDI_KJORETOY),
                fritidseiendom = oversikt.hasFormue(SoknadJsonTyper.VERDI_FRITIDSEIENDOM),
                annet = oversikt.hasFormue(SoknadJsonTyper.VERDI_ANNET),
                beskrivelseAvAnnet = opplysninger.beskrivelseAvAnnet?.verdi,
            )
        } ?: VerdierFrontend()
    }

    @PutMapping
    fun updateVerdier(
        @PathVariable("behandlingsId") behandlingsId: String,
        @RequestBody verdierFrontend: VerdierFrontend,
    ) {
        tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId)
        val soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier())
        val jsonInternalSoknad = soknad.jsonInternalSoknad ?: error("jsonInternalSoknad == null")

        val opplysninger = jsonInternalSoknad.soknad.data.okonomi.opplysninger
        val oversikt = jsonInternalSoknad.soknad.data.okonomi.oversikt

        opplysninger.updateBekreftelse(SoknadJsonTyper.BEKREFTELSE_VERDI, verdierFrontend.bekreftelse, textService.getJsonOkonomiTittel("inntekt.eierandeler"))
        opplysninger.updateOrCreateBeskrivelseAvAnnet(verdi = verdierFrontend.beskrivelseAvAnnet)
        oversikt.updateFormue(verdierFrontend)

        soknadUnderArbeidRepository.oppdaterSoknadsdata(soknad, eier())
    }

    private fun JsonOkonomioversikt.updateFormue(
        verdierFrontend: VerdierFrontend,
    ) = mapOf(
        SoknadJsonTyper.VERDI_BOLIG to verdierFrontend.bolig,
        SoknadJsonTyper.VERDI_CAMPINGVOGN to verdierFrontend.campingvogn,
        SoknadJsonTyper.VERDI_KJORETOY to verdierFrontend.kjoretoy,
        SoknadJsonTyper.VERDI_FRITIDSEIENDOM to verdierFrontend.fritidseiendom,
        SoknadJsonTyper.VERDI_ANNET to verdierFrontend.annet,
    ).forEach { (soknadJsonType, isExpected) ->
        setFormueInOversikt(this.formue, soknadJsonType, isExpected, textService.getJsonOkonomiTittel(soknadTypeToTitleKey[soknadJsonType]))
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
