package no.nav.sosialhjelp.soknad.utgifter

import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper
import no.nav.sbl.soknadsosialhjelp.soknad.bostotte.JsonBostotte
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
import no.nav.sosialhjelp.soknad.v2.okonomi.MigrationToolkit.hasUtbetaling
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
@RequestMapping("/soknader/{behandlingsId}/utgifter/boutgifter", produces = [MediaType.APPLICATION_JSON_VALUE])
class BoutgiftRessurs(
    private val tilgangskontroll: Tilgangskontroll,
    private val soknadUnderArbeidRepository: SoknadUnderArbeidRepository,
    private val textService: TextService,
) {
    @GetMapping
    fun hentBoutgifter(
        @PathVariable("behandlingsId") behandlingsId: String,
    ): BoutgifterFrontend {
        tilgangskontroll.verifiserAtBrukerHarTilgang()
        val soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier())
        val jsonInternalSoknad = soknad.jsonInternalSoknad ?: error("jsonInternalSoknad == null")

        val opplysninger = jsonInternalSoknad.soknad.data.okonomi.opplysninger
        val oversikt = jsonInternalSoknad.soknad.data.okonomi.oversikt
        val stotteFraHusbankenFeilet = jsonInternalSoknad.soknad.driftsinformasjon.stotteFraHusbankenFeilet

        return opplysninger.bekreftelse?.let {
            BoutgifterFrontend(
                bekreftelse = opplysninger.getBekreftelseVerdi(SoknadJsonTyper.BEKREFTELSE_BOUTGIFTER),
                husleie = oversikt.hasUtgift(SoknadJsonTyper.UTGIFTER_HUSLEIE),
                strom = opplysninger.hasUtgift(SoknadJsonTyper.UTGIFTER_STROM),
                kommunalAvgift = opplysninger.hasUtgift(SoknadJsonTyper.UTGIFTER_KOMMUNAL_AVGIFT),
                oppvarming = opplysninger.hasUtgift(SoknadJsonTyper.UTGIFTER_OPPVARMING),
                boliglan = oversikt.hasUtgift(SoknadJsonTyper.UTGIFTER_BOLIGLAN_AVDRAG),
                annet = opplysninger.hasUtgift(SoknadJsonTyper.UTGIFTER_ANNET_BO),
                skalViseInfoVedBekreftelse = harBoutgiftMenIkkeBostotte(opplysninger, stotteFraHusbankenFeilet),
            )
        } ?: BoutgifterFrontend()
    }

    @PutMapping
    fun updateBoutgifter(
        @PathVariable("behandlingsId") behandlingsId: String,
        @RequestBody boutgifterFrontend: BoutgifterFrontend,
    ) {
        tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId)
        val soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier())
        val jsonInternalSoknad = soknad.jsonInternalSoknad ?: error("jsonInternalSoknad == null")

        val opplysninger = jsonInternalSoknad.soknad.data.okonomi.opplysninger
        val oversikt = jsonInternalSoknad.soknad.data.okonomi.oversikt

        opplysninger.updateBekreftelse(SoknadJsonTyper.BEKREFTELSE_BOUTGIFTER, boutgifterFrontend.bekreftelse, textService.getJsonOkonomiTittel("utgifter.boutgift"))
        opplysninger.updateBoutgifter(boutgifterFrontend)
        oversikt.updateBoutgifter(boutgifterFrontend)

        soknadUnderArbeidRepository.oppdaterSoknadsdata(soknad, eier())
    }

    private fun JsonOkonomiopplysninger.updateBoutgifter(
        boutgifterFrontend: BoutgifterFrontend,
    ) = mapOf(
        SoknadJsonTyper.UTGIFTER_STROM to boutgifterFrontend.strom,
        SoknadJsonTyper.UTGIFTER_KOMMUNAL_AVGIFT to boutgifterFrontend.kommunalAvgift,
        SoknadJsonTyper.UTGIFTER_OPPVARMING to boutgifterFrontend.oppvarming,
        SoknadJsonTyper.UTGIFTER_ANNET_BO to boutgifterFrontend.annet,
    ).forEach { (soknadJsonType, isExpected) ->
        setUtgiftInOpplysninger(this.utgift, soknadJsonType, isExpected, textService.getJsonOkonomiTittel(soknadTypeToTitleKey[soknadJsonType]))
    }

    private fun JsonOkonomioversikt.updateBoutgifter(
        boutgifterFrontend: BoutgifterFrontend,
    ) = mapOf(
        SoknadJsonTyper.UTGIFTER_HUSLEIE to boutgifterFrontend.husleie,
        SoknadJsonTyper.UTGIFTER_BOLIGLAN_AVDRAG to boutgifterFrontend.boliglan,
        SoknadJsonTyper.UTGIFTER_BOLIGLAN_RENTER to boutgifterFrontend.boliglan,
    ).forEach { (soknadJsonType, isExpected) ->
        setUtgiftInOversikt(this.utgift, soknadJsonType, isExpected, textService.getJsonOkonomiTittel(soknadTypeToTitleKey[soknadJsonType]))
    }

    /**
     * Dersom husbanken-API feilet, eller bruker ikke har samtykket til bostøtte viser vi info om bostøtte, dersom bruker ikke har bekreftet at de ikke har bostøtte.
     * Ellers viser vi info om bostøtte dersom bruker hverken har utbetalinger i husbanken eller eller har oppført bostøtte som inntekt.
     */
    private fun harBoutgiftMenIkkeBostotte(
        opplysninger: JsonOkonomiopplysninger,
        stotteFraHusbankenFeilet: Boolean,
    ) = if (stotteFraHusbankenFeilet || opplysninger.getBekreftelseVerdi(SoknadJsonTyper.BOSTOTTE_SAMTYKKE) != true) {
        opplysninger.getBekreftelseVerdi(SoknadJsonTyper.BOSTOTTE) != true
    } else {
        !opplysninger.bostotte.harSakerMedUtbetaling() && !opplysninger.hasUtbetaling(SoknadJsonTyper.UTBETALING_HUSBANKEN)
    }

    private fun JsonBostotte.harSakerMedUtbetaling() = this.saker.any { it.type == SoknadJsonTyper.UTBETALING_HUSBANKEN }

    data class BoutgifterFrontend(
        val bekreftelse: Boolean? = null,
        val husleie: Boolean = false,
        val strom: Boolean = false,
        val kommunalAvgift: Boolean = false,
        val oppvarming: Boolean = false,
        val boliglan: Boolean = false,
        val annet: Boolean = false,
        val skalViseInfoVedBekreftelse: Boolean = false,
    )
}
