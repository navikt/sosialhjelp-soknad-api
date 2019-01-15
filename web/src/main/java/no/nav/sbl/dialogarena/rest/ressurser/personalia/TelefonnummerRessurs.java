package no.nav.sbl.dialogarena.rest.ressurser.personalia;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import org.springframework.stereotype.Controller;

import no.nav.metrics.aspects.Timed;
import no.nav.modig.core.context.SubjectHandler;
import no.nav.sbl.dialogarena.rest.ressurser.LegacyHelper;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.personalia.Personalia;
import no.nav.sbl.dialogarena.sikkerhet.Tilgangskontroll;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.FaktaService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.Systemdata;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.personalia.PersonaliaFletter;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonTelefonnummer;
import no.nav.sbl.sosialhjelp.domain.SoknadUnderArbeid;

@Controller
@Path("/soknader/{behandlingsId}/personalia/telefonnummer")
@Timed
@Produces(APPLICATION_JSON)
public class TelefonnummerRessurs {

    @Inject
    private SoknadService soknadService;
        
    @Inject
    private PersonaliaFletter personaliaFletter;
    
    @Inject
    private FaktaService faktaService;
    
    @Inject
    private Tilgangskontroll tilgangskontroll;
    
    @Inject
    private LegacyHelper legacyHelper;

    
    @GET
    public TelefonnummerFrontend hentTelefonnummer(@PathParam("behandlingsId") String behandlingsId) {
        final String eier = SubjectHandler.getSubjectHandler().getUid();
        final JsonInternalSoknad soknad = legacyHelper.hentSoknad(behandlingsId, eier).getJsonInternalSoknad();
        final String personIdentifikator = soknad.getSoknad().getData().getPersonalia().getPersonIdentifikator().getVerdi();
        final JsonTelefonnummer telefonnummer = soknad.getSoknad().getData().getPersonalia().getTelefonnummer();

        final String systemverdi = innhentSystemverdiTelefonnummer(personIdentifikator); 
        
        return new TelefonnummerFrontend()
                .withBrukerdefinert(telefonnummer.getKilde() == JsonKilde.BRUKER)
                .withSystemverdi(systemverdi)
                .withVerdi(telefonnummer != null ? telefonnummer.getVerdi() : null);
    }
    
    @PUT
    public void endreTelefonnummer(@PathParam("behandlingsId") String behandlingsId, TelefonnummerFrontend telefonnummerFrontend) {
        tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId);

        legacyUpdate(behandlingsId, telefonnummerFrontend);
    }

    private void legacyUpdate(String behandlingsId, TelefonnummerFrontend telefonnummerFrontend) {
        final WebSoknad webSoknad = soknadService.hentSoknad(behandlingsId, false, false);
        
        final Faktum brukerdefinert = faktaService.hentFaktumMedKey(webSoknad.getSoknadId(), "kontakt.telefon.brukerendrettoggle");
        brukerdefinert.setValue(Boolean.toString(telefonnummerFrontend.brukerdefinert));
        faktaService.lagreBrukerFaktum(brukerdefinert);
        
        final Faktum telefon = faktaService.hentFaktumMedKey(webSoknad.getSoknadId(), "kontakt.telefon");
        telefon.setValue(telefonnummerFrontend.verdi);
        faktaService.lagreBrukerFaktum(telefon);
    }
    

    private String innhentSystemverdiTelefonnummer(final String personIdentifikator) {
        final Personalia personalia = personaliaFletter.mapTilPersonalia(personIdentifikator);
        final String systemVerdi = norskTelefonnummer(personalia.getMobiltelefonnummer());
        return systemVerdi;
    }
    
    static String norskTelefonnummer(String mobiltelefonnummer) {
        if (mobiltelefonnummer == null) {
            return null;
        }
        if (mobiltelefonnummer.length() == 8) {
            return "+47" + mobiltelefonnummer;
        }
        if (mobiltelefonnummer.startsWith("+47") && mobiltelefonnummer.length() == 11) {
            return mobiltelefonnummer;
        }
        return null;
    }
   

    @XmlAccessorType(XmlAccessType.FIELD)
    public static final class TelefonnummerFrontend {
        public boolean brukerdefinert;
        public String systemverdi;
        public String verdi;
        
        public TelefonnummerFrontend withBrukerdefinert(boolean brukerdefinert) {
            this.brukerdefinert = brukerdefinert;
            return this;
        }
        
        public TelefonnummerFrontend withSystemverdi(String systemverdi) {
            this.systemverdi = systemverdi;
            return this;
        }
        
        public TelefonnummerFrontend withVerdi(String verdi) {
            this.verdi = verdi;
            return this;
        }
    }
}
