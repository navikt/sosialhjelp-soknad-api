package no.nav.sbl.dialogarena.soknadinnsending.business.person;

import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Barn;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.SoknadService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.person.PersonConnector;
import no.nav.tjeneste.virksomhet.person.v1.HentKjerneinformasjonPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.person.v1.HentKjerneinformasjonSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.person.v1.informasjon.Familierelasjon;
import no.nav.tjeneste.virksomhet.person.v1.informasjon.Familierelasjoner;
import no.nav.tjeneste.virksomhet.person.v1.informasjon.NorskIdent;
import no.nav.tjeneste.virksomhet.person.v1.informasjon.Person;
import no.nav.tjeneste.virksomhet.person.v1.informasjon.Personnavn;
import no.nav.tjeneste.virksomhet.person.v1.meldinger.HentKjerneinformasjonRequest;
import no.nav.tjeneste.virksomhet.person.v1.meldinger.HentKjerneinformasjonResponse;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;

import static org.mockito.Mockito.when;

@RunWith(value = MockitoJUnitRunner.class)
public class FamilieRelasjonServiceTest {
    private static final String GYLDIG_IDENT = "56128349974";
    private static final String ET_FORNAVN = "Per";
    private static final String ET_ETTERNAVN = "Persen";
    private static final String FEIL_IDENT = "99999999999";
    private static final String BARN_IDENT = "***REMOVED***";
    private static final String BARN_FORNAVN = "Bjarne";
    private static final String BARN_ETTERNAVN = "Barnet";
    private static final Object BARN_SAMMENSATTNAVN = BARN_FORNAVN + " " + BARN_ETTERNAVN;

    @InjectMocks
    private FamilieRelasjonServiceTPS service;

    @Mock
    private PersonConnector personMock;

    @Mock
    @SuppressWarnings("PMD")
    private SoknadService soknadServiceMock;

    @SuppressWarnings("unchecked")
    @Test
    public void returnerPersonUtenDataHvisPersonenSomReturneresHarFeilIdent() throws HentKjerneinformasjonPersonIkkeFunnet, HentKjerneinformasjonSikkerhetsbegrensning {
        HentKjerneinformasjonRequest request = new HentKjerneinformasjonRequest();
        request.setIdent(FEIL_IDENT);
        when(personMock.hentKjerneinformasjon(request)).thenThrow(HentKjerneinformasjonPersonIkkeFunnet.class);
        no.nav.sbl.dialogarena.soknadinnsending.business.person.Person familieRelasjonPerson = service.hentPerson(1l, FEIL_IDENT);
        Assert.assertNotNull(familieRelasjonPerson);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void returnererPersonObjektDersomPersonenSomReturneresHarRiktigIdent() throws HentKjerneinformasjonPersonIkkeFunnet, HentKjerneinformasjonSikkerhetsbegrensning {
        HentKjerneinformasjonResponse response = new HentKjerneinformasjonResponse();

        Person xmlPerson = genererPersonMedGyldigIdentOgNavn(GYLDIG_IDENT, ET_FORNAVN, ET_ETTERNAVN);
        response.setPerson(xmlPerson);

        when(personMock.hentKjerneinformasjon(Mockito.any(HentKjerneinformasjonRequest.class))).thenReturn(response);

        no.nav.sbl.dialogarena.soknadinnsending.business.person.Person familieRelasjonPerson = service.hentPerson(2l, GYLDIG_IDENT);

        Assert.assertNotNull(familieRelasjonPerson.getFakta());

        Faktum fnr = (Faktum) familieRelasjonPerson.getFakta().get("fnr");
        Faktum fornavn = (Faktum) familieRelasjonPerson.getFakta().get("fornavn");
        Faktum etternavnavn = (Faktum) familieRelasjonPerson.getFakta().get("etternavn");
        Faktum sammensattnavn = (Faktum) familieRelasjonPerson.getFakta().get("sammensattnavn");
        List<Familierelasjon> barn = (List<Familierelasjon>) familieRelasjonPerson.getFakta().get("barn");
        Assert.assertEquals(GYLDIG_IDENT, fnr.getValue());
        Assert.assertEquals(ET_FORNAVN, fornavn.getValue());
        Assert.assertEquals(ET_ETTERNAVN, etternavnavn.getValue());
        Assert.assertEquals(ET_FORNAVN + " " + ET_ETTERNAVN, sammensattnavn.getValue());
        Assert.assertEquals(0, barn.size());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void skalHenteBarn() throws HentKjerneinformasjonPersonIkkeFunnet, HentKjerneinformasjonSikkerhetsbegrensning {
        HentKjerneinformasjonResponse response = new HentKjerneinformasjonResponse();

        Person xmlPerson = genererPersonMedGyldigIdentOgNavn(GYLDIG_IDENT, ET_FORNAVN, ET_ETTERNAVN);
        List<Familierelasjon> familieRelasjoner = xmlPerson.getHarFraRolleI();

        Familierelasjon familierelasjon = new Familierelasjon();
        Person barn1 = genererPersonMedGyldigIdentOgNavn(BARN_IDENT, BARN_FORNAVN, BARN_ETTERNAVN);

        familierelasjon.setTilPerson(barn1);
        Familierelasjoner familieRelasjonRolle = new Familierelasjoner();

        familieRelasjonRolle.setValue("BARN");
        familierelasjon.setTilRolle(familieRelasjonRolle);

        familieRelasjoner.add(familierelasjon);

        response.setPerson(xmlPerson);

        when(personMock.hentKjerneinformasjon(Mockito.any(HentKjerneinformasjonRequest.class))).thenReturn(response);

        no.nav.sbl.dialogarena.soknadinnsending.business.person.Person familieRelasjonPerson = service.hentPerson(2l, GYLDIG_IDENT);

        Faktum fnr = (Faktum) familieRelasjonPerson.getFakta().get("fnr");
        Faktum fornavn = (Faktum) familieRelasjonPerson.getFakta().get("fornavn");
        Faktum etternavnavn = (Faktum) familieRelasjonPerson.getFakta().get("etternavn");
        Faktum sammensattnavn = (Faktum) familieRelasjonPerson.getFakta().get("sammensattnavn");
        List<Barn> barn = (List<Barn>) familieRelasjonPerson.getFakta().get("barn");

        Assert.assertEquals(GYLDIG_IDENT, fnr.getValue());
        Assert.assertEquals(ET_FORNAVN, fornavn.getValue());
        Assert.assertEquals(ET_ETTERNAVN, etternavnavn.getValue());
        Assert.assertEquals(ET_FORNAVN + " " + ET_ETTERNAVN, sammensattnavn.getValue());

        Assert.assertEquals(1, barn.size());
        Barn b1 = barn.get(0);

        Assert.assertEquals(BARN_IDENT, b1.getFnr());
        Assert.assertEquals(BARN_FORNAVN, b1.getFornavn());
        Assert.assertEquals(BARN_ETTERNAVN, b1.getEtternavn());
        Assert.assertEquals(BARN_SAMMENSATTNAVN, b1.getSammensattnavn());
    }


    private Person genererPersonMedGyldigIdentOgNavn(String ident, String fornavn, String etternavn) {
        Person xmlPerson = new Person();

        Personnavn personnavn = new Personnavn();
        personnavn.setFornavn(fornavn);
        personnavn.setMellomnavn("");
        personnavn.setEtternavn(etternavn);
        xmlPerson.setPersonnavn(personnavn);

        NorskIdent norskIdent = new NorskIdent();
        norskIdent.setIdent(ident);
        xmlPerson.setIdent(norskIdent);

        return xmlPerson;
    }

}
