package no.nav.sbl.dialogarena.person.consumer.transform;

import no.nav.sbl.dialogarena.telefonnummer.Telefonnummertype;
import org.junit.Test;

import static no.nav.sbl.dialogarena.person.consumer.transform.Transform.toXMLTelefontype;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class TelefonnummertypeToXMLTelefontypeTest {
    @Test
    public void returnererRiktigTelefonnummerTyper() throws Exception {
        TelefonnummertypeToXMLTelefontype transformer = (TelefonnummertypeToXMLTelefontype) toXMLTelefontype();
        assertThat(transformer.transform(Telefonnummertype.MOBIL).getValue(), is(equalTo("MOBI")));
        assertThat(transformer.transform(Telefonnummertype.HJEMMETELEFON).getValue(), is(equalTo("HJET")));
        assertThat(transformer.transform(Telefonnummertype.JOBBTELEFON).getValue(), is(equalTo("ARBT")));
    }
}
