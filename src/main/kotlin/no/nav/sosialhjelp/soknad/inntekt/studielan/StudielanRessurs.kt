package no.nav.sosialhjelp.soknad.inntekt.studielan

import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.STUDIELAN
import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.sosialhjelp.metrics.aspects.Timed
import no.nav.sosialhjelp.soknad.business.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.business.service.TextService
import no.nav.sosialhjelp.soknad.common.Constants
import no.nav.sosialhjelp.soknad.common.mapper.OkonomiMapper
import no.nav.sosialhjelp.soknad.common.mapper.TitleKeyMapper
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
@Path("/soknader/{behandlingsId}/inntekt/studielan")
@Timed
@Produces(MediaType.APPLICATION_JSON)
open class StudielanRessurs(
    private val tilgangskontroll: Tilgangskontroll,
    private val soknadUnderArbeidRepository: SoknadUnderArbeidRepository,
    private val textService: TextService
) {
    @GET
    open fun hentStudielanBekreftelse(@PathParam("behandlingsId") behandlingsId: String): StudielanFrontend {
        tilgangskontroll.verifiserAtBrukerHarTilgang()
        val eier = SubjectHandlerUtils.getUserIdFromToken()
        val soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier).jsonInternalSoknad
        val opplysninger = soknad.soknad.data.okonomi.opplysninger
        val utdanning = soknad.soknad.data.utdanning

        if (utdanning.erStudent == null || !utdanning.erStudent) {
            return StudielanFrontend(false, null)
        }
        if (opplysninger.bekreftelse == null) {
            return StudielanFrontend(false, null)
        }

        val bekreftelse = opplysninger.bekreftelse
            .firstOrNull { it.type == STUDIELAN }
            ?.verdi

        return StudielanFrontend(true, bekreftelse)
    }

    @PUT
    open fun updateStudielan(@PathParam("behandlingsId") behandlingsId: String, studielanFrontend: StudielanFrontend) {
        tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId)
        val eier = SubjectHandlerUtils.getUserIdFromToken()
        val soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier)
        val opplysninger = soknad.jsonInternalSoknad.soknad.data.okonomi.opplysninger
        val inntekter = soknad.jsonInternalSoknad.soknad.data.okonomi.oversikt.inntekt
        if (opplysninger.bekreftelse == null) {
            opplysninger.bekreftelse = ArrayList()
        }
        OkonomiMapper.setBekreftelse(
            opplysninger,
            STUDIELAN,
            studielanFrontend.bekreftelse,
            textService.getJsonOkonomiTittel("inntekt.student")
        )
        if (studielanFrontend.bekreftelse != null) {
            val tittel = textService.getJsonOkonomiTittel(TitleKeyMapper.soknadTypeToTitleKey[STUDIELAN])
            OkonomiMapper.addInntektIfCheckedElseDeleteInOversikt(
                inntekter,
                STUDIELAN,
                tittel,
                studielanFrontend.bekreftelse
            )
        }
        soknadUnderArbeidRepository.oppdaterSoknadsdata(soknad, eier)
    }

    data class StudielanFrontend(
        val skalVises: Boolean,
        val bekreftelse: Boolean?
    )
}
