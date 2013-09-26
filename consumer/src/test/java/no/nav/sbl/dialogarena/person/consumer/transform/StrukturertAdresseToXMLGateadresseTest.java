package no.nav.sbl.dialogarena.person.consumer.transform;

import no.nav.sbl.dialogarena.adresse.Adressetype;
import no.nav.sbl.dialogarena.adresse.StrukturertAdresse;
import no.nav.tjeneste.virksomhet.behandlebrukerprofil.v1.informasjon.XMLGateadresse;
import org.junit.Test;

import java.math.BigInteger;

import static no.nav.sbl.dialogarena.person.consumer.transform.Transform.toXMLGateadresse;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;


public class StrukturertAdresseToXMLGateadresseTest {

    private static final String GATENAVN = "GATENAVN";
    private static final String BOLIGNUMMER = "1";
    private static final String GATENUMMER = "2";
    private static final String POSTNUMMER = "1234";
    private static final String HUSBOKSTAV = "F";

    @Test
    public void testTransform() throws Exception {

        StrukturertAdresseToXMLGateadresse transformer = toXMLGateadresse();
        StrukturertAdresse adresse = new StrukturertAdresse(Adressetype.GATEADRESSE);
        adresse.setGatenavn(GATENAVN);
        adresse.setBolignummer(BOLIGNUMMER);
        adresse.setGatenummer(GATENUMMER);
        adresse.setPostnummer(POSTNUMMER);
        adresse.setHusbokstav(HUSBOKSTAV);

        XMLGateadresse xmlGateAdresse = transformer.transform(adresse);
        assertThat(xmlGateAdresse.getBolignummer(), is(equalTo(BOLIGNUMMER)));
        assertThat(xmlGateAdresse.getGatenavn(), is(equalTo(GATENAVN)));
        assertThat(xmlGateAdresse.getHusnummer(), is(equalTo(new BigInteger(GATENUMMER))));
        assertThat(xmlGateAdresse.getHusbokstav(), is(equalTo(HUSBOKSTAV)));

    }
}
