//package no.nav.sosialhjelp.soknad.web.rest.ressurser.inntekt;
//
//import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
//import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKildeBruker;
//import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomi;
//import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomiopplysninger;
//import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomioversikt;
//import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomibeskrivelserAvAnnet;
//import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.oversikt.JsonOkonomioversiktFormue;
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
//import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.BEKREFTELSE_VERDI;
//import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.VERDI_ANNET;
//import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.VERDI_BOLIG;
//import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.VERDI_CAMPINGVOGN;
//import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.VERDI_FRITIDSEIENDOM;
//import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.VERDI_KJORETOY;
//import static no.nav.sosialhjelp.soknad.business.mappers.OkonomiMapper.addFormueIfCheckedElseDeleteInOversikt;
//import static no.nav.sosialhjelp.soknad.business.mappers.OkonomiMapper.setBekreftelse;
//import static no.nav.sosialhjelp.soknad.business.mappers.TitleKeyMapper.soknadTypeToTitleKey;
//import static no.nav.sosialhjelp.soknad.web.utils.Constants.CLAIM_ACR_LEVEL_4;
//import static no.nav.sosialhjelp.soknad.web.utils.Constants.SELVBETJENING;
//
//@Controller
//@ProtectedWithClaims(issuer = SELVBETJENING, claimMap = {CLAIM_ACR_LEVEL_4})
//@Path("/soknader/{behandlingsId}/inntekt/verdier")
//@Timed
//@Produces(APPLICATION_JSON)
//public class VerdiRessurs {
//
//    private final Tilgangskontroll tilgangskontroll;
//    private final SoknadUnderArbeidRepository soknadUnderArbeidRepository;
//    private final TextService textService;
//
//    public VerdiRessurs(Tilgangskontroll tilgangskontroll, SoknadUnderArbeidRepository soknadUnderArbeidRepository, TextService textService) {
//        this.tilgangskontroll = tilgangskontroll;
//        this.soknadUnderArbeidRepository = soknadUnderArbeidRepository;
//        this.textService = textService;
//    }
//
//    @GET
//    public VerdierFrontend hentVerdier(@PathParam("behandlingsId") String behandlingsId){
//        tilgangskontroll.verifiserAtBrukerHarTilgang();
//        String eier = SubjectHandler.getUserId();
//        JsonInternalSoknad soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier).getJsonInternalSoknad();
//        JsonOkonomi okonomi = soknad.getSoknad().getData().getOkonomi();
//        VerdierFrontend verdierFrontend = new VerdierFrontend();
//
//        if (okonomi.getOpplysninger().getBekreftelse() == null){
//            return verdierFrontend;
//        }
//
//        setBekreftelseOnVerdierFrontend(okonomi.getOpplysninger(), verdierFrontend);
//        setVerdityperOnVerdierFrontend(okonomi.getOversikt(), verdierFrontend);
//
//        if (okonomi.getOpplysninger().getBeskrivelseAvAnnet() != null){
//            verdierFrontend.setBeskrivelseAvAnnet(okonomi.getOpplysninger().getBeskrivelseAvAnnet().getVerdi());
//        }
//
//        return verdierFrontend;
//    }
//
//    @PUT
//    public void updateVerdier(@PathParam("behandlingsId") String behandlingsId, VerdierFrontend verdierFrontend){
//        tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId);
//        String eier = SubjectHandler.getUserId();
//        SoknadUnderArbeid soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier);
//        JsonOkonomi okonomi = soknad.getJsonInternalSoknad().getSoknad().getData().getOkonomi();
//
//        if (okonomi.getOpplysninger().getBekreftelse() == null){
//            okonomi.getOpplysninger().setBekreftelse(new ArrayList<>());
//        }
//
//        setBekreftelse(okonomi.getOpplysninger(), BEKREFTELSE_VERDI, verdierFrontend.bekreftelse, textService.getJsonOkonomiTittel("inntekt.eierandeler"));
//        setVerdier(okonomi.getOversikt(), verdierFrontend);
//        setBeskrivelseAvAnnet(okonomi.getOpplysninger(), verdierFrontend);
//
//        soknadUnderArbeidRepository.oppdaterSoknadsdata(soknad, eier);
//    }
//
//    private void setVerdier(JsonOkonomioversikt oversikt, VerdierFrontend verdierFrontend) {
//        List<JsonOkonomioversiktFormue> verdier = oversikt.getFormue();
//
//        String tittel = textService.getJsonOkonomiTittel(soknadTypeToTitleKey.get(VERDI_BOLIG));
//        addFormueIfCheckedElseDeleteInOversikt(verdier, VERDI_BOLIG, tittel, verdierFrontend.bolig);
//
//        tittel = textService.getJsonOkonomiTittel(soknadTypeToTitleKey.get(VERDI_CAMPINGVOGN));
//        addFormueIfCheckedElseDeleteInOversikt(verdier, VERDI_CAMPINGVOGN, tittel, verdierFrontend.campingvogn);
//
//        tittel = textService.getJsonOkonomiTittel(soknadTypeToTitleKey.get(VERDI_KJORETOY));
//        addFormueIfCheckedElseDeleteInOversikt(verdier, VERDI_KJORETOY, tittel, verdierFrontend.kjoretoy);
//
//        tittel = textService.getJsonOkonomiTittel(soknadTypeToTitleKey.get(VERDI_FRITIDSEIENDOM));
//        addFormueIfCheckedElseDeleteInOversikt(verdier, VERDI_FRITIDSEIENDOM, tittel, verdierFrontend.fritidseiendom);
//
//        tittel = textService.getJsonOkonomiTittel(soknadTypeToTitleKey.get(VERDI_ANNET));
//        addFormueIfCheckedElseDeleteInOversikt(verdier, VERDI_ANNET, tittel, verdierFrontend.annet);
//    }
//
//    private void setBeskrivelseAvAnnet(JsonOkonomiopplysninger opplysninger, VerdierFrontend verdierFrontend) {
//        if (opplysninger.getBeskrivelseAvAnnet() == null){
//            opplysninger.withBeskrivelseAvAnnet(new JsonOkonomibeskrivelserAvAnnet()
//                    .withKilde(JsonKildeBruker.BRUKER)
//                    .withVerdi("")
//                    .withSparing("")
//                    .withUtbetaling("")
//                    .withBoutgifter("")
//                    .withBarneutgifter(""));
//        }
//        opplysninger.getBeskrivelseAvAnnet().setVerdi(verdierFrontend.beskrivelseAvAnnet != null ? verdierFrontend.beskrivelseAvAnnet : "");
//    }
//
//    private void setBekreftelseOnVerdierFrontend(JsonOkonomiopplysninger opplysninger, VerdierFrontend verdierFrontend) {
//        opplysninger.getBekreftelse().stream()
//                .filter(bekreftelse -> bekreftelse.getType().equals(BEKREFTELSE_VERDI)).findFirst()
//                .ifPresent(jsonOkonomibekreftelse -> verdierFrontend.setBekreftelse(jsonOkonomibekreftelse.getVerdi()));
//    }
//
//    private void setVerdityperOnVerdierFrontend(JsonOkonomioversikt oversikt, VerdierFrontend verdierFrontend) {
//        oversikt.getFormue().forEach(
//                formue -> {
//                    switch(formue.getType()){
//                        case VERDI_BOLIG:
//                            verdierFrontend.setBolig(true);
//                            break;
//                        case VERDI_CAMPINGVOGN:
//                            verdierFrontend.setCampingvogn(true);
//                            break;
//                        case VERDI_KJORETOY:
//                            verdierFrontend.setKjoretoy(true);
//                            break;
//                        case VERDI_FRITIDSEIENDOM:
//                            verdierFrontend.setFritidseiendom(true);
//                            break;
//                        case VERDI_ANNET:
//                            verdierFrontend.setAnnet(true);
//                            break;
//                    }
//                });
//    }
//
//    @XmlAccessorType(XmlAccessType.FIELD)
//    public static final class VerdierFrontend {
//        public Boolean bekreftelse;
//        public boolean bolig;
//        public boolean campingvogn;
//        public boolean kjoretoy;
//        public boolean fritidseiendom;
//        public boolean annet;
//        public String beskrivelseAvAnnet;
//
//        public void setBekreftelse(Boolean bekreftelse) {
//            this.bekreftelse = bekreftelse;
//        }
//
//        public void setBolig(boolean bolig) {
//            this.bolig = bolig;
//        }
//
//        public void setCampingvogn(boolean campingvogn) {
//            this.campingvogn = campingvogn;
//        }
//
//        public void setKjoretoy(boolean kjoretoy) {
//            this.kjoretoy = kjoretoy;
//        }
//
//        public void setFritidseiendom(boolean fritidseiendom) {
//            this.fritidseiendom = fritidseiendom;
//        }
//
//        public void setAnnet(boolean annet) {
//            this.annet = annet;
//        }
//
//        public void setBeskrivelseAvAnnet(String beskrivelseAvAnnet) {
//            this.beskrivelseAvAnnet = beskrivelseAvAnnet;
//        }
//    }
//}
