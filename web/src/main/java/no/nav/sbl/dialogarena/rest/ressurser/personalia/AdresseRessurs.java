package no.nav.sbl.dialogarena.rest.ressurser.personalia;

import no.nav.metrics.aspects.Timed;
import no.nav.sbl.dialogarena.rest.mappers.AdresseMapper;
import no.nav.sbl.dialogarena.rest.ressurser.LegacyHelper;
import no.nav.sbl.dialogarena.rest.ressurser.SoknadsmottakerRessurs;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.OidcFeatureToggleUtils;
import no.nav.sbl.dialogarena.sikkerhet.Tilgangskontroll;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.FaktaService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.systemdata.AdresseSystemdata;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonAdresse;
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonAdresseValg;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonPersonalia;
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

@Controller
@ProtectedWithClaims(issuer = "selvbetjening", claimMap = { "acr=Level4" })
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

    @Inject
    private NavEnhetRessurs navEnhetRessurs;

    @GET
    public AdresserFrontend hentAdresser(@PathParam("behandlingsId") String behandlingsId) {
        final String eier = OidcFeatureToggleUtils.getUserId();
        final JsonInternalSoknad soknad = legacyHelper.hentSoknad(behandlingsId, eier, false).getJsonInternalSoknad();
        final String personIdentifikator = soknad.getSoknad().getData().getPersonalia().getPersonIdentifikator().getVerdi();
        final JsonAdresse jsonOppholdsadresse = soknad.getSoknad().getData().getPersonalia().getOppholdsadresse();

        final JsonAdresse sysFolkeregistrertAdresse = soknad.getSoknad().getData().getPersonalia().getFolkeregistrertAdresse();
        final JsonAdresse sysMidlertidigAdresse = adresseSystemdata.innhentMidlertidigAdresse(personIdentifikator);

        return AdresseMapper.mapToAdresserFrontend(sysFolkeregistrertAdresse, sysMidlertidigAdresse, jsonOppholdsadresse);
    }

    @PUT
    public List<NavEnhetRessurs.NavEnhetFrontend> updateAdresse(@PathParam("behandlingsId") String behandlingsId, AdresserFrontend adresserFrontend) {
        tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId);
        update(behandlingsId, adresserFrontend);
        legacyUpdate(behandlingsId, adresserFrontend);

        /*return findSoknadsmottaker(behandlingsId, mapper.mapValgToString(adresserFrontend.valg)); Bruk når faktum er fjernet*/
        return soknadsmottakerRessurs.findSoknadsmottaker(behandlingsId, adresserFrontend.valg.toString())
                .stream().map(navEnhet -> navEnhetRessurs.mapFromLegacyNavEnhetFrontend(navEnhet, null)).collect(Collectors.toList());
    }

    private void update(String behandlingsId, AdresserFrontend adresserFrontend) {
        final String eier = OidcFeatureToggleUtils.getUserId();
        final SoknadUnderArbeid soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier).get();
        final JsonPersonalia personalia = soknad.getJsonInternalSoknad().getSoknad().getData().getPersonalia();

        switch (adresserFrontend.valg){
            case FOLKEREGISTRERT:
                personalia.setOppholdsadresse(adresseSystemdata.createDeepCopyOfJsonAdresse(personalia.getFolkeregistrertAdresse()));
                break;
            case MIDLERTIDIG:
                personalia.setOppholdsadresse(adresseSystemdata.innhentMidlertidigAdresse(eier));
                break;
            case SOKNAD:
                personalia.setOppholdsadresse(AdresseMapper.mapToJsonAdresse(adresserFrontend.soknad));
                break;
        }

        personalia.getOppholdsadresse().setAdresseValg(adresserFrontend.valg);
        personalia.setPostadresse(midlertidigLosningForPostadresse(personalia.getOppholdsadresse()));

        soknadUnderArbeidRepository.oppdaterSoknadsdata(soknad, eier);
    }

    private void legacyUpdate(String behandlingsId, AdresserFrontend adresserFrontend) {
        final WebSoknad webSoknad = soknadService.hentSoknad(behandlingsId, false, false);

        final Faktum valg = faktaService.hentFaktumMedKey(webSoknad.getSoknadId(), "kontakt.system.oppholdsadresse.valg");
        valg.setValue(adresserFrontend.valg.toString());
        faktaService.lagreBrukerFaktum(valg);

        final Faktum brukerdefinert = faktaService.hentFaktumMedKey(webSoknad.getSoknadId(), "kontakt.adresse.brukerendrettoggle");

        switch (adresserFrontend.valg){
            case FOLKEREGISTRERT:
            case MIDLERTIDIG:
                brukerdefinert.setValue(Boolean.toString(false));
                break;
            case SOKNAD:
                brukerdefinert.setValue(Boolean.toString(true));
                final Faktum brukerAdresse = faktaService.hentFaktumMedKey(webSoknad.getSoknadId(), "kontakt.adresse.bruker");
                populerAdresse(brukerAdresse, adresserFrontend.soknad);
                faktaService.lagreBrukerFaktum(brukerAdresse);
                break;
        }

        faktaService.lagreBrukerFaktum(brukerdefinert);
    }

    private JsonAdresse midlertidigLosningForPostadresse(JsonAdresse oppholdsadresse) {
        if (oppholdsadresse == null){
            return null;
        }
        if (oppholdsadresse.getType() == JsonAdresse.Type.MATRIKKELADRESSE){
            return null;
        }
        return adresseSystemdata.createDeepCopyOfJsonAdresse(oppholdsadresse).withAdresseValg(null);
    }

    private void populerAdresse(final Faktum adresseFaktum, final AdresseFrontend adresse) {
        if (adresse.type.equals(JsonAdresse.Type.GATEADRESSE)) {
            populerGateadresse(adresseFaktum, adresse);
        } else if (adresse.type.equals(JsonAdresse.Type.MATRIKKELADRESSE)) {
            populerMatrikkeladresse(adresseFaktum, adresse);
        } else {
            throw new RuntimeException("Ukjent adressetype: " + adresse.getClass().getName());
        }
    }

    private void populerGateadresse(final Faktum adresseFaktum, final AdresseFrontend adresse) {
        final GateadresseFrontend gateadresse = adresse.gateadresse;
        adresseFaktum
                .medSystemProperty("type", adresse.type.toString())
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
                .medSystemProperty("type", adresse.type.toString())
                .medSystemProperty("landkode", "NOR")
                .medSystemProperty("kommunenummer", matrikkeladresse.kommunenummer)
                .medSystemProperty("gaardsnummer", matrikkeladresse.gaardsnummer)
                .medSystemProperty("bruksnummer", matrikkeladresse.bruksnummer)
                .medSystemProperty("festenummer", matrikkeladresse.festenummer)
                .medSystemProperty("seksjonsnummer", matrikkeladresse.seksjonsnummer)
                .medSystemProperty("undernummer", matrikkeladresse.undernummer);
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
        public JsonAdresse.Type type;
        public GateadresseFrontend gateadresse;
        public MatrikkeladresseFrontend matrikkeladresse;
        public UstrukturertAdresseFrontend ustrukturert;

        public void setType(JsonAdresse.Type type) {
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
        public List<String> adresse;

        public UstrukturertAdresseFrontend withAdresse(List<String> adresse) {
            this.adresse = adresse;
            return this;
        }
    }
}
