package no.nav.sosialhjelp.soknad.business.service;

import no.finn.unleash.Unleash;
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonAdresse;
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonGateAdresse;
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonMatrikkelAdresse;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonPersonalia;
import no.nav.sosialhjelp.soknad.consumer.adresse.TpsAdresseSokService;
import no.nav.sosialhjelp.soknad.consumer.pdl.adressesok.PdlAdresseSokService;
import no.nav.sosialhjelp.soknad.domain.model.adresse.AdresseForslag;
import no.nav.sosialhjelp.soknad.domain.model.adresse.AdresseForslagType;
import no.nav.sosialhjelp.soknad.domain.model.adresse.AdresseSokConsumer.Sokedata;
import no.nav.sosialhjelp.soknad.domain.model.adresse.AdresseSokConsumer.Soketype;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

import static no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonAdresseValg.FOLKEREGISTRERT;
import static org.slf4j.LoggerFactory.getLogger;

@Component
public class SoknadsmottakerService {

    private static final Logger log = getLogger(SoknadsmottakerService.class);

    private static final String FEATURE_PDL_ADRESSESOK_VED_FOLKEREGISTRERT_ADRESSE = "sosialhjelp.soknad.pdl-adressesok-ved-folkeregistrert-adresse";

    private final TpsAdresseSokService tpsAdresseSokService;
    private final PdlAdresseSokService pdlAdresseSokService;
    private final Unleash unleash;

    public SoknadsmottakerService(
            TpsAdresseSokService tpsAdresseSokService,
            PdlAdresseSokService pdlAdresseSokService,
            Unleash unleash
    ) {
        this.tpsAdresseSokService = tpsAdresseSokService;
        this.pdlAdresseSokService = pdlAdresseSokService;
        this.unleash = unleash;
    }

    public List<AdresseForslag> finnAdresseFraSoknad(final JsonPersonalia personalia, String valg) {
        var adresse = hentValgtAdresse(personalia, valg);

        // prøv å ta i bruk pdl adressesok kun ved valgt folkeregistrert adresse
        if (FOLKEREGISTRERT.toString().equals(valg) && unleash.isEnabled(FEATURE_PDL_ADRESSESOK_VED_FOLKEREGISTRERT_ADRESSE, false)) {
            try {
                return getAdresseForslagFraPDL(adresse);
            } catch (Exception e) {
                log.warn("Noe uventet feilet ved henting av adresse fra PDL -> Fallback til TPS adressesøk", e);
                return soknadsmottakerGitt(adresse);
            }
        }

        return soknadsmottakerGitt(adresse);
    }

    private JsonAdresse hentValgtAdresse(JsonPersonalia personalia, String valg) {
        if (valg == null) {
            return null;
        }
        switch (valg) {
            case "folkeregistrert":
                return personalia.getFolkeregistrertAdresse();
            case "midlertidig":
            case "soknad":
                return personalia.getOppholdsadresse();
            default:
                return null;
        }
    }

    private List<AdresseForslag> getAdresseForslagFraPDL(JsonAdresse adresse) {
        if (adresse == null) {
            return Collections.emptyList();
        }
        if (adresse.getType().equals(JsonAdresse.Type.MATRIKKELADRESSE)) {
            return adresseForslagForMatrikkelAdresse((JsonMatrikkelAdresse) adresse);
        } else if (adresse.getType().equals(JsonAdresse.Type.GATEADRESSE)) {
            var sokedata = sokedateFromGateAdresse((JsonGateAdresse) adresse);
            var adresseForslag = pdlAdresseSokService.getAdresseForslag(sokedata);
            return Collections.singletonList(adresseForslag);
        }
        return Collections.emptyList();
    }

    private Sokedata sokedateFromGateAdresse(JsonGateAdresse adresse) {
        return new Sokedata()
                .withSoketype(Soketype.EKSAKT)
                .withAdresse(adresse.getGatenavn())
                .withHusnummer(adresse.getHusnummer())
                .withHusbokstav(adresse.getHusbokstav())
                .withPostnummer(adresse.getPostnummer())
                .withPoststed(adresse.getPoststed());
    }

    private List<AdresseForslag> soknadsmottakerGitt(final JsonAdresse adresse) {
        if (adresse == null) {
            return Collections.emptyList();
        }

        if (adresse.getType().equals(JsonAdresse.Type.MATRIKKELADRESSE)) {
            return adresseForslagForMatrikkelAdresse((JsonMatrikkelAdresse) adresse);
        } else if (adresse.getType().equals(JsonAdresse.Type.GATEADRESSE)) {
            var sokedata = sokedateFromGateAdresse((JsonGateAdresse) adresse);
            var adresser = tpsAdresseSokService.sokEtterAdresser(sokedata);

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

    private List<AdresseForslag> adresseForslagForMatrikkelAdresse(JsonMatrikkelAdresse adresse) {
        var kommunenummer = adresse.getKommunenummer();
        if (kommunenummer == null || kommunenummer.trim().equals("")) {
            return Collections.emptyList();
        }

        var adresseForslag = new AdresseForslag();
        adresseForslag.type = AdresseForslagType.MATRIKKELADRESSE;
        adresseForslag.kommunenummer = kommunenummer;

        return Collections.singletonList(adresseForslag);
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
