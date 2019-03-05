package no.nav.sbl.dialogarena.soknadinnsending.consumer.adresse;

import no.nav.sbl.dialogarena.sendsoknad.domain.adresse.AdresseForslag;
import no.nav.sbl.dialogarena.sendsoknad.domain.adresse.AdresseSokConsumer;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;


public class AdresseSokServiceTest {

    @Test
    public void skalBytteUtKommunenavnForIKSKommunerSoerFronTilNordFron() {
        AdresseSokConsumer.AdresseData adresseData = new AdresseSokConsumer.AdresseData();
        adresseData.kommunenummer = "0519";
        AdresseForslag adresseForslag = AdresseSokService.toAdresseForslag(adresseData);
        assertThat(adresseForslag.kommunenavn).isEqualTo("Nord-Fron");
    }

    @Test
    public void skalBytteUtKommunenavnForIKSKommunerRingebuTilNordFron() {
        AdresseSokConsumer.AdresseData adresseData = new AdresseSokConsumer.AdresseData();
        adresseData.kommunenummer = "0520";
        AdresseForslag adresseForslag = AdresseSokService.toAdresseForslag(adresseData);
        assertThat(adresseForslag.kommunenavn).isEqualTo("Nord-Fron");
    }

    @Test
    public void skalBytteUtKommunenavnDersomKommunenIkkeErIKSKommune() {
        AdresseSokConsumer.AdresseData adresseData = new AdresseSokConsumer.AdresseData();
        adresseData.kommunenummer = "2004";
        adresseData.kommunenavn = "IkkeIKS";
        AdresseForslag adresseForslag = AdresseSokService.toAdresseForslag(adresseData);
        assertThat(adresseForslag.kommunenavn).isEqualTo("IkkeIKS");
    }
}