package no.nav.sbl.dialogarena.rest.ressurser;

import no.nav.sbl.dialogarena.sikkerhet.Tilgangskontroll;
import no.nav.sbl.sosialhjelp.SoknadUnderArbeidService;
import no.nav.sbl.sosialhjelp.domain.SoknadUnderArbeid;
import no.nav.sbl.sosialhjelp.soknadunderbehandling.SoknadUnderArbeidRepository;
import no.nav.security.oidc.api.ProtectedWithClaims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;

import javax.inject.Inject;
import javax.ws.rs.*;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static no.nav.modig.core.context.SubjectHandler.getSubjectHandler;
import static no.nav.sbl.dialogarena.sendsoknad.domain.util.FeatureToggler.Toggle.RESSURS_ALTERNATIVREPRESENTASJON;
import static no.nav.sbl.dialogarena.sendsoknad.domain.util.FeatureToggler.erFeatureAktiv;

@Controller
@ProtectedWithClaims(issuer = "selvbetjening", claimMap = { "acr=Level4" })
@Path("/representasjon")
public class AlternativRepresentasjonRessurs {

    @Inject
    private SoknadUnderArbeidService soknadUnderArbeidService;
    @Inject
    private SoknadUnderArbeidRepository soknadUnderArbeidRepository;
    @Inject
    private Tilgangskontroll tilgangskontroll;
    private static final Logger LOG = LoggerFactory.getLogger(AlternativRepresentasjonRessurs.class);

    @Deprecated
    @GET
    @Path("/json/{behandlingsId}")
    @Produces(APPLICATION_JSON)
    public byte[] jsonRepresentasjon(@PathParam("behandlingsId") String behandlingsId) {
        tilgangskontroll.verifiserBrukerHarTilgangTilSoknad(behandlingsId);
        erRessursAktiv("jsonRepresentasjon");
        String eier = getSubjectHandler().getUid();
        SoknadUnderArbeid soknadUnderArbeid = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier).get();
        return  soknadUnderArbeidService.mapJsonSoknadTilFil(soknadUnderArbeid.getJsonInternalSoknad().getSoknad());
    }

    private void erRessursAktiv(String metode) {
        LOG.warn("OppsummeringRessurs metode {} forsøkt aksessert", metode);
        if (!erFeatureAktiv(RESSURS_ALTERNATIVREPRESENTASJON)) {
            throw new NotFoundException("Denne informasjonen er ikke tilgjengelig");
        }
    }


}
