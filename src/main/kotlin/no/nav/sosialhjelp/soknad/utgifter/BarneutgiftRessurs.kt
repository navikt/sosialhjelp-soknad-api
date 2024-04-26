package no.nav.sosialhjelp.soknad.utgifter

import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomiopplysninger
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomioversikt
import no.nav.sosialhjelp.soknad.app.annotation.ProtectionSelvbetjeningHigh
import no.nav.sosialhjelp.soknad.app.mapper.OkonomiMapper.setUtgiftInOpplysninger
import no.nav.sosialhjelp.soknad.app.mapper.OkonomiMapper.setUtgiftInOversikt
import no.nav.sosialhjelp.soknad.app.mapper.TitleKeyMapper.soknadTypeToTitleKey
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.tekster.TextService
import no.nav.sosialhjelp.soknad.tilgangskontroll.Tilgangskontroll
import no.nav.sosialhjelp.soknad.v2.okonomi.MigrationToolkit.getBekreftelseVerdi
import no.nav.sosialhjelp.soknad.v2.okonomi.MigrationToolkit.hasUtgift
import no.nav.sosialhjelp.soknad.v2.okonomi.MigrationToolkit.updateBekreftelse
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
@RequestMapping("/soknader/{behandlingsId}/utgifter/barneutgifter", produces = [MediaType.APPLICATION_JSON_VALUE])
class BarneutgiftRessurs(
    private val tilgangskontroll: Tilgangskontroll,
    private val soknadUnderArbeidRepository: SoknadUnderArbeidRepository,
    private val textService: TextService,
) {
    @GetMapping
    fun hentBarneutgifter(
        @PathVariable("behandlingsId") behandlingsId: String,
    ): BarneutgifterFrontend {
        tilgangskontroll.verifiserAtBrukerHarTilgang()
        val soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier())
        val jsonInternalSoknad = soknad.jsonInternalSoknad ?: error("jsonInternalSoknad == null")

        val harForsorgerplikt = jsonInternalSoknad.soknad.data.familie.forsorgerplikt.harForsorgerplikt
        val opplysninger = jsonInternalSoknad.soknad.data.okonomi.opplysninger
        val oversikt = jsonInternalSoknad.soknad.data.okonomi.oversikt

        return opplysninger.bekreftelse?.let {
            BarneutgifterFrontend(
                harForsorgerplikt = true,
                bekreftelse = opplysninger.getBekreftelseVerdi(SoknadJsonTyper.BEKREFTELSE_BARNEUTGIFTER),
                fritidsaktiviteter = opplysninger.hasUtgift(SoknadJsonTyper.UTGIFTER_BARN_FRITIDSAKTIVITETER),
                barnehage = oversikt.hasUtgift(SoknadJsonTyper.UTGIFTER_BARNEHAGE),
                sfo = oversikt.hasUtgift(SoknadJsonTyper.UTGIFTER_SFO),
                tannregulering = opplysninger.hasUtgift(SoknadJsonTyper.UTGIFTER_BARN_TANNREGULERING),
                annet = opplysninger.hasUtgift(SoknadJsonTyper.UTGIFTER_ANNET_BARN),
            )
        } ?: BarneutgifterFrontend(harForsorgerplikt = harForsorgerplikt?.verdi == true)
    }

    @PutMapping
    fun updateBarneutgifter(
        @PathVariable("behandlingsId") behandlingsId: String,
        @RequestBody barneutgifterFrontend: BarneutgifterFrontend,
    ) {
        tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId)
        val soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier())
        val jsonInternalSoknad = soknad.jsonInternalSoknad ?: error("jsonInternalSoknad == null")

        val opplysninger = jsonInternalSoknad.soknad.data.okonomi.opplysninger
        val oversikt = jsonInternalSoknad.soknad.data.okonomi.oversikt

        opplysninger.updateBekreftelse(SoknadJsonTyper.BEKREFTELSE_BARNEUTGIFTER, barneutgifterFrontend.bekreftelse, textService.getJsonOkonomiTittel("utgifter.barn"))
        opplysninger.updateBarneutgifter(barneutgifterFrontend)
        oversikt.updateBarneutgifter(barneutgifterFrontend)

        soknadUnderArbeidRepository.oppdaterSoknadsdata(soknad, eier())
    }

    private fun JsonOkonomiopplysninger.updateBarneutgifter(
        barneutgifterFrontend: BarneutgifterFrontend,
    ) = mapOf(
        SoknadJsonTyper.UTGIFTER_BARN_FRITIDSAKTIVITETER to barneutgifterFrontend.fritidsaktiviteter,
        SoknadJsonTyper.UTGIFTER_BARN_TANNREGULERING to barneutgifterFrontend.tannregulering,
        SoknadJsonTyper.UTGIFTER_ANNET_BARN to barneutgifterFrontend.annet,
    ).forEach { (utgiftJsonType, isExpected) ->
        setUtgiftInOpplysninger(this.utgift, utgiftJsonType, isExpected, textService.getJsonOkonomiTittel(soknadTypeToTitleKey[utgiftJsonType]))
    }

    private fun JsonOkonomioversikt.updateBarneutgifter(
        barneutgifterFrontend: BarneutgifterFrontend,
    ) = mapOf(
        SoknadJsonTyper.UTGIFTER_BARNEHAGE to barneutgifterFrontend.barnehage,
        SoknadJsonTyper.UTGIFTER_SFO to barneutgifterFrontend.sfo,
    ).forEach { (utgiftJsonType, isExpected) ->
        setUtgiftInOversikt(this.utgift, utgiftJsonType, isExpected, textService.getJsonOkonomiTittel(soknadTypeToTitleKey[utgiftJsonType]))
    }

    data class BarneutgifterFrontend(
        val harForsorgerplikt: Boolean = false,
        val bekreftelse: Boolean? = null,
        val fritidsaktiviteter: Boolean = false,
        val barnehage: Boolean = false,
        val sfo: Boolean = false,
        val tannregulering: Boolean = false,
        val annet: Boolean = false,
    )
}
