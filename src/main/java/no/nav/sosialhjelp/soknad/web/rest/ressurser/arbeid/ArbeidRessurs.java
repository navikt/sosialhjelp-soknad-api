//package no.nav.sosialhjelp.soknad.web.rest.ressurser.arbeid;
//
//import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
//import no.nav.sbl.soknadsosialhjelp.soknad.arbeid.JsonArbeid;
//import no.nav.sbl.soknadsosialhjelp.soknad.arbeid.JsonArbeidsforhold;
//import no.nav.sbl.soknadsosialhjelp.soknad.arbeid.JsonKommentarTilArbeidsforhold;
//import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKildeBruker;
//import no.nav.security.token.support.core.api.ProtectedWithClaims;
//import no.nav.sosialhjelp.metrics.aspects.Timed;
//import no.nav.sosialhjelp.soknad.business.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository;
//import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeid;
//import no.nav.sosialhjelp.soknad.domain.model.oidc.SubjectHandler;
//import no.nav.sosialhjelp.soknad.web.sikkerhet.Tilgangskontroll;
//import org.springframework.stereotype.Controller;
//
//import javax.ws.rs.GET;
//import javax.ws.rs.PUT;
//import javax.ws.rs.Path;
//import javax.ws.rs.PathParam;
//import javax.ws.rs.Produces;
//import javax.xml.bind.annotation.XmlAccessType;
//import javax.xml.bind.annotation.XmlAccessorType;
//import java.util.List;
//import java.util.stream.Collectors;
//
//import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
//import static no.nav.sosialhjelp.soknad.web.utils.Constants.CLAIM_ACR_LEVEL_4;
//import static no.nav.sosialhjelp.soknad.web.utils.Constants.SELVBETJENING;
//import static org.apache.commons.lang3.StringUtils.isBlank;
//
//@Controller
//@ProtectedWithClaims(issuer = SELVBETJENING, claimMap = {CLAIM_ACR_LEVEL_4})
//@Path("/soknader/{behandlingsId}/arbeid")
//@Timed
//@Produces(APPLICATION_JSON)
//public class ArbeidRessurs {
//
//    private final SoknadUnderArbeidRepository soknadUnderArbeidRepository;
//    private final Tilgangskontroll tilgangskontroll;
//
//    public ArbeidRessurs(SoknadUnderArbeidRepository soknadUnderArbeidRepository, Tilgangskontroll tilgangskontroll) {
//        this.soknadUnderArbeidRepository = soknadUnderArbeidRepository;
//        this.tilgangskontroll = tilgangskontroll;
//    }
//
//    @GET
//    public ArbeidFrontend hentArbeid(@PathParam("behandlingsId") String behandlingsId) {
//        tilgangskontroll.verifiserAtBrukerHarTilgang();
//        String eier = SubjectHandler.getUserId();
//        JsonInternalSoknad soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier).getJsonInternalSoknad();
//        JsonArbeid arbeid = soknad.getSoknad().getData().getArbeid();
//        JsonKommentarTilArbeidsforhold kommentarTilArbeidsforhold = soknad.getSoknad().getData().getArbeid().getKommentarTilArbeidsforhold();
//
//        List<ArbeidsforholdFrontend> forhold;
//        if (arbeid.getForhold() != null){
//            forhold = arbeid.getForhold().stream()
//                    .map(this::mapToArbeidsforholdFrontend)
//                    .collect(Collectors.toList());
//        } else {
//            forhold = null;
//        }
//
//        return new ArbeidFrontend().withArbeidsforhold(forhold)
//                .withKommentarTilArbeidsforhold(kommentarTilArbeidsforhold != null ? kommentarTilArbeidsforhold.getVerdi() : null);
//    }
//
//    @PUT
//    public void updateArbeid(@PathParam("behandlingsId") String behandlingsId, ArbeidFrontend arbeidFrontend) {
//        tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId);
//        String eier = SubjectHandler.getUserId();
//        SoknadUnderArbeid soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier);
//        JsonArbeid arbeid = soknad.getJsonInternalSoknad().getSoknad().getData().getArbeid();
//        if (!isBlank(arbeidFrontend.kommentarTilArbeidsforhold)){
//            arbeid.setKommentarTilArbeidsforhold(new JsonKommentarTilArbeidsforhold()
//                    .withKilde(JsonKildeBruker.BRUKER)
//                    .withVerdi(arbeidFrontend.kommentarTilArbeidsforhold));
//        } else {
//            arbeid.setKommentarTilArbeidsforhold(null);
//        }
//        soknadUnderArbeidRepository.oppdaterSoknadsdata(soknad, eier);
//    }
//
//    private ArbeidsforholdFrontend mapToArbeidsforholdFrontend(JsonArbeidsforhold arbeidsforhold) {
//        return new ArbeidsforholdFrontend()
//                .withArbeidsgivernavn(arbeidsforhold.getArbeidsgivernavn())
//                .withFom(arbeidsforhold.getFom())
//                .withTom(arbeidsforhold.getTom())
//                .withStillingstypeErHeltid(isStillingstypeErHeltid(arbeidsforhold.getStillingstype()))
//                .withStillingsprosent(arbeidsforhold.getStillingsprosent())
//                .withOverstyrtAvBruker(Boolean.FALSE);
//    }
//
//    private static Boolean isStillingstypeErHeltid(JsonArbeidsforhold.Stillingstype stillingstype) {
//        if (stillingstype == null){
//            return null;
//        }
//        return stillingstype == JsonArbeidsforhold.Stillingstype.FAST ? Boolean.TRUE: Boolean.FALSE;
//    }
//
//    @XmlAccessorType(XmlAccessType.FIELD)
//    public static final class ArbeidFrontend {
//        public List<ArbeidsforholdFrontend> arbeidsforhold;
//        public String kommentarTilArbeidsforhold;
//
//        public ArbeidFrontend withArbeidsforhold(List<ArbeidsforholdFrontend> arbeidsforhold){
//            this.arbeidsforhold = arbeidsforhold;
//            return this;
//        }
//
//        public ArbeidFrontend withKommentarTilArbeidsforhold(String kommentarTilArbeidsforhold) {
//            this.kommentarTilArbeidsforhold = kommentarTilArbeidsforhold;
//            return this;
//        }
//    }
//
//    @XmlAccessorType(XmlAccessType.FIELD)
//    public static final class ArbeidsforholdFrontend {
//        public String arbeidsgivernavn;
//        public String fom;
//        public String tom;
//        public Boolean stillingstypeErHeltid;
//        public Integer stillingsprosent;
//        public Boolean overstyrtAvBruker;
//
//        public ArbeidsforholdFrontend withArbeidsgivernavn(String arbeidsgivernavn) {
//            this.arbeidsgivernavn = arbeidsgivernavn;
//            return this;
//        }
//
//        public ArbeidsforholdFrontend withFom(String fom) {
//            this.fom = fom;
//            return this;
//        }
//
//        public ArbeidsforholdFrontend withTom(String tom) {
//            this.tom = tom;
//            return this;
//        }
//
//        public ArbeidsforholdFrontend withStillingstypeErHeltid(Boolean stillingstypeErHeltid) {
//            this.stillingstypeErHeltid = stillingstypeErHeltid;
//            return this;
//        }
//
//        public ArbeidsforholdFrontend withStillingsprosent(Integer stillingsprosent) {
//            this.stillingsprosent = stillingsprosent;
//            return this;
//        }
//
//        public  ArbeidsforholdFrontend withOverstyrtAvBruker(Boolean overstyrtAvBruker){
//            this.overstyrtAvBruker = overstyrtAvBruker;
//            return this;
//        }
//    }
//}
