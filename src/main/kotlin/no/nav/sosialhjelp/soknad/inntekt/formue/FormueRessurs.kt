package no.nav.sosialhjelp.soknad.inntekt.formue

import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.BEKREFTELSE_SPARING
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.FORMUE_ANNET
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.FORMUE_BRUKSKONTO
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.FORMUE_BSU
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.FORMUE_LIVSFORSIKRING
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.FORMUE_SPAREKONTO
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.FORMUE_VERDIPAPIRER
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKildeBruker
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomiopplysninger
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomioversikt
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomibeskrivelserAvAnnet
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.oversikt.JsonOkonomioversiktFormue
import no.nav.sosialhjelp.soknad.app.annotation.ProtectionSelvbetjeningHigh
import no.nav.sosialhjelp.soknad.app.mapper.OkonomiMapper.setBekreftelse
import no.nav.sosialhjelp.soknad.app.mapper.OkonomiMapper.setFormueInOversikt
import no.nav.sosialhjelp.soknad.app.mapper.TitleKeyMapper.soknadTypeToTitleKey
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
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils.getUserIdFromToken as eier

@RestController
@ProtectionSelvbetjeningHigh
@RequestMapping("/soknader/{behandlingsId}/inntekt/formue", produces = [MediaType.APPLICATION_JSON_VALUE])
class FormueRessurs(
    private val tilgangskontroll: Tilgangskontroll,
    private val soknadUnderArbeidRepository: SoknadUnderArbeidRepository,
    private val textService: TextService,
) {
    @GetMapping
    fun hentFormue(
        @PathVariable("behandlingsId") behandlingsId: String,
    ): FormueFrontend {
        tilgangskontroll.verifiserAtBrukerHarTilgang()
        val soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier())
        val jsonInternalSoknad = soknad.jsonInternalSoknad ?: error("jsonInternalSoknad == null")
        val opplysninger = jsonInternalSoknad.soknad.data.okonomi.opplysninger
        val oversikt = jsonInternalSoknad.soknad.data.okonomi.oversikt

        if (opplysninger.bekreftelse == null) {
            return FormueFrontend(beskrivelseAvAnnet = null)
        }

        return FormueFrontend(
            brukskonto = hasFormueType(oversikt, FORMUE_BRUKSKONTO),
            sparekonto = hasFormueType(oversikt, FORMUE_SPAREKONTO),
            bsu = hasFormueType(oversikt, FORMUE_BSU),
            livsforsikring = hasFormueType(oversikt, FORMUE_LIVSFORSIKRING),
            verdipapirer = hasFormueType(oversikt, FORMUE_VERDIPAPIRER),
            annet = hasFormueType(oversikt, FORMUE_ANNET),
            beskrivelseAvAnnet = opplysninger.beskrivelseAvAnnet?.sparing,
        )
    }

    @PutMapping
    fun updateFormue(
        @PathVariable("behandlingsId") behandlingsId: String,
        @RequestBody formueFrontend: FormueFrontend,
    ) {
        tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId)
        val soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier())
        val jsonInternalSoknad = soknad.jsonInternalSoknad ?: error("jsonInternalSoknad == null")
        val opplysninger = jsonInternalSoknad.soknad.data.okonomi.opplysninger
        val oversikt = jsonInternalSoknad.soknad.data.okonomi.oversikt

        val hasAnyFormueType = with(formueFrontend) { listOf(brukskonto, bsu, sparekonto, livsforsikring, verdipapirer, annet).any { it } }
        setBekreftelse(opplysninger, BEKREFTELSE_SPARING, hasAnyFormueType, textService.getJsonOkonomiTittel("inntekt.bankinnskudd"))
        setFormue(oversikt.formue, formueFrontend)
        setBeskrivelseAvAnnet(opplysninger, formueFrontend)
        soknadUnderArbeidRepository.oppdaterSoknadsdata(soknad, eier())
    }

    private fun setFormue(
        formue: MutableList<JsonOkonomioversiktFormue>,
        formueFrontend: FormueFrontend,
    ) {
        mapOf(
            FORMUE_BRUKSKONTO to formueFrontend.brukskonto,
            FORMUE_BSU to formueFrontend.bsu,
            FORMUE_SPAREKONTO to formueFrontend.sparekonto,
            FORMUE_LIVSFORSIKRING to formueFrontend.livsforsikring,
            FORMUE_VERDIPAPIRER to formueFrontend.verdipapirer,
            FORMUE_ANNET to formueFrontend.annet,
        ).forEach { (type, isExpected) ->
            setFormueInOversikt(formue, type, isExpected, textService.getJsonOkonomiTittel(soknadTypeToTitleKey[type]))
        }
    }

    private fun setBeskrivelseAvAnnet(
        opplysninger: JsonOkonomiopplysninger,
        formueFrontend: FormueFrontend,
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
        opplysninger.beskrivelseAvAnnet.sparing = formueFrontend.beskrivelseAvAnnet ?: ""
    }

    private fun hasFormueType(
        oversikt: JsonOkonomioversikt,
        type: String,
    ): Boolean {
        return oversikt.formue.any { it.type == type }
    }

    data class FormueFrontend(
        val brukskonto: Boolean = false,
        val sparekonto: Boolean = false,
        val bsu: Boolean = false,
        val livsforsikring: Boolean = false,
        val verdipapirer: Boolean = false,
        val annet: Boolean = false,
        val beskrivelseAvAnnet: String?,
    )
}
