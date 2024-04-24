package no.nav.sosialhjelp.soknad.utgifter

import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.BEKREFTELSE_BARNEUTGIFTER
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTGIFTER_ANNET_BARN
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTGIFTER_BARNEHAGE
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTGIFTER_BARN_FRITIDSAKTIVITETER
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTGIFTER_BARN_TANNREGULERING
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTGIFTER_SFO
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomiopplysninger
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomioversikt
import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.sosialhjelp.soknad.app.Constants
import no.nav.sosialhjelp.soknad.app.mapper.OkonomiMapper.setBekreftelse
import no.nav.sosialhjelp.soknad.app.mapper.OkonomiMapper.setUtgiftInOpplysninger
import no.nav.sosialhjelp.soknad.app.mapper.OkonomiMapper.setUtgiftInOversikt
import no.nav.sosialhjelp.soknad.app.mapper.TitleKeyMapper.soknadTypeToTitleKey
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
        val eier = SubjectHandlerUtils.getUserIdFromToken()
        val soknad =
            soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier).jsonInternalSoknad
                ?: throw IllegalStateException("Kan ikke hente søknaddata hvis SoknadUnderArbeid.jsonInternalSoknad er null")

        val harForsorgerplikt = soknad.soknad.data.familie.forsorgerplikt.harForsorgerplikt
        if (harForsorgerplikt == null || harForsorgerplikt.verdi == null || !harForsorgerplikt.verdi) {
            return BarneutgifterFrontend()
        }

        val okonomi = soknad.soknad.data.okonomi
        if (okonomi.opplysninger.bekreftelse == null) {
            return BarneutgifterFrontend(true)
        }

        return BarneutgifterFrontend(
            harForsorgerplikt = true,
            bekreftelse = getBekreftelse(okonomi.opplysninger),
            fritidsaktiviteter = getUtgiftstype(okonomi.opplysninger, UTGIFTER_BARN_FRITIDSAKTIVITETER),
            barnehage = getUtgiftstype(okonomi.oversikt, UTGIFTER_BARNEHAGE),
            sfo = getUtgiftstype(okonomi.oversikt, UTGIFTER_SFO),
            tannregulering = getUtgiftstype(okonomi.opplysninger, UTGIFTER_BARN_TANNREGULERING),
            annet = getUtgiftstype(okonomi.opplysninger, UTGIFTER_ANNET_BARN),
        )
    }

    @PutMapping
    fun updateBarneutgifter(
        @PathVariable("behandlingsId") behandlingsId: String,
        @RequestBody barneutgifterFrontend: BarneutgifterFrontend,
    ) {
        tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId)
        val eier = SubjectHandlerUtils.getUserIdFromToken()
        val soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier)
        val jsonInternalSoknad = soknad.jsonInternalSoknad ?: error("jsonInternalSoknad == null")

        val opplysninger = jsonInternalSoknad.soknad.data.okonomi.opplysninger
        val oversikt = jsonInternalSoknad.soknad.data.okonomi.oversikt

        setBekreftelse(opplysninger, BEKREFTELSE_BARNEUTGIFTER, barneutgifterFrontend.bekreftelse, textService.getJsonOkonomiTittel("utgifter.barn"))
        setBarneutgifter(opplysninger, oversikt, barneutgifterFrontend)
        soknadUnderArbeidRepository.oppdaterSoknadsdata(soknad, eier)
    }

    private fun utgiftTittel(opplysningType: String) = textService.getJsonOkonomiTittel(soknadTypeToTitleKey[opplysningType])

    private fun setBarneutgifter(
        opplysninger: JsonOkonomiopplysninger,
        oversikt: JsonOkonomioversikt,
        barneutgifterFrontend: BarneutgifterFrontend,
    ) {
        mapOf(
            UTGIFTER_BARNEHAGE to barneutgifterFrontend.barnehage,
            UTGIFTER_SFO to barneutgifterFrontend.sfo,
        ).forEach { (utgiftJsonType, isExpected) ->
            setUtgiftInOversikt(oversikt.utgift, utgiftJsonType, isExpected, utgiftTittel(utgiftJsonType))
        }

        mapOf(
            UTGIFTER_BARN_FRITIDSAKTIVITETER to barneutgifterFrontend.fritidsaktiviteter,
            UTGIFTER_BARN_TANNREGULERING to barneutgifterFrontend.tannregulering,
            UTGIFTER_ANNET_BARN to barneutgifterFrontend.annet,
        ).forEach { (utgiftJsonType, isExpected) ->
            setUtgiftInOpplysninger(opplysninger.utgift, utgiftJsonType, isExpected, utgiftTittel(utgiftJsonType))
        }
    }

    private fun getBekreftelse(opplysninger: JsonOkonomiopplysninger): Boolean? {
        return opplysninger.bekreftelse.firstOrNull { it.type == BEKREFTELSE_BARNEUTGIFTER }?.verdi
    }

    private fun getUtgiftstype(
        opplysninger: JsonOkonomiopplysninger,
        utgift: String,
    ): Boolean {
        return opplysninger.utgift.any { it.type == utgift }
    }

    private fun getUtgiftstype(
        oversikt: JsonOkonomioversikt,
        utgift: String,
    ): Boolean {
        return oversikt.utgift.any { it.type == utgift }
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
