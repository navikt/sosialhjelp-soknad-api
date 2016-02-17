package no.nav.sbl.dialogarena.soknadinnsending.business.db;


import no.nav.modig.lang.option.Optional;
import no.nav.sbl.dialogarena.sendsoknad.domain.*;
import no.nav.sbl.dialogarena.sendsoknad.domain.oppsett.FaktumStruktur;
import no.nav.sbl.dialogarena.sendsoknad.domain.oppsett.VedleggForFaktumStruktur;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad.SoknadRepository;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static java.util.Collections.sort;
import static java.util.UUID.randomUUID;
import static no.nav.sbl.dialogarena.sendsoknad.domain.Faktum.FaktumType.BRUKERREGISTRERT;
import static no.nav.sbl.dialogarena.sendsoknad.domain.Faktum.FaktumType.SYSTEMREGISTRERT;
import static org.hamcrest.Matchers.*;
import static org.joda.time.DateTime.now;
import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {DbTestConfig.class})
public class SoknadRepositoryJdbcTest {

    @Inject
    private SoknadRepository soknadRepository;

    @Inject
    private RepositoryTestSupport soknadRepositoryTestSupport;

    private WebSoknad soknad;

    private Long soknadId;

    private String aktorId = "1";
    private String behandlingsId = "1";
    private String skjemaNummer = "skjemaNummer";
    private String uuid = "123";

    @After
    public void cleanUp() {
        soknadRepositoryTestSupport.getJdbcTemplate().update("delete from Vedlegg");
        soknadRepositoryTestSupport.getJdbcTemplate().update("delete from faktumegenskap");
        soknadRepositoryTestSupport.getJdbcTemplate().update("delete from soknadbrukerdata");
        soknadRepositoryTestSupport.getJdbcTemplate().update("delete from Soknad");
    }

    @Test
    public void skalKunneOppretteSoknad() {
        opprettOgPersisterSoknad();
    }

    @Test
    public void skalSetteSistLagret() {
        opprettOgPersisterSoknad();
        soknadRepository.settSistLagretTidspunkt(soknadId);
        WebSoknad endret = soknadRepository.hentSoknad(soknadId);
        System.out.println(new DateTime());
        System.out.println(new DateTime(endret.getSistLagret()));
        Interval endretIntervall = new Interval(new DateTime().minusMillis(100), new DateTime().plusMillis(100));
        assertThat(endretIntervall.contains(endret.getSistLagret()), is(true));
    }

    @Test(expected = DataIntegrityViolationException.class)
    public void skalIkkeKunneOppretteUtenAktorId() {
        soknad = WebSoknad.startSoknad()
                .medUuid(uuid)
                .medBehandlingId(behandlingsId)
                .medskjemaNummer(skjemaNummer)
                .medOppretteDato(now());

        soknadRepository.opprettSoknad(soknad);
    }

    @Test(expected = DataIntegrityViolationException.class)
    public void skalIkkeKunneOppretteUtenBehandlingId() {
        soknad = WebSoknad.startSoknad()
                .medUuid(uuid)
                .medAktorId(aktorId)
                .medskjemaNummer(skjemaNummer)
                .medOppretteDato(now());

        soknadRepository.opprettSoknad(soknad);
    }

    @Test(expected = DataIntegrityViolationException.class)
    public void skalIkkeKunneOppretteUtenskjemaNummer() {
        soknad = WebSoknad.startSoknad()
                .medUuid(uuid)
                .medAktorId(aktorId)
                .medBehandlingId(behandlingsId)
                .medOppretteDato(now());

        soknadRepository.opprettSoknad(soknad);
    }

    @Test
    public void skalKunneHenteOpprettetSoknad() {
        opprettOgPersisterSoknad();

        WebSoknad opprettetSoknad = soknadRepository.hentSoknad(soknadId);
        assertThat(opprettetSoknad, notNullValue());
        assertThat(opprettetSoknad.getStatus(), is(SoknadInnsendingStatus.UNDER_ARBEID));
        assertThat(opprettetSoknad.getAktoerId(), is(aktorId));
        assertThat(opprettetSoknad.getBrukerBehandlingId(), is(behandlingsId));
        assertThat(opprettetSoknad.getskjemaNummer(), is(skjemaNummer));
    }

    @Test
    public void skalKunneHenteOpprettetSoknadMedBehandlingsId() {
        String behId = randomUUID().toString();
        opprettOgPersisterSoknad(behId, "aktor-3");

        WebSoknad opprettetSoknad = soknadRepository.hentSoknad(behId);

        assertThat(opprettetSoknad, notNullValue());
        assertThat(opprettetSoknad.getStatus(), is(SoknadInnsendingStatus.UNDER_ARBEID));
        assertThat(opprettetSoknad.getAktoerId(), is("aktor-3"));
        assertThat(opprettetSoknad.getBrukerBehandlingId(), is(behId));
        assertThat(opprettetSoknad.getskjemaNummer(), is(skjemaNummer));
    }

    @Test
    public void skalFaaNullVedUkjentBehandlingsId() {
        String behId = randomUUID().toString();
        WebSoknad soknad = soknadRepository.hentSoknad(behId);
        Assert.assertNull(soknad);
    }

    @Test
    public void skalKunneLagreBrukerData() {
        String key = "Key";
        String value = "Value";

        opprettOgPersisterSoknad();
        lagreData(key, null, value);
    }

    @Test
    public void skalKunneHenteLagretBrukerData() {
        opprettOgPersisterSoknad();
        lagreData("key1", null, "value1");
        lagreData("key2", null, "value2");
        lagreData("key3", null, "value3");


        List<Faktum> soknadBrukerData = soknadRepository.hentAlleBrukerData(behandlingsId);

        assertThat(soknadBrukerData, notNullValue());
        assertThat(soknadBrukerData.size(), is(3));
    }

    @Test
    public void skalHenteSystemfaktum() {
        Faktum faktum = new Faktum().medKey("personalia").medSoknadId(12L).medProperty("fno", "123").medType(SYSTEMREGISTRERT);
        Faktum result = new Faktum().medKey("personalia").medSoknadId(11L).medProperty("fno", "123").medType(SYSTEMREGISTRERT);
        result.setFaktumId(soknadRepository.opprettFaktum(11L, faktum, true));

        List<Faktum> personalia = soknadRepository.hentSystemFaktumList(11L, "personalia");
        assertThat(personalia.get(0), is(equalTo(result)));
    }

    @Test(expected = EmptyResultDataAccessException.class)
    public void skalSletteFaktum() {
        opprettOgPersisterSoknad();
        Long id = lagreData("key", null, "value");
        Faktum faktum = soknadRepository.hentFaktum(id);
        assertThat(faktum, is(notNullValue()));
        soknadRepository.slettBrukerFaktum(soknadId, id);
        soknadRepository.hentFaktum(id);
        fail("ikke slettet");
    }

    @Test
    public void skalKunneHenteFaktum() {
        opprettOgPersisterSoknad();
        lagreData("key1", null, "value1");
        Long faktumId = lagreData("key2", null, "value2");
        lagreData("key3", null, "value3");

        soknadRepository.hentFaktum(faktumId);
    }

    @Test
    public void skalFinneBehandlingsIdTilSoknadFraFaktumId() {
        Long soknadId = opprettOgPersisterSoknad("123abc", "aktor");
        Long faktumId = lagreData(soknadId, "key", null, "value");
        String behandlingsIdTilFaktum = soknadRepository.hentBehandlingsIdTilFaktum(faktumId);
        assertThat(behandlingsIdTilFaktum, is("123abc"));
    }

    @Test
    public void skalReturnereNullHvisFaktumIdIkkeFinnes() {
        String behandlingsIdTilFaktum = soknadRepository.hentBehandlingsIdTilFaktum(999L);
        assertThat(behandlingsIdTilFaktum, is(nullValue()));
    }

    @Test
    public void skalReturnereAtVedleggErPaakrevdOmParentHarEnAvDependOnValues() {
        opprettOgPersisterSoknad();
        Faktum parentFaktum = new Faktum().medKey("key1").medValue("dependOnValue").medSoknadId(soknad.getSoknadId()).medType(BRUKERREGISTRERT);
        Long parentFaktumId = soknadRepository.opprettFaktum(soknad.getSoknadId(), parentFaktum);
        FaktumStruktur parentFaktumStruktur = new FaktumStruktur().medId("key1");

        Faktum faktum = new Faktum().medKey("key2").medValue("true").medSoknadId(soknad.getSoknadId()).medType(BRUKERREGISTRERT).medParrentFaktumId(parentFaktumId);
        soknadRepository.opprettFaktum(soknad.getSoknadId(), faktum);
        FaktumStruktur faktumStruktur = new FaktumStruktur().medId("key2").medDependOn(parentFaktumStruktur).medDependOnValues(Arrays.asList("true", "dependOnValue"));
        VedleggForFaktumStruktur vedlegg = new VedleggForFaktumStruktur().medFaktum(faktumStruktur).medOnValues(Arrays.asList("true"));

        Boolean vedleggPaakrevd = soknadRepository.isVedleggPaakrevd(soknadId, vedlegg);
        Assert.assertTrue(vedleggPaakrevd);
    }

    @Test
    public void skalReturnereAtVedleggIkkeErPaakrevdOmParentIkkeHarEnAvDependOnValues() {
        opprettOgPersisterSoknad();
        Faktum parentFaktum = new Faktum().medKey("key1").medValue("false").medSoknadId(soknad.getSoknadId()).medType(BRUKERREGISTRERT);
        Long parentFaktumId = soknadRepository.opprettFaktum(soknad.getSoknadId(), parentFaktum);
        FaktumStruktur parentFaktumStruktur = new FaktumStruktur().medId("key1");

        Faktum faktum = new Faktum().medKey("key2").medValue("true").medSoknadId(soknad.getSoknadId()).medType(BRUKERREGISTRERT).medParrentFaktumId(parentFaktumId);
        soknadRepository.opprettFaktum(soknad.getSoknadId(), faktum);
        FaktumStruktur faktumStruktur = new FaktumStruktur().medId("key2").medDependOn(parentFaktumStruktur).medDependOnValues(Arrays.asList("true", "dependOnValue"));
        VedleggForFaktumStruktur vedlegg = new VedleggForFaktumStruktur().medFaktum(faktumStruktur);

        Boolean vedleggPaakrevd = soknadRepository.isVedleggPaakrevd(soknadId, vedlegg);
        assertFalse(vedleggPaakrevd);
    }

    @Test
    public void skalReturnereAtVedleggErPaakrevdNaarParentOgParentParentErSattOgHarRettVerdi() {
        opprettOgPersisterSoknad();
        Faktum parentParentFaktum = new Faktum().medKey("parentParent").medValue("parentParentValue").medSoknadId(soknad.getSoknadId()).medType(BRUKERREGISTRERT);
        soknadRepository.opprettFaktum(soknad.getSoknadId(), parentParentFaktum);
        FaktumStruktur parentParentFaktumStruktur = new FaktumStruktur().medId("parentParent");

        Faktum parentFaktum = new Faktum().medKey("parent").medValue("parentValue").medSoknadId(soknad.getSoknadId()).medType(BRUKERREGISTRERT);
        Long parentFaktumId = soknadRepository.opprettFaktum(soknad.getSoknadId(), parentFaktum);
        FaktumStruktur parentFaktumStruktur = new FaktumStruktur().medId("parent").medDependOn(parentParentFaktumStruktur).medDependOnValues(Arrays.asList("parentParentValue"));

        Faktum faktum = new Faktum().medKey("key").medValue("true").medSoknadId(soknad.getSoknadId()).medType(BRUKERREGISTRERT).medParrentFaktumId(parentFaktumId);
        soknadRepository.opprettFaktum(soknad.getSoknadId(), faktum);
        FaktumStruktur faktumStruktur = new FaktumStruktur().medId("key").medDependOn(parentFaktumStruktur).medDependOnValues(Arrays.asList("parentValue"));
        VedleggForFaktumStruktur vedlegg = new VedleggForFaktumStruktur().medFaktum(faktumStruktur).medOnValues(Arrays.asList("true"));

        Boolean vedleggPaakrevd = soknadRepository.isVedleggPaakrevd(soknadId, vedlegg);
        Assert.assertTrue(vedleggPaakrevd);
    }

    @Test
    public void skalReturnereAtVedleggIkkeErPaakrevdNaarParentParentIkkeHarRettVerdi() {
        opprettOgPersisterSoknad();
        Faktum parentParentFaktum = new Faktum().medKey("parentParent").medValue("false").medSoknadId(soknad.getSoknadId()).medType(BRUKERREGISTRERT);
        soknadRepository.opprettFaktum(soknad.getSoknadId(), parentParentFaktum);
        FaktumStruktur parentParentFaktumStruktur = new FaktumStruktur().medId("parentParent");

        Faktum parentFaktum = new Faktum().medKey("parent").medValue("parentValue").medSoknadId(soknad.getSoknadId()).medType(BRUKERREGISTRERT);
        Long parentFaktumId = soknadRepository.opprettFaktum(soknad.getSoknadId(), parentFaktum);
        FaktumStruktur parentFaktumStruktur = new FaktumStruktur().medId("parent").medDependOn(parentParentFaktumStruktur).medDependOnValues(Arrays.asList("parentParentValue"));

        Faktum faktum = new Faktum().medKey("key").medValue("true").medSoknadId(soknad.getSoknadId()).medType(BRUKERREGISTRERT).medParrentFaktumId(parentFaktumId);
        soknadRepository.opprettFaktum(soknad.getSoknadId(), faktum);
        FaktumStruktur faktumStruktur = new FaktumStruktur().medId("key").medDependOn(parentFaktumStruktur).medDependOnValues(Arrays.asList("parentValue"));
        VedleggForFaktumStruktur vedlegg = new VedleggForFaktumStruktur().medFaktum(faktumStruktur);

        Boolean vedleggPaakrevd = soknadRepository.isVedleggPaakrevd(soknadId, vedlegg);
        assertFalse(vedleggPaakrevd);
    }

    @Test
    public void skalTaVarePaaSystemproperties() {
        soknadId = opprettOgPersisterSoknad();
        soknadRepository.opprettFaktum(soknadId, new Faktum().medSoknadId(soknadId).medKey("system1").medType(SYSTEMREGISTRERT));
    }

    @Test
    public void skalHenteSoknadType() {
        opprettOgPersisterSoknad();
        String s = soknadRepository.hentSoknadType(soknadId);
        assertThat(s, is(equalTo(soknad.getskjemaNummer())));
    }

    @Test
    public void skalSetteDelstegstatus() {
        opprettOgPersisterSoknad();
        soknadRepository.settDelstegstatus(soknadId, DelstegStatus.SAMTYKKET);
        assertThat(soknadRepository.hentSoknad(soknadId).getDelstegStatus(), is(equalTo(DelstegStatus.SAMTYKKET)));
    }

    @Test
    public void skalSetteDelstegstatusMedBehandlingsId() {
        opprettOgPersisterSoknad();
        soknadRepository.settDelstegstatus(behandlingsId, DelstegStatus.SAMTYKKET);
        assertThat(soknadRepository.hentSoknad(soknadId).getDelstegStatus(), is(equalTo(DelstegStatus.SAMTYKKET)));
    }

    @Test
    public void skalSetteJournalforendeEnhet() {
        opprettOgPersisterSoknad();
        soknadRepository.settJournalforendeEnhet(behandlingsId, "NAV EØS");
        assertThat(soknadRepository.hentSoknad(behandlingsId).getJournalforendeEnhet(), is(equalTo("NAV EØS")));
    }

    @Test
    public void skalKunneOppdatereLagretBrukerData() {
        String key = "key";
        String value = "value";
        String oppdatertValue = "oppdatert";

        opprettOgPersisterSoknad();
        Long faktumId = lagreData(key, null, value);


        Faktum ikkeOppdaterData = soknadRepository.hentAlleBrukerData(behandlingsId).get(0);
        assertThat(ikkeOppdaterData, notNullValue());
        assertThat(ikkeOppdaterData.getValue(), is(value));


        lagreData(key, faktumId, oppdatertValue);
        Faktum oppdaterData = soknadRepository.hentAlleBrukerData(behandlingsId).get(0);
        assertThat(oppdaterData, notNullValue());
        assertThat(oppdaterData.getValue(), is(oppdatertValue));
    }

    @Test
    public void skalKunneHenteSoknadMedBrukerData() {
        opprettOgPersisterSoknad();
        lagreData("key1", null, "value1");
        lagreData("key2", null, "value2");
        lagreData("key3", null, "value3");

        WebSoknad soknadMedData = soknadRepository.hentSoknadMedData(soknadId);

        assertThat(soknadMedData, notNullValue());
        assertThat(soknadMedData.getFakta(), notNullValue());
        assertThat(soknadMedData.getFakta().size(), is(3));
    }

    @Test
    public void skalReturnereNullOmSoknadMedBehandlingsIdIkkeFinnes() {
        opprettOgPersisterSoknad();
        WebSoknad soknadMedData = soknadRepository.hentSoknadMedVedlegg("soknadSomIkkeFinnes");
        assertThat(soknadMedData, nullValue());
    }

    @Test
    public void skalReturnereNullOmSoknadMedSoknadIdIkkeFinnes() {
        opprettOgPersisterSoknad();
        WebSoknad soknadMedData = soknadRepository.hentSoknadMedData(1000000000L);
        assertThat(soknadMedData, nullValue());
    }

    @Test
    public void plukkerRiktigeSoknaderPaaTversAvAlleTraader() throws InterruptedException {
        List<Long> soknaderSomSkalMellomlagres = lagreXSoknader(15, 3);
        lagreXSoknader(5, 0); // legger til søknader som ikke skal taes med

        final List<Long> soknaderSomBleMellomlagret = Collections.synchronizedList(new ArrayList<Long>());
        int numberOfThreads = 10;
        ExecutorService threadpool = Executors.newFixedThreadPool(numberOfThreads);
        for (int i = 0; i < numberOfThreads; i++) {
            threadpool.submit(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    while (true) {
                        Optional<WebSoknad> soknad = soknadRepository.plukkSoknadTilMellomlagring();
                        if (soknad.isSome()) {
                            soknaderSomBleMellomlagret.add(soknad.get().getSoknadId());
                        } else {
                            break;
                        }
                    }
                    return null;
                }
            });
        }
        threadpool.shutdown();
        threadpool.awaitTermination(1, TimeUnit.MINUTES);

        sort(soknaderSomSkalMellomlagres);
        sort(soknaderSomBleMellomlagret);
        assertThat(soknaderSomBleMellomlagret, equalTo(soknaderSomSkalMellomlagres));
    }

    @Test
    public void skalKunneSletteSoknad() {
        opprettOgPersisterSoknad();
        soknadRepository.slettSoknad(soknadId);
        assertNull(soknadRepository.hentSoknad(soknadId));
    }

    @Test
    public void skalKunneLeggeTilbake() {
        opprettOgPersisterSoknad();
        lagreData("key1", null, "value1");
        lagreData("key2", null, "value2");

        WebSoknad soknad = soknadRepository.hentSoknadMedData(soknadId);
        soknadRepository.leggTilbake(soknad);
        List<Faktum> soknadBrukerData = soknadRepository.hentAlleBrukerData(behandlingsId);
        assertThat(soknadBrukerData, notNullValue());
    }

    @Test
    public void skalRepopulereDatabase() {
        soknad = WebSoknad.startSoknad()
                .medId(101L)
                .medUuid(uuid)
                .medAktorId("123123")
                .medBehandlingId("AH123")
                .medskjemaNummer(skjemaNummer)
                .medOppretteDato(now())
                .leggTilFaktum(new Faktum().medSoknadId(101L).medFaktumId(11L).medKey("key1").medValue("val1").medType(BRUKERREGISTRERT).medProperty("test", "test"))
                .leggTilFaktum(new Faktum().medSoknadId(101L).medFaktumId(12L).medKey("key2").medValue("val2").medType(SYSTEMREGISTRERT).medProperty("test2", "test2"))
                .medVedlegg(Arrays.asList(new Vedlegg(101L, 11L, "L6", Vedlegg.Status.LastetOpp).medOpprettetDato(System.currentTimeMillis())));

        soknadRepository.populerFraStruktur(soknad);
        WebSoknad res = soknadRepository.hentSoknadMedData(soknad.getSoknadId());
        soknad.getVedlegg().get(0).setOpprettetDato(res.getVedlegg().get(0).getOpprettetDato());
        assertThat(res, is(equalTo(soknad)));
    }

    @Test
    public void skalKunneHenteUtEttersendingMedBehandlingskjedeId() {
        opprettOgPersisterEttersending();

        Optional<WebSoknad> res = soknadRepository.hentEttersendingMedBehandlingskjedeId(behandlingsId);

        assertThat(res.isSome(), is(true));
        assertThat(res.get().getDelstegStatus(), is(DelstegStatus.ETTERSENDING_OPPRETTET));
    }

    @Test
    public void skalFaaNullDersomManProverAHenteEttersendingMedBehandlingskjedeIdOgDetIkkeFinnesNoen() {
        Optional<WebSoknad> res = soknadRepository.hentEttersendingMedBehandlingskjedeId(behandlingsId);

        assertThat(res.isSome(), is(false));
    }

    @Test
    public void skalHenteBarnafaktumMedProperties() {
        opprettOgPersisterSoknad();

        Faktum parentFaktum = new Faktum().medSoknadId(soknadId).medType(BRUKERREGISTRERT).medKey("parent");
        soknadRepository.opprettFaktum(soknadId, parentFaktum);

        Faktum child1 = new Faktum().medParrentFaktumId(parentFaktum.getFaktumId())
                .medType(BRUKERREGISTRERT).medSoknadId(soknadId).medProperty("key", "value").medKey("child");
        Faktum child2 = new Faktum().medParrentFaktumId(parentFaktum.getFaktumId())
                .medType(BRUKERREGISTRERT).medSoknadId(soknadId).medProperty("key2", "value").medKey("child");
        Faktum child3 = new Faktum().medParrentFaktumId(parentFaktum.getFaktumId())
                .medType(BRUKERREGISTRERT).medSoknadId(soknadId).medProperty("key3", "value").medKey("child");

        Faktum ikkeChild1 = new Faktum().medSoknadId(soknadId).medType(BRUKERREGISTRERT).medProperty("key4", "value4").medKey("child");
        Faktum ikkeChild2 = new Faktum().medSoknadId(soknadId).medType(BRUKERREGISTRERT).medProperty("key5", "value5").medKey("child");
        Faktum ikkeChild3 = new Faktum().medSoknadId(soknadId).medType(BRUKERREGISTRERT).medProperty("key6", "value6").medKey("child");

        soknadRepository.opprettFaktum(soknadId, child1);
        soknadRepository.opprettFaktum(soknadId, child2);
        soknadRepository.opprettFaktum(soknadId, child3);
        soknadRepository.opprettFaktum(soknadId, ikkeChild1);
        soknadRepository.opprettFaktum(soknadId, ikkeChild2);
        soknadRepository.opprettFaktum(soknadId, ikkeChild3);

        List<Faktum> barneFaktum = soknadRepository.hentBarneFakta(soknadId, parentFaktum.getFaktumId());
        assertThat(barneFaktum.size(), is(3));
        assertTrue(barneFaktum.contains(child1));
        assertTrue(barneFaktum.contains(child2));
        assertTrue(barneFaktum.contains(child3));
        assertFalse(barneFaktum.contains(ikkeChild1));
        assertFalse(barneFaktum.contains(ikkeChild2));
        assertFalse(barneFaktum.contains(ikkeChild3));

        assertTrue(barneFaktum.get(0).getProperties().containsValue("value"));
    }

    private List<Long> lagreXSoknader(int antall, int timerSidenLagring) {
        List<Long> soknadsIder = new ArrayList<>(antall);
        for (int i = 0; i < antall; i++) {
            Long id = opprettOgPersisterSoknad();
            soknadRepositoryTestSupport.getJdbcTemplate().update("update soknad set sistlagret = CURRENT_TIMESTAMP - (INTERVAL '" + timerSidenLagring + "' HOUR) where soknad_id = ?", soknadId);
            soknadsIder.add(id);
        }
        return soknadsIder;
    }

    private Long opprettOgPersisterSoknad() {
        return opprettOgPersisterSoknad(behandlingsId, aktorId);
    }

    private Long opprettOgPersisterSoknad(String nyAktorId) {
        return opprettOgPersisterSoknad(randomUUID().toString(), nyAktorId);
    }

    private Long opprettOgPersisterEttersending() {
        soknad = WebSoknad.startEttersending(behandlingsId)
                .medUuid(uuid)
                .medAktorId(aktorId)
                .medDelstegStatus(DelstegStatus.ETTERSENDING_OPPRETTET)
                .medBehandlingskjedeId(behandlingsId)
                .medskjemaNummer(skjemaNummer).medOppretteDato(now());
        soknadId = soknadRepository.opprettSoknad(soknad);
        soknad.setSoknadId(soknadId);
        return soknadId;
    }

    private Long opprettOgPersisterSoknad(String behId, String aktor) {
        soknad = WebSoknad.startSoknad()
                .medUuid(uuid)
                .medAktorId(aktor)
                .medBehandlingId(behId)
                .medDelstegStatus(DelstegStatus.OPPRETTET)
                .medskjemaNummer(skjemaNummer).medOppretteDato(now());
        soknadId = soknadRepository.opprettSoknad(soknad);
        soknad.setSoknadId(soknadId);
        return soknadId;
    }

    private Long lagreData(String key, Long faktumId, String value) {
        return lagreData(soknadId, key, faktumId, value);
    }

    private Long lagreData(Long soknadId, String key, Long faktumId, String value) {
        if(faktumId != null){
            return soknadRepository.oppdaterFaktum(new Faktum().medSoknadId(soknadId).medFaktumId(faktumId).medKey(key).medValue(value).medType(BRUKERREGISTRERT));
        }
        return soknadRepository.opprettFaktum(soknadId, new Faktum().medSoknadId(soknadId).medKey(key).medValue(value).medType(BRUKERREGISTRERT));

    }

}
