package no.nav.sbl.dialogarena.soknadinnsending.business.person;

import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Barn;

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

    public void skalReturnereFalseForFolkeregistrertAdresseNorge() {
        Personalia personalia = new Personalia();
        Adresse gjeldendeAdresse = new Adresse();
        gjeldendeAdresse.setAdressetype(Adressetype.BOSTEDSADRESSE.name());
        personalia.setGjeldendeAdresse(gjeldendeAdresse);

        Assert.assertEquals(false, personalia.harUtenlandskAdresse());
    }

    @Test
    public void skalReturneGuttVedGuttePersonNummer() {
        String dato = "060258";
        String individisfferEnOgTo = "00";
        String kjonnSiffer = "1";
        String kontrollsiffer = "74";

        String fnr = dato + individisfferEnOgTo + kjonnSiffer + kontrollsiffer;
        Barn barn = new Barn(1l, null, null, fnr, "", "", "svenskeby").withLand("Norge");

        Assert.assertEquals("m", barn.getKjonn());
        Assert.assertEquals("Norge", barn.getLand());

    }

    @Test
    public void skalReturneJenteVedJentePersonNummer() {
        String dato = "140571";
        String individisfferEnOgTo = "32";
        String kjonnSiffer = "8";
        String kontrollsiffer = "42";

        String fnr = dato + individisfferEnOgTo + kjonnSiffer + kontrollsiffer;
        Barn barn = new Barn(1l, null, null, fnr, "janne", "j", "jensen").withLand("Norge");

        Assert.assertEquals("k", barn.getKjonn());

    }
}
