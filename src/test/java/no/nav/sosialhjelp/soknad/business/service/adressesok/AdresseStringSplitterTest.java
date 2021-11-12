package no.nav.sosialhjelp.soknad.business.service.adressesok;

import no.nav.sosialhjelp.soknad.client.kodeverk.KodeverkService;
import org.junit.jupiter.api.Test;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AdresseStringSplitterTest {

    @Test
    void tomStrengGirBlanktSvar() {
        assertThat(AdresseStringSplitter.toSokedata(null,"").adresse).isBlank();
    }
    
    @Test
    void nullStrengGirNullSvar() {
        assertThat(AdresseStringSplitter.toSokedata(null, null).adresse).isNull();
    }
    
    @Test
    void kunAdresseVirker() {
        assertThat(AdresseStringSplitter.toSokedata(null, "asdf").adresse).isEqualTo("asdf");
    }
    
    @Test
    void husnummer() {
        final Sokedata result = AdresseStringSplitter.toSokedata(null, "asdf 2");
        assertThat(result.adresse).isEqualTo("asdf");
        assertThat(result.husnummer).isEqualTo("2");
    }

    @Test
    void kunHusnummer() {
        final Sokedata result = AdresseStringSplitter.toSokedata(null, "234");
        assertThat(result.husnummer).isEqualTo("234");
    }
    
    @Test
    void husbokstav() {
        final Sokedata result = AdresseStringSplitter.toSokedata(null, "asdf 2G");
        assertThat(result.adresse).isEqualTo("asdf");
        assertThat(result.husnummer).isEqualTo("2");
        assertThat(result.husbokstav).isEqualTo("G");
    }

    @Test
    void husnummerOgBokstav() {
        final Sokedata result = AdresseStringSplitter.toSokedata(null, "212G");
        assertThat(result.husnummer).isEqualTo("212");
        assertThat(result.husbokstav).isEqualTo("G");
    }
    
    @Test
    void postnummer() {
        final Sokedata result = AdresseStringSplitter.toSokedata(null, "asdf 2G, 0882");
        assertThat(result.adresse).isEqualTo("asdf");
        assertThat(result.husnummer).isEqualTo("2");
        assertThat(result.husbokstav).isEqualTo("G");
        assertThat(result.postnummer).isEqualTo("0882");
    }

    @Test
    void postnummerMedMellomromFlyttet() {
        final Sokedata result = AdresseStringSplitter.toSokedata(null, "asdf 2G ,0882");
        assertThat(result.adresse).isEqualTo("asdf");
        assertThat(result.husnummer).isEqualTo("2");
        assertThat(result.husbokstav).isEqualTo("G");
        assertThat(result.postnummer).isEqualTo("0882");
    }

    @Test
    void kunPostnummer() {
        final Sokedata result = AdresseStringSplitter.toSokedata(null, "0882");
        assertThat(result.postnummer).isEqualTo("0882");
    }

    @Test
    void kunPostnummerMedMellomrom() {
        final Sokedata result = AdresseStringSplitter.toSokedata(null, "   0882   ");
        assertThat(result.postnummer).isEqualTo("0882");
    }
    
    @Test
    void poststed() {
        final Sokedata result = AdresseStringSplitter.toSokedata(null, "asdf 2G, 0882 OSLO");
        assertThat(result.adresse).isEqualTo("asdf");
        assertThat(result.husnummer).isEqualTo("2");
        assertThat(result.husbokstav).isEqualTo("G");
        assertThat(result.postnummer).isEqualTo("0882");
        assertThat(result.poststed).isEqualTo("OSLO");
    }

    @Test
    void kunGateOgPostnummer() {
        final Sokedata result = AdresseStringSplitter.toSokedata(null, "Veivei, 0110 ");
        assertThat(result.adresse).isEqualTo("Veivei");
        assertThat(result.postnummer).isEqualTo("0110");
    }

    @Test
    void kunGateOgPoststed() {
        final Sokedata result = AdresseStringSplitter.toSokedata(null, "Veivei, OSLO");
        assertThat(result.adresse).isEqualTo("Veivei");
        assertThat(result.poststed).isEqualTo("OSLO");
    }

    @Test
    void dobbeltnavnPlussDiverseMellomrom() {
        final Sokedata result = AdresseStringSplitter.toSokedata(null, "    Nedre Glommas    Vei   211G  ,  0882  ØVRE OSLO   ");
        assertThat(result.adresse).isEqualTo("Nedre Glommas Vei");
        assertThat(result.husnummer).isEqualTo("211");
        assertThat(result.husbokstav).isEqualTo("G");
        assertThat(result.postnummer).isEqualTo("0882");
        assertThat(result.poststed).isEqualTo("ØVRE OSLO");
    }
    @Test
    void kompakt() {
        final Sokedata result = AdresseStringSplitter.toSokedata(null, " Nedre Glommas Vei211G,0882ØVRE OSLO   ");
        assertThat(result.adresse).isEqualTo("Nedre Glommas Vei");
        assertThat(result.husnummer).isEqualTo("211");
        assertThat(result.husbokstav).isEqualTo("G");
        assertThat(result.postnummer).isEqualTo("0882");
        assertThat(result.poststed).isEqualTo("ØVRE OSLO");
    }

    @Test
    void poststedUtenPostnummer() {
        final Sokedata result = AdresseStringSplitter.toSokedata(null, "asdf 2G OSLO");
        assertThat(result.adresse).isEqualTo("asdf");
        assertThat(result.husnummer).isEqualTo("2");
        assertThat(result.husbokstav).isEqualTo("G");
        assertThat(result.poststed).isEqualTo("OSLO");
    }
    
    @Test
    void utenHusnummer() {
        final Sokedata result = AdresseStringSplitter.toSokedata(null, "asdf, 0882 OSLO");
        assertThat(result.adresse).isEqualTo("asdf");
        assertThat(result.postnummer).isEqualTo("0882");
        assertThat(result.poststed).isEqualTo("OSLO");
    }
    
    @Test
    void skalKunneSokeMedKommunenavn() {
        final KodeverkService kodeverkService = mock(KodeverkService.class);
        when(kodeverkService.gjettKommunenummer(anyString())).thenReturn("0301");
        final Sokedata result = AdresseStringSplitter.toSokedata(kodeverkService, "asdf, OSLO");
        assertThat(result.adresse).isEqualTo("asdf");
        assertThat(result.poststed).isNull();
        assertThat(result.kommunenummer).isEqualTo("0301");
    }
    
    @Test
    void skalFungereMedPoststedSelvMedKodeverk() {
        final KodeverkService kodeverkService = mock(KodeverkService.class);
        when(kodeverkService.gjettKommunenummer(anyString())).thenReturn("0301");
        final Sokedata result = AdresseStringSplitter.toSokedata(kodeverkService, "asdf, 0756 OSLO");
        assertThat(result.adresse).isEqualTo("asdf");
        assertThat(result.postnummer).isEqualTo("0756");
        assertThat(result.poststed).isEqualTo("OSLO");
        assertThat(result.kommunenummer).isNull();
    }

    @Test
    void postnummerMatchTest() {
        Sokedata sokedata = AdresseStringSplitter.postnummerMatch("0001");
        assertThat(Objects.requireNonNull(sokedata).postnummer).isEqualTo("0001");
        sokedata = AdresseStringSplitter.postnummerMatch("0001 ");
        assertThat(Objects.requireNonNull(sokedata).postnummer).isEqualTo("0001");
        sokedata = AdresseStringSplitter.postnummerMatch(" 0001");
        assertThat(Objects.requireNonNull(sokedata).postnummer).isEqualTo("0001");

        sokedata = AdresseStringSplitter.postnummerMatch("Haugeveien, 0001 klavestaad");
        assertThat(sokedata).isNull();
        sokedata = AdresseStringSplitter.postnummerMatch("Sannergata 2");
        assertThat(sokedata).isNull();
        sokedata = AdresseStringSplitter.postnummerMatch("Sannergata0001");
        assertThat(sokedata).isNull();
        sokedata = AdresseStringSplitter.postnummerMatch("0001Klavestad");
        assertThat(sokedata).isNull();
        sokedata = AdresseStringSplitter.postnummerMatch("0001 Klavestad");
        assertThat(sokedata).isNull();
    }
}
