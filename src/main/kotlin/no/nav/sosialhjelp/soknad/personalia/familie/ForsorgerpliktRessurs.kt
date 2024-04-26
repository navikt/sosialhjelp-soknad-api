package no.nav.sosialhjelp.soknad.personalia.familie

import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKildeBruker
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonBarnebidrag
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonBorSammenMed
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonForsorgerplikt
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonHarDeltBosted
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonHarForsorgerplikt
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonSamvarsgrad
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomiopplysninger
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomioversikt
import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.app.annotation.ProtectionSelvbetjeningHigh
import no.nav.sosialhjelp.soknad.app.mapper.OkonomiMapper.setInntektInOversikt
import no.nav.sosialhjelp.soknad.app.mapper.OkonomiMapper.setUtgiftInOpplysninger
import no.nav.sosialhjelp.soknad.app.mapper.OkonomiMapper.setUtgiftInOversikt
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.personalia.familie.dto.ForsorgerpliktFrontend
import no.nav.sosialhjelp.soknad.tekster.TextService
import no.nav.sosialhjelp.soknad.tilgangskontroll.Tilgangskontroll
import no.nav.sosialhjelp.soknad.v2.okonomi.MigrationToolkit.updateBekreftelse
import no.nav.sosialhjelp.soknad.v2.shadow.ControllerAdapter
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
@RequestMapping("/soknader/{behandlingsId}/familie/forsorgerplikt", produces = [MediaType.APPLICATION_JSON_VALUE])
class ForsorgerpliktRessurs(
    private val tilgangskontroll: Tilgangskontroll,
    private val textService: TextService,
    private val soknadUnderArbeidRepository: SoknadUnderArbeidRepository,
    private val controllerAdapter: ControllerAdapter,
) {
    private val log by logger()

    @GetMapping
    fun hentForsorgerplikt(
        @PathVariable("behandlingsId") behandlingsId: String,
    ): ForsorgerpliktFrontend {
        tilgangskontroll.verifiserAtBrukerHarTilgang()
        val soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier())
        val jsonInternalSoknad = soknad.jsonInternalSoknad ?: error("jsonInternalSoknad == null")

        return ForsorgerpliktFrontend.fromJson(jsonInternalSoknad.soknad.data.familie.forsorgerplikt)
    }

    @PutMapping
    fun updateForsorgerplikt(
        @PathVariable("behandlingsId") behandlingsId: String,
        @RequestBody forsorgerpliktFrontend: ForsorgerpliktFrontend,
    ) {
        tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId)
        val soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier())
        val jsonInternalSoknad = soknad.jsonInternalSoknad ?: error("jsonInternalSoknad == null")

        val forsorgerplikt = jsonInternalSoknad.soknad.data.familie.forsorgerplikt
        val opplysninger = jsonInternalSoknad.soknad.data.okonomi.opplysninger
        val oversikt = jsonInternalSoknad.soknad.data.okonomi.oversikt

        forsorgerplikt.barnebidrag = forsorgerpliktFrontend.barnebidrag?.let { makeBarnebidrag(it) }
        forsorgerplikt.updateAnsvar(forsorgerpliktFrontend)
        oversikt.updateInntektOgUtgift(forsorgerpliktFrontend.barnebidrag ?: JsonBarnebidrag.Verdi.INGEN)

        // Slett opplysninger dersom harForsorgerplikt kommer fra bruker. Jeg er usikker på hordan dette skulle oppstått?
        if (forsorgerplikt.harForsorgerplikt?.kilde == JsonKilde.BRUKER) {
            forsorgerplikt.harForsorgerplikt = JsonHarForsorgerplikt().withKilde(JsonKilde.SYSTEM).withVerdi(false)
            opplysninger.updateBekreftelse(SoknadJsonTyper.BEKREFTELSE_BARNEUTGIFTER, null)
            opplysninger.clearBarneutgifter()
            oversikt.clearBarneutgifter()
        }

        soknadUnderArbeidRepository.oppdaterSoknadsdata(soknad, eier())
        runCatching {
            controllerAdapter.updateForsorger(behandlingsId, forsorgerpliktFrontend)
        }.onFailure {
            log.error("Noe feilet under oppdatering av forsorgerplikt i ny datamodell", it)
        }
    }

    private fun makeBarnebidrag(barnebidragFrontend: JsonBarnebidrag.Verdi) = JsonBarnebidrag().withKilde(JsonKildeBruker.BRUKER).withVerdi(barnebidragFrontend)

    private fun JsonOkonomioversikt.clearBarneutgifter() = listOf(SoknadJsonTyper.UTGIFTER_BARNEHAGE, SoknadJsonTyper.UTGIFTER_SFO).forEach { setUtgiftInOversikt(this.utgift, it, false) }

    private fun JsonOkonomiopplysninger.clearBarneutgifter() = listOf(SoknadJsonTyper.UTGIFTER_BARN_FRITIDSAKTIVITETER, SoknadJsonTyper.UTGIFTER_BARN_TANNREGULERING, SoknadJsonTyper.UTGIFTER_ANNET_BARN).forEach { setUtgiftInOpplysninger(this.utgift, it, false) }

    private fun JsonOkonomioversikt.updateInntektOgUtgift(
        barnebidrag: JsonBarnebidrag.Verdi,
    ) {
        val harInntekt = listOf(JsonBarnebidrag.Verdi.MOTTAR, JsonBarnebidrag.Verdi.BEGGE).contains(barnebidrag)
        val harUtgift = listOf(JsonBarnebidrag.Verdi.BETALER, JsonBarnebidrag.Verdi.BEGGE).contains(barnebidrag)

        setInntektInOversikt(this.inntekt, SoknadJsonTyper.BARNEBIDRAG, harInntekt, textService.getJsonOkonomiTittel(TEXT_KEY_MOTTAR))
        setUtgiftInOversikt(this.utgift, SoknadJsonTyper.BARNEBIDRAG, harUtgift, textService.getJsonOkonomiTittel(TEXT_KEY_BETALER))
    }

    private fun JsonForsorgerplikt.updateAnsvar(
        forsorgerpliktFrontend: ForsorgerpliktFrontend,
    ) {
        val systemAnsvar = this.ansvar.orEmpty().filter { it.barn.kilde == JsonKilde.SYSTEM }

        forsorgerpliktFrontend.ansvar.forEach { frontend ->
            systemAnsvar.firstOrNull { it.barn.personIdentifikator == frontend.barn?.fodselsnummer }?.let { json ->
                json.borSammenMed = frontend.borSammenMed?.let { JsonBorSammenMed().withKilde(JsonKildeBruker.BRUKER).withVerdi(it) }
                json.harDeltBosted = frontend.harDeltBosted?.let { JsonHarDeltBosted().withKilde(JsonKildeBruker.BRUKER).withVerdi(it) }
                json.samvarsgrad = frontend.samvarsgrad?.let { JsonSamvarsgrad().withKilde(JsonKildeBruker.BRUKER).withVerdi(it) }
            }
        }

        this.ansvar = systemAnsvar.takeIf { it.isNotEmpty() }
    }

    companion object {
        const val TEXT_KEY_BETALER = "opplysninger.familiesituasjon.barnebidrag.betaler"
        const val TEXT_KEY_MOTTAR = "opplysninger.familiesituasjon.barnebidrag.mottar"
    }
}
