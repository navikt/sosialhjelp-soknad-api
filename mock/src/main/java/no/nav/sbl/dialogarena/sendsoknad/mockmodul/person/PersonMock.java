package no.nav.sbl.dialogarena.sendsoknad.mockmodul.person;

import no.nav.sbl.dialogarena.sendsoknad.domain.util.ServiceUtils;
import no.nav.tjeneste.virksomhet.person.v1.informasjon.*;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import java.util.List;


public class PersonMock {
    static final String FNR_BARN = "01010591736";
    static final String FNR_BARN2 = "03061793877";
    static final String FNR_BARN3 = "03061694075";
    static final String FNR_EKTEFELLE = "07127302639";
    private static PersonMock personMock = new PersonMock();
    private PersonPortTypeMock personPortTypeMock;

    private static final String KODE_6 = "SPSF";
    private static final String KODE_7 = "SPFO";

    private PersonMock() {
        personPortTypeMock = new PersonPortTypeMock();

        // Endre også i override-web.xml sin defaultFnr, da det er den som ligger på getSubjectHandler().getUid()
        Person person = genererPersonMedGyldigIdentOgNavn("03076321565", "Donald", "D.", "Mockmann");
        person.setFoedselsdato(fodseldato(1963, 7, 3));

        Statsborgerskap statsborgerskap = new Statsborgerskap();
        Landkoder landkoder = new Landkoder();
        landkoder.setValue("NOR");
        statsborgerskap.setLand(landkoder);
        person.setStatsborgerskap(statsborgerskap);

        List<Familierelasjon> familieRelasjoner = person.getHarFraRolleI();

        Doedsdato doedsdato = new Doedsdato();
        doedsdato.setDoedsdato(ServiceUtils.stringTilXmldato("2014-02-02"));
        Familierelasjon barn = lagBarn(FNR_BARN, "Dole", "Mockmann", doedsdato);
        Familierelasjon barn2 = lagBarn(FNR_BARN2, "Ole", "Mockmann");
        barn2.setHarSammeBosted(true);
        Familierelasjon barn3 = lagBarn(FNR_BARN3, "Doffen", "Mockmann");
        barn3.setHarSammeBosted(false);
        familieRelasjoner.add(barn);
        familieRelasjoner.add(barn2);
        familieRelasjoner.add(barn3);

        // Case: gift
        Person ektefelle = genererPersonMedGyldigIdentOgNavn(FNR_EKTEFELLE, "Daisy", null, "Duck");
        familieRelasjoner.add(lagEktefelle(person, ektefelle));
        ektefelle.setFoedselsdato(fodseldato(1973, 12, 7));
        ektefelle.setBostedsadresse(person.getBostedsadresse());

        // Case: gift, og ektefelle har kode 6
        Diskresjonskoder diskresjonskoder = new Diskresjonskoder();
        diskresjonskoder.setValue(KODE_6);
        //ektefelle.setDiskresjonskode(diskresjonskoder);

        // Case: ugift
        // Sivilstander sivilstander = new Sivilstander();
        // sivilstander.setValue("UGIF");
        // Sivilstand sivilstand = new Sivilstand();
        // sivilstand.setSivilstand(sivilstander);
        // person.setSivilstand(sivilstand);

        // Case: Barn har diskresjonskode
        // barn2.getTilPerson().setDiskresjonskode(diskresjonskoder);
        // barn3.getTilPerson().setDiskresjonskode(diskresjonskoder);

        personPortTypeMock.setPerson(person);
    }

    public static PersonMock getInstance() {
        return personMock;
    }

    public PersonPortTypeMock getPersonPortTypeMock() {
        return personPortTypeMock;
    }


    private Familierelasjon lagEktefelle(Person hovedperson, Person ektefelle) {
        Familierelasjon familierelasjon = new Familierelasjon();

        familierelasjon.setTilPerson(ektefelle);
        familierelasjon.setHarSammeBosted(true);

        Familierelasjoner familieRelasjonRolle = new Familierelasjoner();
        familieRelasjonRolle.setValue("EKTE");
        familierelasjon.setTilRolle(familieRelasjonRolle);

        Sivilstander sivilstander = new Sivilstander();
        sivilstander.setValue("GIFT");
        Sivilstand sivilstand = new Sivilstand();
        sivilstand.setSivilstand(sivilstander);

        hovedperson.setSivilstand(sivilstand);

        return familierelasjon;
    }

    private Familierelasjon lagBarn(String fnr, String fornavn, String etternavn) {
        Familierelasjon familierelasjon = new Familierelasjon();
        Person barn = genererPersonMedGyldigIdentOgNavn(fnr, fornavn, null, etternavn);
        familierelasjon.setTilPerson(barn);
        Familierelasjoner familieRelasjonRolle = new Familierelasjoner();
        familieRelasjonRolle.setValue("BARN");
        familierelasjon.setTilRolle(familieRelasjonRolle);
        return familierelasjon;
    }

    private Familierelasjon lagBarn(String fnr, String fornavn, String etternavn, Doedsdato doedsdato) {
        Familierelasjon familierelasjon = lagBarn(fnr, fornavn, etternavn);
        familierelasjon.getTilPerson().setDoedsdato(doedsdato);
        return familierelasjon;
    }

    private Person genererPersonMedGyldigIdentOgNavn(String ident, String fornavn, String mellomnavn, String etternavn) {
        Person xmlPerson = new Person();

        Personnavn personnavn = new Personnavn();
        personnavn.setFornavn(fornavn);
        personnavn.setMellomnavn(mellomnavn);
        personnavn.setEtternavn(etternavn);
        xmlPerson.setPersonnavn(personnavn);

        Personidenter personidenter = new Personidenter();
        personidenter.setValue("FNR");
        NorskIdent norskIdent = new NorskIdent();
        norskIdent.setIdent(ident);
        norskIdent.setType(personidenter);
        xmlPerson.setIdent(norskIdent);

        return xmlPerson;
    }

    private Foedselsdato fodseldato(int year, int month, int day) {
        Foedselsdato foedselsdato = new Foedselsdato();
        foedselsdato.setFoedselsdato(lagDatatypeFactory().newXMLGregorianCalendarDate(year, month, day, 0));
        return foedselsdato;
    }

    private DatatypeFactory lagDatatypeFactory() {
        try {
            return DatatypeFactory.newInstance();
        } catch (DatatypeConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

}
