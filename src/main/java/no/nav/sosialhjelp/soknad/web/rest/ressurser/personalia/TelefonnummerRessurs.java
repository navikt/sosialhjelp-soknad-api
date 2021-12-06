//package no.nav.sosialhjelp.soknad.web.rest.ressurser.personalia;
//
//import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
//import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonPersonalia;
//import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonTelefonnummer;
//import no.nav.security.token.support.core.api.ProtectedWithClaims;
//import no.nav.sosialhjelp.metrics.aspects.Timed;
//import no.nav.sosialhjelp.soknad.business.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository;
//import no.nav.sosialhjelp.soknad.business.service.systemdata.TelefonnummerSystemdata;
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
//
//import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
//import static no.nav.sosialhjelp.soknad.web.utils.Constants.CLAIM_ACR_LEVEL_4;
//import static no.nav.sosialhjelp.soknad.web.utils.Constants.SELVBETJENING;
//
//@Controller
//@ProtectedWithClaims(issuer = SELVBETJENING, claimMap = {CLAIM_ACR_LEVEL_4})
//@Path("/soknader/{behandlingsId}/personalia/telefonnummer")
//@Timed
//@Produces(APPLICATION_JSON)
//public class TelefonnummerRessurs {
//
//    private final Tilgangskontroll tilgangskontroll;
//    private final TelefonnummerSystemdata telefonnummerSystemdata;
//    private final SoknadUnderArbeidRepository soknadUnderArbeidRepository;
//
//    public TelefonnummerRessurs(Tilgangskontroll tilgangskontroll, TelefonnummerSystemdata telefonnummerSystemdata, SoknadUnderArbeidRepository soknadUnderArbeidRepository) {
//        this.tilgangskontroll = tilgangskontroll;
//        this.telefonnummerSystemdata = telefonnummerSystemdata;
//        this.soknadUnderArbeidRepository = soknadUnderArbeidRepository;
//    }
//
//    @GET
//    public TelefonnummerFrontend hentTelefonnummer(@PathParam("behandlingsId") String behandlingsId) {
//        tilgangskontroll.verifiserAtBrukerHarTilgang();
//        String eier = SubjectHandler.getUserId();
//        SoknadUnderArbeid soknadUnderArbeid = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier);
//        JsonTelefonnummer telefonnummer = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia().getTelefonnummer();
//        String systemverdi;
//        if (telefonnummer != null && telefonnummer.getKilde().equals(JsonKilde.SYSTEM)) {
//            systemverdi = telefonnummer.getVerdi();
//        } else {
//            systemverdi = telefonnummerSystemdata.innhentSystemverdiTelefonnummer(eier);
//        }
//
//        return new TelefonnummerFrontend()
//                .withBrukerdefinert(telefonnummer == null || telefonnummer.getKilde() == JsonKilde.BRUKER)
//                .withSystemverdi(systemverdi)
//                .withBrukerutfyltVerdi(telefonnummer != null && telefonnummer.getKilde() == JsonKilde.BRUKER ? telefonnummer.getVerdi() : null);
//    }
//
//    @PUT
//    public void updateTelefonnummer(@PathParam("behandlingsId") String behandlingsId, TelefonnummerFrontend telefonnummerFrontend) {
//        tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId);
//        String eier = SubjectHandler.getUserId();
//        if ("".equals(telefonnummerFrontend.brukerutfyltVerdi)) {
//            telefonnummerFrontend.brukerutfyltVerdi = null;
//        }
//        SoknadUnderArbeid soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier);
//        JsonPersonalia personalia = soknad.getJsonInternalSoknad().getSoknad().getData().getPersonalia();
//        JsonTelefonnummer jsonTelefonnummer = personalia.getTelefonnummer() != null ? personalia.getTelefonnummer() :
//                personalia.withTelefonnummer(new JsonTelefonnummer()).getTelefonnummer();
//        if (telefonnummerFrontend.brukerdefinert) {
//            if (telefonnummerFrontend.brukerutfyltVerdi == null) {
//                personalia.setTelefonnummer(null);
//            } else {
//                jsonTelefonnummer.setKilde(JsonKilde.BRUKER);
//                jsonTelefonnummer.setVerdi(telefonnummerFrontend.brukerutfyltVerdi);
//            }
//        } else {
//            jsonTelefonnummer.setKilde(JsonKilde.SYSTEM);
//            telefonnummerSystemdata.updateSystemdataIn(soknad, "");
//        }
//        soknadUnderArbeidRepository.oppdaterSoknadsdata(soknad, eier);
//    }
//
//    @XmlAccessorType(XmlAccessType.FIELD)
//    public static final class TelefonnummerFrontend {
//        public boolean brukerdefinert;
//        public String systemverdi;
//        public String brukerutfyltVerdi;
//
//        public TelefonnummerFrontend withBrukerdefinert(boolean brukerdefinert) {
//            this.brukerdefinert = brukerdefinert;
//            return this;
//        }
//
//        public TelefonnummerFrontend withSystemverdi(String systemverdi) {
//            this.systemverdi = systemverdi;
//            return this;
//        }
//
//        public TelefonnummerFrontend withBrukerutfyltVerdi(String brukerutfyltVerdi) {
//            this.brukerutfyltVerdi = brukerutfyltVerdi;
//            return this;
//        }
//    }
//}
