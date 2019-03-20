package no.nav.sbl.dialogarena.rest.ressurser.personalia;

import no.nav.metrics.aspects.Timed;
import no.nav.sbl.dialogarena.rest.ressurser.LegacyHelper;
import no.nav.sbl.dialogarena.rest.ressurser.SoknadsmottakerRessurs;
import no.nav.sbl.dialogarena.rest.ressurser.personalia.adresse.AdresseMapper;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.adresse.AdresseForslag;
import no.nav.sbl.dialogarena.sendsoknad.domain.norg.NavEnhet;
import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.SubjectHandler;
import no.nav.sbl.dialogarena.sendsoknad.domain.util.KommuneTilNavEnhetMapper;
import no.nav.sbl.dialogarena.sikkerhet.Tilgangskontroll;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.FaktaService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.SoknadsmottakerService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.systemdata.AdresseSystemdata;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.norg.NorgService;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonAdresse;
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonAdresseValg;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonPersonalia;
import no.nav.sbl.sosialhjelp.domain.SoknadUnderArbeid;
import no.nav.sbl.sosialhjelp.soknadunderbehandling.SoknadUnderArbeidRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static no.nav.sbl.dialogarena.sendsoknad.domain.util.KommuneTilNavEnhetMapper.getFeaturesForEnhet;

@Controller
@Path("/soknader/{behandlingsId}/personalia/adresser")
@Timed
@Produces(APPLICATION_JSON)
public class AdresseRessurs {
    private static final Logger logger = LoggerFactory.getLogger(AdresseRessurs.class);

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
    private SoknadsmottakerService soknadsmottakerService;

    @Inject
    private NorgService norgService;

    @Inject
    private AdresseMapper mapper;

    @GET
    public AdresserFrontend hentAdresser(@PathParam("behandlingsId") String behandlingsId) {
        final String eier = SubjectHandler.getUserIdFromToken();
        final JsonInternalSoknad soknad = legacyHelper.hentSoknad(behandlingsId, eier, false).getJsonInternalSoknad();
        final String personIdentifikator = soknad.getSoknad().getData().getPersonalia().getPersonIdentifikator().getVerdi();
        final JsonAdresse jsonOppholdsadresse = soknad.getSoknad().getData().getPersonalia().getOppholdsadresse();

        final JsonAdresse sysFolkeregistrertAdresse = adresseSystemdata.innhentFolkeregistrertAdresse(personIdentifikator);
        final JsonAdresse sysMidlertidigAdresse = adresseSystemdata.innhentMidlertidigAdresse(personIdentifikator);

        return mapper.mapToAdresserFrontend(sysFolkeregistrertAdresse, sysMidlertidigAdresse, jsonOppholdsadresse);
    }

    @PUT
    public List<NavEnhetFrontend> updateAdresse(@PathParam("behandlingsId") String behandlingsId, AdresseFrontend adresseFrontend) {
        tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId);
        update(behandlingsId, adresseFrontend);
        legacyUpdate(behandlingsId, adresseFrontend);
        /*return findSoknadsmottaker(behandlingsId, mapper.mapValgToString(adresseFrontend.valg)); Bruk når faktum er fjernet*/
        return soknadsmottakerRessurs.findSoknadsmottaker(behandlingsId, mapper.mapValgToString(adresseFrontend.valg))
                .stream().map(this::mapFromLegacyNavEnhetFrontend).collect(Collectors.toList());
    }

    private NavEnhetFrontend mapFromLegacyNavEnhetFrontend(SoknadsmottakerRessurs.LegacyNavEnhetFrontend legacyNavEnhetFrontend) {
        return new NavEnhetFrontend()
                .withEnhetsId(legacyNavEnhetFrontend.enhetsId)
                .withEnhetsnavn(legacyNavEnhetFrontend.enhetsnavn)
                .withBydelsnummer(legacyNavEnhetFrontend.bydelsnummer)
                .withKommunenummer(legacyNavEnhetFrontend.kommunenummer)
                .withKommunenavn(legacyNavEnhetFrontend.kommunenavn)
                .withSosialOrgnr(legacyNavEnhetFrontend.sosialOrgnr)
                .withFeatures(legacyNavEnhetFrontend.features);
    }

    private List<NavEnhetFrontend> findSoknadsmottaker(String behandlingsId, String valg) {
        final String eier = SubjectHandler.getUserIdFromToken();
        final SoknadUnderArbeid soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier).get();
        final JsonPersonalia personalia = soknad.getJsonInternalSoknad().getSoknad().getData().getPersonalia();

        final List<AdresseForslag> adresseForslagene = soknadsmottakerService.finnAdresseFraSoknad(personalia, valg);
        /*
         * Vi fjerner nå duplikate NAV-enheter med forskjellige bydelsnumre gjennom
         * bruk av distinct. Hvis det er viktig med riktig bydelsnummer bør dette kallet
         * fjernes og brukeren må besvare hvilken bydel han/hun oppholder seg i.
         */
        return adresseForslagene.stream().map((adresseForslag) -> {
            final NavEnhet navEnhet = norgService.finnEnhetForGt(adresseForslag.geografiskTilknytning);
            return mapFraAdresseForslagOgNavEnhetTilNavEnhetFrontend(adresseForslag, navEnhet);
        }).filter(Objects::nonNull).distinct().collect(Collectors.toList());
    }

    private NavEnhetFrontend mapFraAdresseForslagOgNavEnhetTilNavEnhetFrontend(AdresseForslag adresseForslag, NavEnhet navEnhet) {
        if (navEnhet == null) {
            logger.warn("Kunne ikke hente NAV-enhet: " + adresseForslag.geografiskTilknytning);
            return null;
        }
        if (adresseForslag.kommunenummer == null
                || adresseForslag.kommunenummer.length() != 4) {
            return null;
        }

        final boolean digisosKommune = KommuneTilNavEnhetMapper.getDigisoskommuner().contains(adresseForslag.kommunenummer);
        return new NavEnhetFrontend()
                .withEnhetsId(navEnhet.enhetNr)
                .withEnhetsnavn(navEnhet.navn)
                .withBydelsnummer(adresseForslag.bydel)
                .withKommunenummer(adresseForslag.kommunenummer)
                .withKommunenavn(adresseForslag.kommunenavn)
                .withSosialOrgnr((digisosKommune) ? navEnhet.sosialOrgnr : null)
                .withFeatures(getFeaturesForEnhet(navEnhet.enhetNr));
    }

    private void update(String behandlingsId, AdresseFrontend adresseFrontend) {
        final String eier = SubjectHandler.getUserIdFromToken();
        final SoknadUnderArbeid soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier).get();
        final JsonPersonalia personalia = soknad.getJsonInternalSoknad().getSoknad().getData().getPersonalia();

        switch (adresseFrontend.valg){
            case FOLKEREGISTRERT:
                personalia.setOppholdsadresse(adresseSystemdata.innhentFolkeregistrertAdresse(eier));
                personalia.getOppholdsadresse().setAdresseValg(JsonAdresseValg.FOLKEREGISTRERT);
                break;
            case MIDLERTIDIG:
                personalia.setOppholdsadresse(adresseSystemdata.innhentMidlertidigAdresse(eier));
                personalia.getOppholdsadresse().setAdresseValg(JsonAdresseValg.MIDLERTIDIG);
                break;
            case SOKNAD:
                personalia.setOppholdsadresse(mapper.mapToJsonAdresse(adresseFrontend));
                personalia.getOppholdsadresse().setAdresseValg(JsonAdresseValg.SOKNAD);
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
        public String type; // "gateadresse/matrikkeladresse/ustrukturert"
        public JsonAdresseValg valg;
        public GateadresseFrontend gateadresse;
        public MatrikkeladresseFrontend matrikkeladresse;
        public UstrukturertAdresseFrontend ustrukturert;

        public void setType(String type) {
            this.type = type;
        }

        public void setValg(JsonAdresseValg valg) {
            this.valg = valg;
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

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class NavEnhetFrontend {
        public String enhetsId;
        public String enhetsnavn;
        public String kommunenummer;
        public String kommunenavn;
        public String bydelsnummer;
        public String sosialOrgnr;
        public Map<String, Boolean> features;

        NavEnhetFrontend withEnhetsId(String enhetsId) {
            this.enhetsId = enhetsId;
            return this;
        }

        NavEnhetFrontend withEnhetsnavn(String enhetsnavn) {
            this.enhetsnavn = enhetsnavn;
            return this;
        }

        NavEnhetFrontend withKommunenummer(String kommunenummer) {
            this.kommunenummer = kommunenummer;
            return this;
        }

        NavEnhetFrontend withKommunenavn(String kommunenavn) {
            this.kommunenavn = kommunenavn;
            return this;
        }

        NavEnhetFrontend withBydelsnummer(String bydelsnummer) {
            this.bydelsnummer = bydelsnummer;
            return this;
        }

        NavEnhetFrontend withSosialOrgnr(String sosialOrgnr) {
            this.sosialOrgnr = sosialOrgnr;
            return this;
        }

        NavEnhetFrontend withFeatures(Map<String, Boolean> features) {
            this.features = features;
            return this;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((enhetsId == null) ? 0 : enhetsId.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            SoknadsmottakerRessurs.LegacyNavEnhetFrontend other = (SoknadsmottakerRessurs.LegacyNavEnhetFrontend) obj;
            if (enhetsId == null) {
                if (other.enhetsId != null)
                    return false;
            } else if (!enhetsId.equals(other.enhetsId))
                return false;
            return true;
        }
    }
}
