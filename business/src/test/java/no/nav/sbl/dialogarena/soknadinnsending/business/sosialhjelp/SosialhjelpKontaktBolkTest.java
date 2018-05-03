package no.nav.sbl.dialogarena.soknadinnsending.business.sosialhjelp;

import org.junit.Assert;
import org.junit.Test;

public class SosialhjelpKontaktBolkTest {

    @Test
    public void sjekkNorskTelefonnummer() {
        Assert.assertNull(SosialhjelpKontaktBolk.norskTelefonnummer("+4634343434"));
        Assert.assertNull(SosialhjelpKontaktBolk.norskTelefonnummer("4343434"));
        Assert.assertEquals("+4712345678", SosialhjelpKontaktBolk.norskTelefonnummer("+4712345678"));
        Assert.assertEquals("+4712345678", SosialhjelpKontaktBolk.norskTelefonnummer("12345678"));
        Assert.assertNull(SosialhjelpKontaktBolk.norskTelefonnummer(null));
        Assert.assertNull(SosialhjelpKontaktBolk.norskTelefonnummer(""));
    }
}
