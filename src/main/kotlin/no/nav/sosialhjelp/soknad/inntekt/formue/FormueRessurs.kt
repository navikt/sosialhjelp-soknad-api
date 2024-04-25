package no.nav.sosialhjelp.soknad.inntekt.formue

import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKildeBruker
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomiopplysninger
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomioversikt
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomibeskrivelserAvAnnet
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

        return opplysninger.bekreftelse?.let {
            FormueFrontend(
                brukskonto = oversikt.hasFormueType(SoknadJsonTyper.FORMUE_BRUKSKONTO),
                sparekonto = oversikt.hasFormueType(SoknadJsonTyper.FORMUE_SPAREKONTO),
                bsu = oversikt.hasFormueType(SoknadJsonTyper.FORMUE_BSU),
                livsforsikring = oversikt.hasFormueType(SoknadJsonTyper.FORMUE_LIVSFORSIKRING),
                verdipapirer = oversikt.hasFormueType(SoknadJsonTyper.FORMUE_VERDIPAPIRER),
                annet = oversikt.hasFormueType(SoknadJsonTyper.FORMUE_ANNET),
                beskrivelseAvAnnet = opplysninger.beskrivelseAvAnnet?.sparing,
            )
        } ?: FormueFrontend(beskrivelseAvAnnet = null)
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

        oversikt.setFormue(formueFrontend)
        setBekreftelse(opplysninger, SoknadJsonTyper.BEKREFTELSE_SPARING, hasAnyFormueType, textService.getJsonOkonomiTittel("inntekt.bankinnskudd"))
        opplysninger.setBeskrivelseAvAnnet(formueFrontend.beskrivelseAvAnnet ?: "")

        soknadUnderArbeidRepository.oppdaterSoknadsdata(soknad, eier())
    }

    private fun JsonOkonomioversikt.setFormue(
        formueFrontend: FormueFrontend,
    ) {
        mapOf(
            SoknadJsonTyper.FORMUE_BRUKSKONTO to formueFrontend.brukskonto,
            SoknadJsonTyper.FORMUE_BSU to formueFrontend.bsu,
            SoknadJsonTyper.FORMUE_SPAREKONTO to formueFrontend.sparekonto,
            SoknadJsonTyper.FORMUE_LIVSFORSIKRING to formueFrontend.livsforsikring,
            SoknadJsonTyper.FORMUE_VERDIPAPIRER to formueFrontend.verdipapirer,
            SoknadJsonTyper.FORMUE_ANNET to formueFrontend.annet,
        ).forEach { (type, isExpected) ->
            setFormueInOversikt(this.formue, type, isExpected, textService.getJsonOkonomiTittel(soknadTypeToTitleKey[type]))
        }
    }

    private fun JsonOkonomiopplysninger.setBeskrivelseAvAnnet(beskrivelseAvAnnet: String) {
        this.beskrivelseAvAnnet = this.beskrivelseAvAnnet?.apply { sparing = beskrivelseAvAnnet } ?: JsonOkonomibeskrivelserAvAnnet().withKilde(JsonKildeBruker.BRUKER).withVerdi("").withSparing(beskrivelseAvAnnet).withUtbetaling("").withBoutgifter("").withBarneutgifter("")
    }

    private fun JsonOkonomioversikt.hasFormueType(type: String): Boolean = this.formue.any { it.type == type }

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
