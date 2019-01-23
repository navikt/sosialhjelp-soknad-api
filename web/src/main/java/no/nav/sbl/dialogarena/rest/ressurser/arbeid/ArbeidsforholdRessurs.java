package no.nav.sbl.dialogarena.rest.ressurser.arbeid;

import no.nav.metrics.aspects.Timed;
import no.nav.modig.core.context.SubjectHandler;
import no.nav.sbl.dialogarena.rest.ressurser.LegacyHelper;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.systemdata.ArbeidsforholdSystemdata;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.arbeid.JsonArbeid;
import no.nav.sbl.soknadsosialhjelp.soknad.arbeid.JsonArbeidsforhold;
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
@Path("/soknader/{behandlingsId}/arbeid/arbeidsforhold")
@Timed
@Produces(APPLICATION_JSON)
public class ArbeidsforholdRessurs {

    @Inject
    private LegacyHelper legacyHelper;

    @Inject
    private ArbeidsforholdSystemdata arbeidsforholdSystemdata;

    @GET
    public List<ArbeidsforholdFrontend> hentArbeidsforhold(@PathParam("behandlingsId") String behandlingsId) {
        final String eier = SubjectHandler.getSubjectHandler().getUid();
        final JsonInternalSoknad soknad = legacyHelper.hentSoknad(behandlingsId, eier).getJsonInternalSoknad();
        final JsonArbeid arbeid = soknad.getSoknad().getData().getArbeid();

        if (arbeid.getForhold() == null){
            return null;
        }

        return arbeid.getForhold().stream()
                .map(this::mapToArbeidsforholdFrontend)
                .collect(Collectors.toList());
    }

    private ArbeidsforholdFrontend mapToArbeidsforholdFrontend(JsonArbeidsforhold arbeidsforhold) {
        return new ArbeidsforholdFrontend()
                .withArbeidsgivernavn(arbeidsforhold.getArbeidsgivernavn())
                .withFom(arbeidsforhold.getFom())
                .withTom(arbeidsforhold.getTom())
                .withStillingstypeErHeltid(tilStillingstypeErHeltid(arbeidsforhold.getStillingstype()))
                .withStillingsprosent(arbeidsforhold.getStillingsprosent())
                .withOverstyrtAvBruker(Boolean.FALSE);
    }

    private static Boolean tilStillingstypeErHeltid(JsonArbeidsforhold.Stillingstype stillingstype) {
        if (stillingstype == null){
            return null;
        }
        return stillingstype == JsonArbeidsforhold.Stillingstype.FAST ? Boolean.TRUE: Boolean.FALSE;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static final class ArbeidsforholdFrontend {
        public String arbeidsgivernavn;
        public String fom;
        public String tom;
        public Boolean stillingstypeErHeltid;
        public Integer stillingsprosent;
        public Boolean overstyrtAvBruker;

        public ArbeidsforholdFrontend withArbeidsgivernavn(String arbeidsgivernavn) {
            this.arbeidsgivernavn = arbeidsgivernavn;
            return this;
        }

        public ArbeidsforholdFrontend withFom(String fom) {
            this.fom = fom;
            return this;
        }

        public ArbeidsforholdFrontend withTom(String tom) {
            this.tom = tom;
            return this;
        }

        public ArbeidsforholdFrontend withStillingstypeErHeltid(Boolean stillingstypeErHeltid) {
            this.stillingstypeErHeltid = stillingstypeErHeltid;
            return this;
        }

        public ArbeidsforholdFrontend withStillingsprosent(Integer stillingsprosent) {
            this.stillingsprosent = stillingsprosent;
            return this;
        }

        public  ArbeidsforholdFrontend withOverstyrtAvBruker(Boolean overstyrtAvBruker){
            this.overstyrtAvBruker = overstyrtAvBruker;
            return this;
        }
    }
}
