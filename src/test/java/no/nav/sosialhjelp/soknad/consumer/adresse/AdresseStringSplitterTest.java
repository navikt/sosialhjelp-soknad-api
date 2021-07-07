package no.nav.sosialhjelp.soknad.consumer.adresse;

import no.nav.sosialhjelp.soknad.business.service.adressesok.Sokedata;
import no.nav.sosialhjelp.soknad.business.service.adressesok.Soketype;
import no.nav.sosialhjelp.soknad.consumer.kodeverk.KodeverkService;
import org.junit.Test;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AdresseStringSplitterTest {

    @Test
    public void tomStrengGirBlanktSvar() {
        assertThat(AdresseStringSplitter.toSokedata(null,"").adresse).isEqualTo("");
    }
    
    @Test
    public void nullStrengGirNullSvar() {
        assertThat(AdresseStringSplitter.toSokedata(null, null).adresse).isNull();
    }
    
    @Test
    public void kunAdresseVirker() {
        assertThat(AdresseStringSplitter.toSokedata(null, "asdf").adresse).isEqualTo("asdf");
    }
    
    @Test
    public void husnummer() {
        final Sokedata result = AdresseStringSplitter.toSokedata(null, "asdf 2");
        assertThat(result.adresse).isEqualTo("asdf");
        assertThat(result.husnummer).isEqualTo("2");
    }

    @Test
    public void kunHusnummer() {
        final Sokedata result = AdresseStringSplitter.toSokedata(null, "234");
        assertThat(result.husnummer).isEqualTo("234");
    }
    
    @Test
    public void husbokstav() {
        final Sokedata result = AdresseStringSplitter.toSokedata(null, "asdf 2G");
        assertThat(result.adresse).isEqualTo("asdf");
        assertThat(result.husnummer).isEqualTo("2");
        assertThat(result.husbokstav).isEqualTo("G");
    }

    @Test
    public void husnummerOgBokstav() {
        final Sokedata result = AdresseStringSplitter.toSokedata(null, "212G");
        assertThat(result.husnummer).isEqualTo("212");
        assertThat(result.husbokstav).isEqualTo("G");
    }
    
    @Test
    public void postnummer() {
        final Sokedata result = AdresseStringSplitter.toSokedata(null, "asdf 2G, 0882");
        assertThat(result.adresse).isEqualTo("asdf");
        assertThat(result.husnummer).isEqualTo("2");
        assertThat(result.husbokstav).isEqualTo("G");
        assertThat(result.postnummer).isEqualTo("0882");
    }

    @Test
    public void postnummerMedMellomromFlyttet() {
        final Sokedata result = AdresseStringSplitter.toSokedata(null, "asdf 2G ,0882");
        assertThat(result.adresse).isEqualTo("asdf");
        assertThat(result.husnummer).isEqualTo("2");
        assertThat(result.husbokstav).isEqualTo("G");
        assertThat(result.postnummer).isEqualTo("0882");
    }

    @Test
    public void kunPostnummer() {
        final Sokedata result = AdresseStringSplitter.toSokedata(null, "0882");
        assertThat(result.postnummer).isEqualTo("0882");
    }

    @Test
    public void kunPostnummerMedMellomrom() {
        final Sokedata result = AdresseStringSplitter.toSokedata(null, "   0882   ");
        assertThat(result.postnummer).isEqualTo("0882");
    }
    
    @Test
    public void poststed() {
        final Sokedata result = AdresseStringSplitter.toSokedata(null, "asdf 2G, 0882 OSLO");
        assertThat(result.adresse).isEqualTo("asdf");
        assertThat(result.husnummer).isEqualTo("2");
        assertThat(result.husbokstav).isEqualTo("G");
        assertThat(result.postnummer).isEqualTo("0882");
        assertThat(result.poststed).isEqualTo("OSLO");
    }

    @Test
    public void kunGateOgPostnummer() {
        final Sokedata result = AdresseStringSplitter.toSokedata(null, "Veivei, 0110 ");
        assertThat(result.adresse).isEqualTo("Veivei");
        assertThat(result.postnummer).isEqualTo("0110");
    }

    @Test
    public void kunGateOgPoststed() {
        final Sokedata result = AdresseStringSplitter.toSokedata(null, "Veivei, OSLO");
        assertThat(result.adresse).isEqualTo("Veivei");
        assertThat(result.poststed).isEqualTo("OSLO");
    }

    @Test
    public void dobbeltnavnPlussDiverseMellomrom() {
        final Sokedata result = AdresseStringSplitter.toSokedata(null, "    Nedre Glommas    Vei   211G  ,  0882  ØVRE OSLO   ");
        assertThat(result.adresse).isEqualTo("Nedre Glommas Vei");
        assertThat(result.husnummer).isEqualTo("211");
        assertThat(result.husbokstav).isEqualTo("G");
        assertThat(result.postnummer).isEqualTo("0882");
        assertThat(result.poststed).isEqualTo("ØVRE OSLO");
    }
    @Test
    public void kompakt() {
        final Sokedata result = AdresseStringSplitter.toSokedata(null, " Nedre Glommas Vei211G,0882ØVRE OSLO   ");
        assertThat(result.adresse).isEqualTo("Nedre Glommas Vei");
        assertThat(result.husnummer).isEqualTo("211");
        assertThat(result.husbokstav).isEqualTo("G");
        assertThat(result.postnummer).isEqualTo("0882");
        assertThat(result.poststed).isEqualTo("ØVRE OSLO");
    }

    @Test
    public void poststedUtenPostnummer() {
        final Sokedata result = AdresseStringSplitter.toSokedata(null, "asdf 2G OSLO");
        assertThat(result.adresse).isEqualTo("asdf");
        assertThat(result.husnummer).isEqualTo("2");
        assertThat(result.husbokstav).isEqualTo("G");
        assertThat(result.poststed).isEqualTo("OSLO");
    }
    
    @Test
    public void utenHusnummer() {
        final Sokedata result = AdresseStringSplitter.toSokedata(null, "asdf, 0882 OSLO");
        assertThat(result.adresse).isEqualTo("asdf");
        assertThat(result.postnummer).isEqualTo("0882");
        assertThat(result.poststed).isEqualTo("OSLO");
    }

    @Test
    public void adresseMedToBokstaver_girEksaktSokeType() {
        final Sokedata result = AdresseStringSplitter.toSokedata(null, "Sæ");
        assertThat(result.adresse).isEqualTo("Sæ");
        assertThat(result.soketype).isEqualTo(Soketype.EKSAKT);
    }

    @Test
    public void adresseMedFlereBokstaver_girLignendeSokeType() {
        final Sokedata result = AdresseStringSplitter.toSokedata(null, "asd");
        assertThat(result.adresse).isEqualTo("asd");
        assertThat(result.soketype).isEqualTo(Soketype.LIGNENDE);
    }
    
    @Test
    public void skalKunneSokeMedKommunenavn() {
        final KodeverkService kodeverkService = mock(KodeverkService.class);
        when(kodeverkService.gjettKommunenummer(anyString())).thenReturn("0301");
        final Sokedata result = AdresseStringSplitter.toSokedata(kodeverkService, "asdf, OSLO");
        assertThat(result.adresse).isEqualTo("asdf");
        assertThat(result.poststed).isNull();
        assertThat(result.kommunenummer).isEqualTo("0301");
    }
    
    @Test
    public void skalFungereMedPoststedSelvMedKodeverk() {
        final KodeverkService kodeverkService = mock(KodeverkService.class);
        when(kodeverkService.gjettKommunenummer(anyString())).thenReturn("0301");
        final Sokedata result = AdresseStringSplitter.toSokedata(kodeverkService, "asdf, 0756 OSLO");
        assertThat(result.adresse).isEqualTo("asdf");
        assertThat(result.postnummer).isEqualTo("0756");
        assertThat(result.poststed).isEqualTo("OSLO");
        assertThat(result.kommunenummer).isNull();
    }

    @Test
    public void postnummerMatchTest() {
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
