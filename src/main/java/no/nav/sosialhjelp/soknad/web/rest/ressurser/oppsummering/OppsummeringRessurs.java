package no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering;

import no.nav.security.token.support.core.api.ProtectedWithClaims;
import no.nav.sosialhjelp.metrics.aspects.Timed;
import no.nav.sosialhjelp.soknad.business.soknadunderbehandling.SoknadUnderArbeidRepository;
import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeid;
import no.nav.sosialhjelp.soknad.domain.model.oidc.SubjectHandler;
import no.nav.sosialhjelp.soknad.web.sikkerhet.Tilgangskontroll;
import org.springframework.stereotype.Controller;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static no.nav.sosialhjelp.soknad.web.utils.Constants.CLAIM_ACR_LEVEL_4;
import static no.nav.sosialhjelp.soknad.web.utils.Constants.SELVBETJENING;

@Controller
@ProtectedWithClaims(issuer = SELVBETJENING, claimMap = {CLAIM_ACR_LEVEL_4})
@Path("/soknader/{behandlingsId}/oppsummering")
@Timed
@Produces(APPLICATION_JSON)
public class OppsummeringRessurs {

    private final SoknadUnderArbeidRepository soknadUnderArbeidRepository;
    private final Tilgangskontroll tilgangskontroll;

    public OppsummeringRessurs(SoknadUnderArbeidRepository soknadUnderArbeidRepository, Tilgangskontroll tilgangskontroll) {
        this.soknadUnderArbeidRepository = soknadUnderArbeidRepository;
        this.tilgangskontroll = tilgangskontroll;
    }

    @GET
    public Oppsummering getOppsummering(@PathParam("behandlingsId") String behandlingsId) {
        tilgangskontroll.verifiserAtBrukerHarTilgang();
        String eier = SubjectHandler.getUserId();
        SoknadUnderArbeid soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier);

        return new Oppsummering().withSteg(new ArrayList<>(Arrays.asList(
                new Steg("1", "Personopplysninger", true),
                new Steg("2", "Hva søker du om?", true),
                new Steg("3", "Arbeid og utdanning", true),
                new Steg("4", "Familiesituasjon", true),
                new Steg("5", "Bosituasjon", true),
                new Steg("6", "Inntekt og formue", true),
                new Steg("7", "Utgifter og gjeld", true),
                new Steg("8", "Økonomiske opplysninger og vedlegg", true)
        )));
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static final class Oppsummering {
        public List<Steg> steg;

        public Oppsummering withSteg(List<Steg> steg) {
            this.steg = steg;
            return this;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static final class Steg {
        public String steg;
        public String tittel;
        public boolean erUtfylt;

        public Steg(String steg, String tittel, boolean erUtfylt) {
            this.steg = steg;
            this.tittel = tittel;
            this.erUtfylt = erUtfylt;
        }
    }
}


