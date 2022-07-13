package no.nav.sosialhjelp.soknad.utgifter

import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.BEKREFTELSE_BOUTGIFTER
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.BOSTOTTE
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.BOSTOTTE_SAMTYKKE
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTGIFTER_ANNET_BO
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTGIFTER_BOLIGLAN_AVDRAG
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTGIFTER_BOLIGLAN_RENTER
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTGIFTER_HUSLEIE
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTGIFTER_KOMMUNAL_AVGIFT
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTGIFTER_OPPVARMING
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTGIFTER_STROM
import no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknad
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomi
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomiopplysninger
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomioversikt
import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.sosialhjelp.soknad.common.Constants
import no.nav.sosialhjelp.soknad.common.mapper.OkonomiMapper.addutgiftIfCheckedElseDeleteInOpplysninger
import no.nav.sosialhjelp.soknad.common.mapper.OkonomiMapper.addutgiftIfCheckedElseDeleteInOversikt
import no.nav.sosialhjelp.soknad.common.mapper.OkonomiMapper.setBekreftelse
import no.nav.sosialhjelp.soknad.common.mapper.TitleKeyMapper.soknadTypeToTitleKey
import no.nav.sosialhjelp.soknad.common.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.tekster.TextService
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
@Path("/soknader/{behandlingsId}/utgifter/boutgifter")
@Produces(MediaType.APPLICATION_JSON)
open class BoutgiftRessurs(
    private val tilgangskontroll: Tilgangskontroll,
    private val soknadUnderArbeidRepository: SoknadUnderArbeidRepository,
    private val textService: TextService
) {
    @GET
    open fun hentBoutgifter(@PathParam("behandlingsId") behandlingsId: String): BoutgifterFrontend {
        tilgangskontroll.verifiserAtBrukerHarTilgang()
        val eier = SubjectHandlerUtils.getUserIdFromToken()
        val jsonInternalSoknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier).jsonInternalSoknad
            ?: throw IllegalStateException("Kan ikke hente søknaddata hvis SoknadUnderArbeid.jsonInternalSoknad er null")
        val soknad = jsonInternalSoknad.soknad
        val okonomi = soknad.data.okonomi
        if (okonomi.opplysninger.bekreftelse == null) {
            return BoutgifterFrontend(null, skalViseInfoVedBekreftelse = getSkalViseInfoVedBekreftelse(soknad, okonomi))
        }
        return BoutgifterFrontend(
            bekreftelse = getBekreftelse(okonomi.opplysninger),
            husleie = getUtgiftstype(okonomi.oversikt, UTGIFTER_HUSLEIE),
            strom = getUtgiftstype(okonomi.opplysninger, UTGIFTER_STROM),
            kommunalAvgift = getUtgiftstype(okonomi.opplysninger, UTGIFTER_KOMMUNAL_AVGIFT),
            oppvarming = getUtgiftstype(okonomi.opplysninger, UTGIFTER_OPPVARMING),
            boliglan = getUtgiftstype(okonomi.oversikt, UTGIFTER_BOLIGLAN_AVDRAG),
            annet = getUtgiftstype(okonomi.opplysninger, UTGIFTER_ANNET_BO),
            skalViseInfoVedBekreftelse = getSkalViseInfoVedBekreftelse(soknad, okonomi)
        )
    }

    @PUT
    open fun updateBoutgifter(
        @PathParam("behandlingsId") behandlingsId: String,
        boutgifterFrontend: BoutgifterFrontend
    ) {
        tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId)
        val eier = SubjectHandlerUtils.getUserIdFromToken()
        val soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier)
        val jsonInternalSoknad = soknad.jsonInternalSoknad
            ?: throw IllegalStateException("Kan ikke oppdatere søknaddata hvis SoknadUnderArbeid.jsonInternalSoknad er null")
        val okonomi = jsonInternalSoknad.soknad.data.okonomi
        if (okonomi.opplysninger.bekreftelse == null) {
            okonomi.opplysninger.bekreftelse = ArrayList()
        }
        setBekreftelse(
            okonomi.opplysninger,
            BEKREFTELSE_BOUTGIFTER,
            boutgifterFrontend.bekreftelse,
            textService.getJsonOkonomiTittel("utgifter.boutgift")
        )
        setBoutgifter(okonomi, boutgifterFrontend)
        soknadUnderArbeidRepository.oppdaterSoknadsdata(soknad, eier)
    }

    private fun setBoutgifter(okonomi: JsonOkonomi, boutgifterFrontend: BoutgifterFrontend) {
        val opplysningerBoutgifter = okonomi.opplysninger.utgift
        val oversiktBoutgifter = okonomi.oversikt.utgift
        var tittel = textService.getJsonOkonomiTittel(soknadTypeToTitleKey[UTGIFTER_HUSLEIE])
        addutgiftIfCheckedElseDeleteInOversikt(
            oversiktBoutgifter,
            UTGIFTER_HUSLEIE,
            tittel,
            boutgifterFrontend.husleie
        )
        tittel = textService.getJsonOkonomiTittel(soknadTypeToTitleKey[UTGIFTER_STROM])
        addutgiftIfCheckedElseDeleteInOpplysninger(
            opplysningerBoutgifter,
            UTGIFTER_STROM,
            tittel,
            boutgifterFrontend.strom
        )
        tittel = textService.getJsonOkonomiTittel(soknadTypeToTitleKey[UTGIFTER_KOMMUNAL_AVGIFT])
        addutgiftIfCheckedElseDeleteInOpplysninger(
            opplysningerBoutgifter,
            UTGIFTER_KOMMUNAL_AVGIFT,
            tittel,
            boutgifterFrontend.kommunalAvgift
        )
        tittel = textService.getJsonOkonomiTittel(soknadTypeToTitleKey[UTGIFTER_OPPVARMING])
        addutgiftIfCheckedElseDeleteInOpplysninger(
            opplysningerBoutgifter,
            UTGIFTER_OPPVARMING,
            tittel,
            boutgifterFrontend.oppvarming
        )
        tittel = textService.getJsonOkonomiTittel(soknadTypeToTitleKey[UTGIFTER_BOLIGLAN_AVDRAG])
        addutgiftIfCheckedElseDeleteInOversikt(
            oversiktBoutgifter,
            UTGIFTER_BOLIGLAN_AVDRAG,
            tittel,
            boutgifterFrontend.boliglan
        )
        tittel = textService.getJsonOkonomiTittel(soknadTypeToTitleKey[UTGIFTER_BOLIGLAN_RENTER])
        addutgiftIfCheckedElseDeleteInOversikt(
            oversiktBoutgifter,
            UTGIFTER_BOLIGLAN_RENTER,
            tittel,
            boutgifterFrontend.boliglan
        )
        tittel = textService.getJsonOkonomiTittel(soknadTypeToTitleKey[UTGIFTER_ANNET_BO])
        addutgiftIfCheckedElseDeleteInOpplysninger(
            opplysningerBoutgifter,
            UTGIFTER_ANNET_BO,
            tittel,
            boutgifterFrontend.annet
        )
    }

    private fun getBekreftelse(opplysninger: JsonOkonomiopplysninger): Boolean? {
        return opplysninger.bekreftelse.firstOrNull { it.type == BEKREFTELSE_BOUTGIFTER }?.verdi
    }

    private fun getUtgiftstype(opplysninger: JsonOkonomiopplysninger, utgift: String): Boolean {
        return opplysninger.utgift.any { it.type == utgift }
    }

    private fun getUtgiftstype(oversikt: JsonOkonomioversikt, utgift: String): Boolean {
        return oversikt.utgift.any { it.type == utgift }
    }

    private fun getSkalViseInfoVedBekreftelse(soknad: JsonSoknad, okonomi: JsonOkonomi): Boolean {
        if (bostotteFeiletEllerManglerSamtykke(soknad)) {
            if (okonomi.opplysninger.bekreftelse != null) {
                val bekreftelse = okonomi.opplysninger.bekreftelse.firstOrNull { it.type == BOSTOTTE }
                return if (bekreftelse != null) {
                    bekreftelse.verdi != null && !bekreftelse.verdi
                } else {
                    true
                }
            }
            return false
        } else {
            return !isAnyHusbankenSaker(soknad) && !isAnyHusbankenUtbetalinger(soknad)
        }
    }

    private fun bostotteFeiletEllerManglerSamtykke(soknad: JsonSoknad): Boolean {
        return soknad.driftsinformasjon.stotteFraHusbankenFeilet ||
            soknad.data.okonomi.opplysninger.bekreftelse
                .none { it.type.equals(BOSTOTTE_SAMTYKKE, ignoreCase = true) && it.verdi }
    }

    private fun isAnyHusbankenUtbetalinger(soknad: JsonSoknad): Boolean {
        return soknad.data.okonomi.opplysninger.utbetaling.any { it.type == SoknadJsonTyper.UTBETALING_HUSBANKEN }
    }

    private fun isAnyHusbankenSaker(soknad: JsonSoknad): Boolean {
        return soknad.data.okonomi.opplysninger.bostotte.saker.any { it.type == SoknadJsonTyper.UTBETALING_HUSBANKEN }
    }

    data class BoutgifterFrontend(
        val bekreftelse: Boolean?,
        val husleie: Boolean = false,
        val strom: Boolean = false,
        val kommunalAvgift: Boolean = false,
        val oppvarming: Boolean = false,
        val boliglan: Boolean = false,
        val annet: Boolean = false,
        val skalViseInfoVedBekreftelse: Boolean = false
    )
}
