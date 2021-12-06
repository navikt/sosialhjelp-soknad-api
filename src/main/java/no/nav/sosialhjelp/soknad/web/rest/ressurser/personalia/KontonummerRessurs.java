//package no.nav.sosialhjelp.soknad.web.rest.ressurser.personalia;
//
//import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
//import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonKontonummer;
//import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonPersonalia;
//import no.nav.security.token.support.core.api.ProtectedWithClaims;
//import no.nav.sosialhjelp.metrics.aspects.Timed;
//import no.nav.sosialhjelp.soknad.business.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository;
//import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeid;
//import no.nav.sosialhjelp.soknad.domain.model.oidc.SubjectHandler;
//import no.nav.sosialhjelp.soknad.personalia.kontonummer.KontonummerSystemdata;
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
//
//import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
//import static no.nav.sosialhjelp.soknad.web.utils.Constants.CLAIM_ACR_LEVEL_4;
//import static no.nav.sosialhjelp.soknad.web.utils.Constants.SELVBETJENING;
//
//@Controller
//@ProtectedWithClaims(issuer = SELVBETJENING, claimMap = {CLAIM_ACR_LEVEL_4})
//@Path("/soknader/{behandlingsId}/personalia/kontonummer")
//@Timed
//@Produces(APPLICATION_JSON)
//public class KontonummerRessurs {
//
//    private final Tilgangskontroll tilgangskontroll;
//    private final SoknadUnderArbeidRepository soknadUnderArbeidRepository;
//    private final KontonummerSystemdata kontonummerSystemdata;
//
//    public KontonummerRessurs(Tilgangskontroll tilgangskontroll, SoknadUnderArbeidRepository soknadUnderArbeidRepository, KontonummerSystemdata kontonummerSystemdata) {
//        this.tilgangskontroll = tilgangskontroll;
//        this.soknadUnderArbeidRepository = soknadUnderArbeidRepository;
//        this.kontonummerSystemdata = kontonummerSystemdata;
//    }
//
//    @GET
//    public KontonummerFrontend hentKontonummer(@PathParam("behandlingsId") String behandlingsId) {
//        tilgangskontroll.verifiserAtBrukerHarTilgang();
//        String eier = SubjectHandler.getUserId();
//        SoknadUnderArbeid soknadUnderArbeid = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier);
//        JsonKontonummer kontonummer = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia().getKontonummer();
//        String systemverdi;
//        if (kontonummer.getKilde().equals(JsonKilde.SYSTEM)) {
//            systemverdi = kontonummer.getVerdi();
//        } else {
//            systemverdi = kontonummerSystemdata.innhentSystemverdiKontonummer(eier);
//        }
//
//        return new KontonummerFrontend()
//                .withBrukerdefinert(kontonummer.getKilde() == JsonKilde.BRUKER)
//                .withSystemverdi(systemverdi)
//                .withBrukerutfyltVerdi(kontonummer.getKilde() == JsonKilde.BRUKER ? kontonummer.getVerdi() : null)
//                .withHarIkkeKonto(kontonummer.getHarIkkeKonto());
//    }
//
//    @PUT
//    public void updateKontonummer(@PathParam("behandlingsId") String behandlingsId, KontonummerFrontend kontonummerFrontend) {
//        tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId);
//        String eier = SubjectHandler.getUserId();
//        SoknadUnderArbeid soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier);
//        JsonPersonalia personalia = soknad.getJsonInternalSoknad().getSoknad().getData().getPersonalia();
//        JsonKontonummer kontonummer = personalia.getKontonummer();
//        if (kontonummerFrontend.brukerdefinert) {
//            kontonummer.setKilde(JsonKilde.BRUKER);
//            if ("".equals(kontonummerFrontend.brukerutfyltVerdi)) {
//                kontonummerFrontend.brukerutfyltVerdi = null;
//            }
//            kontonummer.setVerdi(kontonummerFrontend.brukerutfyltVerdi);
//            kontonummer.setHarIkkeKonto(kontonummerFrontend.harIkkeKonto);
//        } else if (kontonummer.getKilde() == JsonKilde.BRUKER) {
//            kontonummer.setKilde(JsonKilde.SYSTEM);
//            kontonummerSystemdata.updateSystemdataIn(soknad, "");
//            kontonummer.setHarIkkeKonto(null);
//        }
//        soknadUnderArbeidRepository.oppdaterSoknadsdata(soknad, eier);
//    }
//
//    @XmlAccessorType(XmlAccessType.FIELD)
//    public static final class KontonummerFrontend {
//        public boolean brukerdefinert;
//        public String systemverdi;
//        public String brukerutfyltVerdi;
//        public Boolean harIkkeKonto;
//
//        public KontonummerFrontend withBrukerdefinert(boolean brukerdefinert) {
//            this.brukerdefinert = brukerdefinert;
//            return this;
//        }
//
//        public KontonummerFrontend withSystemverdi(String systemverdi) {
//            this.systemverdi = systemverdi;
//            return this;
//        }
//
//        public KontonummerFrontend withBrukerutfyltVerdi(String brukerutfyltVerdi) {
//            this.brukerutfyltVerdi = brukerutfyltVerdi;
//            return this;
//        }
//
//        public KontonummerFrontend withHarIkkeKonto(Boolean harIkkeKonto) {
//            this.harIkkeKonto = harIkkeKonto;
//            return this;
//        }
//    }
//}
