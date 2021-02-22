package no.nav.sosialhjelp.soknad.web.rest.ressurser.inntekt;

import no.nav.metrics.aspects.Timed;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtbetaling;
import no.nav.security.token.support.core.api.ProtectedWithClaims;
import no.nav.sosialhjelp.soknad.business.soknadunderbehandling.SoknadUnderArbeidRepository;
import no.nav.sosialhjelp.soknad.domain.model.oidc.SubjectHandler;
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
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTBETALING_NAVYTELSE;

@Controller
@ProtectedWithClaims(issuer = "selvbetjening", claimMap = { "acr=Level4" })
@Path("/soknader/{behandlingsId}/inntekt/systemdata")
@Timed
@Produces(APPLICATION_JSON)
public class SystemregistrertInntektRessurs {

    @Inject
    private SoknadUnderArbeidRepository soknadUnderArbeidRepository;

    @GET
    public SysteminntekterFrontend hentSystemregistrerteInntekter(@PathParam("behandlingsId") String behandlingsId){
        String eier = SubjectHandler.getUserId();
        JsonInternalSoknad soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier).getJsonInternalSoknad();
        List<JsonOkonomiOpplysningUtbetaling> utbetalinger = soknad.getSoknad().getData().getOkonomi().getOpplysninger().getUtbetaling();

        return new SysteminntekterFrontend().withSysteminntekter(utbetalinger.stream()
                .filter(utbetaling -> utbetaling.getType().equals(UTBETALING_NAVYTELSE))
                .map(this::mapToUtbetalingFrontend).collect(Collectors.toList()))
                .withUtbetalingerFraNavFeilet(soknad.getSoknad().getDriftsinformasjon().getUtbetalingerFraNavFeilet());
    }

    private SysteminntektFrontend mapToUtbetalingFrontend(JsonOkonomiOpplysningUtbetaling utbetaling) {
        return new SysteminntektFrontend().withInntektType(utbetaling.getTittel())
                .withBelop(utbetaling.getNetto())
                .withUtbetalingsdato(utbetaling.getUtbetalingsdato());
    }

    @SuppressWarnings("WeakerAccess")
    @XmlAccessorType(XmlAccessType.FIELD)
    public static final class SysteminntekterFrontend {
        public List<SysteminntektFrontend> systeminntekter;
        public Boolean utbetalingerFraNavFeilet;

        public SysteminntekterFrontend withSysteminntekter(List<SysteminntektFrontend> systeminntekter) {
            this.systeminntekter = systeminntekter;
            return this;
        }

        public SysteminntekterFrontend withUtbetalingerFraNavFeilet(Boolean utbetalingerFraNavFeilet) {
            this.utbetalingerFraNavFeilet = utbetalingerFraNavFeilet;
            return this;
        }
    }

    @SuppressWarnings("WeakerAccess")
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
