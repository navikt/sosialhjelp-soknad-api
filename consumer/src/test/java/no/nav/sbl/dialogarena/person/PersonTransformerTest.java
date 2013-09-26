package no.nav.sbl.dialogarena.person;

import no.nav.sbl.dialogarena.adresse.Adresse;
import no.nav.sbl.dialogarena.adresse.Adressetype;
import no.nav.sbl.dialogarena.common.UnableToHandleException;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLBostedsadresse;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLGateadresse;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLLandkoder;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLPostadresse;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLPostboksadresseNorsk;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLPostnummer;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLStedsadresseNorge;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLUstrukturertAdresse;
import org.junit.Test;

import static no.nav.modig.lang.collections.TransformerUtils.first;
import static no.nav.sbl.dialogarena.adresse.AdresseTransform.STRUKTURERT_BOSTEDSADRESSE;
import static no.nav.sbl.dialogarena.adresse.AdresseTransform.TO_ADRESSE;
import static no.nav.sbl.dialogarena.adresse.AdresseTransform.USTRUKTURERT_POSTADRESSE;
import static org.junit.Assert.assertTrue;

public class PersonTransformerTest {

    XMLLandkoder sverige = new XMLLandkoder().withValue("SE");
    XMLLandkoder norge = new XMLLandkoder().withValue("NO");

    @Test(expected = UnableToHandleException.class)
    public void feilerBrutaltVedUkjentAdressetype() {
        TO_ADRESSE.transform(new XMLStedsadresseNorge());
    }

    @Test
    public void returnererRiktigAdresseType() {

        XMLPostnummer ridabu2322 = new XMLPostnummer().withValue("2322");

        XMLBostedsadresse adresse1 = new XMLBostedsadresse().withStrukturertAdresse(new XMLGateadresse().withGatenavn("Testgata").withPoststed(ridabu2322));
        Adresse transformertAdresse1 = first(STRUKTURERT_BOSTEDSADRESSE).then(TO_ADRESSE).transform(adresse1);
        assertTrue(transformertAdresse1.is(Adressetype.GATEADRESSE));

        XMLBostedsadresse adresse2 = new XMLBostedsadresse().withStrukturertAdresse(new XMLPostboksadresseNorsk().withPostboksanlegg("Test").withPoststed(ridabu2322));
        Adresse transformertAdresse2 = first(STRUKTURERT_BOSTEDSADRESSE).then(TO_ADRESSE).transform(adresse2);
        assertTrue(transformertAdresse2.is(Adressetype.POSTBOKSADRESSE));


        XMLPostadresse adresse3 = new XMLPostadresse().withUstrukturertAdresse(new XMLUstrukturertAdresse().withAdresselinje1("Testgata 1").withLandkode(sverige));
        Adresse transformertAdresse3 = first(USTRUKTURERT_POSTADRESSE).then(TO_ADRESSE).transform(adresse3);
        assertTrue(transformertAdresse3.is(Adressetype.UTENLANDSK_ADRESSE));

    }




}
