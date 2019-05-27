package no.nav.sbl.dialogarena.rest.ressurser.arbeid;

import no.nav.metrics.aspects.Timed;
import no.nav.sbl.dialogarena.rest.ressurser.LegacyHelper;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.OidcFeatureToggleUtils;
import no.nav.sbl.dialogarena.sikkerhet.Tilgangskontroll;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.FaktaService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadService;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.arbeid.JsonArbeid;
import no.nav.sbl.soknadsosialhjelp.soknad.arbeid.JsonArbeidsforhold;
import no.nav.sbl.soknadsosialhjelp.soknad.arbeid.JsonKommentarTilArbeidsforhold;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKildeBruker;
import no.nav.sbl.sosialhjelp.domain.SoknadUnderArbeid;
import no.nav.sbl.sosialhjelp.soknadunderbehandling.SoknadUnderArbeidRepository;
import no.nav.security.oidc.api.ProtectedWithClaims;
import org.springframework.stereotype.Controller;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import java.util.List;
import java.util.stream.Collectors;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp.json.JsonUtils.erIkkeTom;

@Controller
@ProtectedWithClaims(issuer = "selvbetjening", claimMap = { "acr=Level4" })
@Path("/soknader/{behandlingsId}/arbeid")
@Timed
@Produces(APPLICATION_JSON)
public class ArbeidRessurs {

    @Inject
    private LegacyHelper legacyHelper;

    @Inject
    private SoknadUnderArbeidRepository soknadUnderArbeidRepository;

    @Inject
    private SoknadService soknadService;

    @Inject
    private Tilgangskontroll tilgangskontroll;

    @Inject
    private FaktaService faktaService;

    @GET
    public ArbeidFrontend hentArbeid(@PathParam("behandlingsId") String behandlingsId) {
        final String eier = OidcFeatureToggleUtils.getUserId();
        final JsonInternalSoknad soknad = legacyHelper.hentSoknad(behandlingsId, eier, false).getJsonInternalSoknad();
        final JsonArbeid arbeid = soknad.getSoknad().getData().getArbeid();
        final JsonKommentarTilArbeidsforhold kommentarTilArbeidsforhold = soknad.getSoknad().getData().getArbeid().getKommentarTilArbeidsforhold();

        final List<ArbeidsforholdFrontend> forhold;
        if (arbeid.getForhold() != null){
            forhold = arbeid.getForhold().stream()
                    .map(this::mapToArbeidsforholdFrontend)
                    .collect(Collectors.toList());
        } else {
            forhold = null;
        }

        return new ArbeidFrontend().withArbeidsforhold(forhold)
                .withKommentarTilArbeidsforhold(kommentarTilArbeidsforhold != null ? kommentarTilArbeidsforhold.getVerdi() : null);
    }

    @PUT
    public void updateArbeid(@PathParam("behandlingsId") String behandlingsId, ArbeidFrontend arbeidFrontend) {
        tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId);
        update(behandlingsId, arbeidFrontend);
        legacyUpdate(behandlingsId, arbeidFrontend);
    }

    private void update(String behandlingsId, ArbeidFrontend arbeidFrontend) {
        final String eier = OidcFeatureToggleUtils.getUserId();
        final SoknadUnderArbeid soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier).get();
        final JsonArbeid arbeid = soknad.getJsonInternalSoknad().getSoknad().getData().getArbeid();
        if (erIkkeTom(arbeidFrontend.kommentarTilArbeidsforhold)){
            arbeid.setKommentarTilArbeidsforhold(new JsonKommentarTilArbeidsforhold()
                    .withKilde(JsonKildeBruker.BRUKER)
                    .withVerdi(arbeidFrontend.kommentarTilArbeidsforhold));
        } else {
            arbeid.setKommentarTilArbeidsforhold(null);
        }
        soknadUnderArbeidRepository.oppdaterSoknadsdata(soknad, eier);
    }

    private void legacyUpdate(String behandlingsId, ArbeidFrontend arbeidFrontend) {
        final WebSoknad webSoknad = soknadService.hentSoknad(behandlingsId, false, false);

        final Faktum kommentarTilArbeidsforhold = faktaService.hentFaktumMedKey(webSoknad.getSoknadId(), "opplysninger.arbeidsituasjon.kommentarer");
        kommentarTilArbeidsforhold.setValue(arbeidFrontend.kommentarTilArbeidsforhold);
        faktaService.lagreBrukerFaktum(kommentarTilArbeidsforhold);
    }

    private ArbeidsforholdFrontend mapToArbeidsforholdFrontend(JsonArbeidsforhold arbeidsforhold) {
        return new ArbeidsforholdFrontend()
                .withArbeidsgivernavn(arbeidsforhold.getArbeidsgivernavn())
                .withFom(arbeidsforhold.getFom())
                .withTom(arbeidsforhold.getTom())
                .withStillingstypeErHeltid(isStillingstypeErHeltid(arbeidsforhold.getStillingstype()))
                .withStillingsprosent(arbeidsforhold.getStillingsprosent())
                .withOverstyrtAvBruker(Boolean.FALSE);
    }

    private static Boolean isStillingstypeErHeltid(JsonArbeidsforhold.Stillingstype stillingstype) {
        if (stillingstype == null){
            return null;
        }
        return stillingstype == JsonArbeidsforhold.Stillingstype.FAST ? Boolean.TRUE: Boolean.FALSE;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static final class ArbeidFrontend {
        public List<ArbeidsforholdFrontend> arbeidsforhold;
        public String kommentarTilArbeidsforhold;

        public ArbeidFrontend withArbeidsforhold(List<ArbeidsforholdFrontend> arbeidsforhold){
            this.arbeidsforhold = arbeidsforhold;
            return this;
        }

        public ArbeidFrontend withKommentarTilArbeidsforhold(String kommentarTilArbeidsforhold) {
            this.kommentarTilArbeidsforhold = kommentarTilArbeidsforhold;
            return this;
        }
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
