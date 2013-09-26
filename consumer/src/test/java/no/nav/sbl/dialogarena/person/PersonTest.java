package no.nav.sbl.dialogarena.person;

import no.nav.modig.lang.option.Optional;
import no.nav.sbl.dialogarena.adresse.Adresse;
import no.nav.sbl.dialogarena.adresse.StrukturertAdresse;
import no.nav.sbl.dialogarena.adresse.UstrukturertAdresse;
import no.nav.sbl.dialogarena.person.Person.KanIkkeVelgeAdresse;
import org.junit.Test;

import static no.nav.modig.lang.option.Optional.optional;
import static no.nav.sbl.dialogarena.adresse.Adressetype.BOSTEDSADRESSE;
import static no.nav.sbl.dialogarena.adresse.Adressetype.UTENLANDSK_ADRESSE;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class PersonTest {

    private final Optional<Adresse> ingenFolkeregistrertAdresse = Optional.none();

    @Test
    public void builderPopulatesPerson() {
        Person person = new Person("Per Hansen", "01018549928", ingenFolkeregistrertAdresse);
        assertThat(person.navn, is("Per Hansen"));
        assertThat(person.ident, is("01018549928"));
    }

    @Test(expected = KanIkkeVelgeAdresse.class)
    public void kanIkkeVelgeFolkeregistrertAdressePaaBrukerUtenFolkeregistrertAdresse() {
        new Person("Bønna", "01018549928", ingenFolkeregistrertAdresse)
            .velg(GjeldendeAdressetype.FOLKEREGISTRERT);
    }

    @Test
    public void nyPersonUtenFolkeregistrertAdresseHarGjeldendeAdressetypeUkjent() {
        assertTrue(new Person("Bønna", "01018549928", ingenFolkeregistrertAdresse).har(GjeldendeAdressetype.UKJENT));
    }

    @Test
    public void nyPersonMedFolkeregistrertAdresseHarGjeldenAdressetypeFolkegistrert() {
        assertTrue(new Person("Bønna", "01018549928", optional(new StrukturertAdresse(BOSTEDSADRESSE))).har(GjeldendeAdressetype.FOLKEREGISTRERT));
    }

    @Test
    public void lageNyPersonBasertPaaEksisterendeKopiererIdentOgNavnOgFolkeregistrertAdresse() {
        Person lestFraTjeneste = new Person("Bønna", "01018549928", optional(new StrukturertAdresse(BOSTEDSADRESSE)));
        Person tilOppdateringstjeneste = lestFraTjeneste.lagNy();

        assertThat(tilOppdateringstjeneste.ident, is(lestFraTjeneste.ident));
        assertThat(tilOppdateringstjeneste.navn, is(lestFraTjeneste.navn));
        assertThat(tilOppdateringstjeneste.folkeregistrertAdresse, is(lestFraTjeneste.folkeregistrertAdresse));
    }

    @Test
    public void postAdresseSomFolkeregistrertAdresse() {
        Adresse adresse = new StrukturertAdresse(BOSTEDSADRESSE);
        Person person = new Person("Ola Normann", "42018549799", optional(adresse));
        assertThat(person.folkeregistrertAdresse, is(adresse));
    }

    @Test
    public void utenlandskMidlertidigAdresseVeierTyngreEnnNorskMidlertidigAdresse() {
        StrukturertAdresse norskAdresse = new StrukturertAdresse(BOSTEDSADRESSE);
        UstrukturertAdresse utenlandskAdresse = new UstrukturertAdresse(UTENLANDSK_ADRESSE, "SE");

        Person person = new Person("Åke Svenkmann", "42018549799", ingenFolkeregistrertAdresse);
        person.setNorskMidlertidig(norskAdresse);
        person.setUtenlandskMidlertidig(utenlandskAdresse);

        assertThat(person.getValgtMidlertidigAdresse(), is((Adresse) utenlandskAdresse));
    }

}