package no.nav.sosialhjelp.soknad.consumer.pdl.adressesok;

import no.nav.sosialhjelp.soknad.consumer.pdl.PdlConsumer;
import no.nav.sosialhjelp.soknad.consumer.pdl.adressesok.dto.AdresseDto;
import no.nav.sosialhjelp.soknad.consumer.pdl.adressesok.dto.AdresseSokHit;
import no.nav.sosialhjelp.soknad.consumer.pdl.adressesok.dto.AdresseSokResult;
import no.nav.sosialhjelp.soknad.domain.model.adresse.AdresseSokConsumer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PdlAdresseSokServiceTest {

    private static final String BYDELSNUMMER = "030101";
    private static final String KOMMUNENUMMER = "0301";
    private static final String KOMMUNENAVN = "OSLO";

    @Mock
    private PdlConsumer pdlConsumer;

    @InjectMocks
    private PdlAdresseSokService pdlAdresseSokService;

    private AdresseSokConsumer.Sokedata sokedata = new AdresseSokConsumer.Sokedata()
            .withAdresse("Testveien")
            .withHusnummer("1")
            .withHusbokstav("B")
            .withPoststed("Oslo");

    @Test
    public void skalKasteFeil_AdresseSokResultErNull() {
        when(pdlConsumer.getAdresseSokResult(any())).thenReturn(null);

        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> pdlAdresseSokService.getGeografiskTilknytning(sokedata));
    }

    @Test
    public void skalKasteFeil_SokedataErNull() {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> pdlAdresseSokService.getGeografiskTilknytning(null));
    }

    @Test
    public void skalKasteFeil_AdresseSokResultHitsErNull() {
        var adresseSokResultMock = mock(AdresseSokResult.class);
        when(adresseSokResultMock.getHits()).thenReturn(null);

        when(pdlConsumer.getAdresseSokResult(any())).thenReturn(adresseSokResultMock);

        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> pdlAdresseSokService.getGeografiskTilknytning(sokedata));
    }

    @Test
    public void skalKasteFeil_AdresseSokGirTomListe() {
        var adresseSokResultMock = mock(AdresseSokResult.class);
        when(adresseSokResultMock.getHits()).thenReturn(emptyList());

        when(pdlConsumer.getAdresseSokResult(any())).thenReturn(adresseSokResultMock);

        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> pdlAdresseSokService.getGeografiskTilknytning(sokedata));
    }

    @Test
    public void skalKasteFeil_AdresseSokGirFlereHits() {
        var adresseSokResultMock = mock(AdresseSokResult.class);
        when(adresseSokResultMock.getHits()).thenReturn(List.of(mock(AdresseSokHit.class), mock(AdresseSokHit.class)));

        when(pdlConsumer.getAdresseSokResult(any())).thenReturn(adresseSokResultMock);

        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> pdlAdresseSokService.getGeografiskTilknytning(sokedata));
    }

    @Test
    public void skalReturnereBydelsnummerSomGeografiskTilknytning() {
        var hitMock = mock(AdresseSokHit.class);
        when(hitMock.getVegadresse()).thenReturn(adresseMedBydelsnummer());

        var adresseSokResultMock = mock(AdresseSokResult.class);
        when(adresseSokResultMock.getHits()).thenReturn(singletonList(hitMock));

        when(pdlConsumer.getAdresseSokResult(any())).thenReturn(adresseSokResultMock);

        var geografiskTilknytning = pdlAdresseSokService.getGeografiskTilknytning(sokedata);
        assertThat(geografiskTilknytning).isEqualTo(BYDELSNUMMER);
    }

    @Test
    public void skalReturnereKommunenummerSomGeografiskTilknytning() {
        var hitMock = mock(AdresseSokHit.class);
        when(hitMock.getVegadresse()).thenReturn(adresseUtenBydelsnummer());
        var adresseSokResultMock = mock(AdresseSokResult.class);
        when(adresseSokResultMock.getHits()).thenReturn(singletonList(hitMock));

        when(pdlConsumer.getAdresseSokResult(any())).thenReturn(adresseSokResultMock);

        var geografiskTilknytning = pdlAdresseSokService.getGeografiskTilknytning(sokedata);
        assertThat(geografiskTilknytning).isEqualTo(KOMMUNENUMMER);
    }

    @Test
    public void skalReturnereAdresseForslagMedGeografiskTilknytningLikBydelsnummer() {
        var hitMock = mock(AdresseSokHit.class);
        when(hitMock.getVegadresse()).thenReturn(adresseMedBydelsnummer());

        var adresseSokResultMock = mock(AdresseSokResult.class);
        when(adresseSokResultMock.getHits()).thenReturn(singletonList(hitMock));

        when(pdlConsumer.getAdresseSokResult(any())).thenReturn(adresseSokResultMock);

        var adresseForslag = pdlAdresseSokService.getAdresseForslag(sokedata);
        assertThat(adresseForslag.geografiskTilknytning).isEqualTo(BYDELSNUMMER);
    }

    @Test
    public void skalReturnereAdresseForslagMedGeografiskTilknytningLikKommunenummer() {
        var hitMock = mock(AdresseSokHit.class);
        when(hitMock.getVegadresse()).thenReturn(adresseUtenBydelsnummer());
        var adresseSokResultMock = mock(AdresseSokResult.class);
        when(adresseSokResultMock.getHits()).thenReturn(singletonList(hitMock));

        when(pdlConsumer.getAdresseSokResult(any())).thenReturn(adresseSokResultMock);

        var adresseForslag = pdlAdresseSokService.getAdresseForslag(sokedata);
        assertThat(adresseForslag.geografiskTilknytning).isEqualTo(KOMMUNENUMMER);
    }

    @Test
    public void skalKasteFeil_flereHitsMedUlikeKommunenavn() {
        var hitMock = mock(AdresseSokHit.class);
        when(hitMock.getVegadresse()).thenReturn(adresse("kommune1", "0101", null));
        var hitMock2 = mock(AdresseSokHit.class);
        when(hitMock2.getVegadresse()).thenReturn(adresse("kommune2", "0101", null));

        var adresseSokResultMock = mock(AdresseSokResult.class);
        when(adresseSokResultMock.getHits()).thenReturn(Arrays.asList(hitMock, hitMock2));

        when(pdlConsumer.getAdresseSokResult(any())).thenReturn(adresseSokResultMock);

        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> pdlAdresseSokService.getAdresseForslag(sokedata));
    }

    @Test
    public void skalKasteFeil_flereHitsMedUlikeKommunenummer() {
        var hitMock = mock(AdresseSokHit.class);
        when(hitMock.getVegadresse()).thenReturn(adresse("kommune", "1111", null));
        var hitMock2 = mock(AdresseSokHit.class);
        when(hitMock2.getVegadresse()).thenReturn(adresse("kommune", "2222", null));

        var adresseSokResultMock = mock(AdresseSokResult.class);
        when(adresseSokResultMock.getHits()).thenReturn(Arrays.asList(hitMock, hitMock2));

        when(pdlConsumer.getAdresseSokResult(any())).thenReturn(adresseSokResultMock);

        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> pdlAdresseSokService.getAdresseForslag(sokedata));
    }

    @Test
    public void skalKasteFeil_flereHitsMedUlikeBydelsnummer() {
        var hitMock = mock(AdresseSokHit.class);
        when(hitMock.getVegadresse()).thenReturn(adresse("kommune", "1111", "030101"));
        var hitMock2 = mock(AdresseSokHit.class);
        when(hitMock2.getVegadresse()).thenReturn(adresse("kommune", "1111", "030102"));

        var adresseSokResultMock = mock(AdresseSokResult.class);
        when(adresseSokResultMock.getHits()).thenReturn(Arrays.asList(hitMock, hitMock2));

        when(pdlConsumer.getAdresseSokResult(any())).thenReturn(adresseSokResultMock);

        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> pdlAdresseSokService.getAdresseForslag(sokedata));
    }

    @Test
    public void skalReturnereAdresseForslagVedFlereHitsHvisDeHarSammeKommunenummerKommunenavnOgBydelsnummer() {
        var hitMock = mock(AdresseSokHit.class);
        when(hitMock.getVegadresse()).thenReturn(adresse("Oslo", "1111", "030101"));
        var hitMock2 = mock(AdresseSokHit.class);
        when(hitMock2.getVegadresse()).thenReturn(adresse("Oslo", "1111", "030101"));

        var adresseSokResultMock = mock(AdresseSokResult.class);
        when(adresseSokResultMock.getHits()).thenReturn(Arrays.asList(hitMock, hitMock2));

        when(pdlConsumer.getAdresseSokResult(any())).thenReturn(adresseSokResultMock);

        var adresseForslag = pdlAdresseSokService.getAdresseForslag(sokedata);
        assertThat(adresseForslag.kommunenavn).isEqualTo("Oslo");
        assertThat(adresseForslag.kommunenummer).isEqualTo("1111");
        assertThat(adresseForslag.geografiskTilknytning).isEqualTo("030101");
    }

    private AdresseDto adresseMedBydelsnummer() {
        return adresse(KOMMUNENAVN, KOMMUNENUMMER, BYDELSNUMMER);
    }

    private AdresseDto adresseUtenBydelsnummer() {
        return adresse(KOMMUNENAVN, KOMMUNENUMMER, null);
    }

    private AdresseDto adresse(String kommunenavn, String kommunenummer, String bydelsnummer) {
        return new AdresseDto("matrikkelId", 1, "B", "Testveien", kommunenavn, kommunenummer, "0123", "Oslo", bydelsnummer);
    }
}