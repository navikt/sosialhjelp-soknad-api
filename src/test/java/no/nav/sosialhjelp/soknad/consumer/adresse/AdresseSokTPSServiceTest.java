package no.nav.sosialhjelp.soknad.consumer.adresse;

import no.nav.sosialhjelp.soknad.business.service.adressesok.AdresseForslag;
import no.nav.sosialhjelp.soknad.business.service.adressesok.Sokedata;
import no.nav.sosialhjelp.soknad.consumer.kodeverk.KodeverkService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdresseSokTPSServiceTest {

    @Mock
    private AdresseSokConsumer adresseSokConsumer;

    @Mock
    private KodeverkService kodeverkService;

    @InjectMocks
    private TpsAdresseSokService tpsAdresseSokService;

    @Test
    void skalBytteUtKommunenavnForIKSKommunerSoerFronTilNordFron() {
        AdresseSokConsumer.AdresseData adresseData = new AdresseSokConsumer.AdresseData();
        adresseData.kommunenummer = "3438";
        adresseData.kommunenavn = "Sør-Fron";
        AdresseForslag adresseForslag = TpsAdresseSokService.toAdresseForslag(adresseData);
        assertThat(adresseForslag.kommunenavn).isEqualTo("Nord-Fron");
    }

    @Test
    void skalBytteUtKommunenavnForIKSKommunerRingebuTilNordFron() {
        AdresseSokConsumer.AdresseData adresseData = new AdresseSokConsumer.AdresseData();
        adresseData.kommunenummer = "3439";
        adresseData.kommunenavn = "Ringebu";
        AdresseForslag adresseForslag = TpsAdresseSokService.toAdresseForslag(adresseData);
        assertThat(adresseForslag.kommunenavn).isEqualTo("Nord-Fron");
    }
    @Test
    void skalBytteUtKommunenavnForIKSKommunerFlesbergTilRollag() {
        AdresseSokConsumer.AdresseData adresseData = new AdresseSokConsumer.AdresseData();
        adresseData.kommunenummer = "3050";
        adresseData.kommunenavn = "Flesberg";
        AdresseForslag adresseForslag = TpsAdresseSokService.toAdresseForslag(adresseData);
        assertThat(adresseForslag.kommunenavn).isEqualTo("Rollag");
    }

    @Test
    void skalBytteUtKommunenavnForIKSKommunerNoreOgUvedalTilRollag() {
        AdresseSokConsumer.AdresseData adresseData = new AdresseSokConsumer.AdresseData();
        adresseData.kommunenummer = "3052";
        adresseData.kommunenavn = "Nore og Uvdal";
        AdresseForslag adresseForslag = TpsAdresseSokService.toAdresseForslag(adresseData);
        assertThat(adresseForslag.kommunenavn).isEqualTo("Rollag");
    }

    @Test
    void skalBytteUtKommunenavnForIKSKommunerNoreOgUvedalTilHaugesund() {
        AdresseSokConsumer.AdresseData adresseData = new AdresseSokConsumer.AdresseData();
        adresseData.kommunenummer = "1151";
        adresseData.kommunenavn = "Utsira";
        AdresseForslag adresseForslag = TpsAdresseSokService.toAdresseForslag(adresseData);
        assertThat(adresseForslag.kommunenavn).isEqualTo("Haugesund");
    }

    @Test
    void skalIkkeBytteUtKommunenavnDersomKommunenIkkeErIKSKommune() {
        AdresseSokConsumer.AdresseData adresseData = new AdresseSokConsumer.AdresseData();
        adresseData.kommunenummer = "2004";
        adresseData.kommunenavn = "IkkeIKS";
        AdresseForslag adresseForslag = TpsAdresseSokService.toAdresseForslag(adresseData);
        assertThat(adresseForslag.kommunenavn).isEqualTo("IkkeIKS");
    }

    @Test
    void sokEtterAdresserString_medNullAdresseString_skalGiTomtResultat() {
        List<AdresseForslag> adresseForslags = tpsAdresseSokService.sokEtterAdresser((String) null);
        assertThat(adresseForslags).isEmpty();
    }

    @Test
    void sokEtterAdresserString_medAdressePaEnBokstav_skalGiTomtResultat() {
        List<AdresseForslag> adresseForslags = tpsAdresseSokService.sokEtterAdresser("a");
        assertThat(adresseForslags).isEmpty();
    }

    @Test
    void sokEtterAdresserString_medAdressePaToBokstaver_skalGiTomtResultat() {
        String adressenavn = "Sæ";
        when(adresseSokConsumer.sokAdresse(any())).thenReturn(mockAddressResponse(adressenavn));

        List<AdresseForslag> adresseForslags = tpsAdresseSokService.sokEtterAdresser(adressenavn);
        assertThat(adresseForslags).isNotEmpty();
    }

    @Test
    void sokEtterAdresserSokedata_medNullSokedata_skalGiTomtResultat() {
        List<AdresseForslag> adresseForslags = tpsAdresseSokService.sokEtterAdresser((Sokedata) null);
        assertThat(adresseForslags).isEmpty();
    }

    @Test
    void sokEtterAdresserSokedata_derSokedataHarNullAdresse_skalGiTomtResultat() {
        List<AdresseForslag> adresseForslags = tpsAdresseSokService.sokEtterAdresser(new Sokedata());
        assertThat(adresseForslags).isEmpty();
    }

    @Test
    void sokEtterAdresserSokedata_medAdressePaEnBokstav_skalGiTomtResultat() {
        Sokedata sokedata = new Sokedata();
        sokedata.adresse = "a";
        List<AdresseForslag> adresseForslags = tpsAdresseSokService.sokEtterAdresser(sokedata);
        assertThat(adresseForslags).isEmpty();
    }

    @Test
    void sokEtterAdresserSokedata_medAdressePaToBokstaver_skalGiResultat() {
        String adressenavn = "Sæ";
        when(adresseSokConsumer.sokAdresse(any())).thenReturn(mockAddressResponse(adressenavn));

        Sokedata sokedata = new Sokedata();
        sokedata.adresse = adressenavn;
        List<AdresseForslag> adresseForslags = tpsAdresseSokService.sokEtterAdresser(sokedata);

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