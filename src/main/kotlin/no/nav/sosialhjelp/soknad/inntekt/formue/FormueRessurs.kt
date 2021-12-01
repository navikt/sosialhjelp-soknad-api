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
import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.sosialhjelp.metrics.aspects.Timed
import no.nav.sosialhjelp.soknad.business.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.business.mappers.OkonomiMapper.addFormueIfCheckedElseDeleteInOversikt
import no.nav.sosialhjelp.soknad.business.mappers.OkonomiMapper.setBekreftelse
import no.nav.sosialhjelp.soknad.business.mappers.TitleKeyMapper.soknadTypeToTitleKey
import no.nav.sosialhjelp.soknad.business.service.TextService
import no.nav.sosialhjelp.soknad.domain.model.oidc.SubjectHandler
import no.nav.sosialhjelp.soknad.web.sikkerhet.Tilgangskontroll
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
@Path("/soknader/{behandlingsId}/inntekt/formue")
@Timed
@Produces(MediaType.APPLICATION_JSON)
open class FormueRessurs(
    private val tilgangskontroll: Tilgangskontroll,
    private val soknadUnderArbeidRepository: SoknadUnderArbeidRepository,
    private val textService: TextService
) {
    @GET
    open fun hentFormue(@PathParam("behandlingsId") behandlingsId: String): FormueFrontend {
        tilgangskontroll.verifiserAtBrukerHarTilgang()
        val eier = SubjectHandler.getUserId()
        val soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier).jsonInternalSoknad
        val okonomi = soknad.soknad.data.okonomi

        if (okonomi.opplysninger.bekreftelse == null) {
            return FormueFrontend(beskrivelseAvAnnet = null)
        }

        return FormueFrontend(
            brukskonto = hasFormueType(okonomi.oversikt, FORMUE_BRUKSKONTO),
            sparekonto = hasFormueType(okonomi.oversikt, FORMUE_SPAREKONTO),
            bsu = hasFormueType(okonomi.oversikt, FORMUE_BSU),
            livsforsikring = hasFormueType(okonomi.oversikt, FORMUE_LIVSFORSIKRING),
            verdipapirer = hasFormueType(okonomi.oversikt, FORMUE_VERDIPAPIRER),
            annet = hasFormueType(okonomi.oversikt, FORMUE_ANNET),
            beskrivelseAvAnnet = okonomi.opplysninger.beskrivelseAvAnnet?.sparing
        )
    }

    @PUT
    open fun updateFormue(@PathParam("behandlingsId") behandlingsId: String, formueFrontend: FormueFrontend) {
        tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId)
        val eier = SubjectHandler.getUserId()
        val soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier)
        val okonomi = soknad.jsonInternalSoknad.soknad.data.okonomi
        if (okonomi.opplysninger.bekreftelse == null) {
            okonomi.opplysninger.bekreftelse = ArrayList()
        }
        val hasAnyFormueType = formueFrontend.brukskonto || formueFrontend.bsu || formueFrontend.sparekonto ||
            formueFrontend.livsforsikring || formueFrontend.verdipapirer || formueFrontend.annet
        setBekreftelse(
            okonomi.opplysninger,
            BEKREFTELSE_SPARING,
            hasAnyFormueType,
            textService.getJsonOkonomiTittel("inntekt.bankinnskudd")
        )
        setFormue(okonomi.oversikt, formueFrontend)
        setBeskrivelseAvAnnet(okonomi.opplysninger, formueFrontend)
        soknadUnderArbeidRepository.oppdaterSoknadsdata(soknad, eier)
    }

    private fun setFormue(oversikt: JsonOkonomioversikt, formueFrontend: FormueFrontend) {
        val formue = oversikt.formue

        var tittel = textService.getJsonOkonomiTittel(soknadTypeToTitleKey[FORMUE_BRUKSKONTO])
        addFormueIfCheckedElseDeleteInOversikt(formue, FORMUE_BRUKSKONTO, tittel, formueFrontend.brukskonto)

        tittel = textService.getJsonOkonomiTittel(soknadTypeToTitleKey[FORMUE_BSU])
        addFormueIfCheckedElseDeleteInOversikt(formue, FORMUE_BSU, tittel, formueFrontend.bsu)

        tittel = textService.getJsonOkonomiTittel(soknadTypeToTitleKey[FORMUE_LIVSFORSIKRING])
        addFormueIfCheckedElseDeleteInOversikt(formue, FORMUE_LIVSFORSIKRING, tittel, formueFrontend.livsforsikring)

        tittel = textService.getJsonOkonomiTittel(soknadTypeToTitleKey[FORMUE_SPAREKONTO])
        addFormueIfCheckedElseDeleteInOversikt(formue, FORMUE_SPAREKONTO, tittel, formueFrontend.sparekonto)

        tittel = textService.getJsonOkonomiTittel(soknadTypeToTitleKey[FORMUE_VERDIPAPIRER])
        addFormueIfCheckedElseDeleteInOversikt(formue, FORMUE_VERDIPAPIRER, tittel, formueFrontend.verdipapirer)

        tittel = textService.getJsonOkonomiTittel(soknadTypeToTitleKey[FORMUE_ANNET])
        addFormueIfCheckedElseDeleteInOversikt(formue, FORMUE_ANNET, tittel, formueFrontend.annet)
    }

    private fun setBeskrivelseAvAnnet(opplysninger: JsonOkonomiopplysninger, formueFrontend: FormueFrontend) {
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
        opplysninger.beskrivelseAvAnnet.sparing = formueFrontend.beskrivelseAvAnnet ?: ""
    }

    private fun hasFormueType(oversikt: JsonOkonomioversikt, type: String): Boolean {
        return oversikt.formue.any { it.type == type }
    }

    data class FormueFrontend(
        val brukskonto: Boolean = false,
        val sparekonto: Boolean = false,
        val bsu: Boolean = false,
        val livsforsikring: Boolean = false,
        val verdipapirer: Boolean = false,
        val annet: Boolean = false,
        val beskrivelseAvAnnet: String?
    )
}
