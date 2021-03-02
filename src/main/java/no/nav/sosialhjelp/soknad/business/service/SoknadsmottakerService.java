package no.nav.sosialhjelp.soknad.business.service;

import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonAdresse;
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonGateAdresse;
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonMatrikkelAdresse;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonPersonalia;
import no.nav.sosialhjelp.soknad.consumer.adresse.AdresseSokService;
import no.nav.sosialhjelp.soknad.domain.model.adresse.AdresseForslag;
import no.nav.sosialhjelp.soknad.domain.model.adresse.AdresseForslagType;
import no.nav.sosialhjelp.soknad.domain.model.adresse.AdresseSokConsumer.Sokedata;
import no.nav.sosialhjelp.soknad.domain.model.adresse.AdresseSokConsumer.Soketype;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;

@Component
public class SoknadsmottakerService {

    @Inject
    private AdresseSokService adresseSokService;

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

            AdresseForslag adresseForslag = new AdresseForslag();
            adresseForslag.type = AdresseForslagType.matrikkelAdresse;
            adresseForslag.kommunenummer = kommunenummer;

            return Collections.singletonList(adresseForslag);

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
    
    private boolean hasIkkeUnikGate(List<AdresseForslag> adresser) {
        final AdresseForslag forste = adresser.get(0);
        if (forste.adresse == null || forste.postnummer == null || forste.poststed == null) {
            return true;
        }
        return adresser.stream().anyMatch(af -> !forste.adresse.equals(af.adresse)
                || !forste.postnummer.equals(af.postnummer)
                || !forste.poststed.equals(af.poststed));
    }
}
