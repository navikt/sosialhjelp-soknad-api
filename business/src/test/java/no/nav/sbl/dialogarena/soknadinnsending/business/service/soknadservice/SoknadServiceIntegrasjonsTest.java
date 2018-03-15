package no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice;

import no.nav.modig.core.context.AuthenticationLevelCredential;
import no.nav.modig.core.context.OpenAmTokenCredential;
import no.nav.modig.core.context.ThreadLocalSubjectHandler;
import no.nav.modig.core.domain.ConsumerId;
import no.nav.modig.core.domain.SluttBruker;
import no.nav.sbl.dialogarena.common.kodeverk.Kodeverk;
import no.nav.sbl.dialogarena.sendsoknad.domain.DelstegStatus;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.oppsett.SoknadStruktur;
import no.nav.sbl.dialogarena.soknadinnsending.business.SoknadDataFletterIntegrationTestContext;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.fillager.FillagerRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.fillager.FillagerRepository.Fil;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad.SoknadRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.FaktaService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.FillagerService;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import javax.naming.NamingException;
import javax.security.auth.Subject;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static no.nav.modig.core.context.SubjectHandler.getSubjectHandler;
import static no.nav.sbl.dialogarena.sendsoknad.domain.HendelseType.AVBRUTT_AUTOMATISK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.joda.time.DateTime.now;
import static org.mockito.Mockito.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = SoknadDataFletterIntegrationTestContext.class)
public class SoknadServiceIntegrasjonsTest {
    private final String EN_BEHANDLINGSID = "EN_BEHANDLINGSID";
    WebSoknad soknad;
    String uuid = "uid";
    String skjemaNummer = "";
    long soknadId;

    @Inject
    Kodeverk kodeverk;
    @Inject
    private SoknadRepository lokalDb;

    @Inject
    private SoknadService soknadService;

    @Inject
    private FillagerService fillagerService;

    @Inject
    private FillagerRepository fillagerRepository;

    @Inject
    private FaktaService faktaService;

    @BeforeClass
    public static void beforeClass() throws IOException, NamingException {
        System.setProperty("no.nav.modig.core.context.subjectHandlerImplementationClass", ThreadLocalSubjectHandler.class.getName());
        System.setProperty("soknad.feature.foreldrepenger.alternativrepresentasjon.enabled", "true");
    }

    @Test
    public void henterTemakode_FOR_forForeldrepenger() {
        skjemaNummer = "NAV 14-05.06";
        when(kodeverk.getKode("NAV 14-05.06", Kodeverk.Nokkel.TEMA)).thenReturn("FOR");
        opprettOgPersisterSoknadMedData("behId", "aktor");
        SoknadStruktur soknadStruktur = soknadService.hentSoknadStruktur(skjemaNummer);
        assertThat(soknadStruktur.getTemaKode()).isEqualTo("FOR");
    }

    @Test
    public void hentSoknadFraLokalDbReturnererPopulertSoknad() {
        skjemaNummer = "NAV 14-05.06";
        Long soknadId = opprettOgPersisterSoknad(EN_BEHANDLINGSID, "aktor");

        WebSoknad webSoknad = soknadService.hentSoknadFraLokalDb(soknadId);

        assertThat(webSoknad.getBrukerBehandlingId()).isEqualTo(EN_BEHANDLINGSID);
        assertThat(webSoknad.getAktoerId()).isEqualTo("aktor");
        assertThat(webSoknad.getUuid()).isEqualTo(uuid);
        assertThat(webSoknad.getDelstegStatus()).isEqualTo(DelstegStatus.OPPRETTET);
        assertThat(webSoknad.getskjemaNummer()).isEqualTo(skjemaNummer);
    }

    @Test
    public void settDelstegPersistererNyttDelstegTilDb() {
        Long soknadId = opprettOgPersisterSoknad(EN_BEHANDLINGSID, "aktor");

        soknadService.settDelsteg(EN_BEHANDLINGSID, DelstegStatus.SAMTYKKET);

        WebSoknad webSoknad = soknadService.hentSoknadFraLokalDb(soknadId);
        assertThat(webSoknad.getDelstegStatus()).isEqualTo(DelstegStatus.SAMTYKKET);
    }

    @Test
    public void settJournalforendeEnhetPersistererNyJournalforendeEnhetTilDb() {
        Long soknadId = opprettOgPersisterSoknad(EN_BEHANDLINGSID, "aktor");

        soknadService.settJournalforendeEnhet(EN_BEHANDLINGSID, "NAV UTLAND");

        WebSoknad webSoknad = soknadService.hentSoknadFraLokalDb(soknadId);
        assertThat(webSoknad.getJournalforendeEnhet()).isEqualTo("NAV UTLAND");
    }

    @Test
    public void avbrytSoknadSletterSoknadenFraLokalDb() {
        Long soknadId = opprettOgPersisterSoknad(EN_BEHANDLINGSID, "aktor");

        soknadService.avbrytSoknad(EN_BEHANDLINGSID);

        WebSoknad webSoknad = soknadService.hentSoknadFraLokalDb(soknadId);
        assertThat(webSoknad).isNull();
    }

    @Test
    public void avbrytSoknadSletterFiler() {
        opprettOgPersisterSoknad(EN_BEHANDLINGSID, "aktor");

        soknadService.avbrytSoknad(EN_BEHANDLINGSID);

        List<Fil> filer = fillagerService.hentFiler(EN_BEHANDLINGSID);
        assertThat(filer).isEmpty();
    }

    private ArgumentMatcher<Fil> filMatcher(String behandlingsId) {
        return new ArgumentMatcher<Fil>() {
            @Override
            public boolean matches(Object argument) {
                return ((Fil) argument).behandlingsId.equals(behandlingsId);
            }
        };
    }

    @Test
    public void sendSoknadSkalLagreToFilerTilHenvendelseHvisForeldrepengerEngangsstonad() {
        ((ThreadLocalSubjectHandler) getSubjectHandler()).setSubject(getSubject());
        skjemaNummer = "NAV 14-05.07";
        String behandlingsId = nyBehandlnigsId();
        opprettOgPersisterSoknad(behandlingsId, "aktor");

        soknadService.sendSoknad(behandlingsId, new byte[]{});

        verify(fillagerRepository, times(2)).lagreFil(argThat(filMatcher(behandlingsId)));
    }

    @Test
    public void sendSoknadSkalLagreEnFilTilHenvendelseHvisBilstonad() {
        ((ThreadLocalSubjectHandler) getSubjectHandler()).setSubject(getSubject());
        skjemaNummer = "NAV 10-07.40";
        String behandlingsId = nyBehandlnigsId();
        opprettOgPersisterSoknad(behandlingsId, "aktor");

        soknadService.sendSoknad(behandlingsId, new byte[]{});

        verify(fillagerRepository, times(1)).lagreFil(argThat(filMatcher(behandlingsId)));
    }

    @Test
    public void sendSoknadSkalLagreToFilerTilHenvendelseHvisTilleggsstonader() {
        ((ThreadLocalSubjectHandler) getSubjectHandler()).setSubject(getSubject());
        skjemaNummer = "NAV 11-12.12";
        String behandlingsId = nyBehandlnigsId();
        opprettOgPersisterSoknadMedData(behandlingsId, "aktor");
        lokalDb.opprettFaktum(soknadId, maalgruppeFaktum(), true);

        soknadService.sendSoknad(behandlingsId, new byte[]{});

        verify(fillagerRepository, times(2)).lagreFil(argThat(filMatcher(behandlingsId)));
    }

    private Subject getSubject() {
        Subject subject = new Subject();
        subject.getPrincipals().add(SluttBruker.eksternBruker("98989898989"));
        subject.getPrincipals().add(new ConsumerId("StaticSubjectHandlerConsumerId"));
        subject.getPublicCredentials().add(new OpenAmTokenCredential("98989898989-4"));
        subject.getPublicCredentials().add(new AuthenticationLevelCredential(4));
        return subject;
    }

    private String nyBehandlnigsId() {
        return UUID.randomUUID().toString();
    }

    private Long opprettOgPersisterSoknad(String behId, String aktor) {
        soknad = WebSoknad.startSoknad()
                .medUuid(uuid)
                .medAktorId(aktor)
                .medBehandlingId(behId)
                .medDelstegStatus(DelstegStatus.OPPRETTET)
                .medskjemaNummer(skjemaNummer).medOppretteDato(now());
        soknadId = lokalDb.opprettSoknad(soknad);
        soknad.setSoknadId(soknadId);
        return soknadId;
    }

    private Long opprettOgPersisterSoknadMedData(String behId, String aktor) {
        soknad = WebSoknad.startSoknad()
                .medUuid(uuid)
                .medAktorId(aktor)
                .medBehandlingId(behId)
                .medVersjon(0)
                .medDelstegStatus(DelstegStatus.OPPRETTET)
                .medskjemaNummer(skjemaNummer).medOppretteDato(now());

        soknadId = lokalDb.opprettSoknad(soknad);
        faktaService.opprettBrukerFaktum(behId, new Faktum()
                .medKey("bostotte.aarsak")
                .medValue("fasteboutgifter"));
        faktaService.opprettBrukerFaktum(behId, new Faktum()
                .medKey("bostotte.periode")
                .medProperty("fom", "2015-07-22")
                .medProperty("tom", "2015-10-22"));
        faktaService.opprettBrukerFaktum(behId, new Faktum()
                .medKey("bostotte.kommunestotte"));
        faktaService.opprettBrukerFaktum(behId, new Faktum()
                .medKey("bostotte.adresseutgifter.aktivitetsadresse")
                .medProperty("utgift", "2000"));
        faktaService.opprettBrukerFaktum(behId, new Faktum()
                .medKey("bostotte.adresseutgifter.hjemstedsaddresse"));

        soknad.setSoknadId(soknadId);

        return soknadId;
    }

    private Faktum maalgruppeFaktum() {
        return new Faktum()
                .medType(Faktum.FaktumType.SYSTEMREGISTRERT)
                .medKey("maalgruppe")
                .medProperty("kodeverkVerdi", "ARBSOKER")
                .medProperty("fom", "2015-01-01");
    }

    @After
    public void afterEach() {
        lokalDb.slettSoknad(soknad.medId(soknadId), AVBRUTT_AUTOMATISK);
    }

}