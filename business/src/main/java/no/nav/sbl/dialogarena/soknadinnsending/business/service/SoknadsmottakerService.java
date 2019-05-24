package no.nav.sbl.dialogarena.soknadinnsending.business.service;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonAdresse;
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonGateAdresse;
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonMatrikkelAdresse;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonPersonalia;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.adresse.AdresseForslag;
import no.nav.sbl.dialogarena.sendsoknad.domain.adresse.AdresseSokConsumer.Sokedata;
import no.nav.sbl.dialogarena.sendsoknad.domain.adresse.AdresseSokConsumer.Soketype;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.adresse.AdresseSokService;

@Component
public class SoknadsmottakerService {
    private static final Logger logger = LoggerFactory.getLogger(SoknadsmottakerService.class);

    @Inject
    private AdresseSokService adresseSokService;

    public List<AdresseForslag> legacyFinnAdresseFraSoknad(final WebSoknad webSoknad) {
        return legacyFinnAdresseFraSoknad(webSoknad, null);
    }

    public List<AdresseForslag> legacyFinnAdresseFraSoknad(final WebSoknad webSoknad, String valg) {
        final Faktum adresseFaktum = hentAdresseFaktum(webSoknad, valg);
        if (adresseFaktum == null) {
            return Collections.emptyList();
        }
        
        final Map<String, String> adresse = adresseFaktum.getProperties();
        return legacySoknadsmottakerGitt(adresse);
    }

    public List<AdresseForslag> finnAdresseFraSoknad(final JsonPersonalia personalia, String valg) {
        final JsonAdresse adresse = hentValgtAdresse(personalia, valg);

        return soknadsmottakerGitt(adresse);
    }

    private JsonAdresse hentValgtAdresse(JsonPersonalia personalia, String valg) {
        if (valg == null) {
            return null;
        }
        switch (valg){
            case "folkeregistrert":
                return personalia.getFolkeregistrertAdresse();
            case "midlertidig":
            case "soknad":
                return personalia.getOppholdsadresse();
            default:
                return null;
        }
    }

    private List<AdresseForslag> soknadsmottakerGitt(final JsonAdresse adresse) {
        if (adresse == null) {
            return Collections.emptyList();
        }

        if (adresse.getType().equals(JsonAdresse.Type.MATRIKKELADRESSE)) {
            final JsonMatrikkelAdresse matrikkelAdresse = (JsonMatrikkelAdresse) adresse;
            final String kommunenummer = matrikkelAdresse.getKommunenummer();
            if (kommunenummer == null || kommunenummer.trim().equals("")) {
                return Collections.emptyList();
            }

            return adresseSokService.sokEtterNavKontor(new Sokedata().withKommunenummer(kommunenummer));
        } else if (adresse.getType().equals(JsonAdresse.Type.GATEADRESSE)) {
            final JsonGateAdresse gateAdresse = (JsonGateAdresse) adresse;
            final List<AdresseForslag> adresser = adresseSokService.sokEtterAdresser(new Sokedata()
                    .withSoketype(Soketype.EKSAKT)
                    .withAdresse(gateAdresse.getGatenavn())
                    .withHusnummer(gateAdresse.getHusnummer())
                    .withHusbokstav(gateAdresse.getHusbokstav())
                    .withPostnummer(gateAdresse.getPostnummer())
                    .withPoststed(gateAdresse.getPoststed())
            );

            if (adresser.size() <= 1) {
                return adresser;
            }

            if (hasIkkeUnikGate(adresser)) {
                return Collections.emptyList();
            }

            return adresser;
        } else {
            return Collections.emptyList();
        }
    }


    private List<AdresseForslag> legacySoknadsmottakerGitt(final Map<String, String> adresse) {
        if (adresse == null || adresse.isEmpty()) {
            return Collections.emptyList();
        }
        
        final String type = adresse.get("type");
        if (type == null || type.trim().equals("")) {
            return Collections.emptyList();
        }
        
        if ("matrikkeladresse".equals(type)) {
            final String kommunenummer = adresse.get("kommunenummer");
            if (kommunenummer == null || kommunenummer.trim().equals("")) {
                return Collections.emptyList();
            }
            
            return adresseSokService.sokEtterNavKontor(new Sokedata().withKommunenummer(kommunenummer));
        } else {
            final List<AdresseForslag> adresser = adresseSokService.sokEtterAdresser(new Sokedata()
                        .withSoketype(Soketype.EKSAKT)
                        .withAdresse(nullIfEmpty(adresse.get("gatenavn")))
                        .withHusnummer(nullIfEmpty(adresse.get("husnummer")))
                        .withHusbokstav(nullIfEmpty(adresse.get("husbokstav")))
                        .withPostnummer(nullIfEmpty(adresse.get("postnummer")))
                        .withPoststed(nullIfEmpty(adresse.get("poststed")))
                    );
            
            if (adresser.size() <= 1) {
                return adresser;
            }
            
            if (hasIkkeUnikGate(adresser)) {
                return Collections.emptyList();
            }

            return adresser;
        }
    }
    
    private boolean hasIkkeUnikGate(List<AdresseForslag> adresser) {
        final AdresseForslag forste = adresser.get(0);
        if (forste.adresse == null || forste.postnummer == null || forste.poststed == null) {
            return true;
        }
        return adresser.stream().anyMatch(af -> {
            return !forste.adresse.equals(af.adresse)
                    || !forste.postnummer.equals(af.postnummer)
                    || !forste.poststed.equals(af.poststed);
        });
    }

    private static String nullIfEmpty(String s) {
        if (s != null && s.trim().equals("")) {
            return null;
        }
        return s;
    }

    Faktum hentAdresseFaktum(final WebSoknad webSoknad) {
        return hentAdresseFaktum(webSoknad, null);
    }

    Faktum hentAdresseFaktum(final WebSoknad webSoknad, final String valg) {
        if (webSoknad == null) {
            logger.warn("Søknaden er null");
            return null;
        }
        final String adressevalg;
        if (valg != null && !"".equals(valg.trim())) {
            adressevalg = valg;
        } else {
            adressevalg = webSoknad.getValueForFaktum("kontakt.system.oppholdsadresse.valg");
        }

        if ("folkeregistrert".equals(adressevalg)) {
            return webSoknad.getFaktumMedKey("kontakt.system.folkeregistrert.adresse");
        } else if ("midlertidig".equals(adressevalg)) {
            return webSoknad.getFaktumMedKey("kontakt.system.adresse");
        } else if ("soknad".equals(adressevalg)){
            return webSoknad.getFaktumMedKey("kontakt.adresse.bruker");
        } else {
            return null;
        }
    }
}
