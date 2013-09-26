package no.nav.sbl.dialogarena.telefonnummer;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

public class TelefonnummertypeTest {
    @Test
    public void inneholderRiktigeElementer() {
        Telefonnummertype type1 = Telefonnummertype.HJEMMETELEFON;
        Telefonnummertype type2 = Telefonnummertype.JOBBTELEFON;
        Telefonnummertype type3 = Telefonnummertype.MOBIL;

        assertThat(type1, is(not(nullValue())));
        assertThat(type2, is(not(nullValue())));
        assertThat(type3, is(not(nullValue())));
    }
}
