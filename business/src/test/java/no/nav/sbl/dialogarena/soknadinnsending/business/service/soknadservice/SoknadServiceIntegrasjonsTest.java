package no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice;

import static no.nav.sbl.dialogarena.sendsoknad.domain.HendelseType.AVBRUTT_AUTOMATISK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.joda.time.DateTime.now;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.naming.NamingException;

import no.nav.sbl.dialogarena.soknadinnsending.business.kodeverk.Kodeverk;
import no.nav.sbl.sosialhjelp.domain.SoknadUnderArbeid;
import no.nav.sbl.sosialhjelp.soknadunderbehandling.SoknadUnderArbeidRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import no.nav.sbl.dialogarena.sendsoknad.domain.DelstegStatus;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.SosialhjelpInformasjon;
import no.nav.sbl.dialogarena.sendsoknad.domain.oppsett.SoknadStruktur;
import no.nav.sbl.dialogarena.soknadinnsending.business.SoknadDataFletterIntegrationTestContext;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.fillager.FillagerRepository.Fil;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad.SoknadRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknadmetadata.SoknadMetadataRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.SoknadMetadata;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.FaktaService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.FillagerService;

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
    private SoknadMetadataRepository soknadMetadataRepository;

    @Inject
    private FaktaService faktaService;

    @Inject
    private SoknadUnderArbeidRepository soknadUnderArbeidRepository;

    @BeforeClass
    public static void beforeClass() throws IOException, NamingException {
        System.setProperty("soknad.feature.foreldrepenger.alternativrepresentasjon.enabled", "true");
    }

    @Before
    public void setUp() {
        when(soknadMetadataRepository.hent(anyString())).thenReturn(new SoknadMetadata());
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(Optional.of(new SoknadUnderArbeid()));
    }

    @Test
    public void henterTemakode_FOR_forSoknadsosialhjelp() {
        skjemaNummer = SosialhjelpInformasjon.SKJEMANUMMER;
        when(kodeverk.getKode(skjemaNummer, Kodeverk.Nokkel.TEMA)).thenReturn("FOR");
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
        verify(soknadUnderArbeidRepository).slettSoknad(any(SoknadUnderArbeid.class), anyString());
    }

    @Test
    public void avbrytSoknadSletterFiler() {
        opprettOgPersisterSoknad(EN_BEHANDLINGSID, "aktor");

        soknadService.avbrytSoknad(EN_BEHANDLINGSID);

        List<Fil> filer = fillagerService.hentFiler(EN_BEHANDLINGSID);
        assertThat(filer).isEmpty();
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

    @After
    public void afterEach() {
        lokalDb.slettSoknad(soknad.medId(soknadId), AVBRUTT_AUTOMATISK);
    }

}