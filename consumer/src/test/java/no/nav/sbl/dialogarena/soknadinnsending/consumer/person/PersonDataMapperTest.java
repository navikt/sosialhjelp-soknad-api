package no.nav.sbl.dialogarena.soknadinnsending.consumer.person;

import no.nav.sbl.dialogarena.sendsoknad.domain.Barn;
import no.nav.sbl.dialogarena.sendsoknad.domain.Ektefelle;
import no.nav.tjeneste.virksomhet.person.v1.informasjon.Diskresjonskoder;
import no.nav.tjeneste.virksomhet.person.v1.informasjon.Familierelasjon;
import no.nav.tjeneste.virksomhet.person.v1.informasjon.Familierelasjoner;
import no.nav.tjeneste.virksomhet.person.v1.informasjon.Foedselsdato;
import no.nav.tjeneste.virksomhet.person.v1.informasjon.Kjoenn;
import no.nav.tjeneste.virksomhet.person.v1.informasjon.Kjoennstyper;
import no.nav.tjeneste.virksomhet.person.v1.informasjon.Landkoder;
import no.nav.tjeneste.virksomhet.person.v1.informasjon.NorskIdent;
import no.nav.tjeneste.virksomhet.person.v1.informasjon.Person;
import no.nav.tjeneste.virksomhet.person.v1.informasjon.Personidenter;
import no.nav.tjeneste.virksomhet.person.v1.informasjon.Personnavn;
import no.nav.tjeneste.virksomhet.person.v1.informasjon.Personstatus;
import no.nav.tjeneste.virksomhet.person.v1.informasjon.Personstatuser;
import no.nav.tjeneste.virksomhet.person.v1.informasjon.Sivilstand;
import no.nav.tjeneste.virksomhet.person.v1.informasjon.Sivilstander;
import no.nav.tjeneste.virksomhet.person.v1.informasjon.Statsborgerskap;
import org.joda.time.LocalDate;
import org.junit.Test;

import java.util.List;

import static no.nav.sbl.dialogarena.sendsoknad.domain.util.ServiceUtils.lagDatatypeFactory;
import static no.nav.sbl.dialogarena.soknadinnsending.consumer.person.PersonMapper.DOED;
import static no.nav.sbl.dialogarena.soknadinnsending.consumer.person.PersonMapper.KODE_6;
import static no.nav.sbl.dialogarena.soknadinnsending.consumer.person.PersonMapper.KODE_6_TALLFORM;
import static no.nav.sbl.dialogarena.soknadinnsending.consumer.person.PersonMapper.KODE_7_TALLFORM;
import static no.nav.sbl.dialogarena.soknadinnsending.consumer.person.PersonMapper.RELASJON_BARN;
import static no.nav.sbl.dialogarena.soknadinnsending.consumer.person.PersonMapper.RELASJON_EKTEFELLE;
import static no.nav.sbl.dialogarena.soknadinnsending.consumer.person.PersonMapper.RELASJON_REGISTRERT_PARTNER;
import static no.nav.sbl.dialogarena.soknadinnsending.consumer.person.PersonMapper.erDoed;
import static no.nav.sbl.dialogarena.soknadinnsending.consumer.person.PersonMapper.erMyndig;
import static no.nav.sbl.dialogarena.soknadinnsending.consumer.person.PersonMapper.finnBarnForPerson;
import static no.nav.sbl.dialogarena.soknadinnsending.consumer.person.PersonMapper.finnEktefelleForPerson;
import static no.nav.sbl.dialogarena.soknadinnsending.consumer.person.PersonMapper.finnSammensattNavn;
import static no.nav.sbl.dialogarena.soknadinnsending.consumer.person.PersonMapper.finnSivilstatus;
import static no.nav.sbl.dialogarena.soknadinnsending.consumer.person.PersonMapper.mapXmlPersonTilPerson;
import static no.nav.sbl.dialogarena.soknadinnsending.consumer.person.PersonMapper.xmlPersonHarDiskresjonskode;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;

public class PersonDataMapperTest {
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
    private static final String FNR = "21098691736"; // Ikke ekte person
    private static final int FODSELSAR = 1986;
    private static final int FODSELSMANED = 9;
    private static final int FODSELSDAG = 21;
    private static final String LANDKODE = "DNK";
    private static final String FORNAVN_BARN = "Fornavn1";
    private static final String MELLOMNAVN_BARN = "Mellomnavn1";
    private static final String ETTERNAVN_BARN = "Etternavn1";
    private static final String FNR_BARN = "010206691736"; // Ikke ekte person
    private static final int FODSELSAR_BARN = 2006;
    private static final int FODSELSMANED_BARN = 2;
    private static final int FODSELSDAG_BARN = 1;
    private static final String FORNAVN_BARN2 = "Fornavn2";
    private static final String MELLOMNAVN_BARN2 = "Mellomnavn2";
    private static final String ETTERNAVN_BARN2 = "Etternavn2";
    private static final String FNR_BARN2 = "030210691736"; // Ikke ekte person
    private static final int FODSELSAR_BARN2 = 2010;
    private static final int FODSELSMANED_BARN2 = 2;
    private static final int FODSELSDAG_BARN2 = 3;

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
    public void finnEktefelleForPersonSetterRiktigInfoForRegistrertPartnerUtenDiskresjonskodeOgSammeAdresse() {
        Ektefelle registrertPartner = finnEktefelleForPerson(lagPersonSomHarEnRelasjon(SIVILSTATUS_REPA, RELASJON_REGISTRERT_PARTNER,
                false, null, true, null));

        assertThat(registrertPartner.getFnr(), is(FNR));
        assertThat(registrertPartner.getFornavn(), is(FORNAVN));
        assertThat(registrertPartner.getMellomnavn(), is(MELLOMNAVN));
        assertThat(registrertPartner.getEtternavn(), is(ETTERNAVN));
        assertThat(registrertPartner.getFodselsdato().getYear(), is(FODSELSAR));
        assertThat(registrertPartner.getFodselsdato().getMonthOfYear(), is(FODSELSMANED));
        assertThat(registrertPartner.getFodselsdato().getDayOfMonth(), is(FODSELSDAG));
        assertThat(registrertPartner.erFolkeregistrertsammen(), is(true));
        assertThat(registrertPartner.harIkketilgangtilektefelle(), is(false));
    }

    @Test
    public void finnEktefelleForPersonSetterRiktigInfoForEktefelleUtenDiskresjonskodeOgUlikFolkeregistrertAdresse() {
        Ektefelle ektefelle = finnEktefelleForPerson(lagPersonSomHarEnRelasjon(SIVILSTATUS_GIFT, RELASJON_EKTEFELLE,
                false, null, false, null));

        assertThat(ektefelle.getFnr(), is(FNR));
        assertThat(ektefelle.getFornavn(), is(FORNAVN));
        assertThat(ektefelle.getMellomnavn(), is(MELLOMNAVN));
        assertThat(ektefelle.getEtternavn(), is(ETTERNAVN));
        assertThat(ektefelle.getFodselsdato().getYear(), is(FODSELSAR));
        assertThat(ektefelle.getFodselsdato().getMonthOfYear(), is(FODSELSMANED));
        assertThat(ektefelle.getFodselsdato().getDayOfMonth(), is(FODSELSDAG));
        assertThat(ektefelle.erFolkeregistrertsammen(), is(false));
        assertThat(ektefelle.harIkketilgangtilektefelle(), is(false));
    }

    @Test
    public void finnEktefelleForPersonViserAtManErGiftSelvOmInfoMangler() {
        Ektefelle ektefelle = finnEktefelleForPerson(lagPersonMedEktefelleUtenInfo());

        assertThat(ektefelle.getFnr(), nullValue());
        assertThat(ektefelle.getFornavn(), is(emptyString()));
        assertThat(ektefelle.getFodselsdato(), nullValue());
        assertThat(ektefelle.erFolkeregistrertsammen(), is(false));
        assertThat(ektefelle.harIkketilgangtilektefelle(), is(false));
    }

    @Test
    public void finnEktefelleForPersonReturnererIngenEktefelleHvisBrukerErUgift() {
        Ektefelle ektefelle = finnEktefelleForPerson(new Person());

        assertThat(ektefelle, nullValue());
    }

    @Test
    public void finnEktefelleForPersonViserIngenInfoForEktefelleMedDiskresjonskode() {
        Ektefelle ektefelle = finnEktefelleForPerson(lagPersonSomHarEnRelasjon(SIVILSTATUS_GIFT, RELASJON_EKTEFELLE, true,
                KODE_6_TALLFORM, false, null));

        assertThat(ektefelle.getFnr(), nullValue());
        assertThat(ektefelle.getFornavn(), nullValue());
        assertThat(ektefelle.getMellomnavn(), nullValue());
        assertThat(ektefelle.getEtternavn(), nullValue());
        assertThat(ektefelle.getFodselsdato(), nullValue());
        assertThat(ektefelle.erFolkeregistrertsammen(), is(false));
        assertThat(ektefelle.harIkketilgangtilektefelle(), is(true));
    }

    @Test
    public void finnEktefelleForPersonSetterRiktigInfoForEktefelleMedDiskresjonskodeUFBOgUlikFolkeregistrertAdresse() {
        Ektefelle ektefelle = finnEktefelleForPerson(lagPersonSomHarEnRelasjon(SIVILSTATUS_GIFT, RELASJON_EKTEFELLE, true,
                DISKRESJONSKODE_UFB, false, null));

        assertThat(ektefelle.getFnr(), is(FNR));
        assertThat(ektefelle.getFornavn(), is(FORNAVN));
        assertThat(ektefelle.getMellomnavn(), is(MELLOMNAVN));
        assertThat(ektefelle.getEtternavn(), is(ETTERNAVN));
        assertThat(ektefelle.getFodselsdato().getYear(), is(FODSELSAR));
        assertThat(ektefelle.getFodselsdato().getMonthOfYear(), is(FODSELSMANED));
        assertThat(ektefelle.getFodselsdato().getDayOfMonth(), is(FODSELSDAG));
        assertThat(ektefelle.erFolkeregistrertsammen(), is(false));
        assertThat(ektefelle.harIkketilgangtilektefelle(), is(false));
    }

    @Test
    public void finnBarnForPersonSetterRiktigInfoForToBarn() {
        Person xmlBarn = lagXmlPersonMedNavnOgIdent(FORNAVN_BARN, MELLOMNAVN_BARN, ETTERNAVN_BARN, FNR_BARN, null);
        Person xmlBarn2 = lagXmlPersonMedNavnOgIdent(FORNAVN_BARN2, MELLOMNAVN_BARN2, ETTERNAVN_BARN2, FNR_BARN2, null);

        List<Barn> barnliste = finnBarnForPerson(lagPersonSomHarToBarn(xmlBarn, xmlBarn2));
        Barn barn = barnliste.get(0);
        Barn barn2 = barnliste.get(1);

        assertThat(barn.getFnr(), is(FNR_BARN));
        assertThat(barn.getFornavn(), is(FORNAVN_BARN));
        assertThat(barn.getMellomnavn(), is(MELLOMNAVN_BARN));
        assertThat(barn.getEtternavn(), is(ETTERNAVN_BARN));
        assertThat(barn.getFodselsdato().getYear(), is(FODSELSAR_BARN));
        assertThat(barn.getFodselsdato().getMonthOfYear(), is(FODSELSMANED_BARN));
        assertThat(barn.getFodselsdato().getDayOfMonth(), is(FODSELSDAG_BARN));
        assertThat(barn.erFolkeregistrertsammen(), is(true));
        assertThat(barn.harIkkeTilgang(), is(false));

        assertThat(barn2.getFnr(), is(FNR_BARN2));
        assertThat(barn2.getFornavn(), is(FORNAVN_BARN2));
        assertThat(barn2.getMellomnavn(), is(MELLOMNAVN_BARN2));
        assertThat(barn2.getEtternavn(), is(ETTERNAVN_BARN2));
        assertThat(barn2.getFodselsdato().getYear(), is(FODSELSAR_BARN2));
        assertThat(barn2.getFodselsdato().getMonthOfYear(), is(FODSELSMANED_BARN2));
        assertThat(barn2.getFodselsdato().getDayOfMonth(), is(FODSELSDAG_BARN2));
        assertThat(barn2.erFolkeregistrertsammen(), is(false));
        assertThat(barn2.harIkkeTilgang(), is(false));
    }

    @Test
    public void finnBarnForPersonViserIngenInfoForBarnMedDiskresjonskode() {
        List<Barn> barnliste = finnBarnForPerson(lagPersonSomHarEnRelasjon(SIVILSTATUS_UGIF, RELASJON_BARN, true,
                KODE_6_TALLFORM, true, null));

        assertThat(barnliste.isEmpty(), is(true));
    }

    @Test
    public void finnBarnForPersonIgnorererMyndigBarn() {
        List<Barn> barnliste = finnBarnForPerson(lagPersonSomHarEnRelasjon(SIVILSTATUS_UGIF, RELASJON_BARN, false,
                "", false, null));

        assertThat(barnliste.size(), is(0));
    }

    @Test
    public void finnBarnForPersonIgnorererDoedtBarn() {
        List<Barn> barnliste = finnBarnForPerson(lagPersonSomHarEnRelasjon(SIVILSTATUS_UGIF, RELASJON_BARN, false,
                "", false, null));

        assertThat(barnliste.size(), is(0));
    }

    @Test
    public void finnBarnForPersonFinnerIngenBarnForBrukerUtenBarn() {
        List<Barn> barn = finnBarnForPerson(lagPersonSomHarEnRelasjon(SIVILSTATUS_REPA, RELASJON_REGISTRERT_PARTNER, false,
                null, true, null));

        assertThat(barn.size(), is(0));
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

        assertThat(sammensattNavn, is(emptyString()));
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

    private Person lagPersonSomHarEnRelasjon(String sivilstatus, String relasjonstype, boolean relasjonHarDiskresjonskode, String diskresjonskode,
                                             boolean harSammeFolkeregistrerteAdresse, String personstatus) {
        Person person = new Person();
        Person relasjon = lagXmlPersonMedNavnOgIdent(FORNAVN, MELLOMNAVN, ETTERNAVN, FNR, personstatus);
        if (relasjonHarDiskresjonskode) {
            relasjon.setDiskresjonskode(lagDiskresjonskode(diskresjonskode));
        }

        Familierelasjon familierelasjon = lagFamilierelasjon(relasjonstype, harSammeFolkeregistrerteAdresse, relasjon);

        person.setSivilstand(lagSivilstand(sivilstatus));
        person.getHarFraRolleI().add(familierelasjon);

        return person;
    }

    private Person lagPersonSomHarToBarn(Person barn, Person barn2) {
        Person person = new Person();

        Familierelasjon familierelasjon = lagFamilierelasjon(RELASJON_BARN, true, barn);
        Familierelasjon familierelasjon2 = lagFamilierelasjon(RELASJON_BARN, false, barn2);

        person.getHarFraRolleI().add(familierelasjon);
        person.getHarFraRolleI().add(familierelasjon2);

        return person;
    }

    private Familierelasjon lagFamilierelasjon(String relasjonstype, boolean harSammeFolkeregistrerteAdresse, Person relasjon) {
        Familierelasjon familierelasjon = new Familierelasjon();
        familierelasjon.setTilPerson(relasjon);
        familierelasjon.setHarSammeBosted(harSammeFolkeregistrerteAdresse);
        Familierelasjoner familierelasjoner = new Familierelasjoner();
        familierelasjoner.setValue(relasjonstype);
        familierelasjon.setTilRolle(familierelasjoner);
        return familierelasjon;
    }

    private Person lagXmlPersonMedNavnOgIdent(String fornavn, String mellomnavn, String etternavn, String fnr, String personstatus) {
        Person xmlPerson = lagXmlPersonMedNavn(fornavn, mellomnavn, etternavn);

        Personidenter personidenter = new Personidenter();
        personidenter.setValue("FNR");
        NorskIdent norskIdent = new NorskIdent();
        norskIdent.setIdent(fnr);
        norskIdent.setType(personidenter);
        xmlPerson.setIdent(norskIdent);

        if (personstatus != null) {
            Personstatuser personstatuser = new Personstatuser();
            personstatuser.setValue(personstatus);
            Personstatus status = new Personstatus();
            status.setPersonstatus(personstatuser);
            xmlPerson.setPersonstatus(status);
        }
        return xmlPerson;
    }

    private Person lagPersonMedEktefelleUtenInfo() {
        Person person = new Person();

        Person partner = new Person();
        Familierelasjon familierelasjon = lagFamilierelasjon(RELASJON_EKTEFELLE, false, partner);

        person.setSivilstand(lagSivilstand(SIVILSTATUS_GIFT));
        person.getHarFraRolleI().add(familierelasjon);

        return person;
    }
}