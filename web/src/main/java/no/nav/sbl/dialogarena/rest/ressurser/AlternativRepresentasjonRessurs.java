package no.nav.sbl.dialogarena.rest.ressurser;

import no.nav.sbl.dialogarena.sendsoknad.domain.AlternativRepresentasjon;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.soknadsosialhjelp.message.NavMessageSource;
import no.nav.sbl.dialogarena.sendsoknad.domain.transformer.AlternativRepresentasjonType;
import no.nav.sbl.dialogarena.sikkerhet.SjekkTilgangTilSoknad;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.AlternativRepresentasjonService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadDataFletter;
import no.nav.security.oidc.api.ProtectedWithClaims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;

import javax.inject.Inject;
import javax.ws.rs.*;
import java.io.IOException;
import java.util.List;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.TEXT_XML;
import static no.nav.sbl.dialogarena.sendsoknad.domain.util.FeatureToggler.Toggle.RESSURS_ALTERNATIVREPRESENTASJON;
import static no.nav.sbl.dialogarena.sendsoknad.domain.util.FeatureToggler.erFeatureAktiv;

@Controller
@ProtectedWithClaims(issuer = "selvbetjening", claimMap = { "acr=Level4" })
@Path("/representasjon")
public class AlternativRepresentasjonRessurs {

    @Inject
    private AlternativRepresentasjonService alternativRepresentasjonService;
    @Inject
    private NavMessageSource messageSource;
    @Inject
    private SoknadDataFletter soknadDataFletter;
    private static final Logger LOG = LoggerFactory.getLogger(AlternativRepresentasjonRessurs.class);

    @Deprecated
    @GET
    @Path("/xml/{behandlingsId}")
    @Produces(TEXT_XML)
    @SjekkTilgangTilSoknad
    public byte[] xmlRepresentasjon(@PathParam("behandlingsId") String behandlingsId) throws IOException {
        erRessursAktiv("xmlRepresentasjon");
        WebSoknad soknad = soknadDataFletter.hentSoknad(behandlingsId, true, true, false);
        List<AlternativRepresentasjon> representasjoner = alternativRepresentasjonService.hentAlternativeRepresentasjoner(soknad, messageSource);

        return representasjoner.stream()
                .filter(r -> r.getRepresentasjonsType().equals(AlternativRepresentasjonType.XML))
                .findFirst()
                .map(AlternativRepresentasjon::getContent)
                .orElseThrow(() -> new NotFoundException(String.format("Ingen alternativ representasjon for [%s] funnet (%s)", behandlingsId, soknad.getSoknadPrefix())));
    }

    @Deprecated
    @GET
    @Path("/json/{behandlingsId}")
    @Produces(APPLICATION_JSON)
    @SjekkTilgangTilSoknad
    public byte[] jsonRepresentasjon(@PathParam("behandlingsId") String behandlingsId) throws IOException {
        erRessursAktiv("jsonRepresentasjon");
        WebSoknad soknad = soknadDataFletter.hentSoknad(behandlingsId, true, true, false);
        List<AlternativRepresentasjon> representasjoner = alternativRepresentasjonService.hentAlternativeRepresentasjoner(soknad, messageSource);

        return representasjoner.stream()
                .filter(r -> r.getRepresentasjonsType().equals(AlternativRepresentasjonType.JSON))
                .filter(r -> !r.getFilnavn().contains("vedlegg"))
                .findFirst()
                .map(AlternativRepresentasjon::getContent)
                .orElseThrow(() -> new NotFoundException(String.format("Ingen alternativ representasjon for [%s] funnet (%s)", behandlingsId, soknad.getSoknadPrefix())));
    }

    private void erRessursAktiv(String metode) {
        LOG.warn("OppsummeringRessurs metode {} forsøkt aksessert", metode);
        if (!erFeatureAktiv(RESSURS_ALTERNATIVREPRESENTASJON)) {
            throw new NotFoundException("Denne informasjonen er ikke tilgjengelig");
        }
    }


}
