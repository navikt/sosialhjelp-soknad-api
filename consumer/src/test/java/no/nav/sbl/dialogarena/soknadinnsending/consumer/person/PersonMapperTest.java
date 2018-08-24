package no.nav.sbl.dialogarena.soknadinnsending.consumer.person;

import no.nav.tjeneste.virksomhet.person.v1.informasjon.*;
import org.joda.time.LocalDate;
import org.junit.Test;

import static no.nav.sbl.dialogarena.sendsoknad.domain.util.ServiceUtils.lagDatatypeFactory;
import static no.nav.sbl.dialogarena.soknadinnsending.consumer.person.PersonMapper.*;
import static org.hamcrest.Matchers.isEmptyString;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class PersonMapperTest {
    private static final String SIVILSTATUS_REPA = "REPA";
    private static final String SIVILSTATUS_GIFT = "GIFT";
    private static final String SIVILSTATUS_GLAD = "GLAD";
    private static final String SIVILSTATUS_UGIF = "UGIF";
    private static final String SIVILSTATUS_ENKE = "ENKE";
    private static final String DISKRESJONSKODE_UFB = "UFB";
    private static final String FORNAVN = "Fornavn";
    private static final String MELLOMNAVN = "Mellomnavn";
    private static final String ETTERNAVN = "Etternavn";
    private static final String SAMMENSATT_NAVN = "Fornavn Mellomnavn Etternavn";
    private static final String SAMMENSATT_NAVN_UTEN_MELLOMNAVN = "Fornavn Etternavn";
    private static final String FNR = "***REMOVED***";
    private static final int FODSELSAR = 1952;
    private static final int FODSELSMANED = 9;
    private static final int FODSELSDAG = 21;
    private static final String LANDKODE = "DNK";

    @Test
    public void mapXmlPersonTilPersonMapperPersonRiktig() {
        no.nav.sbl.dialogarena.sendsoknad.domain.Person person = mapXmlPersonTilPerson(lagXmlPerson());

        assertThat(person.getFornavn(), is(FORNAVN));
        assertThat(person.getMellomnavn(), is(MELLOMNAVN));
        assertThat(person.getEtternavn(), is(ETTERNAVN));
        assertThat(person.getSammensattNavn(), is(SAMMENSATT_NAVN));
        assertThat(person.getFnr(), is(FNR));
        assertThat(person.getFodselsdato().getYear(), is(FODSELSAR));
        assertThat(person.getFodselsdato().getMonthOfYear(), is(FODSELSMANED));
        assertThat(person.getFodselsdato().getDayOfMonth(), is(FODSELSDAG));
        assertThat(person.getAlder(), notNullValue());
        assertThat(person.getKjonn(), is("k"));
        assertThat(person.getSivilstatus(), is("enke"));
        assertThat(person.getStatsborgerskap(), is(LANDKODE));
        assertThat(person.getDiskresjonskode(), is(DISKRESJONSKODE_UFB));
    }

    @Test
    public void mapXmlPersonTilPersonTaklerTomPerson() {
        no.nav.sbl.dialogarena.sendsoknad.domain.Person person = mapXmlPersonTilPerson(new Person());

        assertThat(person, notNullValue());
    }

    @Test
    public void xmlPersonHarDiskresjonskodeReturnererTrueHvisBrukerHarKode6() {
        boolean harDiskresjonskode = xmlPersonHarDiskresjonskode(lagXmlPersonMedDiskresjonskode(KODE_6_TALLFORM));

        assertThat(harDiskresjonskode, is(true));
    }

    @Test
    public void xmlPersonHarDiskresjonskodeReturnererTrueHvisBrukerHarKode7() {
        boolean harDiskresjonskode = xmlPersonHarDiskresjonskode(lagXmlPersonMedDiskresjonskode(KODE_7_TALLFORM));

        assertThat(harDiskresjonskode, is(true));
    }

    @Test
    public void xmlPersonHarDiskresjonskodeReturnererTrueHvisBrukerHarKodeSPSF() {
        boolean harDiskresjonskode = xmlPersonHarDiskresjonskode(lagXmlPersonMedDiskresjonskode(KODE_6));

        assertThat(harDiskresjonskode, is(true));
    }

    @Test
    public void xmlPersonHarDiskresjonskodeReturnererFalseHvisBrukerHarDiskresjonskodeUFB() {
        boolean harDiskresjonskode = xmlPersonHarDiskresjonskode(lagXmlPersonMedDiskresjonskode(DISKRESJONSKODE_UFB));

        assertThat(harDiskresjonskode, is(false));
    }

    @Test
    public void erDoedReturnererTrueHvisPersonErDoed() {
        boolean erDoed = erDoed(lagXmlPersonMedPersonstatus(DOED));

        assertThat(erDoed, is(true));
    }

    @Test
    public void personErUtvandretReturnererTrueForUtvandretPerson() {
        boolean erUtvandret = personErUtvandret(lagXmlPersonMedPersonstatus(UTVANDRET));

        assertThat(erUtvandret, is(true));
    }

    @Test
    public void finnSivilstatusSetterSivilstatusGiftForGiftBruker() {
        String sivilstatus = finnSivilstatus(lagXmlPersonMedSivilstatus(SIVILSTATUS_GIFT));

        assertThat(sivilstatus, notNullValue());
        assertThat(sivilstatus, is("gift"));
    }

    @Test
    public void finnSivilstatusSetterSivilstatusGiftForGLADBruker() {
        String sivilstatus = finnSivilstatus(lagXmlPersonMedSivilstatus(SIVILSTATUS_GLAD));

        assertThat(sivilstatus, notNullValue());
        assertThat(sivilstatus, is("gift"));
    }

    @Test
    public void finnSivilstatusSetterSivilstatusGiftForBrukerMedRegistrertPartner() {
        String sivilstatus = finnSivilstatus(lagXmlPersonMedSivilstatus(SIVILSTATUS_REPA));

        assertThat(sivilstatus, notNullValue());
        assertThat(sivilstatus, is("gift"));
    }

    @Test
    public void finnSivilstatusSetterSivilstatusUgiftForUgiftBruker() {
        String sivilstatus = finnSivilstatus(lagXmlPersonMedSivilstatus(SIVILSTATUS_UGIF));

        assertThat(sivilstatus, notNullValue());
        assertThat(sivilstatus, is("ugift"));
    }

    @Test
    public void erMyndigReturnererFalseForDatoMindreEnnAttenAarSiden() {
        boolean erMyndig = erMyndig(new LocalDate(2017, 1, 1));

        assertThat(erMyndig, is(false));
    }

    @Test
    public void erMyndigReturnererTrueForDatoMerEnnAttenAarSiden() {
        boolean erMyndig = erMyndig(new LocalDate(1984, 1, 1));

        assertThat(erMyndig, is(true));
    }

    @Test
    public void finnSammensattNavnSetterSammenNavnRiktig() {
        String sammensattNavn = finnSammensattNavn(lagXmlPersonMedNavn());

        assertThat(sammensattNavn, is(SAMMENSATT_NAVN));
    }

    @Test
    public void finnSammensattNavnSetterSammenNavnUtenMellomnavnRiktig() {
        String sammensattNavn = finnSammensattNavn(lagXmlPersonMedNavn(FORNAVN, null, ETTERNAVN));

        assertThat(sammensattNavn, is(SAMMENSATT_NAVN_UTEN_MELLOMNAVN));
    }

    @Test
    public void mapXmlPersonTilPersonTaklerPersonUtenNavn() {
        Person person = new Person();
        person.setPersonnavn(new Personnavn());

        String sammensattNavn = finnSammensattNavn(person);

        assertThat(sammensattNavn, isEmptyString());
    }

    private Person lagXmlPerson() {
        Person xmlPerson = lagXmlPersonMedNavn();
        Personidenter personidenter = new Personidenter();
        personidenter.setValue("FNR");
        NorskIdent norskIdent = new NorskIdent();
        norskIdent.setIdent(FNR);
        norskIdent.setType(personidenter);
        xmlPerson.setIdent(norskIdent);

        xmlPerson.setFoedselsdato(fodseldato(FODSELSAR, FODSELSMANED, FODSELSDAG));

        Kjoennstyper kjoennstyper = new Kjoennstyper();
        kjoennstyper.setValue("K");
        Kjoenn kjoenn = new Kjoenn();
        kjoenn.setKjoenn(kjoennstyper);
        xmlPerson.setKjoenn(kjoenn);

        xmlPerson.setSivilstand(lagSivilstand(SIVILSTATUS_ENKE));

        Statsborgerskap statsborgerskap = new Statsborgerskap();
        Landkoder landkode = new Landkoder();
        landkode.setValue(LANDKODE);
        statsborgerskap.setLand(landkode);
        xmlPerson.setStatsborgerskap(statsborgerskap);

        xmlPerson.setDiskresjonskode(lagDiskresjonskode(DISKRESJONSKODE_UFB));

        return xmlPerson;
    }

    private Person lagXmlPersonMedDiskresjonskode(String diskresjonskode) {
        Person xmlPerson = new Person();
        xmlPerson.setDiskresjonskode(lagDiskresjonskode(diskresjonskode));
        return xmlPerson;
    }

    private Diskresjonskoder lagDiskresjonskode(String diskresjonskode) {
        Diskresjonskoder diskresjonskoder = new Diskresjonskoder();
        diskresjonskoder.setValue(diskresjonskode);
        return diskresjonskoder;
    }

    private Person lagXmlPersonMedPersonstatus(String personstatus) {
        Person xmlPerson = new Person();
        Personstatuser personstatuser = new Personstatuser();
        personstatuser.setValue(personstatus);
        Personstatus status = new Personstatus();
        status.setPersonstatus(personstatuser);
        xmlPerson.setPersonstatus(status);
        return xmlPerson;
    }

    private Person lagXmlPersonMedSivilstatus(String sivilstatus) {
        Person xmlPerson = new Person();
        xmlPerson.setSivilstand(lagSivilstand(sivilstatus));
        return xmlPerson;
    }

    private Sivilstand lagSivilstand(String sivilstatus) {
        Sivilstander sivilstander = new Sivilstander();
        sivilstander.setValue(sivilstatus);
        Sivilstand sivilstand = new Sivilstand();
        sivilstand.setSivilstand(sivilstander);
        return sivilstand;
    }

    private Person lagXmlPersonMedNavn() {
        return this.lagXmlPersonMedNavn(FORNAVN, MELLOMNAVN, ETTERNAVN);
    }

    private Person lagXmlPersonMedNavn(String fornavn, String mellomnavn, String etternavn) {
        Person xmlPerson = new Person();
        Personnavn navn = new Personnavn();
        navn.setFornavn(fornavn);
        navn.setMellomnavn(mellomnavn);
        navn.setEtternavn(etternavn);
        xmlPerson.setPersonnavn(navn);
        return xmlPerson;
    }

    public static Foedselsdato fodseldato(int year, int month, int day) {
        Foedselsdato foedselsdato = new Foedselsdato();
        foedselsdato.setFoedselsdato(lagDatatypeFactory().newXMLGregorianCalendarDate(year, month, day, 0));
        return foedselsdato;
    }
}