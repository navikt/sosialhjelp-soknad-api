package no.nav.sbl.dialogarena.soknadinnsending.consumer.adresse;

import org.junit.Assert;
import org.junit.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.anyString;

import no.nav.sbl.dialogarena.kodeverk.Kodeverk;
import no.nav.sbl.dialogarena.sendsoknad.domain.adresse.AdresseSokConsumer.Sokedata;

public class AdresseStringSplitterTest {

    @Test
    public void tomStrengGirBlanktSvar() {
        Assert.assertEquals("", AdresseStringSplitter.toSokedata("").adresse);
    }
    
    @Test
    public void nullStrengGirNullSvar() {
        Assert.assertEquals(null, AdresseStringSplitter.toSokedata(null).adresse);
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
    public void husbokstav() {
        final Sokedata result = AdresseStringSplitter.toSokedata("asdf 2G");
        Assert.assertEquals("asdf", result.adresse);
        Assert.assertEquals("2", result.husnummer);
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
    public void poststed() {
        final Sokedata result = AdresseStringSplitter.toSokedata("asdf 2G, 0882 OSLO");
        Assert.assertEquals("asdf", result.adresse);
        Assert.assertEquals("2", result.husnummer);
        Assert.assertEquals("G", result.husbokstav);
        Assert.assertEquals("0882", result.postnummer);
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
        Assert.assertEquals(null, result.poststed);
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
        Assert.assertEquals(null, result.kommunenummer);
    }
}
