package no.nav.sbl.dialogarena.person.consumer.transform;

import no.nav.sbl.dialogarena.adresse.Adressetype;
import no.nav.sbl.dialogarena.adresse.UstrukturertAdresse;
import no.nav.tjeneste.virksomhet.behandlebrukerprofil.v1.informasjon.XMLMidlertidigPostadresseUtland;
import org.apache.commons.collections15.Transformer;
import org.joda.time.LocalDate;
import org.junit.Test;

import static no.nav.sbl.dialogarena.person.consumer.transform.Transform.toXMLMidlertidigPostadresseUtland;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class UstrukturertAdresseToXMLMidlertidigPostadresseUtlandTest {


    @Test
    public void testTransform() {
        UstrukturertAdresse adresse = new UstrukturertAdresse(Adressetype.UTENLANDSK_ADRESSE, LocalDate.now(), "SE", "linje1", "linje2", "linje3");
        Transformer<UstrukturertAdresse, XMLMidlertidigPostadresseUtland> transformer = toXMLMidlertidigPostadresseUtland(adresse.getUtlopsdato().toDateTimeAtStartOfDay());
        assertThat(transformer.transform(adresse).getUstrukturertAdresse().getAdresselinje1(), is(equalTo(adresse.getAdresselinje(0).getOrElse(null))));
        assertThat(transformer.transform(adresse).getUstrukturertAdresse().getAdresselinje2(), is(equalTo(adresse.getAdresselinje(1).getOrElse(null))));
        assertThat(transformer.transform(adresse).getUstrukturertAdresse().getAdresselinje3(), is(equalTo(adresse.getAdresselinje(2).getOrElse(null))));
        assertThat(transformer.transform(adresse).getUstrukturertAdresse().getLandkode().getValue(), is(equalTo(adresse.getLandkode())));
        assertThat(transformer.transform(adresse).getPostleveringsPeriode().getTom().toLocalDate(), is(equalTo(adresse.getUtlopsdato())));
    }
}
