//package no.nav.sosialhjelp.soknad.web.rest.ressurser.inntekt;
//
//import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
//import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomiopplysninger;
//import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.oversikt.JsonOkonomioversiktInntekt;
//import no.nav.sbl.soknadsosialhjelp.soknad.utdanning.JsonUtdanning;
//import no.nav.security.token.support.core.api.ProtectedWithClaims;
//import no.nav.sosialhjelp.metrics.aspects.Timed;
//import no.nav.sosialhjelp.soknad.business.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository;
//import no.nav.sosialhjelp.soknad.business.service.TextService;
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
//import java.util.ArrayList;
//import java.util.List;
//
//import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
//import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.STUDIELAN;
//import static no.nav.sosialhjelp.soknad.business.mappers.OkonomiMapper.addInntektIfCheckedElseDeleteInOversikt;
//import static no.nav.sosialhjelp.soknad.business.mappers.OkonomiMapper.setBekreftelse;
//import static no.nav.sosialhjelp.soknad.business.mappers.TitleKeyMapper.soknadTypeToTitleKey;
//import static no.nav.sosialhjelp.soknad.web.utils.Constants.CLAIM_ACR_LEVEL_4;
//import static no.nav.sosialhjelp.soknad.web.utils.Constants.SELVBETJENING;
//
//@Controller
//@ProtectedWithClaims(issuer = SELVBETJENING, claimMap = {CLAIM_ACR_LEVEL_4})
//@Path("/soknader/{behandlingsId}/inntekt/studielan")
//@Timed
//@Produces(APPLICATION_JSON)
//public class StudielanRessurs {
//
//    private final Tilgangskontroll tilgangskontroll;
//    private final SoknadUnderArbeidRepository soknadUnderArbeidRepository;
//    private final TextService textService;
//
//    public StudielanRessurs(Tilgangskontroll tilgangskontroll, SoknadUnderArbeidRepository soknadUnderArbeidRepository, TextService textService) {
//        this.tilgangskontroll = tilgangskontroll;
//        this.soknadUnderArbeidRepository = soknadUnderArbeidRepository;
//        this.textService = textService;
//    }
//
//    @GET
//    public StudielanFrontend hentStudielanBekreftelse(@PathParam("behandlingsId") String behandlingsId) {
//        tilgangskontroll.verifiserAtBrukerHarTilgang();
//        String eier = SubjectHandler.getUserId();
//        JsonInternalSoknad soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier).getJsonInternalSoknad();
//        JsonOkonomiopplysninger opplysninger = soknad.getSoknad().getData().getOkonomi().getOpplysninger();
//        JsonUtdanning utdanning = soknad.getSoknad().getData().getUtdanning();
//        StudielanFrontend studielanFrontend = new StudielanFrontend();
//
//        if (utdanning.getErStudent() == null || !utdanning.getErStudent()) {
//            return studielanFrontend;
//        }
//        studielanFrontend.setSkalVises(true);
//
//        if (opplysninger.getBekreftelse() == null) {
//            studielanFrontend.setSkalVises(false);
//            return studielanFrontend;
//        }
//
//        setBekreftelseOnStudielanFrontend(opplysninger, studielanFrontend);
//
//        return studielanFrontend;
//    }
//
//    @PUT
//    public void updateStudielan(@PathParam("behandlingsId") String behandlingsId, StudielanFrontend studielanFrontend) {
//        tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId);
//        String eier = SubjectHandler.getUserId();
//        SoknadUnderArbeid soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier);
//        JsonOkonomiopplysninger opplysninger = soknad.getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOpplysninger();
//        List<JsonOkonomioversiktInntekt> inntekter = soknad.getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOversikt().getInntekt();
//
//        if (opplysninger.getBekreftelse() == null) {
//            opplysninger.setBekreftelse(new ArrayList<>());
//        }
//
//        setBekreftelse(opplysninger, STUDIELAN, studielanFrontend.bekreftelse, textService.getJsonOkonomiTittel("inntekt.student"));
//
//        if (studielanFrontend.bekreftelse != null) {
//            String tittel = textService.getJsonOkonomiTittel(soknadTypeToTitleKey.get(STUDIELAN));
//            addInntektIfCheckedElseDeleteInOversikt(inntekter, STUDIELAN, tittel, studielanFrontend.bekreftelse);
//        }
//
//        soknadUnderArbeidRepository.oppdaterSoknadsdata(soknad, eier);
//    }
//
//    private void setBekreftelseOnStudielanFrontend(JsonOkonomiopplysninger opplysninger, StudielanFrontend studielanFrontend) {
//        opplysninger.getBekreftelse().stream()
//                .filter(bekreftelse -> bekreftelse.getType().equals(STUDIELAN))
//                .findFirst()
//                .ifPresent(jsonOkonomibekreftelse -> studielanFrontend.setBekreftelse(jsonOkonomibekreftelse.getVerdi()));
//    }
//
//    @XmlAccessorType(XmlAccessType.FIELD)
//    public static final class StudielanFrontend {
//        public boolean skalVises;
//        public Boolean bekreftelse;
//
//        public void setSkalVises(boolean skalVises) {
//            this.skalVises = skalVises;
//        }
//
//        public void setBekreftelse(Boolean bekreftelse) {
//            this.bekreftelse = bekreftelse;
//        }
//    }
//}
