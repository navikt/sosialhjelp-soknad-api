package no.nav.sbl.dialogarena.person.consumer.transform;

import no.nav.tjeneste.virksomhet.behandlebrukerprofil.v1.informasjon.XMLBankkontoUtland;
import no.nav.tjeneste.virksomhet.behandlebrukerprofil.v1.informasjon.XMLBankkontonummerUtland;
import org.junit.Test;

import static no.nav.sbl.dialogarena.person.consumer.transform.Transform.toXMLBankkontoUtland;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class XMLBankkontonummerUtlandToXMLBankkontoUtlandTest {
    @Test
    public void testTransform() {
        String kontonummer = "1234567901";
        XMLBankkontonummerUtland xmlBankkontonummerUtland = new XMLBankkontonummerUtland().withBankkontonummer(kontonummer);
        XMLBankkontoUtland xmlBankkontoUtland = toXMLBankkontoUtland().transform(xmlBankkontonummerUtland);
        assertThat(xmlBankkontoUtland.getBankkontoUtland().getBankkontonummer(), is(equalTo(kontonummer)));
    }
}
