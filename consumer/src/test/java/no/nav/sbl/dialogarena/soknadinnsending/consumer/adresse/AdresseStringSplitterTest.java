package no.nav.sbl.dialogarena.soknadinnsending.consumer.adresse;

import org.junit.Assert;
import org.junit.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.anyString;

import no.nav.sbl.dialogarena.kodeverk.Kodeverk;
import no.nav.sbl.dialogarena.sendsoknad.domain.adresse.AdresseSokConsumer.Sokedata;

import java.util.Objects;

public class AdresseStringSplitterTest {

    @Test
    public void tomStrengGirBlanktSvar() {
        Assert.assertEquals("", AdresseStringSplitter.toSokedata("").adresse);
    }
    
    @Test
    public void nullStrengGirNullSvar() {
        Assert.assertNull(AdresseStringSplitter.toSokedata(null).adresse);
    }
    
    @Test
    public void kunAdresseVirker() {
        Assert.assertEquals("asdf", AdresseStringSplitter.toSokedata("asdf").adresse);
    }
    
    @Test
    public void husnummer() {
        final Sokedata result = AdresseStringSplitter.toSokedata("asdf 2");
        Assert.assertEquals("asdf", result.adresse);
        Assert.assertEquals("2", result.husnummer);
    }

    @Test
    public void kunHusnummer() {
        final Sokedata result = AdresseStringSplitter.toSokedata("234");
        Assert.assertEquals("234", result.husnummer);
    }
    
    @Test
    public void husbokstav() {
        final Sokedata result = AdresseStringSplitter.toSokedata("asdf 2G");
        Assert.assertEquals("asdf", result.adresse);
        Assert.assertEquals("2", result.husnummer);
        Assert.assertEquals("G", result.husbokstav);
    }

    @Test
    public void husnummerOgBokstav() {
        final Sokedata result = AdresseStringSplitter.toSokedata("212G");
        Assert.assertEquals("212", result.husnummer);
        Assert.assertEquals("G", result.husbokstav);
    }
    
    @Test
    public void postnummer() {
        final Sokedata result = AdresseStringSplitter.toSokedata("asdf 2G, 0882");
        Assert.assertEquals("asdf", result.adresse);
        Assert.assertEquals("2", result.husnummer);
        Assert.assertEquals("G", result.husbokstav);
        Assert.assertEquals("0882", result.postnummer);
    }

    @Test
    public void postnummerMedMellomromFlyttet() {
        final Sokedata result = AdresseStringSplitter.toSokedata("asdf 2G ,0882");
        Assert.assertEquals("asdf", result.adresse);
        Assert.assertEquals("2", result.husnummer);
        Assert.assertEquals("G", result.husbokstav);
        Assert.assertEquals("0882", result.postnummer);
    }

    @Test
    public void kunPostnummer() {
        final Sokedata result = AdresseStringSplitter.toSokedata("0882");
        Assert.assertEquals("0882", result.postnummer);
    }

    @Test
    public void kunPostnummerMedMellomrom() {
        final Sokedata result = AdresseStringSplitter.toSokedata("   0882   ");
        Assert.assertEquals("0882", result.postnummer);
    }
    
    @Test
    public void poststed() {
        final Sokedata result = AdresseStringSplitter.toSokedata("asdf 2G, 0882 OSLO");
        Assert.assertEquals("asdf", result.adresse);
        Assert.assertEquals("2", result.husnummer);
        Assert.assertEquals("G", result.husbokstav);
        Assert.assertEquals("0882", result.postnummer);
        Assert.assertEquals("OSLO", result.poststed);
    }

    @Test
    public void kunGateOgPostnummer() {
        final Sokedata result = AdresseStringSplitter.toSokedata("Veivei, 0110 ");
        Assert.assertEquals("Veivei", result.adresse);
        Assert.assertEquals("0110", result.postnummer);
    }

    @Test
    public void kunGateOgPoststed() {
        final Sokedata result = AdresseStringSplitter.toSokedata("Veivei, OSLO");
        Assert.assertEquals("Veivei", result.adresse);
        Assert.assertEquals("OSLO", result.poststed);
    }

    @Test
    public void dobbeltnavnPlussDiverseMellomrom() {
        final Sokedata result = AdresseStringSplitter.toSokedata("    Nedre Glommas    Vei   211G  ,  0882  ØVRE OSLO   ");
        Assert.assertEquals("Nedre Glommas Vei", result.adresse);
        Assert.assertEquals("211", result.husnummer);
        Assert.assertEquals("G", result.husbokstav);
        Assert.assertEquals("0882", result.postnummer);
        Assert.assertEquals("ØVRE OSLO", result.poststed);
    }
    @Test
    public void kompakt() {
        final Sokedata result = AdresseStringSplitter.toSokedata(" Nedre Glommas Vei211G,0882ØVRE OSLO   ");
        Assert.assertEquals("Nedre Glommas Vei", result.adresse);
        Assert.assertEquals("211", result.husnummer);
        Assert.assertEquals("G", result.husbokstav);
        Assert.assertEquals("0882", result.postnummer);
        Assert.assertEquals("ØVRE OSLO", result.poststed);
    }

    @Test
    public void poststedUtenPostnummer() {
        final Sokedata result = AdresseStringSplitter.toSokedata("asdf 2G OSLO");
        Assert.assertEquals("asdf", result.adresse);
        Assert.assertEquals("2", result.husnummer);
        Assert.assertEquals("G", result.husbokstav);
        Assert.assertEquals("OSLO", result.poststed);
    }
    
    @Test
    public void utenHusnummer() {
        final Sokedata result = AdresseStringSplitter.toSokedata("asdf, 0882 OSLO");
        Assert.assertEquals("asdf", result.adresse);
        Assert.assertEquals("0882", result.postnummer);
        Assert.assertEquals("OSLO", result.poststed);
    }
    
    @Test
    public void skalKunneSokeMedKommunenavn() {
        final Kodeverk kodeverk = mock(Kodeverk.class);
        when(kodeverk.gjettKommunenummer(anyString())).thenReturn("0301");
        final Sokedata result = AdresseStringSplitter.toSokedata(kodeverk, "asdf, OSLO");
        Assert.assertEquals("asdf", result.adresse);
        Assert.assertNull(result.poststed);
        Assert.assertEquals("0301", result.kommunenummer);
    }
    
    @Test
    public void skalFungereMedPoststedSelvMedKodeverk() {
        final Kodeverk kodeverk = mock(Kodeverk.class);
        when(kodeverk.gjettKommunenummer(anyString())).thenReturn("0301");
        final Sokedata result = AdresseStringSplitter.toSokedata(kodeverk, "asdf, 0756 OSLO");
        Assert.assertEquals("asdf", result.adresse);
        Assert.assertEquals("0756", result.postnummer);
        Assert.assertEquals("OSLO", result.poststed);
        Assert.assertNull(result.kommunenummer);
    }

    @Test
    public void postnummerMatchTest() {
        Sokedata sokedata = AdresseStringSplitter.postnummerMatch("0001");
        Assert.assertEquals("0001", Objects.requireNonNull(sokedata).postnummer);
        sokedata = AdresseStringSplitter.postnummerMatch("0001 ");
        Assert.assertEquals("0001", Objects.requireNonNull(sokedata).postnummer);
        sokedata = AdresseStringSplitter.postnummerMatch(" 0001");
        Assert.assertEquals("0001", Objects.requireNonNull(sokedata).postnummer);

        sokedata = AdresseStringSplitter.postnummerMatch("Haugeveien, 0001 klavestaad");
        Assert.assertNull(sokedata);
        sokedata = AdresseStringSplitter.postnummerMatch("Sannergata 2");
        Assert.assertNull(sokedata);
        sokedata = AdresseStringSplitter.postnummerMatch("Sannergata0001");
        Assert.assertNull(sokedata);
        sokedata = AdresseStringSplitter.postnummerMatch("0001Klavestad");
        Assert.assertNull(sokedata);
        sokedata = AdresseStringSplitter.postnummerMatch("0001 Klavestad");
        Assert.assertNull(sokedata);
    }
}
