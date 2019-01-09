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
import no.nav.sbl.dialogarena.rest.ressurser.LegacyHelper;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.personalia.Personalia;
import no.nav.sbl.dialogarena.sikkerhet.Tilgangskontroll;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.FaktaService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.personalia.PersonaliaFletter;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonKontonummer;

@Controller
@Path("/soknader/{behandlingsId}/personalia/kontonummer")
@Timed
@Produces(APPLICATION_JSON)
public class KontonummerRessurs {

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
    public KontonummerFrontend hentKontonummer(@PathParam("behandlingsId") String behandlingsId) {
        tilgangskontroll.verifiserBrukerHarTilgangTilSoknad(behandlingsId);
        
        final JsonInternalSoknad soknad = legacyHelper.hentSoknad(behandlingsId);
        final String personIdentifikator = soknad.getSoknad().getData().getPersonalia().getPersonIdentifikator().getVerdi();
        final JsonKontonummer kontonummer = soknad.getSoknad().getData().getPersonalia().getKontonummer();

        final String systemverdi = innhentSystemverdiKontonummer(personIdentifikator); 
        
        return new KontonummerFrontend()
                .withBrukerdefinert(kontonummer.getKilde() == JsonKilde.BRUKER)
                .withSystemverdi(systemverdi)
                .withVerdi(kontonummer.getVerdi())
                .withHarIkkeKonto(kontonummer.getHarIkkeKonto());
    }
    
    @PUT
    public void endreKontonummer(@PathParam("behandlingsId") String behandlingsId, KontonummerFrontend kontonummerFrontend) {
        tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId);
        
        final WebSoknad webSoknad = soknadService.hentSoknad(behandlingsId, false, false);
        
        final Faktum brukerdefinert = faktaService.hentFaktumMedKey(webSoknad.getSoknadId(), "kontakt.kontonummer.brukerendrettoggle");
        brukerdefinert.setValue(Boolean.toString(kontonummerFrontend.brukerdefinert));
        faktaService.lagreBrukerFaktum(brukerdefinert);
        
        final Faktum kontonummer = faktaService.hentFaktumMedKey(webSoknad.getSoknadId(), "kontakt.kontonummer");
        kontonummer.setValue(kontonummerFrontend.verdi);
        faktaService.lagreBrukerFaktum(kontonummer);
        
        final Faktum harIkke = faktaService.hentFaktumMedKey(webSoknad.getSoknadId(), "kontakt.kontonummer.harikke");
        harIkke.setValue(booleanToString(kontonummerFrontend.harIkkeKonto));
        faktaService.lagreBrukerFaktum(harIkke);
    }
    
    
    private String booleanToString(Boolean b) {
        if (b == null) {
            return null;
        }
        return b.toString();
    }

    private String innhentSystemverdiKontonummer(final String personIdentifikator) {
        final Personalia personalia = personaliaFletter.mapTilPersonalia(personIdentifikator);
        final String systemVerdi = norskKontonummer(personalia);
        return systemVerdi;
    }
    
    private String norskKontonummer(Personalia personalia) {
        if (personalia.getErUtenlandskBankkonto() != null && personalia.getErUtenlandskBankkonto()) {
            return "";
        } else {
            return personalia.getKontonummer();
        }
    }
   

    @XmlAccessorType(XmlAccessType.FIELD)
    public static final class KontonummerFrontend {
        public boolean brukerdefinert;
        public String systemverdi;
        public String verdi;
        public Boolean harIkkeKonto;
        
        public KontonummerFrontend withBrukerdefinert(boolean brukerdefinert) {
            this.brukerdefinert = brukerdefinert;
            return this;
        }
        
        public KontonummerFrontend withSystemverdi(String systemverdi) {
            this.systemverdi = systemverdi;
            return this;
        }
        
        public KontonummerFrontend withVerdi(String verdi) {
            this.verdi = verdi;
            return this;
        }
        
        public KontonummerFrontend withHarIkkeKonto(Boolean harIkkeKonto) {
            this.harIkkeKonto = harIkkeKonto;
            return this;
        }
    }
}
