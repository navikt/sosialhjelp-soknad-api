package no.nav.sbl.dialogarena.soknadinnsending.consumer.personalia;


import no.nav.sbl.dialogarena.sendsoknad.domain.Adresse;
import no.nav.sbl.dialogarena.sendsoknad.domain.Adressetype;
import no.nav.sbl.dialogarena.sendsoknad.domain.personalia.Personalia;
import org.junit.Assert;
import org.junit.Test;

public class PersonTest {
    @Test
    public void skalReturnereTrueForPostadresseUtland() {
        Personalia personalia = new Personalia();
        Adresse gjeldendeAdresse = new Adresse();
        gjeldendeAdresse.setAdressetype(Adressetype.POSTADRESSE_UTLAND.name());
        personalia.setGjeldendeAdresse(gjeldendeAdresse);

        Assert.assertEquals(true, personalia.harUtenlandskAdresse());
    }

    @Test
    public void skalReturnereTrueForFolkeregistrertPostadresseUtland() {
        Personalia personalia = new Personalia();
        Adresse gjeldendeAdresse = new Adresse();
        gjeldendeAdresse.setAdressetype(Adressetype.UTENLANDSK_ADRESSE.name());
        personalia.setGjeldendeAdresse(gjeldendeAdresse);

        Assert.assertEquals(true, personalia.harUtenlandskAdresse());
    }

    @Test
    public void skalReturnereTrueForMidlertidigPostadresseUtland() {
        Personalia personalia = new Personalia();
        Adresse gjeldendeAdresse = new Adresse();
        gjeldendeAdresse
                .setAdressetype(Adressetype.MIDLERTIDIG_POSTADRESSE_UTLAND
                        .name());
        personalia.setGjeldendeAdresse(gjeldendeAdresse);

        Assert.assertEquals(true, personalia.harUtenlandskAdresse());
    }

    @Test
    public void skalReturnereFalseForMidlertidigPostadresseNorge() {
        Personalia personalia = new Personalia();
        Adresse gjeldendeAdresse = new Adresse();
        gjeldendeAdresse
                .setAdressetype(Adressetype.MIDLERTIDIG_POSTADRESSE_NORGE
                        .name());
        personalia.setGjeldendeAdresse(gjeldendeAdresse);

        Assert.assertEquals(false, personalia.harUtenlandskAdresse());
    }

    @Test
    public void skalReturnereFalseForFolkeregistrertAdresseNorge() {
        Personalia personalia = new Personalia();
        Adresse gjeldendeAdresse = new Adresse();
        gjeldendeAdresse.setAdressetype(Adressetype.BOSTEDSADRESSE.name());
        personalia.setGjeldendeAdresse(gjeldendeAdresse);

        Assert.assertEquals(false, personalia.harUtenlandskAdresse());
    }
}
