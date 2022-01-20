package no.nav.sosialhjelp.soknad.utgifter

import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.BEKREFTELSE_BARNEUTGIFTER
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTGIFTER_ANNET_BARN
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTGIFTER_BARNEHAGE
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTGIFTER_BARN_FRITIDSAKTIVITETER
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTGIFTER_BARN_TANNREGULERING
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTGIFTER_SFO
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomi
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomiopplysninger
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomioversikt
import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.sosialhjelp.metrics.aspects.Timed
import no.nav.sosialhjelp.soknad.business.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.business.service.TextService
import no.nav.sosialhjelp.soknad.common.Constants
import no.nav.sosialhjelp.soknad.common.mapper.OkonomiMapper.addutgiftIfCheckedElseDeleteInOpplysninger
import no.nav.sosialhjelp.soknad.common.mapper.OkonomiMapper.addutgiftIfCheckedElseDeleteInOversikt
import no.nav.sosialhjelp.soknad.common.mapper.OkonomiMapper.setBekreftelse
import no.nav.sosialhjelp.soknad.common.mapper.TitleKeyMapper.soknadTypeToTitleKey
import no.nav.sosialhjelp.soknad.common.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.tilgangskontroll.Tilgangskontroll
import org.springframework.stereotype.Controller
import javax.ws.rs.GET
import javax.ws.rs.PUT
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType

@Controller
@ProtectedWithClaims(issuer = Constants.SELVBETJENING, claimMap = [Constants.CLAIM_ACR_LEVEL_4])
@Path("/soknader/{behandlingsId}/utgifter/barneutgifter")
@Timed
@Produces(MediaType.APPLICATION_JSON)
open class BarneutgiftRessurs(
    private val tilgangskontroll: Tilgangskontroll,
    private val soknadUnderArbeidRepository: SoknadUnderArbeidRepository,
    private val textService: TextService
) {
    @GET
    open fun hentBarneutgifter(@PathParam("behandlingsId") behandlingsId: String): BarneutgifterFrontend {
        tilgangskontroll.verifiserAtBrukerHarTilgang()
        val eier = SubjectHandlerUtils.getUserIdFromToken()
        val soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier).jsonInternalSoknad

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
            annet = getUtgiftstype(okonomi.opplysninger, UTGIFTER_ANNET_BARN)
        )
    }

    @PUT
    open fun updateBarneutgifter(
        @PathParam("behandlingsId") behandlingsId: String,
        barneutgifterFrontend: BarneutgifterFrontend
    ) {
        tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId)
        val eier = SubjectHandlerUtils.getUserIdFromToken()
        val soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier)
        val okonomi = soknad.jsonInternalSoknad.soknad.data.okonomi
        if (okonomi.opplysninger.bekreftelse == null) {
            okonomi.opplysninger.bekreftelse = ArrayList()
        }
        setBekreftelse(
            okonomi.opplysninger,
            BEKREFTELSE_BARNEUTGIFTER,
            barneutgifterFrontend.bekreftelse,
            textService.getJsonOkonomiTittel("utgifter.barn")
        )
        setBarneutgifter(okonomi, barneutgifterFrontend)
        soknadUnderArbeidRepository.oppdaterSoknadsdata(soknad, eier)
    }

    private fun setBarneutgifter(okonomi: JsonOkonomi, barneutgifterFrontend: BarneutgifterFrontend) {
        val opplysningerBarneutgifter = okonomi.opplysninger.utgift
        val oversiktBarneutgifter = okonomi.oversikt.utgift
        var tittel = textService.getJsonOkonomiTittel(soknadTypeToTitleKey[UTGIFTER_BARNEHAGE])
        addutgiftIfCheckedElseDeleteInOversikt(
            oversiktBarneutgifter,
            UTGIFTER_BARNEHAGE,
            tittel,
            barneutgifterFrontend.barnehage
        )
        tittel = textService.getJsonOkonomiTittel(soknadTypeToTitleKey[UTGIFTER_SFO])
        addutgiftIfCheckedElseDeleteInOversikt(
            oversiktBarneutgifter,
            UTGIFTER_SFO,
            tittel,
            barneutgifterFrontend.sfo
        )
        tittel = textService.getJsonOkonomiTittel(soknadTypeToTitleKey[UTGIFTER_BARN_FRITIDSAKTIVITETER])
        addutgiftIfCheckedElseDeleteInOpplysninger(
            opplysningerBarneutgifter,
            UTGIFTER_BARN_FRITIDSAKTIVITETER,
            tittel,
            barneutgifterFrontend.fritidsaktiviteter
        )
        tittel =
            textService.getJsonOkonomiTittel(soknadTypeToTitleKey[UTGIFTER_BARN_TANNREGULERING])
        addutgiftIfCheckedElseDeleteInOpplysninger(
            opplysningerBarneutgifter,
            UTGIFTER_BARN_TANNREGULERING,
            tittel,
            barneutgifterFrontend.tannregulering
        )
        tittel =
            textService.getJsonOkonomiTittel(soknadTypeToTitleKey[UTGIFTER_ANNET_BARN])
        addutgiftIfCheckedElseDeleteInOpplysninger(
            opplysningerBarneutgifter,
            UTGIFTER_ANNET_BARN,
            tittel,
            barneutgifterFrontend.annet
        )
    }

    private fun getBekreftelse(opplysninger: JsonOkonomiopplysninger): Boolean? {
        return opplysninger.bekreftelse.firstOrNull { it.type == BEKREFTELSE_BARNEUTGIFTER }?.verdi
    }

    private fun getUtgiftstype(opplysninger: JsonOkonomiopplysninger, utgift: String): Boolean {
        return opplysninger.utgift.any { it.type == utgift }
    }

    private fun getUtgiftstype(oversikt: JsonOkonomioversikt, utgift: String): Boolean {
        return oversikt.utgift.any { it.type == utgift }
    }

    data class BarneutgifterFrontend(
        val harForsorgerplikt: Boolean = false,
        val bekreftelse: Boolean? = null,
        val fritidsaktiviteter: Boolean = false,
        val barnehage: Boolean = false,
        val sfo: Boolean = false,
        val tannregulering: Boolean = false,
        val annet: Boolean = false
    )
}
