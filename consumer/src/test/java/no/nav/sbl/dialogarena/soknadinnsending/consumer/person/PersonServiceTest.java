package no.nav.sbl.dialogarena.soknadinnsending.consumer.person;

import no.nav.sbl.dialogarena.sendsoknad.domain.Barn;
import no.nav.sbl.dialogarena.sendsoknad.domain.Ektefelle;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.exceptions.*;
import no.nav.tjeneste.virksomhet.person.v1.*;
import no.nav.tjeneste.virksomhet.person.v1.informasjon.*;
import no.nav.tjeneste.virksomhet.person.v1.meldinger.HentKjerneinformasjonRequest;
import no.nav.tjeneste.virksomhet.person.v1.meldinger.HentKjerneinformasjonResponse;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.xml.ws.WebServiceException;
import java.util.List;

import static no.nav.sbl.dialogarena.soknadinnsending.consumer.person.PersonMapper.*;
import static no.nav.sbl.dialogarena.soknadinnsending.consumer.person.PersonMapperTest.fodseldato;
import static no.nav.sbl.dialogarena.soknadinnsending.consumer.person.PersonService.*;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class PersonServiceTest {
    private static final String SIVILSTATUS_REPA = "REPA";
    private static final String SIVILSTATUS_GIFT = "GIFT";
    private static final String SIVILSTATUS_UGIFT = "UGIF";
    private static final String DISKRESJONSKODE_UFB = "UFB";
    private static final String FORNAVN = "Fornavn";
    private static final String MELLOMNAVN = "Mellomnavn";
    private static final String ETTERNAVN = "Etternavn";
    private static final String FNR = "01018691736";
    private static final int FODSELSAR = 1952;
    private static final int FODSELSMANED = 9;
    private static final int FODSELSDAG = 21;
    private static final String FORNAVN_BARN = "Fornavn1";
    private static final String MELLOMNAVN_BARN = "Mellomnavn1";
    private static final String ETTERNAVN_BARN = "Etternavn1";
    private static final String FNR_BARN = "010206691736";
    private static final int FODSELSAR_BARN = 2006;
    private static final int FODSELSMANED_BARN = 2;
    private static final int FODSELSDAG_BARN = 1;
    private static final String FORNAVN_BARN2 = "Fornavn2";
    private static final String MELLOMNAVN_BARN2 = "Mellomnavn2";
    private static final String ETTERNAVN_BARN2 = "Etternavn2";
    private static final String FNR_BARN2 = "030310691736";
    private static final int FODSELSAR_BARN2 = 2010;
    private static final int FODSELSMANED_BARN2 = 2;
    private static final int FODSELSDAG_BARN2 = 3;

    @InjectMocks
    private PersonService personService;
    @Mock
    private PersonPortType personPortType;

    @Test
    public void finnEktefelleSetterRiktigInfoForRegistrertPartnerUtenDiskresjonskodeOgSammeAdresse() throws HentKjerneinformasjonPersonIkkeFunnet, HentKjerneinformasjonSikkerhetsbegrensning {
        when(personPortType.hentKjerneinformasjon(any(HentKjerneinformasjonRequest.class)))
                .thenReturn(lagHentKjerneinformasjonResponseForMyndigPerson(null));

        Ektefelle registrertPartner = personService.finnEktefelleForPerson(lagResponse(
                lagPersonSomHarEnRelasjon(SIVILSTATUS_REPA, RELASJON_REGISTRERT_PARTNER, false,
                        null, true, null)));

        assertThat(registrertPartner.getFnr(), is(FNR));
        assertThat(registrertPartner.getFornavn(), is(FORNAVN));
        assertThat(registrertPartner.getMellomnavn(), is(MELLOMNAVN));
        assertThat(registrertPartner.getEtternavn(), is(ETTERNAVN));
        assertThat(registrertPartner.getFodselsdato().getYear(), is(FODSELSAR));
        assertThat(registrertPartner.getFodselsdato().getMonthOfYear(), is(FODSELSMANED));
        assertThat(registrertPartner.getFodselsdato().getDayOfMonth(), is(FODSELSDAG));
        assertThat(registrertPartner.erFolkeregistrertsammen(), is(true));
        assertThat(registrertPartner.erUtvandret(), is(false));
        assertThat(registrertPartner.harIkketilgangtilektefelle(), is(false));
    }

    @Test
    public void finnEktefelleSetterRiktigInfoForEktefelleUtenDiskresjonskodeOgUlikFolkeregistrertAdresse() throws HentKjerneinformasjonPersonIkkeFunnet, HentKjerneinformasjonSikkerhetsbegrensning {
        when(personPortType.hentKjerneinformasjon(any(HentKjerneinformasjonRequest.class)))
                .thenReturn(lagHentKjerneinformasjonResponseForMyndigPerson(null));

        Ektefelle ektefelle = personService.finnEktefelleForPerson(lagResponse(
                lagPersonSomHarEnRelasjon(SIVILSTATUS_GIFT, RELASJON_EKTEFELLE, false,
                        null, false, null)));

        assertThat(ektefelle.getFnr(), is(FNR));
        assertThat(ektefelle.getFornavn(), is(FORNAVN));
        assertThat(ektefelle.getMellomnavn(), is(MELLOMNAVN));
        assertThat(ektefelle.getEtternavn(), is(ETTERNAVN));
        assertThat(ektefelle.getFodselsdato().getYear(), is(FODSELSAR));
        assertThat(ektefelle.getFodselsdato().getMonthOfYear(), is(FODSELSMANED));
        assertThat(ektefelle.getFodselsdato().getDayOfMonth(), is(FODSELSDAG));
        assertThat(ektefelle.erFolkeregistrertsammen(), is(false));
        assertThat(ektefelle.erUtvandret(), is(false));
        assertThat(ektefelle.harIkketilgangtilektefelle(), is(false));
    }

    @Test
    public void finnEktefelleViserAtManErGiftSelvOmInfoMangler() throws HentKjerneinformasjonPersonIkkeFunnet, HentKjerneinformasjonSikkerhetsbegrensning {
        when(personPortType.hentKjerneinformasjon(any(HentKjerneinformasjonRequest.class)))
                .thenReturn(lagHentKjerneinformasjonResponseForEktefelleUtenInfo());

        Ektefelle ektefelle = personService.finnEktefelleForPerson(lagResponse(lagPersonMedEktefelleUtenInfo()));

        verify(personPortType, never()).hentKjerneinformasjon(any(HentKjerneinformasjonRequest.class));
        assertThat(ektefelle.getFnr(), nullValue());
        assertThat(ektefelle.getFornavn(), nullValue());
        assertThat(ektefelle.getFodselsdato(), nullValue());
        assertThat(ektefelle.erFolkeregistrertsammen(), is(false));
        assertThat(ektefelle.erUtvandret(), is(false));
        assertThat(ektefelle.harIkketilgangtilektefelle(), is(false));
    }

    @Test
    public void finnEktefelleReturnererIngenEktefelleHvisBrukerErUgift() throws HentKjerneinformasjonPersonIkkeFunnet, HentKjerneinformasjonSikkerhetsbegrensning {
        when(personPortType.hentKjerneinformasjon(any(HentKjerneinformasjonRequest.class)))
                .thenReturn(null);

        Ektefelle ektefelle = personService.finnEktefelleForPerson(lagResponse(new Person()));

        verify(personPortType, never()).hentKjerneinformasjon(any(HentKjerneinformasjonRequest.class));
        assertThat(ektefelle, nullValue());
    }

    @Test
    public void finnEktefelleViserIngenInfoForEktefelleMedDiskresjonskode() throws HentKjerneinformasjonPersonIkkeFunnet, HentKjerneinformasjonSikkerhetsbegrensning {
        when(personPortType.hentKjerneinformasjon(any(HentKjerneinformasjonRequest.class)))
                .thenReturn(lagHentKjerneinformasjonResponseForMyndigPerson(null));

        Ektefelle ektefelle = personService.finnEktefelleForPerson(lagResponse(
                lagPersonSomHarEnRelasjon(SIVILSTATUS_GIFT, RELASJON_EKTEFELLE, true,
                        KODE_6_TALLFORM, false, null)));

        verify(personPortType, never()).hentKjerneinformasjon(any(HentKjerneinformasjonRequest.class));
        assertThat(ektefelle.getFnr(), nullValue());
        assertThat(ektefelle.getFornavn(), nullValue());
        assertThat(ektefelle.getMellomnavn(), nullValue());
        assertThat(ektefelle.getEtternavn(), nullValue());
        assertThat(ektefelle.getFodselsdato(), nullValue());
        assertThat(ektefelle.erFolkeregistrertsammen(), is(false));
        assertThat(ektefelle.erUtvandret(), is(false));
        assertThat(ektefelle.harIkketilgangtilektefelle(), is(true));
    }

    @Test
    public void finnEktefelleSetterRiktigInfoForEktefelleMedDiskresjonskodeUFBOgUlikFolkeregistrertAdresse() throws HentKjerneinformasjonPersonIkkeFunnet, HentKjerneinformasjonSikkerhetsbegrensning {
        when(personPortType.hentKjerneinformasjon(any(HentKjerneinformasjonRequest.class)))
                .thenReturn(lagHentKjerneinformasjonResponseForMyndigPerson(null));

        Ektefelle ektefelle = personService.finnEktefelleForPerson(lagResponse(
                lagPersonSomHarEnRelasjon(SIVILSTATUS_GIFT, RELASJON_EKTEFELLE, true,
                        DISKRESJONSKODE_UFB, false, null)));

        assertThat(ektefelle.getFnr(), is(FNR));
        assertThat(ektefelle.getFornavn(), is(FORNAVN));
        assertThat(ektefelle.getMellomnavn(), is(MELLOMNAVN));
        assertThat(ektefelle.getEtternavn(), is(ETTERNAVN));
        assertThat(ektefelle.getFodselsdato().getYear(), is(FODSELSAR));
        assertThat(ektefelle.getFodselsdato().getMonthOfYear(), is(FODSELSMANED));
        assertThat(ektefelle.getFodselsdato().getDayOfMonth(), is(FODSELSDAG));
        assertThat(ektefelle.erFolkeregistrertsammen(), is(false));
        assertThat(ektefelle.erUtvandret(), is(false));
        assertThat(ektefelle.harIkketilgangtilektefelle(), is(false));
    }

    @Test
    public void finnEktefelleSetterFolkeregistrertSammenTilFalseForUtvandretEktefelle() throws HentKjerneinformasjonPersonIkkeFunnet, HentKjerneinformasjonSikkerhetsbegrensning {
        when(personPortType.hentKjerneinformasjon(any(HentKjerneinformasjonRequest.class)))
                .thenReturn(lagHentKjerneinformasjonResponseForMyndigPerson(UTVANDRET));

        Ektefelle ektefelle = personService.finnEktefelleForPerson(lagResponse(
                lagPersonSomHarEnRelasjon(SIVILSTATUS_GIFT, RELASJON_EKTEFELLE, false,
                        "", true, UTVANDRET)));

        assertThat(ektefelle.erFolkeregistrertsammen(), is(false));
    }

    @Test
    public void finnBarnSetterRiktigInfoForToBarn() throws HentKjerneinformasjonPersonIkkeFunnet, HentKjerneinformasjonSikkerhetsbegrensning {
        Person xmlBarn = lagXmlPersonMedNavnOgIdent(FORNAVN_BARN, MELLOMNAVN_BARN, ETTERNAVN_BARN, FNR_BARN, null,
                fodseldato(FODSELSAR_BARN, FODSELSMANED_BARN, FODSELSDAG_BARN));
        Person xmlBarn2 = lagXmlPersonMedNavnOgIdent(FORNAVN_BARN2, MELLOMNAVN_BARN2, ETTERNAVN_BARN2, FNR_BARN2, null,
                fodseldato(FODSELSAR_BARN2, FODSELSMANED_BARN2, FODSELSDAG_BARN2));
        when(personPortType.hentKjerneinformasjon(any(HentKjerneinformasjonRequest.class)))
                .thenReturn(lagResponse(xmlBarn), lagResponse(xmlBarn2));

        List<Barn> barnliste = personService.hentBarnForPerson(lagResponse(lagPersonSomHarToBarn(xmlBarn, xmlBarn2)));
        Barn barn = barnliste.get(0);
        Barn barn2 = barnliste.get(1);

        verify(personPortType, times(2)).hentKjerneinformasjon(any(HentKjerneinformasjonRequest.class));
        assertThat(barn.getFnr(), is(FNR_BARN));
        assertThat(barn.getFornavn(), is(FORNAVN_BARN));
        assertThat(barn.getMellomnavn(), is(MELLOMNAVN_BARN));
        assertThat(barn.getEtternavn(), is(ETTERNAVN_BARN));
        assertThat(barn.getFodselsdato().getYear(), is(FODSELSAR_BARN));
        assertThat(barn.getFodselsdato().getMonthOfYear(), is(FODSELSMANED_BARN));
        assertThat(barn.getFodselsdato().getDayOfMonth(), is(FODSELSDAG_BARN));
        assertThat(barn.erFolkeregistrertsammen(), is(true));
        assertThat(barn.erUtvandret(), is(false));
        assertThat(barn.harIkkeTilgang(), is(false));

        assertThat(barn2.getFnr(), is(FNR_BARN2));
        assertThat(barn2.getFornavn(), is(FORNAVN_BARN2));
        assertThat(barn2.getMellomnavn(), is(MELLOMNAVN_BARN2));
        assertThat(barn2.getEtternavn(), is(ETTERNAVN_BARN2));
        assertThat(barn2.getFodselsdato().getYear(), is(FODSELSAR_BARN2));
        assertThat(barn2.getFodselsdato().getMonthOfYear(), is(FODSELSMANED_BARN2));
        assertThat(barn2.getFodselsdato().getDayOfMonth(), is(FODSELSDAG_BARN2));
        assertThat(barn2.erFolkeregistrertsammen(), is(false));
        assertThat(barn2.erUtvandret(), is(false));
        assertThat(barn2.harIkkeTilgang(), is(false));
    }

    @Test
    public void finnBarnViserIngenInfoForBarnMedDiskresjonskode() throws HentKjerneinformasjonPersonIkkeFunnet, HentKjerneinformasjonSikkerhetsbegrensning {
        when(personPortType.hentKjerneinformasjon(any(HentKjerneinformasjonRequest.class)))
                .thenReturn(null);
        HentKjerneinformasjonResponse response = new HentKjerneinformasjonResponse();
        response.setPerson(lagPersonSomHarEnRelasjon(SIVILSTATUS_UGIFT, RELASJON_BARN, true,
                KODE_6_TALLFORM, true, null));

        List<Barn> barnliste = personService.hentBarnForPerson(response);
        Barn barn = barnliste.get(0);

        verify(personPortType, never()).hentKjerneinformasjon(any(HentKjerneinformasjonRequest.class));
        assertThat(barnliste.size(), is(1));
        assertThat(barn.getFnr(), nullValue());
        assertThat(barn.getFornavn(), nullValue());
        assertThat(barn.getMellomnavn(), nullValue());
        assertThat(barn.getEtternavn(), nullValue());
        assertThat(barn.getFodselsdato(), nullValue());
        assertThat(barn.erFolkeregistrertsammen(), is(false));
        assertThat(barn.erUtvandret(), is(false));
        assertThat(barn.harIkkeTilgang(), is(true));
    }

    @Test
    public void finnBarnIgnorererMyndigBarn() throws HentKjerneinformasjonPersonIkkeFunnet, HentKjerneinformasjonSikkerhetsbegrensning {
        when(personPortType.hentKjerneinformasjon(any(HentKjerneinformasjonRequest.class)))
                .thenReturn(lagHentKjerneinformasjonResponseForMyndigPerson(null));
        HentKjerneinformasjonResponse response = new HentKjerneinformasjonResponse();
        response.setPerson(lagPersonSomHarEnRelasjon(SIVILSTATUS_UGIFT, RELASJON_BARN, false,
                "", false, null));

        List<Barn> barnliste = personService.hentBarnForPerson(response);

        assertThat(barnliste.size(), is(0));
        verify(personPortType, times(1)).hentKjerneinformasjon(any(HentKjerneinformasjonRequest.class));
    }

    @Test
    public void finnBarnIgnorererDoedtBarn() throws HentKjerneinformasjonPersonIkkeFunnet, HentKjerneinformasjonSikkerhetsbegrensning {
        when(personPortType.hentKjerneinformasjon(any(HentKjerneinformasjonRequest.class)))
                .thenReturn(lagHentKjerneinformasjonResponseForDoedtBarn());
        HentKjerneinformasjonResponse response = new HentKjerneinformasjonResponse();
        response.setPerson(lagPersonSomHarEnRelasjon(SIVILSTATUS_UGIFT, RELASJON_BARN, false,
                "", false, null));

        List<Barn> barnliste = personService.hentBarnForPerson(response);

        assertThat(barnliste.size(), is(0));
        verify(personPortType, times(1)).hentKjerneinformasjon(any(HentKjerneinformasjonRequest.class));
    }

    @Test
    public void finnBarnFinnerIngenBarnForBrukerUtenBarn() throws HentKjerneinformasjonPersonIkkeFunnet, HentKjerneinformasjonSikkerhetsbegrensning {
        when(personPortType.hentKjerneinformasjon(any(HentKjerneinformasjonRequest.class)))
                .thenReturn(null);
        HentKjerneinformasjonResponse response = new HentKjerneinformasjonResponse();
        response.setPerson(lagPersonSomHarEnRelasjon(SIVILSTATUS_REPA, RELASJON_REGISTRERT_PARTNER, false,
                null, true, null));

        List<Barn> barn = personService.hentBarnForPerson(response);

        verify(personPortType, never()).hentKjerneinformasjon(any(HentKjerneinformasjonRequest.class));
        assertThat(barn.size(), is(0));
    }

    @Test(expected = IkkeFunnetException.class)
    public void skalWrappeExceptions() throws HentKjerneinformasjonPersonIkkeFunnet, HentKjerneinformasjonSikkerhetsbegrensning {
        when(personPortType.hentKjerneinformasjon(any(HentKjerneinformasjonRequest.class))).thenThrow(new HentKjerneinformasjonPersonIkkeFunnet("", null));
        personService.hentKjerneinformasjon("");
    }

    @Test(expected = SikkerhetsBegrensningException.class)
    public void skalWrappeExceptions2() throws HentKjerneinformasjonPersonIkkeFunnet, HentKjerneinformasjonSikkerhetsbegrensning {
        when(personPortType.hentKjerneinformasjon(any(HentKjerneinformasjonRequest.class))).thenThrow(new HentKjerneinformasjonSikkerhetsbegrensning("", null));
        personService.hentKjerneinformasjon("");
    }

    @Test(expected = TjenesteUtilgjengeligException.class)
    public void skalWrappeExceptions3() throws HentKjerneinformasjonPersonIkkeFunnet, HentKjerneinformasjonSikkerhetsbegrensning {
        when(personPortType.hentKjerneinformasjon(any(HentKjerneinformasjonRequest.class))).thenThrow(new WebServiceException("", null));
        personService.hentKjerneinformasjon("");
    }

    private HentKjerneinformasjonResponse lagResponse(Person person) {
        HentKjerneinformasjonResponse response = new HentKjerneinformasjonResponse();
        response.setPerson(person);
        return response;
    }

    private HentKjerneinformasjonResponse lagHentKjerneinformasjonResponseForMyndigPerson(String personstatus) {
        HentKjerneinformasjonResponse response = new HentKjerneinformasjonResponse();
        response.setPerson(lagXmlPersonMedNavnOgIdent(FORNAVN, MELLOMNAVN, ETTERNAVN, FNR, personstatus,
                fodseldato(FODSELSAR, FODSELSMANED, FODSELSDAG)));
        return response;
    }

    private HentKjerneinformasjonResponse lagHentKjerneinformasjonResponseForDoedtBarn() {
        HentKjerneinformasjonResponse response = new HentKjerneinformasjonResponse();
        response.setPerson(lagXmlPersonMedNavnOgIdent(FORNAVN, MELLOMNAVN, ETTERNAVN, FNR_BARN, DOED,
                fodseldato(FODSELSAR_BARN, FODSELSMANED_BARN, FODSELSDAG_BARN)));
        return response;
    }

    private Person lagPersonSomHarEnRelasjon(String sivilstatus, String relasjonstype, boolean relasjonHarDiskresjonskode, String diskresjonskode,
                                             boolean harSammeFolkeregistrerteAdresse, String personstatus) {
        Person person = new Person();
        Person relasjon = lagXmlPersonMedNavnOgIdent(FORNAVN, MELLOMNAVN, ETTERNAVN, FNR, personstatus,
                fodseldato(FODSELSAR, FODSELSMANED, FODSELSDAG));
        if (relasjonHarDiskresjonskode) {
            Diskresjonskoder diskresjonskoder = new Diskresjonskoder();
            diskresjonskoder.setValue(diskresjonskode);
            relasjon.setDiskresjonskode(diskresjonskoder);
        }

        Familierelasjon familierelasjon = lagFamilierelasjon(relasjonstype, harSammeFolkeregistrerteAdresse, relasjon);

        Sivilstander sivilstander = new Sivilstander();
        sivilstander.setValue(sivilstatus);
        Sivilstand sivilstand = new Sivilstand();
        sivilstand.setSivilstand(sivilstander);
        person.setSivilstand(sivilstand);

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

    private Person lagXmlPersonMedNavnOgIdent(String fornavn, String mellomnavn, String etternavn, String fnr,
                                              String personstatus, Foedselsdato foedselsdato) {
        Person xmlPerson = new Person();
        Personnavn navn = new Personnavn();
        navn.setFornavn(fornavn);
        navn.setMellomnavn(mellomnavn);
        navn.setEtternavn(etternavn);
        xmlPerson.setPersonnavn(navn);

        Personidenter personidenter = new Personidenter();
        personidenter.setValue("FNR");
        NorskIdent norskIdent = new NorskIdent();
        norskIdent.setIdent(fnr);
        norskIdent.setType(personidenter);
        xmlPerson.setIdent(norskIdent);

        xmlPerson.setFoedselsdato(foedselsdato);

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

        Sivilstander sivilstander = new Sivilstander();
        sivilstander.setValue(SIVILSTATUS_GIFT);
        Sivilstand sivilstand = new Sivilstand();
        sivilstand.setSivilstand(sivilstander);

        person.setSivilstand(sivilstand);
        person.getHarFraRolleI().add(familierelasjon);

        return person;
    }

    private HentKjerneinformasjonResponse lagHentKjerneinformasjonResponseForEktefelleUtenInfo() {
        HentKjerneinformasjonResponse response = new HentKjerneinformasjonResponse();
        response.setPerson(new Person());
        return response;
    }
}
