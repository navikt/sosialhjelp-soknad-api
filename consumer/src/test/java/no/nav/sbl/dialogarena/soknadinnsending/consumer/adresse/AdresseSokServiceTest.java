package no.nav.sbl.dialogarena.soknadinnsending.consumer.adresse;

import no.nav.sbl.dialogarena.sendsoknad.domain.adresse.AdresseForslag;
import no.nav.sbl.dialogarena.sendsoknad.domain.adresse.AdresseSokConsumer;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;


public class AdresseSokServiceTest {

    @Test
    public void skal_bytte_ut_kommunenavn_for_iks_kommuner() {
        AdresseSokConsumer.AdresseData adresseData = new AdresseSokConsumer.AdresseData();
        adresseData.kommunenummer = "0516";
        AdresseForslag adresseForslag = AdresseSokService.toAdresseForslag(adresseData);
        assertThat(adresseForslag.kommunenavn).isEqualTo("Sør-Fron");
    }
}