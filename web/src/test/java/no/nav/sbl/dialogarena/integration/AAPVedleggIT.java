package no.nav.sbl.dialogarena.integration;

import com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl;
import no.nav.sbl.dialogarena.StartSoknadJetty;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.AAPOrdinaerInformasjon;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.SendSoknadPortType;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.meldinger.WSBehandlingsId;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.meldinger.WSStartSoknadRequest;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.BrukerprofilPortType;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLBankkontoNorge;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLBankkontonummer;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLBruker;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLNorskIdent;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.meldinger.XMLHentKontaktinformasjonOgPreferanserResponse;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.DigitalKontaktinformasjonV1;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.informasjon.WSEpostadresse;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.informasjon.WSKontaktinformasjon;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.meldinger.WSHentDigitalKontaktinformasjonResponse;
import no.nav.tjeneste.virksomhet.person.v1.PersonPortType;
import no.nav.tjeneste.virksomhet.person.v1.informasjon.*;
import no.nav.tjeneste.virksomhet.person.v1.meldinger.HentKjerneinformasjonResponse;
import org.glassfish.jersey.test.TestProperties;
import org.junit.Test;

import java.io.File;

import static no.nav.sbl.dialogarena.config.IntegrationConfig.getMocked;
import static no.nav.sbl.dialogarena.soknadinnsending.business.db.config.DatabaseTestContext.buildDataSource;
import static no.nav.sbl.dialogarena.test.path.FilesAndDirs.TEST_RESOURCES;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

public class AAPVedleggIT {
    private static final int PORT = 10001;
    static {
        System.setProperty(TestProperties.CONTAINER_FACTORY, "org.glassfish.jersey.test.external.ExternalTestContainerFactory");
        System.setProperty(TestProperties.CONTAINER_PORT, "" + PORT);
        System.setProperty(TestProperties.LOG_TRAFFIC, "true");
        System.setProperty("jersey.test.host", "localhost");
        System.setProperty("no.nav.sbl.dialogarena.sendsoknad.hsqldb", "true");
    }

    @Test
    public void alleVedleggsKrav() throws Exception {
        StartSoknadJetty jetty = new StartSoknadJetty(
                StartSoknadJetty.Env.Intellij,
                new File(TEST_RESOURCES, "override-web-integration.xml"),
                buildDataSource("hsqldb.properties"),
                PORT
        );
        jetty.jetty.start();

        setupMockWsEndpointData();

        String aapOrdinaerSkjemaNummer = new AAPOrdinaerInformasjon().getSkjemanummer().get(0);
        SoknadTester.startSoknad(aapOrdinaerSkjemaNummer)
                .settDelstegstatus("opprettet")
                .hentSoknad()
                .hentFakta()
                .hentPaakrevdeVedlegg()
                .skalIkkeKreveNoenVedlegg()
                .soknad()
                .faktum("soknadstype").withValue("ordinaer").utforEndring()
                .faktum("tilknytningnorge.oppholdinorgesistetreaar").withValue("false").utforEndring()
                .faktum("tilknytningnorge.oppholdinorgesistetreaar.false.registrertflyktning").withValue("true").utforEndring()
                .faktum("ungufor").withValue("false").utforEndring()
                .faktum("andreytelser.fraarbeidsgiver").withValue("false").utforEndring()
                .faktum("andreytelser.fraandre.omsorgslonn").withValue("true").utforEndring()
                .faktum("andreytelser.fraandre.fosterhjemsgodtgjorelse").withValue("true").utforEndring()
                .faktum("andreytelser.fraandre.utenlandsketrygdemyndigheter").withValue("true").utforEndring()
                .alleFaktum("barn").skalVareAntall(1).skalVareSystemFaktum()
                .soknad()
                .hentPaakrevdeVedlegg()
                .skalHaVedlegg("L9", "P2", "V1", "K6", "K1")
                .skalHaVedleggMedSkjemaNummerTillegg("K5", "omsorgslonn")
                .skalHaVedleggMedSkjemaNummerTillegg("K5", "fosterhjemsgodtgjorelse")
                .skalIkkeHaVedlegg("X8", "N6")
                .soknad()
                .opprettFaktumWithValue("barn", null)
                .opprettFaktumWithValue("ekstraVedlegg", "true")
                .hentPaakrevdeVedlegg()
                .skalHaVedlegg("X8", "N6");

        jetty.jetty.stop.run();

    }

    private void setupMockWsEndpointData() throws Exception {
        mockSendSoknadEndpoint();
        mockBrukerProfilEndpoint();
        mockPersonEndpoint();
        mockDkifService();
    }

    private void mockSendSoknadEndpoint() {
        SendSoknadPortType soknad = getMocked("sendSoknadEndpoint");
        when(soknad.startSoknad(any(WSStartSoknadRequest.class))).thenReturn(new WSBehandlingsId().withBehandlingsId("TEST-123"));
    }

    private void mockBrukerProfilEndpoint() throws Exception {
        BrukerprofilPortType brukerProfil = getMocked("brukerProfilEndpoint");
        when(brukerProfil.hentKontaktinformasjonOgPreferanser(any())).thenReturn(
                new XMLHentKontaktinformasjonOgPreferanserResponse().withPerson(
                        new XMLBruker()
                                .withBankkonto(new XMLBankkontoNorge()
                                        .withBankkonto(new XMLBankkontonummer().withBankkontonummer("65294512345"))
                                )
                                .withIdent(new XMLNorskIdent().withIdent("12125612345"))
                )
        );
    }

    private void mockPersonEndpoint() throws Exception {
        PersonPortType personEndpoint = getMocked("personEndpoint");
        HentKjerneinformasjonResponse hentKjerneinformasjonResponse = new HentKjerneinformasjonResponse();

        Foedselsdato foedselsdato = new Foedselsdato();
        foedselsdato.setFoedselsdato(new XMLGregorianCalendarImpl());

        Familierelasjoner familierelasjoner = new Familierelasjoner();
        familierelasjoner.setValue("BARN");

        Familierelasjon familierelasjon = new Familierelasjon();
        familierelasjon.setTilRolle(familierelasjoner);

        Person person = new Person();
        Person barn = new Person();
        NorskIdent norskIdent = new NorskIdent();
        norskIdent.setIdent("12129912345");
        barn.setIdent(norskIdent);
        barn.setFoedselsdato(foedselsdato);
        familierelasjon.setTilPerson(barn);
        person.getHarFraRolleI().add(familierelasjon);

        person.setFoedselsdato(foedselsdato);
        hentKjerneinformasjonResponse.setPerson(person);

        when(personEndpoint.hentKjerneinformasjon(any())).thenReturn(hentKjerneinformasjonResponse);
    }

    private void mockDkifService() throws Exception {
        DigitalKontaktinformasjonV1 dkif = getMocked("dkifService");
        when(dkif.hentDigitalKontaktinformasjon(any())).thenReturn(
                new WSHentDigitalKontaktinformasjonResponse()
                        .withDigitalKontaktinformasjon(new WSKontaktinformasjon()
                                .withEpostadresse(new WSEpostadresse().withValue(""))
                        )
        );
    }

}
