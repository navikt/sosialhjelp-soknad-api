package no.nav.sbl.dialogarena.soknadinnsending.consumer.adresse;

import org.junit.Assert;
import org.junit.Test;

import no.nav.sbl.dialogarena.soknadinnsending.consumer.adresse.AdresseStringSplitter.Adressefelter;

public class AdresseStringSplitterTest {

    @Test
    public void tomStrengGirBlanktSvar() {
        Assert.assertEquals("", AdresseStringSplitter.toAdressefelter("").adresse);
    }
    
    @Test
    public void nullStrengGirNullSvar() {
        Assert.assertEquals(null, AdresseStringSplitter.toAdressefelter(null).adresse);
    }
    
    @Test
    public void kunAdresseVirker() {
        Assert.assertEquals("asdf", AdresseStringSplitter.toAdressefelter("asdf").adresse);
    }
    
    @Test
    public void husnummer() {
        final Adressefelter result = AdresseStringSplitter.toAdressefelter("asdf 2");
        Assert.assertEquals("asdf", result.adresse);
        Assert.assertEquals("2", result.husnummer);
    }
    
    @Test
    public void husbokstav() {
        final Adressefelter result = AdresseStringSplitter.toAdressefelter("asdf 2G");
        Assert.assertEquals("asdf", result.adresse);
        Assert.assertEquals("2", result.husnummer);
        Assert.assertEquals("G", result.husbokstav);
    }
    
    @Test
    public void postnummer() {
        final Adressefelter result = AdresseStringSplitter.toAdressefelter("asdf 2G, 0882");
        Assert.assertEquals("asdf", result.adresse);
        Assert.assertEquals("2", result.husnummer);
        Assert.assertEquals("G", result.husbokstav);
        Assert.assertEquals("0882", result.postnummer);
    }
    
    @Test
    public void poststed() {
        final Adressefelter result = AdresseStringSplitter.toAdressefelter("asdf 2G, 0882 OSLO");
        Assert.assertEquals("asdf", result.adresse);
        Assert.assertEquals("2", result.husnummer);
        Assert.assertEquals("G", result.husbokstav);
        Assert.assertEquals("0882", result.postnummer);
        Assert.assertEquals("OSLO", result.poststed);
    }
    
    @Test
    public void utenHusnummer() {
        final Adressefelter result = AdresseStringSplitter.toAdressefelter("asdf, 0882 OSLO");
        Assert.assertEquals("asdf", result.adresse);
        Assert.assertEquals("0882", result.postnummer);
        Assert.assertEquals("OSLO", result.poststed);
    }
}
