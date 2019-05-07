package no.nav.sbl.dialogarena.rest.ressurser.informasjon;

import no.nav.metrics.aspects.Timed;
import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.OidcFeatureToggleUtils;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.AktivitetService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.MaalgrupperService;
import no.nav.security.oidc.api.ProtectedWithClaims;
import org.springframework.stereotype.Controller;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.util.List;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Controller
@ProtectedWithClaims(issuer = "selvbetjening", claimMap = { "acr=Level4" })
@Produces(APPLICATION_JSON)
@Timed
public class TjenesterRessurs {

    @Inject
    private AktivitetService aktivitetService;

    @Inject
    private MaalgrupperService maalgrupperService;

    @GET
    @Path("/aktiviteter")
    public List<Faktum> hentAktiviteter() {
        return aktivitetService.hentAktiviteter(OidcFeatureToggleUtils.getUserId());
    }

    @GET
    @Path("/vedtak")
    public List<Faktum> hentVedtak() {
        return aktivitetService.hentVedtak(OidcFeatureToggleUtils.getUserId());
    }

    @GET
    @Path("/maalgrupper")
    public List<Faktum> hentMaalgrupper() {
        return maalgrupperService.hentMaalgrupper(OidcFeatureToggleUtils.getUserId());
    }
}
