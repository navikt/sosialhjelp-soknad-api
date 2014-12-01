package no.nav.sbl.dialogarena.soknadinnsending.business.db;


import no.nav.modig.lang.option.Optional;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.DelstegStatus;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.SoknadInnsendingStatus;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Vedlegg;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;
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
import static no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum.FaktumType.BRUKERREGISTRERT;
import static no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum.FaktumType.SYSTEMREGISTRERT;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.joda.time.DateTime.now;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

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
        soknadRepositoryTestSupport.getJdbcTemplate().update("delete from soknadbrukerdata");
        soknadRepositoryTestSupport.getJdbcTemplate().update("delete from Soknad");
    }

    @Test
    public void skalKunneOppretteSoknad() {
        opprettOgPersisterSoknad();
    }

    @Test
    public void skalSetteSistLagret(){
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

        WebSoknad opprettetSoknad = soknadRepository.hentMedBehandlingsId(behId);

        assertThat(opprettetSoknad, notNullValue());
        assertThat(opprettetSoknad.getStatus(), is(SoknadInnsendingStatus.UNDER_ARBEID));
        assertThat(opprettetSoknad.getAktoerId(), is("aktor-3"));
        assertThat(opprettetSoknad.getBrukerBehandlingId(), is(behId));
        assertThat(opprettetSoknad.getskjemaNummer(), is(skjemaNummer));
    }

    @Test
    public void skalFaaNullVedUkjentBehandlingsId() {
        String behId = randomUUID().toString();
        WebSoknad soknad = soknadRepository.hentMedBehandlingsId(behId);
        Assert.assertNull(soknad);
    }


    @Test
    public void skalKunneHenteListeMedSoknader() {
        String aId = "2";
        opprettOgPersisterSoknad(aId);
        opprettOgPersisterSoknad(aId);
        opprettOgPersisterSoknad(aId);
        opprettOgPersisterSoknad(aId);

        List<WebSoknad> soknader = soknadRepository.hentListe("2");
        assertThat(soknader, notNullValue());
        assertThat(soknader.size(), is(4));
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


        List<Faktum> soknadBrukerData = soknadRepository.hentAlleBrukerData(soknadId);

        assertThat(soknadBrukerData, notNullValue());
        assertThat(soknadBrukerData.size(), is(3));
    }

    @Test
    public void skalHenteSystemfaktum() {
        Faktum faktum = new Faktum().medKey("personalia").medSoknadId(12L).medProperty("fno", "123").medType(SYSTEMREGISTRERT);
        Faktum result = new Faktum().medKey("personalia").medSoknadId(11L).medProperty("fno", "123").medType(SYSTEMREGISTRERT);
        result.setFaktumId(soknadRepository.lagreFaktum(11L, faktum, true));

        List<Faktum> personalia = soknadRepository.hentSystemFaktumList(11L, "personalia");
        assertThat(personalia.get(0), is(equalTo(result)));
    }

    @Test
    public void skalSletteFaktum() {
        opprettOgPersisterSoknad();
        Long id = lagreData("key", null, "value");
        Faktum faktum = soknadRepository.hentFaktum(soknadId, id);
        assertThat(faktum, is(notNullValue()));
        soknadRepository.slettBrukerFaktum(soknadId, id);
        try {
            soknadRepository.hentFaktum(soknadId, id);
            fail("ikke slettet");
        } catch (EmptyResultDataAccessException ex) {
        }
    }

    @Test
    public void skalKunneHenteFaktum() {
        opprettOgPersisterSoknad();
        lagreData("key1", null, "value1");
        Long faktumId = lagreData("key2", null, "value2");
        lagreData("key3", null, "value3");

        soknadRepository.hentFaktum(soknadId, faktumId);
    }

    @Test
    public void skalReturnereAtVedleggErPaakrevd() {
        opprettOgPersisterSoknad();
        Long parrent = soknadRepository.lagreFaktum(soknad.getSoknadId(), new Faktum().medKey("key1").medValue("dependOnValue").medSoknadId(soknad.getSoknadId()).medType(BRUKERREGISTRERT));
        soknadRepository.lagreFaktum(soknad.getSoknadId(), new Faktum().medKey("key2").medValue("true").medSoknadId(soknad.getSoknadId()).medType(BRUKERREGISTRERT).medParrentFaktumId(parrent));
        Boolean vedleggPaakrevd = soknadRepository.isVedleggPaakrevd(soknadId, "key2", "true", "dependOnValue");
        Assert.assertTrue(vedleggPaakrevd);
    }

    @Test
    public void skalTaVarePaaSystemproperties() {
        soknadId = opprettOgPersisterSoknad();
        soknadRepository.lagreFaktum(soknadId, new Faktum().medSoknadId(soknadId).medKey("system1").medType(SYSTEMREGISTRERT));
    }
    @Test
    public void skalHenteSoknadType(){
        opprettOgPersisterSoknad();
        String s = soknadRepository.hentSoknadType(soknadId);
        assertThat(s, is(equalTo(soknad.getskjemaNummer())));
    }
    @Test
    public void skalSetteDelstegstatus(){
        opprettOgPersisterSoknad();
        soknadRepository.settDelstegstatus(soknadId, DelstegStatus.SAMTYKKET);
        assertThat(soknadRepository.hentSoknad(soknadId).getDelstegStatus(), is(equalTo(DelstegStatus.SAMTYKKET)));
    }

    @Test
    public void skalKunneOppdatereLagretBrukerData() {
        String key = "key";
        String value = "value";
        String oppdatertValue = "oppdatert";

        opprettOgPersisterSoknad();
        Long faktumId = lagreData(key, null, value);

        Faktum ikkeOppdaterData = soknadRepository.hentAlleBrukerData(soknadId).get(0);
        assertThat(ikkeOppdaterData, notNullValue());
        assertThat(ikkeOppdaterData.getValue(), is(value));


        lagreData(key, faktumId, oppdatertValue);
        Faktum oppdaterData = soknadRepository.hentAlleBrukerData(soknadId).get(0);
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
        assertThat(soknadMedData.getFaktaListe(), notNullValue());
        assertThat(soknadMedData.getFaktaListe().size(), is(3));
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

    @Test(expected = EmptyResultDataAccessException.class)
    public void skalKunneSletteSoknad() {
        opprettOgPersisterSoknad();
        soknadRepository.slettSoknad(soknadId);
        soknadRepository.hentSoknad(soknadId);
    }

    @Test
    public void skalKunneLeggeTilbake() {
        opprettOgPersisterSoknad();
        lagreData("key1", null, "value1");
        lagreData("key2", null, "value2");

        WebSoknad soknad = soknadRepository.hentSoknadMedData(soknadId);
        soknadRepository.leggTilbake(soknad);
        List<Faktum> soknadBrukerData = soknadRepository.hentAlleBrukerData(soknadId);
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
                .medVedlegg(Arrays.asList(new Vedlegg(101L, 11L, "L6", Vedlegg.Status.LastetOpp)));

        soknadRepository.populerFraStruktur(soknad);
        WebSoknad res = soknadRepository.hentSoknadMedData(soknad.getSoknadId());
        assertThat(res, is(equalTo(soknad)));
    }

    @Test
    public void skalKunneHenteUtEttersendingMedBehandlingskjedeId() {
        opprettOgPersisterEttersending("BehandlingsId");

        Optional<WebSoknad> res = soknadRepository.hentEttersendingMedBehandlingskjedeId(behandlingsId);

        assertThat(res.isSome(), is(true));
        assertThat(res.get().getDelstegStatus(), is(DelstegStatus.ETTERSENDING_OPPRETTET));
    }

    @Test
    public void skalFaaNullDersomManProverAHenteEttersendingMedBehandlingskjedeIdOgDetIkkeFinnesNoen() {
        Optional<WebSoknad> res = soknadRepository.hentEttersendingMedBehandlingskjedeId(behandlingsId);

        assertThat(res.isSome(), is(false));
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

    private Long opprettOgPersisterEttersending(String behandlinsId) {
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
        return soknadRepository.lagreFaktum(soknadId, new Faktum().medSoknadId(soknadId).medFaktumId(faktumId).medKey(key).medValue(value).medType(BRUKERREGISTRERT));
    }

}
