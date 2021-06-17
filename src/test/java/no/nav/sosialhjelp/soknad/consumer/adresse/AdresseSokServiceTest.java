package no.nav.sosialhjelp.soknad.consumer.adresse;

import no.finn.unleash.Unleash;
import no.nav.sosialhjelp.soknad.consumer.kodeverk.KodeverkService;
import no.nav.sosialhjelp.soknad.domain.model.adresse.AdresseForslag;
import no.nav.sosialhjelp.soknad.domain.model.adresse.AdresseSokConsumer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class AdresseSokServiceTest {

    @Mock
    private AdresseSokConsumer adresseSokConsumer;

    @Mock
    private KodeverkService kodeverkService;

    @Mock
    private Unleash unleash;


    @InjectMocks
    private AdresseSokService adresseSokService;

    @Before
    public void setUp() throws Exception {
        when(unleash.isEnabled(any(), anyBoolean())).thenReturn(false);
    }

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

    @Test
    public void sokEtterAdresserString_medNullAdresseString_skalGiTomtResultat() {
        List<AdresseForslag> adresseForslags = adresseSokService.sokEtterAdresser((String) null);
        assertThat(adresseForslags).isEmpty();
    }

    @Test
    public void sokEtterAdresserString_medAdressePaEnBokstav_skalGiTomtResultat() {
        List<AdresseForslag> adresseForslags = adresseSokService.sokEtterAdresser("a");
        assertThat(adresseForslags).isEmpty();
    }

    @Test
    public void sokEtterAdresserString_medAdressePaToBokstaver_skalGiTomtResultat() {
        String adressenavn = "Sæ";
        when(adresseSokConsumer.sokAdresse(any())).thenReturn(mockAddressResponse(adressenavn));

        List<AdresseForslag> adresseForslags = adresseSokService.sokEtterAdresser(adressenavn);
        assertThat(adresseForslags).isNotEmpty();
    }

    @Test
    public void sokEtterAdresserSokedata_medNullSokedata_skalGiTomtResultat() {
        List<AdresseForslag> adresseForslags = adresseSokService.sokEtterAdresser((AdresseSokConsumer.Sokedata) null);
        assertThat(adresseForslags).isEmpty();
    }

    @Test
    public void sokEtterAdresserSokedata_derSokedataHarNullAdresse_skalGiTomtResultat() {
        List<AdresseForslag> adresseForslags = adresseSokService.sokEtterAdresser(new AdresseSokConsumer.Sokedata());
        assertThat(adresseForslags).isEmpty();
    }

    @Test
    public void sokEtterAdresserSokedata_medAdressePaEnBokstav_skalGiTomtResultat() {
        AdresseSokConsumer.Sokedata sokedata = new AdresseSokConsumer.Sokedata();
        sokedata.adresse = "a";
        List<AdresseForslag> adresseForslags = adresseSokService.sokEtterAdresser(sokedata);
        assertThat(adresseForslags).isEmpty();
    }

    @Test
    public void sokEtterAdresserSokedata_medAdressePaToBokstaver_skalGiResultat() {
        String adressenavn = "Sæ";
        when(adresseSokConsumer.sokAdresse(any())).thenReturn(mockAddressResponse(adressenavn));

        AdresseSokConsumer.Sokedata sokedata = new AdresseSokConsumer.Sokedata();
        sokedata.adresse = adressenavn;
        List<AdresseForslag> adresseForslags = adresseSokService.sokEtterAdresser(sokedata);

        assertThat(adresseForslags).isNotEmpty();
    }


    private static AdresseSokConsumer.AdressesokRespons mockAddressResponse(String adressenavn){
        AdresseSokConsumer.AdressesokRespons adressesokRespons = new AdresseSokConsumer.AdressesokRespons();
        adressesokRespons.adresseDataList.add(mockAddress(adressenavn));
        return adressesokRespons;
    }

    private static AdresseSokConsumer.AdresseData mockAddress(String adressenavn){
        final AdresseSokConsumer.AdresseData a1 = new AdresseSokConsumer.AdresseData();
        a1.adressenavn = adressenavn;
        a1.postnummer = "5417";
        a1.poststed = "Stord";
        a1.gatekode = "1111";
        return a1;
    }

}