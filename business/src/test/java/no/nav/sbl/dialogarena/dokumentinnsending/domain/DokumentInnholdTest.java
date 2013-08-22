package no.nav.sbl.dialogarena.dokumentinnsending.domain;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

public class DokumentInnholdTest {

    private DokumentInnhold dokumentInnhold;

    @Before
    public void oppsett() {
        dokumentInnhold = new DokumentInnhold();
    }

    @Test
    public void testAtHentInnholdReturnereNullNaarInnholdErNull() {
        dokumentInnhold.setInnhold(null);
        assertThat(dokumentInnhold.hentInnholdSomBytes(), nullValue());
    }

    @Test
    public void testAtHentInnholdReturnererByteArrayNaarInnholdHarBytes() {
        dokumentInnhold.setInnhold(new byte[]{'a'});
        assertThat(dokumentInnhold.hentInnholdSomBytes(), notNullValue());
    }
}