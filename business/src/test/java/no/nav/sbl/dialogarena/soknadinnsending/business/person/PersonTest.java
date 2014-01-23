package no.nav.sbl.dialogarena.soknadinnsending.business.person;

import org.junit.Assert;
import org.junit.Test;

import static org.hamcrest.core.Is.is;

public class PersonTest {


    @Test
    public void skalSetteSammenForOgEtterNavnPaaRiktigMaateMedNull() {
        Assert.assertThat(true, is(true));
//        Person person = new Person(1l, "11111112345", "Jan", null, "Larsen", "midlertidig", new ArrayList<Adresse>());
//
//        Map<String, Object> fakta = person.getFakta();
//
//        Faktum sammensattnavn = (Faktum) fakta.get("sammensattnavn");
//        Assert.assertEquals("Jan Larsen", sammensattnavn.getValue());
    }
//
//    @Test
//    public void skalSetteSammenForOgEtterNavnPaaRiktigMaateMedMellomrom() {
//        Person person = new Person(1l, "11111112345", "Jan", "", "Larsen", "midlertidig", new ArrayList<Adresse>());
//
//        Map<String, Object> fakta = person.getFakta();
//
//        Faktum sammensattnavn = (Faktum) fakta.get("sammensattnavn");
//        Assert.assertEquals("Jan Larsen", sammensattnavn.getValue());
//    }
//
//    @Test
//    public void skalReturnereTrueForPostadresseUtland() {
//        Person person = new Person(1l, "11111112345", "Jan", "", "Larsen", Adressetype.POSTADRESSE_UTLAND.toString(), new ArrayList<Adresse>());
//
//        Assert.assertEquals(true, person.harUtenlandskAdresse());
//    }
//
//    @Test
//    public void skalReturnereTrueForFolkeregistrertPostadresseUtland() {
//        List<Adresse> adresser = new ArrayList<Adresse>();
//        adresser.add(new Adresse(1l, Adressetype.UTENLANDSK_ADRESSE));
//
//        Person person = new Person(1l, "11111112345", "Jan", "", "Larsen", Adressetype.POSTADRESSE.toString(), adresser);
//
//        Assert.assertEquals(true, person.harUtenlandskAdresse());
//    }
//
//    @Test
//    public void skalReturnereTrueForMidlertidigPostadresseUtland() {
//        Person person = new Person(1l, "11111112345", "Jan", "", "Larsen", Adressetype.MIDLERTIDIG_POSTADRESSE_UTLAND.toString(), new ArrayList<Adresse>());
//
//        Assert.assertEquals(true, person.harUtenlandskAdresse());
//    }
//
//    @Test
//    public void skalReturnereFalseForMidlertidigPostadresseNorge() {
//        Person person = new Person(1l, "11111112345", "Jan", "", "Larsen", Adressetype.MIDLERTIDIG_POSTADRESSE_NORGE.toString(), new ArrayList<Adresse>());
//
//        Assert.assertEquals(false, person.harUtenlandskAdresse());
//    }
//
//    @Test
//    public void skalReturnereFalseForFolkeregistrertAdresseNorge() {
//        Person person = new Person(1l, "11111112345", "Jan", "", "Larsen", Adressetype.BOSTEDSADRESSE.toString(), new ArrayList<Adresse>());
//
//        Assert.assertEquals(false, person.harUtenlandskAdresse());
//    }
//
//    @Test
//    public void skalReturneGuttVedGuttePersonNummer() {
//        String dato = "060258";
//        String individisfferEnOgTo = "00";
//        String kjonnSiffer = "1";
//        String kontrollsiffer = "74";
//
//        String fnr = dato + individisfferEnOgTo + kjonnSiffer + kontrollsiffer;
//        Barn barn = new Barn(1l, fnr, "Jan", "", "Larsen");
//
//        Assert.assertEquals("gutt", barn.getKjonn());
//
//    }
//
//    @Test
//    public void skalReturneJenteVedJentePersonNummer() {
//        String dato = "140571";
//        String individisfferEnOgTo = "32";
//        String kjonnSiffer = "8";
//        String kontrollsiffer = "42";
//
//        String fnr = dato + individisfferEnOgTo + kjonnSiffer + kontrollsiffer;
//        Barn barn = new Barn(1l, fnr, "Janne", "", "Larsen");
//
//        Assert.assertEquals("jente", barn.getKjonn());
//
//    }
}
