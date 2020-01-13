package no.nav.sbl.dialogarena.soknadinnsending.consumer.adresse;

import no.nav.sbl.dialogarena.sendsoknad.domain.adresse.AdresseForslag;
import no.nav.sbl.dialogarena.sendsoknad.domain.adresse.AdresseSokConsumer;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;


public class AdresseSokServiceTest {

    @Test
    public void skalBytteUtKommunenavnForIKSKommunerSoerFronTilNordFron() {
        AdresseSokConsumer.AdresseData adresseData = new AdresseSokConsumer.AdresseData();
        adresseData.kommunenummer = "3438";
        adresseData.kommunenavn = "Sør-Fron";
        AdresseForslag adresseForslag = AdresseSokService.toAdresseForslag(adresseData);
        assertThat(adresseForslag.kommunenavn).isEqualTo("Nord-Fron");
    }

    @Test
    public void skalBytteUtKommunenavnForIKSKommunerRingebuTilNordFron() {
        AdresseSokConsumer.AdresseData adresseData = new AdresseSokConsumer.AdresseData();
        adresseData.kommunenummer = "3439";
        adresseData.kommunenavn = "Ringebu";
        AdresseForslag adresseForslag = AdresseSokService.toAdresseForslag(adresseData);
        assertThat(adresseForslag.kommunenavn).isEqualTo("Nord-Fron");
    }
    @Test
    public void skalBytteUtKommunenavnForIKSKommunerFlesbergTilRollag() {
        AdresseSokConsumer.AdresseData adresseData = new AdresseSokConsumer.AdresseData();
        adresseData.kommunenummer = "3050";
        adresseData.kommunenavn = "Flesberg";
        AdresseForslag adresseForslag = AdresseSokService.toAdresseForslag(adresseData);
        assertThat(adresseForslag.kommunenavn).isEqualTo("Rollag");
    }

    @Test
    public void skalBytteUtKommunenavnForIKSKommunerNoreOgUvedalTilRollag() {
        AdresseSokConsumer.AdresseData adresseData = new AdresseSokConsumer.AdresseData();
        adresseData.kommunenummer = "3052";
        adresseData.kommunenavn = "Nore og Uvdal";
        AdresseForslag adresseForslag = AdresseSokService.toAdresseForslag(adresseData);
        assertThat(adresseForslag.kommunenavn).isEqualTo("Rollag");
    }

    @Test
    public void skalBytteUtKommunenavnForIKSKommunerNoreOgUvedalTilHaugesund() {
        AdresseSokConsumer.AdresseData adresseData = new AdresseSokConsumer.AdresseData();
        adresseData.kommunenummer = "1151";
        adresseData.kommunenavn = "Utsira";
        AdresseForslag adresseForslag = AdresseSokService.toAdresseForslag(adresseData);
        assertThat(adresseForslag.kommunenavn).isEqualTo("Haugesund");
    }

    @Test
    public void skalIkkeBytteUtKommunenavnDersomKommunenIkkeErIKSKommune() {
        AdresseSokConsumer.AdresseData adresseData = new AdresseSokConsumer.AdresseData();
        adresseData.kommunenummer = "2004";
        adresseData.kommunenavn = "IkkeIKS";
        AdresseForslag adresseForslag = AdresseSokService.toAdresseForslag(adresseData);
        assertThat(adresseForslag.kommunenavn).isEqualTo("IkkeIKS");
    }
}