package no.nav.sosialhjelp.soknad.consumer.pdl.adressesok;

import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonGateAdresse;
import no.nav.sosialhjelp.soknad.adressesok.dto.AdressesokHitDto;
import no.nav.sosialhjelp.soknad.adressesok.dto.AdressesokResultDto;
import no.nav.sosialhjelp.soknad.adressesok.dto.VegadresseDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PdlAdresseSokServiceTest {

    private static final String BYDELSNUMMER = "030101";
    private static final String KOMMUNENUMMER = "0301";
    private static final String KOMMUNENAVN = "OSLO";

    @Mock
    private PdlAdresseSokConsumer pdlAdresseSokConsumer;

    @InjectMocks
    private PdlAdresseSokService pdlAdresseSokService;

    private final JsonGateAdresse folkeregistretAdresse = new JsonGateAdresse()
            .withGatenavn("Testveien")
            .withHusnummer("1")
            .withHusbokstav("B")
            .withPoststed("Oslo");

    @Test
    void skalKasteFeil_AdresseSokResultErNull() {
        when(pdlAdresseSokConsumer.getAdresseSokResult(any())).thenReturn(null);

        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> pdlAdresseSokService.getAdresseForslag(folkeregistretAdresse));
    }

    @Test
    void skalKasteFeil_SokedataErNull() {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> pdlAdresseSokService.getAdresseForslag(null));
    }

    @Test
    void skalKasteFeil_AdresseSokResultHitsErNull() {
        var adressesokResult = createAdressesokResultDto(null);

        when(pdlAdresseSokConsumer.getAdresseSokResult(any())).thenReturn(adressesokResult);

        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> pdlAdresseSokService.getAdresseForslag(folkeregistretAdresse));
    }

    @Test
    void skalKasteFeil_AdresseSokGirTomListe() {
        var adressesokResult = createAdressesokResultDto(emptyList());

        when(pdlAdresseSokConsumer.getAdresseSokResult(any())).thenReturn(adressesokResult);

        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> pdlAdresseSokService.getAdresseForslag(folkeregistretAdresse));
    }

    @Test
    void skalKasteFeil_AdresseSokGirFlereHits() {
        var adressesokResult = createAdressesokResultDto(List.of(
                new AdressesokHitDto(vegadresseMedBydelsnummer(), 0.5F),
                new AdressesokHitDto(vegadresseMedBydelsnummer(), 0.7F)
        ));

        when(pdlAdresseSokConsumer.getAdresseSokResult(any())).thenReturn(adressesokResult);

        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> pdlAdresseSokService.getAdresseForslag(folkeregistretAdresse));
    }

    @Test
    void skalReturnereAdresseForslagMedGeografiskTilknytningLikBydelsnummer() {
        var adressesokResult = createAdressesokResultDto(List.of(
                new AdressesokHitDto(vegadresseMedBydelsnummer(), 0.5F)
        ));

        when(pdlAdresseSokConsumer.getAdresseSokResult(any())).thenReturn(adressesokResult);

        var adresseForslag = pdlAdresseSokService.getAdresseForslag(folkeregistretAdresse);
        assertThat(adresseForslag.geografiskTilknytning).isEqualTo(BYDELSNUMMER);
    }

    @Test
    void skalReturnereAdresseForslagMedGeografiskTilknytningLikKommunenummer() {
        var adressesokResult = createAdressesokResultDto(List.of(
                new AdressesokHitDto(vegadresseUtenBydelsnummer(), 0.5F)
        ));

        when(pdlAdresseSokConsumer.getAdresseSokResult(any())).thenReturn(adressesokResult);

        var adresseForslag = pdlAdresseSokService.getAdresseForslag(folkeregistretAdresse);
        assertThat(adresseForslag.geografiskTilknytning).isEqualTo(KOMMUNENUMMER);
    }

    @Test
    void skalKasteFeil_flereHitsMedUlikeKommunenavn() {
        var adressesokResult = createAdressesokResultDto(List.of(
                new AdressesokHitDto(vegadresse("kommune1", "0101", null), 0.5F),
                new AdressesokHitDto(vegadresse("kommune2", "0101", null), 0.5F)
        ));

        when(pdlAdresseSokConsumer.getAdresseSokResult(any())).thenReturn(adressesokResult);

        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> pdlAdresseSokService.getAdresseForslag(folkeregistretAdresse));
    }

    @Test
    void skalKasteFeil_flereHitsMedUlikeKommunenummer() {
        var adressesokResult = createAdressesokResultDto(List.of(
                new AdressesokHitDto(vegadresse("kommune", "1111", null), 0.5F),
                new AdressesokHitDto(vegadresse("kommune", "2222", null), 0.5F)
        ));

        when(pdlAdresseSokConsumer.getAdresseSokResult(any())).thenReturn(adressesokResult);

        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> pdlAdresseSokService.getAdresseForslag(folkeregistretAdresse));
    }

    @Test
    void skalKasteFeil_flereHitsMedUlikeBydelsnummer() {
        var adressesokResult = createAdressesokResultDto(List.of(
                new AdressesokHitDto(vegadresse("kommune", "1111", "030101"), 0.5F),
                new AdressesokHitDto(vegadresse("kommune", "1111", "030102"), 0.5F)
        ));

        when(pdlAdresseSokConsumer.getAdresseSokResult(any())).thenReturn(adressesokResult);

        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> pdlAdresseSokService.getAdresseForslag(folkeregistretAdresse));
    }

    @Test
    void skalReturnereAdresseForslagVedFlereHitsHvisDeHarSammeKommunenummerKommunenavnOgBydelsnummer() {
        var adressesokResult = createAdressesokResultDto(List.of(
                new AdressesokHitDto(vegadresse("Oslo", "1111", "030101"), 0.5F),
                new AdressesokHitDto(vegadresse("Oslo", "1111", "030101"), 0.5F)
        ));

        when(pdlAdresseSokConsumer.getAdresseSokResult(any())).thenReturn(adressesokResult);

        var adresseForslag = pdlAdresseSokService.getAdresseForslag(folkeregistretAdresse);
        assertThat(adresseForslag.kommunenavn).isEqualTo("Oslo");
        assertThat(adresseForslag.kommunenummer).isEqualTo("1111");
        assertThat(adresseForslag.geografiskTilknytning).isEqualTo("030101");
    }

    private VegadresseDto vegadresseMedBydelsnummer() {
        return vegadresse(KOMMUNENAVN, KOMMUNENUMMER, BYDELSNUMMER);
    }

    private VegadresseDto vegadresseUtenBydelsnummer() {
        return vegadresse(KOMMUNENAVN, KOMMUNENUMMER, null);
    }

    private VegadresseDto vegadresse(String kommunenavn, String kommunenummer, String bydelsnummer) {
        return new VegadresseDto("matrikkelId", 1, "B", "Testveien", kommunenavn, kommunenummer, "0123", "Oslo", bydelsnummer);
    }

    private AdressesokResultDto createAdressesokResultDto(List<AdressesokHitDto> hits) {
        return new AdressesokResultDto(hits, 1,1, hits.size());
    }
}