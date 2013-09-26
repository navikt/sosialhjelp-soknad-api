package no.nav.sbl.dialogarena.person.consumer;

import no.nav.modig.core.exception.ApplicationException;
import no.nav.modig.core.exception.SystemException;
import no.nav.modig.lang.option.Optional;
import no.nav.sbl.dialogarena.adresse.Adresse;
import no.nav.sbl.dialogarena.adresse.Adressetype;
import no.nav.sbl.dialogarena.adresse.StrukturertAdresse;
import no.nav.sbl.dialogarena.adresse.UstrukturertAdresse;
import no.nav.sbl.dialogarena.person.Person;
import no.nav.tjeneste.virksomhet.behandlebrukerprofil.v1.BehandleBrukerprofilPortType;
import no.nav.tjeneste.virksomhet.behandlebrukerprofil.v1.OppdaterKontaktinformasjonOgPreferanserPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.behandlebrukerprofil.v1.OppdaterKontaktinformasjonOgPreferanserSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.behandlebrukerprofil.v1.OppdaterKontaktinformasjonOgPreferanserUgyldigInput;
import no.nav.tjeneste.virksomhet.behandlebrukerprofil.v1.feil.XMLUgyldigInput;
import no.nav.tjeneste.virksomhet.behandlebrukerprofil.v1.informasjon.XMLBruker;
import no.nav.tjeneste.virksomhet.behandlebrukerprofil.v1.informasjon.XMLMatrikkeladresse;
import no.nav.tjeneste.virksomhet.behandlebrukerprofil.v1.informasjon.XMLMidlertidigPostadresseNorge;
import no.nav.tjeneste.virksomhet.behandlebrukerprofil.v1.informasjon.XMLStedsadresseNorge;
import no.nav.tjeneste.virksomhet.behandlebrukerprofil.v1.meldinger.XMLOppdaterKontaktinformasjonOgPreferanserRequest;
import org.joda.time.DateTimeUtils;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;

import static no.nav.modig.lang.option.Optional.optional;
import static no.nav.sbl.dialogarena.adresse.Adressetype.BOSTEDSADRESSE;
import static no.nav.sbl.dialogarena.adresse.Adressetype.GATEADRESSE;
import static no.nav.sbl.dialogarena.adresse.Adressetype.OMRAADEADRESSE;
import static no.nav.sbl.dialogarena.person.GjeldendeAdressetype.MIDLERTIDIG_NORGE;
import static no.nav.sbl.dialogarena.person.GjeldendeAdressetype.MIDLERTIDIG_UTLAND;
import static no.nav.sbl.dialogarena.person.consumer.TpsValideringsfeil.MIDLERTIDIG_ADRESSE_LIK_FOLKEREGISTRERT;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.joda.time.LocalDate.now;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;

@RunWith(MockitoJUnitRunner.class)
public class OppdaterBrukerprofilConsumerTest {

    private WebService webServiceStub = new WebService();

    @Mock
    private BehandleBrukerprofilPortType webServiceMock;

    private final OppdaterBrukerprofilConsumer service = new OppdaterBrukerprofilConsumer(webServiceStub);


    private final Optional<Adresse> ingenFolkeregistrertAdresse = Optional.none();

    private static final LocalDate IDAG = new LocalDate(1981, 6, 24);
    private static final LocalDate OM_ET_AAR = IDAG.plusYears(1).minusDays(1);


    @Test
    public void senderIkkeEventuellMidlertidigAdresseNaarPersonHarValgtFolkeregistrertAdresse()  {
        Person p = new Person("OLA NORMANN", "1234567809", optional(new StrukturertAdresse(BOSTEDSADRESSE)));
        p.setNorskMidlertidig(new StrukturertAdresse(GATEADRESSE, now().plusYears(1)));
        service.oppdaterPerson(p);
        assertThat(webServiceStub.sistOppdatert.getGjeldendePostadresseType().getValue(), is(GjeldendeAdresseKodeverk.BOSTEDSADRESSE.name()));
        assertThat(webServiceStub.sistOppdatert.getMidlertidigPostadresse(), nullValue());
    }

    @Test
    public void senderIkkeEventuellMidlertidigAdresseNaarPersonHarUkjentAdresse()  {
        Person p = new Person("OLA NORMANN", "1234567809", ingenFolkeregistrertAdresse);
        service.oppdaterPerson(p);
        assertThat(webServiceStub.sistOppdatert.getGjeldendePostadresseType().getValue(), is(GjeldendeAdresseKodeverk.UKJENT_ADRESSE.name()));
        assertThat(webServiceStub.sistOppdatert.getMidlertidigPostadresse(), nullValue());
    }

    @Test
    public void senderMedMidlertidigNorskAdresseNaarBrukerHarValgtDet() {
        Person p = new Person("OLA NORMANN", "1234567809", ingenFolkeregistrertAdresse);
        p.setNorskMidlertidig(new StrukturertAdresse(Adressetype.OMRAADEADRESSE, OM_ET_AAR));
        p.velg(MIDLERTIDIG_NORGE);
        service.oppdaterPerson(p);
        assertThat(webServiceStub.sistOppdatert.getGjeldendePostadresseType().getValue(), is(GjeldendeAdresseKodeverk.MIDLERTIDIG_POSTADRESSE_NORGE.name()));
        assertThat(webServiceStub.sistOppdatert.getMidlertidigPostadresse().getPostleveringsPeriode().getFom(), is(IDAG.toDateTimeAtStartOfDay()));
        assertThat(webServiceStub.sistOppdatert.getMidlertidigPostadresse().getPostleveringsPeriode().getTom(), is(OM_ET_AAR.toDateTime(new LocalTime(23, 59, 59))));
    }

    @Test
    public void senderMedUtenlandskMidlertidigAdresseNaarBrukerHarValgtDet() {
        Person p = new Person("OLA NORMANN", "1234567809", ingenFolkeregistrertAdresse);
        p.setUtenlandskMidlertidig(new UstrukturertAdresse(Adressetype.UTENLANDSK_ADRESSE, OM_ET_AAR, "UK", "Oxford Street"));
        p.velg(MIDLERTIDIG_UTLAND);
        service.oppdaterPerson(p);
        assertThat(webServiceStub.sistOppdatert.getGjeldendePostadresseType().getValue(), is(GjeldendeAdresseKodeverk.MIDLERTIDIG_POSTADRESSE_UTLAND.name()));
        assertThat(webServiceStub.sistOppdatert.getMidlertidigPostadresse().getPostleveringsPeriode().getFom(), is(IDAG.toDateTimeAtStartOfDay()));
        assertThat(webServiceStub.sistOppdatert.getMidlertidigPostadresse().getPostleveringsPeriode().getTom(), is(OM_ET_AAR.toDateTime(new LocalTime(23, 59, 59))));
    }

    @Test(expected = SystemException.class)
    public void sikkerhetsFeilWrappesISystemException() throws Exception {
        Person person = new Person("Bønna", "12345612345", ingenFolkeregistrertAdresse);
        OppdaterBrukerprofilConsumer consumer = new OppdaterBrukerprofilConsumer(webServiceMock);

        doThrow(OppdaterKontaktinformasjonOgPreferanserSikkerhetsbegrensning.class)
                .when(webServiceMock).oppdaterKontaktinformasjonOgPreferanser(any(XMLOppdaterKontaktinformasjonOgPreferanserRequest.class));
        consumer.oppdaterPerson(person);
    }

    @Test(expected = ApplicationException.class)
    public void personFinnesIkkeWrappesIApplicationException() throws Exception {
        OppdaterBrukerprofilConsumer consumer = new OppdaterBrukerprofilConsumer(webServiceMock);
        Person person = new Person("Bønna", "12345612345", ingenFolkeregistrertAdresse);

        doThrow(OppdaterKontaktinformasjonOgPreferanserPersonIkkeFunnet.class)
                .when(webServiceMock).oppdaterKontaktinformasjonOgPreferanser(any(XMLOppdaterKontaktinformasjonOgPreferanserRequest.class));
        consumer.oppdaterPerson(person);
    }

    @Test
    public void personForsokerAaRegistrereMidlertidigAdresseLikDenFolkeregistrerte() throws Exception {
        Person person = new Person("Bønna", "12345612345", ingenFolkeregistrertAdresse);

        int thrownExceptions = 0;
        List<String> feilkoder = MIDLERTIDIG_ADRESSE_LIK_FOLKEREGISTRERT.feilkoder;
        assertThat("Ikke interessant å teste dersom feilkoder er tom", feilkoder, not(empty()));
        for (String feilkode : feilkoder) {
            try {
                OppdaterKontaktinformasjonOgPreferanserUgyldigInput ugyldigInput = new OppdaterKontaktinformasjonOgPreferanserUgyldigInput(null,
                        new XMLUgyldigInput().withFeilaarsak(feilkode));
                doThrow(ugyldigInput).when(webServiceMock).oppdaterKontaktinformasjonOgPreferanser(any(XMLOppdaterKontaktinformasjonOgPreferanserRequest.class));
                new OppdaterBrukerprofilConsumer(webServiceMock).oppdaterPerson(person);
            } catch (TpsValideringException e) {
                assertThat(e.messagekey, is(MIDLERTIDIG_ADRESSE_LIK_FOLKEREGISTRERT.feilmeldingMsgKey));
                thrownExceptions++;
            }
        }
        assertThat("har kastet exception for hver feilkode", feilkoder, hasSize(thrownExceptions));
    }

    @Test
    public void senderXMLStedsadresseNaarOmraadeadresseErTom() {
        Person per = new Person("Per Hansen", "12345678910", ingenFolkeregistrertAdresse);
        StrukturertAdresse adresse = new StrukturertAdresse(OMRAADEADRESSE);
        adresse.setPostnummer("7000");
        adresse.setUtlopsdato(now().plusMonths(1));
        per.setNorskMidlertidig(adresse);
        per.velg(MIDLERTIDIG_NORGE);
        service.oppdaterPerson(per);
        XMLMidlertidigPostadresseNorge midlertidigPostadresse = (XMLMidlertidigPostadresseNorge) webServiceStub.sistOppdatert.getMidlertidigPostadresse();
        assertTrue(midlertidigPostadresse.getStrukturertAdresse() instanceof XMLStedsadresseNorge);
    }

    @Test
    public void senderXMLMatrikkeladresseNaarPersonHarOmraadeadresse() {
        Person paal = new Person("Paal Hansen", "12345678910", ingenFolkeregistrertAdresse);
        StrukturertAdresse adresse = new StrukturertAdresse(OMRAADEADRESSE);
        adresse.setOmraadeadresse("Gården");
        adresse.setPostnummer("7000");
        adresse.setUtlopsdato(now().plusMonths(1));
        paal.setNorskMidlertidig(adresse);
        paal.velg(MIDLERTIDIG_NORGE);
        service.oppdaterPerson(paal);
        XMLMidlertidigPostadresseNorge midlertidigPostadresse = (XMLMidlertidigPostadresseNorge) webServiceStub.sistOppdatert.getMidlertidigPostadresse();
        assertTrue(midlertidigPostadresse.getStrukturertAdresse() instanceof XMLMatrikkeladresse);
    }

    @BeforeClass
    public static void setupDates() {
        DateTimeUtils.setCurrentMillisFixed(IDAG.toDate().getTime());
    }

    @AfterClass
    public static void resetToSystemClock() {
        DateTimeUtils.setCurrentMillisSystem();
    }

}

class WebService implements BehandleBrukerprofilPortType {

    XMLBruker sistOppdatert;

    @Override
    public void ping() {
    }


    @Override
    public void oppdaterKontaktinformasjonOgPreferanser(XMLOppdaterKontaktinformasjonOgPreferanserRequest request) {
        sistOppdatert = (XMLBruker) request.getPerson();
    }
}
