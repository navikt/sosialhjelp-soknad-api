package no.nav.sbl.dialogarena.telefonnummer;

import org.junit.Test;

import static no.nav.sbl.dialogarena.telefonnummer.HarTelefonLand.LANDKODE_NORGE;
import static no.nav.sbl.dialogarena.telefonnummer.Telefonnummer.validerTelefonnummer;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

public class TelefonnummerTest {

    @Test
    public void norskTelefonnummerKrever8Tegn() {
        assertThat(validerTelefonnummer(LANDKODE_NORGE, "98765432"), empty());
        assertThat(validerTelefonnummer(LANDKODE_NORGE, "1234"), not(empty()));
    }

    @Test
    public void utenlandskTelefonnummerHarMaks16Tegn() {
        assertThat(validerTelefonnummer("46", "012345678901234567890"), not(empty()));
        assertThat(validerTelefonnummer("46", "1234567890123456"), empty());
        assertThat(validerTelefonnummer("46", "1234"), empty());
    }

    @Test
    public void telefonnummerKanHaSeparatorTegn() {
        assertThat(validerTelefonnummer(LANDKODE_NORGE, "987 65 432"), empty());
        assertThat(validerTelefonnummer(LANDKODE_NORGE, "987-65-432"), empty());
    }

    @Test
    public void nyttNummerSkalHaNorskLandkodeSomStandard() {
        Telefonnummer telefonnummer = new Telefonnummer();
        assertThat(telefonnummer.getLandkode(), is(HarTelefonLand.LANDKODE_NORGE));
    }

    @Test
    public void nyttTelefonnumerMedUtenlandskLandkodeReturnererRiktigLandkode() {
        Telefonnummer telefonnummer = new Telefonnummer("46", "123456");
        assertThat(telefonnummer.erUtfylt(), is(true));
        assertThat(telefonnummer.getLandkode(), is("46"));

    }
}
