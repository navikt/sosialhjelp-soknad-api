package no.nav.sbl.dialogarena.rest.ressurser.inntekt;

import no.nav.metrics.aspects.Timed;
import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.OidcFeatureToggleUtils;
import no.nav.sbl.dialogarena.rest.ressurser.LegacyHelper;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtbetaling;
import no.nav.security.oidc.api.ProtectedWithClaims;
import org.springframework.stereotype.Controller;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import java.util.List;
import java.util.stream.Collectors;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Controller
@ProtectedWithClaims(issuer = "selvbetjening", claimMap = { "acr=Level4" })
@Path("/soknader/{behandlingsId}/inntekt/systemdata")
@Timed
@Produces(APPLICATION_JSON)
public class SystemregistrertInntektRessurs {

    @Inject
    private LegacyHelper legacyHelper;

    @GET
    public SysteminntekterFrontend hentSystemregistrerteInntekter(@PathParam("behandlingsId") String behandlingsId){
        final String eier = OidcFeatureToggleUtils.getUserId();
        final JsonInternalSoknad soknad = legacyHelper.hentSoknad(behandlingsId, eier, false).getJsonInternalSoknad();
        final List<JsonOkonomiOpplysningUtbetaling> utbetalinger = soknad.getSoknad().getData().getOkonomi().getOpplysninger().getUtbetaling();

        return new SysteminntekterFrontend().withSysteminntekter(utbetalinger.stream()
                        .filter(utbetaling -> utbetaling.getType().equals("navytelse"))
                        .map(this::mapToUtbetalingFrontend).collect(Collectors.toList()));
    }

    private SysteminntektFrontend mapToUtbetalingFrontend(JsonOkonomiOpplysningUtbetaling utbetaling) {
        return new SysteminntektFrontend().withInntektType(utbetaling.getTittel())
                .withBelop(utbetaling.getNetto())
                .withUtbetalingsdato(utbetaling.getUtbetalingsdato());
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static final class SysteminntekterFrontend {
        public List<SysteminntektFrontend> systeminntekter;

        public SysteminntekterFrontend withSysteminntekter(List<SysteminntektFrontend> systeminntekter) {
            this.systeminntekter = systeminntekter;
            return this;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static final class SysteminntektFrontend {
        public String inntektType;
        public String utbetalingsdato;
        public Double belop;

        public SysteminntektFrontend withInntektType(String inntektType) {
            this.inntektType = inntektType;
            return this;
        }

        public SysteminntektFrontend withUtbetalingsdato(String utbetalingsdato) {
            this.utbetalingsdato = utbetalingsdato;
            return this;
        }

        public SysteminntektFrontend withBelop(Double belop) {
            this.belop = belop;
            return this;
        }
    }
}
