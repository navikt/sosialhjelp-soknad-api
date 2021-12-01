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
//import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.BEKREFTELSE_SPARING;
//import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.FORMUE_ANNET;
//import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.FORMUE_BRUKSKONTO;
//import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.FORMUE_BSU;
//import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.FORMUE_LIVSFORSIKRING;
//import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.FORMUE_SPAREKONTO;
//import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.FORMUE_VERDIPAPIRER;
//import static no.nav.sosialhjelp.soknad.business.mappers.OkonomiMapper.addFormueIfCheckedElseDeleteInOversikt;
//import static no.nav.sosialhjelp.soknad.business.mappers.OkonomiMapper.setBekreftelse;
//import static no.nav.sosialhjelp.soknad.business.mappers.TitleKeyMapper.soknadTypeToTitleKey;
//import static no.nav.sosialhjelp.soknad.web.utils.Constants.CLAIM_ACR_LEVEL_4;
//import static no.nav.sosialhjelp.soknad.web.utils.Constants.SELVBETJENING;
//
//
//
//@Controller
//@ProtectedWithClaims(issuer = SELVBETJENING, claimMap = {CLAIM_ACR_LEVEL_4})
//@Path("/soknader/{behandlingsId}/inntekt/formue")
//@Timed
//@Produces(APPLICATION_JSON)
//public class FormueRessurs {
//
//    private final Tilgangskontroll tilgangskontroll;
//    private final SoknadUnderArbeidRepository soknadUnderArbeidRepository;
//    private final TextService textService;
//
//    public FormueRessurs(Tilgangskontroll tilgangskontroll, SoknadUnderArbeidRepository soknadUnderArbeidRepository, TextService textService) {
//        this.tilgangskontroll = tilgangskontroll;
//        this.soknadUnderArbeidRepository = soknadUnderArbeidRepository;
//        this.textService = textService;
//    }
//
//    @GET
//    public FormueFrontend hentFormue(@PathParam("behandlingsId") String behandlingsId){
//        tilgangskontroll.verifiserAtBrukerHarTilgang();
//        String eier = SubjectHandler.getUserId();
//        JsonInternalSoknad soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier).getJsonInternalSoknad();
//        JsonOkonomi okonomi = soknad.getSoknad().getData().getOkonomi();
//        FormueFrontend formueFrontend = new FormueFrontend();
//
//        if (okonomi.getOpplysninger().getBekreftelse() == null){
//            return formueFrontend;
//        }
//
//        setFormuetyperOnFormueFrontend(okonomi.getOversikt(), formueFrontend);
//
//        if (okonomi.getOpplysninger().getBeskrivelseAvAnnet() != null){
//            formueFrontend.setBeskrivelseAvAnnet(okonomi.getOpplysninger().getBeskrivelseAvAnnet().getSparing());
//        }
//
//        return formueFrontend;
//    }
//
//    @PUT
//    public void updateFormue(@PathParam("behandlingsId") String behandlingsId, FormueFrontend formueFrontend){
//        tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId);
//        String eier = SubjectHandler.getUserId();
//        SoknadUnderArbeid soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier);
//        JsonOkonomi okonomi = soknad.getJsonInternalSoknad().getSoknad().getData().getOkonomi();
//
//        if (okonomi.getOpplysninger().getBekreftelse() == null){
//            okonomi.getOpplysninger().setBekreftelse(new ArrayList<>());
//        }
//
//        boolean hasAnyFormueType = formueFrontend.brukskonto || formueFrontend.bsu || formueFrontend.sparekonto ||
//                formueFrontend.livsforsikring || formueFrontend.verdipapirer || formueFrontend.annet;
//
//        setBekreftelse(okonomi.getOpplysninger(), BEKREFTELSE_SPARING, hasAnyFormueType, textService.getJsonOkonomiTittel("inntekt.bankinnskudd"));
//        setFormue(okonomi.getOversikt(), formueFrontend);
//        setBeskrivelseAvAnnet(okonomi.getOpplysninger(), formueFrontend);
//
//        soknadUnderArbeidRepository.oppdaterSoknadsdata(soknad, eier);
//    }
//
//    private void setFormue(JsonOkonomioversikt oversikt, FormueFrontend formueFrontend) {
//        List<JsonOkonomioversiktFormue> formue = oversikt.getFormue();
//
//        String tittel = textService.getJsonOkonomiTittel(soknadTypeToTitleKey.get(FORMUE_BRUKSKONTO));
//        addFormueIfCheckedElseDeleteInOversikt(formue, FORMUE_BRUKSKONTO, tittel, formueFrontend.brukskonto);
//
//        tittel = textService.getJsonOkonomiTittel(soknadTypeToTitleKey.get(FORMUE_BSU));
//        addFormueIfCheckedElseDeleteInOversikt(formue, FORMUE_BSU, tittel, formueFrontend.bsu);
//
//        tittel = textService.getJsonOkonomiTittel(soknadTypeToTitleKey.get(FORMUE_LIVSFORSIKRING));
//        addFormueIfCheckedElseDeleteInOversikt(formue, FORMUE_LIVSFORSIKRING, tittel, formueFrontend.livsforsikring);
//
//        tittel = textService.getJsonOkonomiTittel(soknadTypeToTitleKey.get(FORMUE_SPAREKONTO));
//        addFormueIfCheckedElseDeleteInOversikt(formue, FORMUE_SPAREKONTO, tittel, formueFrontend.sparekonto);
//
//        tittel = textService.getJsonOkonomiTittel(soknadTypeToTitleKey.get(FORMUE_VERDIPAPIRER));
//        addFormueIfCheckedElseDeleteInOversikt(formue, FORMUE_VERDIPAPIRER, tittel, formueFrontend.verdipapirer);
//
//        tittel = textService.getJsonOkonomiTittel(soknadTypeToTitleKey.get(FORMUE_ANNET));
//        addFormueIfCheckedElseDeleteInOversikt(formue, FORMUE_ANNET, tittel, formueFrontend.annet);
//    }
//
//    private void setBeskrivelseAvAnnet(JsonOkonomiopplysninger opplysninger, FormueFrontend formueFrontend) {
//        if (opplysninger.getBeskrivelseAvAnnet() == null){
//            opplysninger.withBeskrivelseAvAnnet(new JsonOkonomibeskrivelserAvAnnet()
//                    .withKilde(JsonKildeBruker.BRUKER)
//                    .withVerdi("")
//                    .withSparing("")
//                    .withUtbetaling("")
//                    .withBoutgifter("")
//                    .withBarneutgifter(""));
//        }
//        opplysninger.getBeskrivelseAvAnnet().setSparing(formueFrontend.beskrivelseAvAnnet != null ? formueFrontend.beskrivelseAvAnnet : "");
//    }
//
//    private void setFormuetyperOnFormueFrontend(JsonOkonomioversikt oversikt, FormueFrontend formueFrontend) {
//        oversikt.getFormue().forEach(
//                formue -> {
//                    switch(formue.getType()){
//                        case FORMUE_BRUKSKONTO:
//                            formueFrontend.setBrukskonto(true);
//                            break;
//                        case FORMUE_BSU:
//                            formueFrontend.setBsu(true);
//                            break;
//                        case FORMUE_SPAREKONTO:
//                            formueFrontend.setSparekonto(true);
//                            break;
//                        case FORMUE_LIVSFORSIKRING:
//                            formueFrontend.setLivsforsikring(true);
//                            break;
//                        case FORMUE_VERDIPAPIRER:
//                            formueFrontend.setVerdipapirer(true);
//                            break;
//                        case FORMUE_ANNET:
//                            formueFrontend.setAnnet(true);
//                            break;
//                    }
//                });
//    }
//
//    @XmlAccessorType(XmlAccessType.FIELD)
//    public static final class FormueFrontend {
//        public boolean brukskonto;
//        public boolean sparekonto;
//        public boolean bsu;
//        public boolean livsforsikring;
//        public boolean verdipapirer;
//        public boolean annet;
//        public String beskrivelseAvAnnet;
//
//        public void setBrukskonto(boolean brukskonto) {
//            this.brukskonto = brukskonto;
//        }
//
//        public void setSparekonto(boolean sparekonto) {
//            this.sparekonto = sparekonto;
//        }
//
//        public void setBsu(boolean bsu) {
//            this.bsu = bsu;
//        }
//
//        public void setLivsforsikring(boolean livsforsikring) {
//            this.livsforsikring = livsforsikring;
//        }
//
//        public void setVerdipapirer(boolean verdipapirer) {
//            this.verdipapirer = verdipapirer;
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
