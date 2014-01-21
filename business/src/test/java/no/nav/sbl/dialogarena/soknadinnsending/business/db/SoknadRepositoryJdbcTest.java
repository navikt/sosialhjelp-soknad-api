package no.nav.sbl.dialogarena.soknadinnsending.business.db;


import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.SoknadInnsendingStatus;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import java.util.List;

import static java.util.UUID.randomUUID;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.joda.time.DateTime.now;
import static org.joda.time.DateTimeUtils.setCurrentMillisFixed;
import static org.joda.time.DateTimeUtils.setCurrentMillisSystem;
import static org.junit.Assert.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {DbConfig.class})
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

    @Test(expected = DataIntegrityViolationException.class)
    public void skalIkkeKunneOppretteUtenAktorId() {
        soknad = WebSoknad.startSoknad()
                .medBehandlingId(behandlingsId)
                .medskjemaNummer(skjemaNummer)
                .opprettetDato(now());

        soknadRepository.opprettSoknad(soknad);
    }

    @Test(expected = DataIntegrityViolationException.class)
    public void skalIkkeKunneOppretteUtenBehandlingId() {
        soknad = WebSoknad.startSoknad()
                .medAktorId(aktorId)
                .medskjemaNummer(skjemaNummer)
                .opprettetDato(now());

        soknadRepository.opprettSoknad(soknad);
    }

    @Test(expected = DataIntegrityViolationException.class)
    public void skalIkkeKunneOppretteUtenskjemaNummer() {
        soknad = WebSoknad.startSoknad()
                .medAktorId(aktorId)
                .medBehandlingId(behandlingsId)
                .opprettetDato(now());

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
        assertThat(soknadMedData.getFakta(), notNullValue());
        assertThat(soknadMedData.getFakta().size(), is(3));
    }

    @Test
    public void skalHenteOppSoknaderEldreEnnEnTime() {
        opprettOgPersisterSoknad();
        setCurrentMillisFixed(now().minusMinutes(61).getMillis()); //bedre enn � sette Thread.sleep(61 min)
        soknadRepository.settSistLagretTidspunkt(soknadId);
        setCurrentMillisSystem();
        assertThat(soknadRepository.hentAlleSoknaderSistLagretOverEnTimeSiden().size(), equalTo(1));
    }

    @Test
    public void skalKunneAvslutteEnSoknad() {
        opprettOgPersisterSoknad();

        WebSoknad ikkeAvsluttetSoknad = soknadRepository.hentSoknad(soknadId);
        soknadRepository.avslutt(ikkeAvsluttetSoknad);

        WebSoknad avsluttetSoknad = soknadRepository.hentSoknad(soknadId);
        assertThat(avsluttetSoknad, notNullValue());
        assertThat(avsluttetSoknad.getStatus(), is(SoknadInnsendingStatus.FERDIG));
    }

    @Test
    public void skalKunneAvbryteEnSoknad() {
        opprettOgPersisterSoknad();

        soknadRepository.avbryt(soknadId);

        WebSoknad avbruttSoknad = soknadRepository.hentSoknad(soknadId);
        assertThat(avbruttSoknad, notNullValue());
        assertThat(avbruttSoknad.getStatus(), is(SoknadInnsendingStatus.AVBRUTT_AV_BRUKER));
    }

    @Test(expected = EmptyResultDataAccessException.class)
    public void skalKunneSletteSoknad() {
        opprettOgPersisterSoknad();
        soknadRepository.slettSoknad(soknadId);
        soknadRepository.hentSoknad(soknadId);
    }

    @Test
    public void skalSletteAllBrukerDataNaarEnSoknadAvbrytes() {
        opprettOgPersisterSoknad();
        lagreData("key1", null, "value1");
        lagreData("key2", null, "value2");

        WebSoknad ikkeAvbruttSoknad = soknadRepository.hentSoknadMedData(soknadId);
        assertThat(ikkeAvbruttSoknad, notNullValue());
        assertThat(ikkeAvbruttSoknad.getFakta(), notNullValue());
        assertThat(ikkeAvbruttSoknad.getFakta().size(), is(2));
        soknadRepository.avbryt(soknadId);

        WebSoknad avbruttSoknad = soknadRepository.hentSoknadMedData(soknadId);
        assertThat(avbruttSoknad, notNullValue());
        assertThat(avbruttSoknad.getStatus(), is(SoknadInnsendingStatus.AVBRUTT_AV_BRUKER));
        assertThat(avbruttSoknad.getFakta(), notNullValue());
//        assertThat(avbruttSoknad.getFakta(), empty());

        List<Faktum> soknadBrukerData = soknadRepository.hentAlleBrukerData(soknadId);
        assertThat(soknadBrukerData, notNullValue());
        assertThat(soknadBrukerData, empty());
    }


    private void opprettOgPersisterSoknad() {
        opprettOgPersisterSoknad(behandlingsId, aktorId);
    }

    private void opprettOgPersisterSoknad(String nyAktorId) {
        opprettOgPersisterSoknad(randomUUID().toString(), nyAktorId);
    }

    private void opprettOgPersisterSoknad(String behId, String aktor) {
        soknad = WebSoknad.startSoknad()
                .medAktorId(aktor)
                .medBehandlingId(behId)
                .medskjemaNummer(skjemaNummer).opprettetDato(now());

        soknadId = soknadRepository.opprettSoknad(soknad);
        assertThat(soknadId, greaterThan(0L));
    }

    private Long lagreData(String key, Long faktumId, String value) {
        return soknadRepository.lagreFaktum(soknadId, new Faktum(soknadId, faktumId, key, value, "BRUKERREGISTRERT"));
    }
}
