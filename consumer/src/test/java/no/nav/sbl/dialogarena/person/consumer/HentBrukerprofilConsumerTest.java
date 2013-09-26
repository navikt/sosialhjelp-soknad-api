package no.nav.sbl.dialogarena.person.consumer;

import no.nav.modig.core.exception.ApplicationException;
import no.nav.sbl.dialogarena.person.Person;
import no.nav.sbl.dialogarena.person.ValgtKontotype;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.BrukerprofilPortType;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.HentKontaktinformasjonOgPreferanserPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.HentKontaktinformasjonOgPreferanserSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLBankkontoNorge;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLBankkontoUtland;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLBankkontonummer;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLBankkontonummerUtland;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLBruker;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLGateadresse;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLGyldighetsperiode;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLMidlertidigPostadresse;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLMidlertidigPostadresseNorge;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLMidlertidigPostadresseUtland;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLPersonnavn;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLPostadresse;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLUstrukturertAdresse;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.meldinger.XMLHentKontaktinformasjonOgPreferanserRequest;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.meldinger.XMLHentKontaktinformasjonOgPreferanserResponse;
import org.joda.time.DateTimeUtils;
import org.joda.time.LocalDate;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static no.nav.sbl.dialogarena.person.consumer.GjeldendeAdresseKodeverk.MIDLERTIDIG_POSTADRESSE_NORGE;
import static no.nav.sbl.dialogarena.person.consumer.GjeldendeAdresseKodeverk.MIDLERTIDIG_POSTADRESSE_UTLAND;
import static no.nav.sbl.dialogarena.person.consumer.GjeldendeAdresseKodeverk.UKJENT_ADRESSE;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class HentBrukerprofilConsumerTest {

    private static final LocalDate IDAG = new LocalDate(2013, 5, 4);

    private static final String IDENT = "12345678910";

    @Mock
    private BrukerprofilPortType brukerprofilServiceMock;

    private HentBrukerprofilConsumer consumer;

    private XMLBruker response;


    @Before
    public void initIntegrationServiceWithMock() throws Exception {
        consumer = new HentBrukerprofilConsumer(brukerprofilServiceMock);
        response = (XMLBruker) stubResponseFromService().getPerson();
    }


    @Test
    public void skalHentePerson() {
        Person person = consumer.hentPerson(IDENT);
        assertNotNull(person);
    }

    @Test
    public void postadresseUtenLandkode() {
        response
            .withGjeldendePostadresseType(GjeldendeAdresseKodeverk.POSTADRESSE.forLestjeneste)
            .withPostadresse(new XMLPostadresse().withUstrukturertAdresse(new XMLUstrukturertAdresse().withAdresselinje1("Jæren")));


        Person person = consumer.hentPerson(IDENT);
        assertNotNull(person);
    }


    @Test
    public void utenlandskKontoUtenBankadresse() {
        response.withBankkonto(new XMLBankkontoUtland().withBankkontoUtland(new XMLBankkontonummerUtland()));

        Person person = consumer.hentPerson(IDENT);
        assertTrue(person.har(ValgtKontotype.UTLAND));
    }

    @Test
    public void norskBankkontoMedOgUtenKontonr() {
        XMLBankkontonummer kontonr = new XMLBankkontonummer();
        response.withBankkonto(new XMLBankkontoNorge().withBankkonto(kontonr));

        Person person = consumer.hentPerson(IDENT);
        assertTrue(person.har(ValgtKontotype.NORGE));
        assertThat(person.getKontonummer(), nullValue());

        kontonr.withBankkontonummer("18002085549");

        person = consumer.hentPerson(IDENT);
        assertTrue(person.har(ValgtKontotype.NORGE));
        assertThat(person.getKontonummer(), is("18002085549"));
    }

    @Test
    public void norskMidlertidigAdresseUtenUtlopsdatoFaarUtlopsdatoEttAarFremITid() {
        stubMidlertidigAdresse(MIDLERTIDIG_POSTADRESSE_NORGE, new XMLMidlertidigPostadresseNorge()
            .withPostleveringsPeriode(new XMLGyldighetsperiode())
            .withStrukturertAdresse(new XMLGateadresse()));

        assertThat(consumer.hentPerson(IDENT).getValgtMidlertidigAdresse().getUtlopsdato(), is(IDAG.plusYears(1).minusDays(1)));
    }

    @Test
    public void utenlandskMidlertidigAdresseUtenUtlopsdatoFaarUtlopsdatoEttAarFremITid() {
        stubMidlertidigAdresse(MIDLERTIDIG_POSTADRESSE_UTLAND, new XMLMidlertidigPostadresseUtland()
            .withPostleveringsPeriode(new XMLGyldighetsperiode())
            .withUstrukturertAdresse(new XMLUstrukturertAdresse()));

        assertThat(consumer.hentPerson(IDENT).getValgtMidlertidigAdresse().getUtlopsdato(), is(IDAG.plusYears(1).minusDays(1)));
    }

    @Test(expected = ApplicationException.class)
    @SuppressWarnings("unchecked")
    public void kasterApplicationExceptionVedSikkerhetsbegrensningsfeil() throws Exception {
        when(brukerprofilServiceMock.hentKontaktinformasjonOgPreferanser(any(XMLHentKontaktinformasjonOgPreferanserRequest.class)))
            .thenThrow(HentKontaktinformasjonOgPreferanserSikkerhetsbegrensning.class);
        consumer.hentPerson(IDENT);
    }

    @Test(expected = ApplicationException.class)
    @SuppressWarnings("unchecked")
    public void kasterApplicationExceptionVedPersonIkkeFunnet() throws Exception {
        when(brukerprofilServiceMock.hentKontaktinformasjonOgPreferanser(any(XMLHentKontaktinformasjonOgPreferanserRequest.class)))
            .thenThrow(HentKontaktinformasjonOgPreferanserPersonIkkeFunnet.class);
        consumer.hentPerson(IDENT);
    }

    private void stubMidlertidigAdresse(GjeldendeAdresseKodeverk adressetype, XMLMidlertidigPostadresse adresse) {
        response
            .withGjeldendePostadresseType(adressetype.forLestjeneste)
            .withMidlertidigPostadresse(adresse);
    }

    private XMLHentKontaktinformasjonOgPreferanserResponse stubResponseFromService() throws Exception {
        XMLHentKontaktinformasjonOgPreferanserResponse xmlResponse = new XMLHentKontaktinformasjonOgPreferanserResponse();
        xmlResponse.withPerson(new XMLBruker()
                .withPersonnavn(new XMLPersonnavn())
                .withGjeldendePostadresseType(UKJENT_ADRESSE.forLestjeneste));
        when(brukerprofilServiceMock.hentKontaktinformasjonOgPreferanser(any(XMLHentKontaktinformasjonOgPreferanserRequest.class))).thenReturn(xmlResponse);
        return xmlResponse;
    }

    @BeforeClass
    public static void lockTime() {
        DateTimeUtils.setCurrentMillisFixed(IDAG.toDate().getTime());
    }

    @AfterClass
    public static void unlockTime() {
        DateTimeUtils.setCurrentMillisSystem();
    }

}
