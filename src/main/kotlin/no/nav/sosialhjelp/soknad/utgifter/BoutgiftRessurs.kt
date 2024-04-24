package no.nav.sosialhjelp.soknad.utgifter

import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.BEKREFTELSE_BOUTGIFTER
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.BOSTOTTE
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.BOSTOTTE_SAMTYKKE
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTBETALING_HUSBANKEN
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTGIFTER_ANNET_BO
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTGIFTER_BOLIGLAN_AVDRAG
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTGIFTER_BOLIGLAN_RENTER
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTGIFTER_HUSLEIE
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTGIFTER_KOMMUNAL_AVGIFT
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTGIFTER_OPPVARMING
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTGIFTER_STROM
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomiopplysninger
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomioversikt
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtgift
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.oversikt.JsonOkonomioversiktUtgift
import no.nav.sosialhjelp.soknad.app.annotation.ProtectionSelvbetjeningHigh
import no.nav.sosialhjelp.soknad.app.mapper.OkonomiMapper.setBekreftelse
import no.nav.sosialhjelp.soknad.app.mapper.OkonomiMapper.setUtgiftInOpplysninger
import no.nav.sosialhjelp.soknad.app.mapper.OkonomiMapper.setUtgiftInOversikt
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

        if (opplysninger.bekreftelse == null) {
            return BoutgifterFrontend(
                bekreftelse = null,
                skalViseInfoVedBekreftelse = getSkalViseInfoVedBekreftelse(opplysninger, stotteFraHusbankenFeilet),
            )
        }
        return BoutgifterFrontend(
            bekreftelse = getBekreftelse(opplysninger),
            husleie = getUtgiftstype(oversikt, UTGIFTER_HUSLEIE),
            strom = getUtgiftstype(opplysninger, UTGIFTER_STROM),
            kommunalAvgift = getUtgiftstype(opplysninger, UTGIFTER_KOMMUNAL_AVGIFT),
            oppvarming = getUtgiftstype(opplysninger, UTGIFTER_OPPVARMING),
            boliglan = getUtgiftstype(oversikt, UTGIFTER_BOLIGLAN_AVDRAG),
            annet = getUtgiftstype(opplysninger, UTGIFTER_ANNET_BO),
            skalViseInfoVedBekreftelse = getSkalViseInfoVedBekreftelse(opplysninger, stotteFraHusbankenFeilet),
        )
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

        setBekreftelse(opplysninger, BEKREFTELSE_BOUTGIFTER, boutgifterFrontend.bekreftelse, textService.getJsonOkonomiTittel("utgifter.boutgift"))
        setBoutgifter(opplysninger.utgift, oversikt.utgift, boutgifterFrontend)

        soknadUnderArbeidRepository.oppdaterSoknadsdata(soknad, eier())
    }

    private fun setBoutgifter(
        opplysningerUtgifter: MutableList<JsonOkonomiOpplysningUtgift>,
        oversiktUtgifter: MutableList<JsonOkonomioversiktUtgift>,
        boutgifterFrontend: BoutgifterFrontend,
    ) {
        mapOf(
            UTGIFTER_HUSLEIE to boutgifterFrontend.husleie,
            UTGIFTER_BOLIGLAN_AVDRAG to boutgifterFrontend.boliglan,
            UTGIFTER_BOLIGLAN_RENTER to boutgifterFrontend.boliglan,
        )
            .forEach { (soknadJsonType, isExpected) ->
                setUtgiftInOversikt(oversiktUtgifter, soknadJsonType, isExpected, textService.getJsonOkonomiTittel(soknadTypeToTitleKey[soknadJsonType]))
            }

        mapOf(
            UTGIFTER_STROM to boutgifterFrontend.strom,
            UTGIFTER_KOMMUNAL_AVGIFT to boutgifterFrontend.kommunalAvgift,
            UTGIFTER_OPPVARMING to boutgifterFrontend.oppvarming,
            UTGIFTER_ANNET_BO to boutgifterFrontend.annet,
        )
            .forEach { (soknadJsonType, isExpected) ->
                setUtgiftInOpplysninger(opplysningerUtgifter, soknadJsonType, isExpected, textService.getJsonOkonomiTittel(soknadTypeToTitleKey[soknadJsonType]))
            }
    }

    private fun getBekreftelse(opplysninger: JsonOkonomiopplysninger): Boolean? = opplysninger.bekreftelse.firstOrNull { it.type == BEKREFTELSE_BOUTGIFTER }?.verdi

    private fun getUtgiftstype(
        opplysninger: JsonOkonomiopplysninger,
        utgift: String,
    ): Boolean = opplysninger.utgift.any { it.type == utgift }

    private fun getUtgiftstype(
        oversikt: JsonOkonomioversikt,
        utgift: String,
    ): Boolean = oversikt.utgift.any { it.type == utgift }

    private fun getSkalViseInfoVedBekreftelse(
        opplysninger: JsonOkonomiopplysninger,
        stotteFraHusbankenFeilet: Boolean,
    ): Boolean {
        return if (!stotteFraHusbankenFeilet && !manglerSamtykke(opplysninger)) {
            opplysninger.bostotte.saker.none { it.type == UTBETALING_HUSBANKEN } &&
                opplysninger.utbetaling.none { it.type == UTBETALING_HUSBANKEN }
        } else if (opplysninger.bekreftelse == null) {
            false
        } else {
            opplysninger.bekreftelse.firstOrNull { it.type == BOSTOTTE }?.let { it.verdi != null && !it.verdi } ?: true
        }
    }

    private fun manglerSamtykke(opplysninger: JsonOkonomiopplysninger): Boolean =
        opplysninger.bekreftelse.none { it.type.equals(BOSTOTTE_SAMTYKKE, ignoreCase = true) && it.verdi }

    data class BoutgifterFrontend(
        val bekreftelse: Boolean?,
        val husleie: Boolean = false,
        val strom: Boolean = false,
        val kommunalAvgift: Boolean = false,
        val oppvarming: Boolean = false,
        val boliglan: Boolean = false,
        val annet: Boolean = false,
        val skalViseInfoVedBekreftelse: Boolean = false,
    )
}
