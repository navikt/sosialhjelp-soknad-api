package no.nav.sbl.dialogarena.rest.ressurser.personalia;

import no.nav.metrics.aspects.Timed;
import no.nav.modig.core.context.SubjectHandler;
import no.nav.sbl.dialogarena.rest.ressurser.LegacyHelper;
import no.nav.sbl.dialogarena.rest.ressurser.SoknadsmottakerRessurs;
import no.nav.sbl.dialogarena.rest.ressurser.SoknadsmottakerRessurs.NavEnhetFrontend;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sikkerhet.Tilgangskontroll;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.FaktaService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.systemdata.AdresseSystemdata;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.*;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonPersonalia;
import no.nav.sbl.sosialhjelp.domain.SoknadUnderArbeid;
import no.nav.sbl.sosialhjelp.soknadunderbehandling.SoknadUnderArbeidRepository;
import org.springframework.stereotype.Controller;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import java.util.List;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Controller
@Path("/soknader/{behandlingsId}/personalia/adresser")
@Timed
@Produces(APPLICATION_JSON)
public class AdresseRessurs {

    @Inject
    private SoknadService soknadService;

    @Inject
    private FaktaService faktaService;

    @Inject
    private Tilgangskontroll tilgangskontroll;

    @Inject
    private LegacyHelper legacyHelper;

    @Inject
    private AdresseSystemdata adresseSystemdata;

    @Inject
    private SoknadUnderArbeidRepository soknadUnderArbeidRepository;

    @Inject
    private SoknadsmottakerRessurs soknadsmottakerRessurs;

    @GET
    public AdresserFrontend hentAdresser(@PathParam("behandlingsId") String behandlingsId) {
        final String eier = SubjectHandler.getSubjectHandler().getUid();
        final JsonInternalSoknad soknad = legacyHelper.hentSoknad(behandlingsId, eier).getJsonInternalSoknad();
        final String personIdentifikator = soknad.getSoknad().getData().getPersonalia().getPersonIdentifikator().getVerdi();
        final JsonAdresse jsonOppholdsadresse = soknad.getSoknad().getData().getPersonalia().getOppholdsadresse();

        final JsonAdresse sysFolkeregistrertAdresse = adresseSystemdata.innhentFolkeregistrertAdresse(personIdentifikator);
        final JsonAdresse sysMidlertidigAdresse = adresseSystemdata.innhentMidlertidigAdresse(personIdentifikator);

        return mapToAdresseFrontend(sysFolkeregistrertAdresse, sysMidlertidigAdresse, jsonOppholdsadresse);
    }

    @PUT
    public List<NavEnhetFrontend> updateAdresse(@PathParam("behandlingsId") String behandlingsId, AdresseFrontend adresseFrontend) {
        tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId);
        update(behandlingsId, adresseFrontend);
        legacyUpdate(behandlingsId, adresseFrontend);
        return soknadsmottakerRessurs.findSoknadsmottaker(behandlingsId, mapValgToString(adresseFrontend.valg));
    }

    private void update(String behandlingsId, AdresseFrontend adresseFrontend) {
        final String eier = SubjectHandler.getSubjectHandler().getUid();
        final SoknadUnderArbeid soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier).get();
        final JsonPersonalia personalia = soknad.getJsonInternalSoknad().getSoknad().getData().getPersonalia();

        switch (adresseFrontend.valg){
            case FOLKEREGISTRERT:
                personalia.setOppholdsadresse(adresseSystemdata.innhentFolkeregistrertAdresse(eier));
                break;
            case MIDLERTIDIG:
                personalia.setOppholdsadresse(adresseSystemdata.innhentMidlertidigAdresse(eier));
                break;
            case SOKNAD:
                personalia.setOppholdsadresse(mapToJsonAdresse(adresseFrontend));
                break;
        }

        personalia.setPostadresse(midlertidigLosningForPostadresse(personalia.getOppholdsadresse()));

        soknadUnderArbeidRepository.oppdaterSoknadsdata(soknad, eier);
    }

    private void legacyUpdate(String behandlingsId, AdresseFrontend adresseFrontend) {
        final WebSoknad webSoknad = soknadService.hentSoknad(behandlingsId, false, false);

        final Faktum brukerdefinert = faktaService.hentFaktumMedKey(webSoknad.getSoknadId(), "kontakt.adresse.brukerendrettoggle");
        final Faktum valg = faktaService.hentFaktumMedKey(webSoknad.getSoknadId(), "kontakt.system.oppholdsadresse.valg");

        switch (adresseFrontend.valg){
            case FOLKEREGISTRERT:
                valg.setValue("folkerregistrert");
                brukerdefinert.setValue(Boolean.toString(false));
                break;
            case MIDLERTIDIG:
                valg.setValue("midlertidig");
                brukerdefinert.setValue(Boolean.toString(false));
                break;
            case SOKNAD:
                valg.setValue("soknad");
                brukerdefinert.setValue(Boolean.toString(true));
                final Faktum brukerAdresse = faktaService.hentFaktumMedKey(webSoknad.getSoknadId(), "kontakt.adresse.bruker");
                populerAdresse(brukerAdresse, adresseFrontend);
                faktaService.lagreBrukerFaktum(brukerAdresse);
                break;
        }

        faktaService.lagreBrukerFaktum(brukerdefinert);
        faktaService.lagreBrukerFaktum(valg);
    }

    private JsonAdresse midlertidigLosningForPostadresse(JsonAdresse oppholdsadresse) {
        if (oppholdsadresse == null){
            return null;
        }
        if (oppholdsadresse.getType() == JsonAdresse.Type.MATRIKKELADRESSE){
            return null;
        }
        return oppholdsadresse;
    }

    private void populerAdresse(final Faktum adresseFaktum, final AdresseFrontend adresse) {
        if (adresse.type.equals("gateadresse")) {
            populerGateadresse(adresseFaktum, adresse);
        } else if (adresse.type.equals("matrikkelAdresse")) {
            populerMatrikkeladresse(adresseFaktum, adresse);
        } else {
            throw new RuntimeException("Ukjent adressetype: " + adresse.getClass().getName());
        }
    }

    private void populerGateadresse(final Faktum adresseFaktum, final AdresseFrontend adresse) {
        final GateadresseFrontend gateadresse = adresse.gateadresse;
        adresseFaktum
                .medSystemProperty("type", adresse.type)
                .medSystemProperty("landkode", "NOR")
                .medSystemProperty("kommunenummer", gateadresse.kommunenummer)
                .medSystemProperty("bolignummer", gateadresse.bolignummer)
                .medSystemProperty("postnummer", gateadresse.postnummer)
                .medSystemProperty("poststed", gateadresse.poststed)
                .medSystemProperty("gatenavn", gateadresse.gatenavn)
                .medSystemProperty("husnummer", gateadresse.husnummer)
                .medSystemProperty("husbokstav", gateadresse.husbokstav);

        /*
         * Kombinert gatenavn og husnummer etter ønske fra interaksjonsdesigner.
         * Vises kun til bruker der adresse kan overstyres. Brukes ikke i oppsummering.
         */
        adresseFaktum.medSystemProperty("adresse", (gateadresse.gatenavn + " " + gateadresse.husnummer + gateadresse.husbokstav).trim());
    }

    private void populerMatrikkeladresse(final Faktum adresseFaktum, final AdresseFrontend adresse) {
        final MatrikkeladresseFrontend matrikkeladresse = adresse.matrikkeladresse;
        adresseFaktum
                .medSystemProperty("type", adresse.type)
                .medSystemProperty("landkode", "NOR")
                .medSystemProperty("kommunenummer", matrikkeladresse.kommunenummer)
                .medSystemProperty("gaardsnummer", matrikkeladresse.gaardsnummer)
                .medSystemProperty("bruksnummer", matrikkeladresse.bruksnummer)
                .medSystemProperty("festenummer", matrikkeladresse.festenummer)
                .medSystemProperty("seksjonsnummer", matrikkeladresse.seksjonsnummer)
                .medSystemProperty("undernummer", matrikkeladresse.undernummer);
    }

    private AdresserFrontend mapToAdresseFrontend(JsonAdresse sysFolkeregistrert, JsonAdresse sysMidlertidig, JsonAdresse jsonOpphold) {
        return new AdresserFrontend()
                .withValg(jsonOpphold.getAdresseValg())
                .withFolkeregistrert(mapToAdresseObjectFrontend(sysFolkeregistrert))
                .withMidlertidig(mapToAdresseObjectFrontend(sysMidlertidig))
                .withSoknad(mapToAdresseObjectFrontend(jsonOpphold));
    }

    private AdresseFrontend mapToAdresseObjectFrontend(JsonAdresse adresse) {
        AdresseFrontend adresseFrontend = new AdresseFrontend();
        switch (adresse.getType()){
            case GATEADRESSE:
                adresseFrontend.setType("gateadresse");
                adresseFrontend.setGateadresse(mapToGateadresseFrontend(adresse));
                break;
            case MATRIKKELADRESSE:
                adresseFrontend.setType("matrikkeladresse");
                adresseFrontend.setMatrikkeladresse(mapToMatrikkeladresseFrontend(adresse));
                break;
            case USTRUKTURERT:
                adresseFrontend.setType("ustrukturert");
                adresseFrontend.setUstrukturert(mapToUstrukturertAdresseFrontend(adresse));
                break;
        }
        return adresseFrontend;
    }

    private GateadresseFrontend mapToGateadresseFrontend(JsonAdresse adresse) {
        JsonGateAdresse gateAdresse = (JsonGateAdresse) adresse;
        return new GateadresseFrontend()
                .withLandkode(gateAdresse.getLandkode())
                .withKommunenummer(gateAdresse.getKommunenummer())
                .withAdresselinjer(gateAdresse.getAdresselinjer())
                .withBolignummer(gateAdresse.getBolignummer())
                .withPostnummer(gateAdresse.getPostnummer())
                .withPoststed(gateAdresse.getPoststed())
                .withGatenavn(gateAdresse.getGatenavn())
                .withHusnummer(gateAdresse.getHusnummer())
                .withHusbokstav(gateAdresse.getHusbokstav());
    }

    private MatrikkeladresseFrontend mapToMatrikkeladresseFrontend(JsonAdresse adresse) {
        JsonMatrikkelAdresse matrikkelAdresse = (JsonMatrikkelAdresse) adresse;
        return new MatrikkeladresseFrontend()
                .withKommunenummer(matrikkelAdresse.getKommunenummer())
                .withGaardsnummer(matrikkelAdresse.getGaardsnummer())
                .withBruksnummer(matrikkelAdresse.getBruksnummer())
                .withFestenummer(matrikkelAdresse.getFestenummer())
                .withSeksjonsnummer(matrikkelAdresse.getSeksjonsnummer())
                .withUndernummer(matrikkelAdresse.getUndernummer());
    }

    private UstrukturertAdresseFrontend mapToUstrukturertAdresseFrontend(JsonAdresse adresse){
        JsonUstrukturertAdresse ustrukturertAdresse = (JsonUstrukturertAdresse) adresse;
        return new UstrukturertAdresseFrontend().withAdresse(ustrukturertAdresse.getAdresse());
    }

    private JsonAdresse mapToJsonAdresse(AdresseFrontend adresseFrontend) {
        JsonAdresse adresse;
        switch (adresseFrontend.type){
            case "gateadresse":
                GateadresseFrontend gateadresse = adresseFrontend.gateadresse;
                adresse = new JsonGateAdresse()
                        .withKilde(JsonKilde.BRUKER)
                        .withType(JsonAdresse.Type.GATEADRESSE)
                        .withLandkode(gateadresse.landkode)
                        .withKommunenummer(gateadresse.kommunenummer)
                        .withAdresselinjer(gateadresse.adresselinjer)
                        .withBolignummer(gateadresse.bolignummer)
                        .withPostnummer(gateadresse.postnummer)
                        .withPoststed(gateadresse.poststed)
                        .withGatenavn(gateadresse.gatenavn)
                        .withHusnummer(gateadresse.husnummer)
                        .withHusbokstav(gateadresse.husbokstav);
                break;
            case "matrikkeladresse":
                MatrikkeladresseFrontend matrikkeladresse = adresseFrontend.matrikkeladresse;
                adresse = new JsonMatrikkelAdresse()
                        .withKilde(JsonKilde.BRUKER)
                        .withType(JsonAdresse.Type.MATRIKKELADRESSE)
                        .withKommunenummer(matrikkeladresse.kommunenummer)
                        .withGaardsnummer(matrikkeladresse.gaardsnummer)
                        .withBruksnummer(matrikkeladresse.bruksnummer)
                        .withFestenummer(matrikkeladresse.festenummer)
                        .withSeksjonsnummer(matrikkeladresse.seksjonsnummer)
                        .withUndernummer(matrikkeladresse.undernummer);
                break;
            default:
                throw new IllegalStateException("Ukjent adressetype: \"" + adresseFrontend.type + "\".");
        }
        return adresse;
    }

    private String mapValgToString(JsonAdresseValg valg) {
        switch (valg){
            case FOLKEREGISTRERT:
                return "folkeregistrert";
            case MIDLERTIDIG:
                return "midlertidig";
            case SOKNAD:
                return "soknad";
            default:
                return null;

        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static final class AdresserFrontend {

        public JsonAdresseValg valg;
        public AdresseFrontend folkeregistrert;
        public AdresseFrontend midlertidig;
        public AdresseFrontend soknad;

        public AdresserFrontend withValg(JsonAdresseValg valg) {
            this.valg = valg;
            return this;
        }

        public AdresserFrontend withFolkeregistrert(AdresseFrontend folkeregistrert) {
            this.folkeregistrert = folkeregistrert;
            return this;
        }

        public AdresserFrontend withMidlertidig(AdresseFrontend midlertidig) {
            this.midlertidig = midlertidig;
            return this;
        }

        public AdresserFrontend withSoknad(AdresseFrontend soknad) {
            this.soknad = soknad;
            return this;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static final class AdresseFrontend {
        public String type; // "gateadresse/matrikkeladresse"
        public JsonAdresseValg valg;
        public GateadresseFrontend gateadresse;
        public MatrikkeladresseFrontend matrikkeladresse;
        public UstrukturertAdresseFrontend ustrukturert;

        public void setType(String type) {
            this.type = type;
        }

        public void setGateadresse(GateadresseFrontend gateadresse) {
            this.gateadresse = gateadresse;
        }

        public void setMatrikkeladresse(MatrikkeladresseFrontend matrikkeladresse) {
            this.matrikkeladresse = matrikkeladresse;
        }

        public void setUstrukturert(UstrukturertAdresseFrontend ustrukturert) {
            this.ustrukturert = ustrukturert;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static final class GateadresseFrontend {
        public String type = "gateadresse";
        public String landkode;
        public String kommunenummer;
        public List<String> adresselinjer;
        public String bolignummer;
        public String postnummer;
        public String poststed;
        public String gatenavn;
        public String husnummer;
        public String husbokstav;

        public GateadresseFrontend withLandkode(String landkode) {
            this.landkode = landkode;
            return this;
        }

        public GateadresseFrontend withKommunenummer(String kommunenummer) {
            this.kommunenummer = kommunenummer;
            return this;
        }

        public GateadresseFrontend withAdresselinjer(List<String> adresselinjer) {
            this.adresselinjer = adresselinjer;
            return this;
        }

        public GateadresseFrontend withBolignummer(String bolignummer) {
            this.bolignummer = bolignummer;
            return this;
        }

        public GateadresseFrontend withPostnummer(String postnummer) {
            this.postnummer = postnummer;
            return this;
        }

        public GateadresseFrontend withPoststed(String poststed) {
            this.poststed = poststed;
            return this;
        }

        public GateadresseFrontend withGatenavn(String gatenavn) {
            this.gatenavn = gatenavn;
            return this;
        }

        public GateadresseFrontend withHusbokstav(String husbokstav) {
            this.husbokstav = husbokstav;
            return this;
        }

        public GateadresseFrontend withHusnummer(String husnummer) {
            this.husnummer = husnummer;
            return this;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static final class MatrikkeladresseFrontend {
        public String type = "matrikkeladresse";
        public String kommunenummer;
        public String gaardsnummer;
        public String bruksnummer;
        public String festenummer;
        public String seksjonsnummer;
        public String undernummer;

        public MatrikkeladresseFrontend withKommunenummer(String kommunenummer) {
            this.kommunenummer = kommunenummer;
            return this;
        }

        public MatrikkeladresseFrontend withGaardsnummer(String gaardsnummer) {
            this.gaardsnummer = gaardsnummer;
            return this;
        }

        public MatrikkeladresseFrontend withBruksnummer(String bruksnummer) {
            this.bruksnummer = bruksnummer;
            return this;
        }

        public MatrikkeladresseFrontend withFestenummer(String festenummer) {
            this.festenummer = festenummer;
            return this;
        }

        public MatrikkeladresseFrontend withSeksjonsnummer(String seksjonsnummer) {
            this.seksjonsnummer = seksjonsnummer;
            return this;
        }

        public MatrikkeladresseFrontend withUndernummer(String undernummer) {
            this.undernummer = undernummer;
            return this;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static final class UstrukturertAdresseFrontend {
        public String type = "ustrukturert";
        public List<String> adresse;

        public UstrukturertAdresseFrontend withAdresse(List<String> adresse) {
            this.adresse = adresse;
            return this;
        }
    }
}
