package no.nav.sosialhjelp.soknad.inntekt.verdi

import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.BEKREFTELSE_VERDI
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.VERDI_ANNET
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.VERDI_BOLIG
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.VERDI_CAMPINGVOGN
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.VERDI_FRITIDSEIENDOM
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.VERDI_KJORETOY
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKildeBruker
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomiopplysninger
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomioversikt
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomibeskrivelserAvAnnet
import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.sosialhjelp.metrics.aspects.Timed
import no.nav.sosialhjelp.soknad.business.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.common.mapper.OkonomiMapper.addFormueIfCheckedElseDeleteInOversikt
import no.nav.sosialhjelp.soknad.common.mapper.OkonomiMapper.setBekreftelse
import no.nav.sosialhjelp.soknad.common.mapper.TitleKeyMapper.soknadTypeToTitleKey
import no.nav.sosialhjelp.soknad.common.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.tekster.TextService
import no.nav.sosialhjelp.soknad.tilgangskontroll.Tilgangskontroll
import no.nav.sosialhjelp.soknad.web.utils.Constants
import org.springframework.stereotype.Controller
import javax.ws.rs.GET
import javax.ws.rs.PUT
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType

@Controller
@ProtectedWithClaims(issuer = Constants.SELVBETJENING, claimMap = [Constants.CLAIM_ACR_LEVEL_4])
@Path("/soknader/{behandlingsId}/inntekt/verdier")
@Timed
@Produces(MediaType.APPLICATION_JSON)
open class VerdiRessurs(
    private val tilgangskontroll: Tilgangskontroll,
    private val soknadUnderArbeidRepository: SoknadUnderArbeidRepository,
    private val textService: TextService
) {
    @GET
    open fun hentVerdier(@PathParam("behandlingsId") behandlingsId: String): VerdierFrontend {
        tilgangskontroll.verifiserAtBrukerHarTilgang()
        val eier = SubjectHandlerUtils.getUserIdFromToken()
        val soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier).jsonInternalSoknad
        val okonomi = soknad.soknad.data.okonomi

        if (okonomi.opplysninger.bekreftelse == null) {
            return VerdierFrontend()
        }

        return VerdierFrontend(
            bekreftelse = getBekreftelse(okonomi.opplysninger),
            bolig = hasVerdiType(okonomi.oversikt, VERDI_BOLIG),
            campingvogn = hasVerdiType(okonomi.oversikt, VERDI_CAMPINGVOGN),
            kjoretoy = hasVerdiType(okonomi.oversikt, VERDI_KJORETOY),
            fritidseiendom = hasVerdiType(okonomi.oversikt, VERDI_FRITIDSEIENDOM),
            annet = hasVerdiType(okonomi.oversikt, VERDI_ANNET),
            beskrivelseAvAnnet = okonomi.opplysninger.beskrivelseAvAnnet?.verdi
        )
    }

    @PUT
    open fun updateVerdier(@PathParam("behandlingsId") behandlingsId: String, verdierFrontend: VerdierFrontend) {
        tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId)
        val eier = SubjectHandlerUtils.getUserIdFromToken()
        val soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier)
        val okonomi = soknad.jsonInternalSoknad.soknad.data.okonomi
        if (okonomi.opplysninger.bekreftelse == null) {
            okonomi.opplysninger.bekreftelse = ArrayList()
        }
        setBekreftelse(
            okonomi.opplysninger,
            BEKREFTELSE_VERDI,
            verdierFrontend.bekreftelse,
            textService.getJsonOkonomiTittel("inntekt.eierandeler")
        )
        setVerdier(okonomi.oversikt, verdierFrontend)
        setBeskrivelseAvAnnet(okonomi.opplysninger, verdierFrontend)
        soknadUnderArbeidRepository.oppdaterSoknadsdata(soknad, eier)
    }

    private fun setVerdier(oversikt: JsonOkonomioversikt, verdierFrontend: VerdierFrontend) {
        val verdier = oversikt.formue
        var tittel = textService.getJsonOkonomiTittel(soknadTypeToTitleKey[VERDI_BOLIG])
        addFormueIfCheckedElseDeleteInOversikt(verdier, VERDI_BOLIG, tittel, verdierFrontend.bolig)
        tittel = textService.getJsonOkonomiTittel(soknadTypeToTitleKey[VERDI_CAMPINGVOGN])
        addFormueIfCheckedElseDeleteInOversikt(verdier, VERDI_CAMPINGVOGN, tittel, verdierFrontend.campingvogn)
        tittel = textService.getJsonOkonomiTittel(soknadTypeToTitleKey[VERDI_KJORETOY])
        addFormueIfCheckedElseDeleteInOversikt(verdier, VERDI_KJORETOY, tittel, verdierFrontend.kjoretoy)
        tittel = textService.getJsonOkonomiTittel(soknadTypeToTitleKey[VERDI_FRITIDSEIENDOM])
        addFormueIfCheckedElseDeleteInOversikt(verdier, VERDI_FRITIDSEIENDOM, tittel, verdierFrontend.fritidseiendom)
        tittel = textService.getJsonOkonomiTittel(soknadTypeToTitleKey[VERDI_ANNET])
        addFormueIfCheckedElseDeleteInOversikt(verdier, VERDI_ANNET, tittel, verdierFrontend.annet)
    }

    private fun setBeskrivelseAvAnnet(opplysninger: JsonOkonomiopplysninger, verdierFrontend: VerdierFrontend) {
        if (opplysninger.beskrivelseAvAnnet == null) {
            opplysninger.withBeskrivelseAvAnnet(
                JsonOkonomibeskrivelserAvAnnet()
                    .withKilde(JsonKildeBruker.BRUKER)
                    .withVerdi("")
                    .withSparing("")
                    .withUtbetaling("")
                    .withBoutgifter("")
                    .withBarneutgifter("")
            )
        }
        opplysninger.beskrivelseAvAnnet.verdi = verdierFrontend.beskrivelseAvAnnet ?: ""
    }

    private fun getBekreftelse(opplysninger: JsonOkonomiopplysninger): Boolean? {
        return opplysninger.bekreftelse.firstOrNull { it.type == BEKREFTELSE_VERDI }?.verdi
    }

    private fun hasVerdiType(oversikt: JsonOkonomioversikt, type: String): Boolean {
        return oversikt.formue.any { it.type == type }
    }

    data class VerdierFrontend(
        var bekreftelse: Boolean? = null,
        var bolig: Boolean = false,
        var campingvogn: Boolean = false,
        var kjoretoy: Boolean = false,
        var fritidseiendom: Boolean = false,
        var annet: Boolean = false,
        var beskrivelseAvAnnet: String? = null
    )
}
