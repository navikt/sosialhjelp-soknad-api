package no.nav.sbl.dialogarena.soknadinnsending.business.db;


import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.SoknadInnsendingStatus;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Vedlegg;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;
import org.apache.commons.io.IOUtils;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {DbConfig.class})
public class SoknadRepositoryJdbcTest {

    @Inject
    private SoknadRepository soknadRepository;

    private WebSoknad soknad;

    private Long soknadId;

    private String aktorId = "1";
    private String behandlingsId = "1";
    private String gosysId = "gosysid";


    @After
    public void cleanUp() {

    }

    @Test
    public void skalKunneOppretteSoknad() {
        opprettOgPersisterSoknad();
    }

    @Test(expected = DataIntegrityViolationException.class)
    public void skalIkkeKunneOppretteUtenAktorId() {
        soknad = WebSoknad.startSoknad()
                .medBehandlingId(behandlingsId)
                .medGosysId(gosysId)
                .opprettetDato(DateTime.now());

        soknadRepository.opprettSoknad(soknad);
    }

    @Test(expected = DataIntegrityViolationException.class)
    public void skalIkkeKunneOppretteUtenBehandlingId() {
        soknad = WebSoknad.startSoknad()
                .medAktorId(aktorId)
                .medGosysId(gosysId)
                .opprettetDato(DateTime.now());

        soknadRepository.opprettSoknad(soknad);
    }

    @Test(expected = DataIntegrityViolationException.class)
    public void skalIkkeKunneOppretteUtenGosysId() {
        soknad = WebSoknad.startSoknad()
                .medAktorId(aktorId)
                .medBehandlingId(behandlingsId)
                .opprettetDato(DateTime.now());

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
        assertThat(opprettetSoknad.getGosysId(), is(gosysId));
    }

    @Test
    public void skalKunneHenteOpprettetSoknadMedBehandlingsId() {
        String behId = UUID.randomUUID().toString();
        opprettOgPersisterSoknad(behId, "aktor-3");

        WebSoknad opprettetSoknad = soknadRepository.hentMedBehandlingsId(behId);

        assertThat(opprettetSoknad, notNullValue());
        assertThat(opprettetSoknad.getStatus(), is(SoknadInnsendingStatus.UNDER_ARBEID));
        assertThat(opprettetSoknad.getAktoerId(), is("aktor-3"));
        assertThat(opprettetSoknad.getBrukerBehandlingId(), is(behId));
        assertThat(opprettetSoknad.getGosysId(), is(gosysId));
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
        lagreData(key, value);
    }

    @Test
    public void skalKunneHenteLagretBrukerData() {
        opprettOgPersisterSoknad();
        lagreData("key1", "value1");
        lagreData("key2", "value2");
        lagreData("key3", "value3");

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
        lagreData(key, value);

        Faktum ikkeOppdaterData = soknadRepository.hentAlleBrukerData(soknadId).get(0);
        assertThat(ikkeOppdaterData, notNullValue());
        assertThat(ikkeOppdaterData.getValue(), is(value));

        lagreData(key, oppdatertValue);
        Faktum oppdaterData = soknadRepository.hentAlleBrukerData(soknadId).get(0);
        assertThat(oppdaterData, notNullValue());
        assertThat(oppdaterData.getValue(), is(oppdatertValue));
    }

    @Test
    public void skalKunneHenteSoknadMedBrukerData() {
        opprettOgPersisterSoknad();
        lagreData("key1", "value1");
        lagreData("key2", "value2");
        lagreData("key3", "value3");

        WebSoknad soknadMedData = soknadRepository.hentSoknadMedData(soknadId);

        assertThat(soknadMedData, notNullValue());
        assertThat(soknadMedData.getFakta(), notNullValue());
        assertThat(soknadMedData.getFakta().size(), is(3));
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

    @Test
    public void skalSletteAllBrukerDataNaarEnSoknadAvbrytes() {
        opprettOgPersisterSoknad();
        lagreData("key1", "value1");
        lagreData("key2", "value2");

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

    @Test
    public void skalLasteOppBlob() throws IOException {
        byte[] bytes = {1, 2, 3};
        Vedlegg v = getVedlegg(bytes);
        soknadRepository.lagreVedlegg(v);
        List<Vedlegg> vedlegg = soknadRepository.hentVedleggForFaktum(v.getSoknadId(), v.getFaktum());
        assertThat(vedlegg.size(), is(equalTo(1)));
        v.setId(vedlegg.get(0).getId());
        assertThat(vedlegg.get(0), is(equalTo(v)));
        assertThat(IOUtils.toByteArray(vedlegg.get(0).getInputStream()), is(equalTo(bytes)));
    }

    @Test
    public void skalKunneSletteVedlegg() {
        final Vedlegg v = getVedlegg();
        Long id = soknadRepository.lagreVedlegg(v);
        List<Vedlegg> hentet = soknadRepository.hentVedleggForFaktum(v.getSoknadId(), id);
        assertThat(hentet, is(notNullValue()));
        assertThat(hentet.size(), is(1));
        soknadRepository.slettVedlegg(v.getSoknadId(), id);
        hentet = soknadRepository.hentVedleggForFaktum(v.getSoknadId(), id);
        assertThat(hentet, is(notNullValue()));
        assertThat(hentet.size(), is(0));
    }

    private Vedlegg getVedlegg() {
        return getVedlegg(new byte[]{1, 2, 3});
    }

    private Vedlegg getVedlegg(byte[] bytes) {
        ByteArrayInputStream is = new ByteArrayInputStream(bytes);
        final Vedlegg v = new Vedlegg();
        v.setFaktum(1L);
        v.setNavn("navn");
        v.setSoknadId(1L);
        v.setStorrelse(bytes.length);
        v.setInputStream(is);
        return v;
    }

    private void opprettOgPersisterSoknad() {
        opprettOgPersisterSoknad(behandlingsId, aktorId);
    }

    private void opprettOgPersisterSoknad(String nyAktorId) {
        opprettOgPersisterSoknad(UUID.randomUUID().toString(), nyAktorId);
    }

    private void opprettOgPersisterSoknad(String behId, String aktor) {
        soknad = WebSoknad.startSoknad()
                .medAktorId(aktor)
                .medBehandlingId(behId)
                .medGosysId(gosysId).opprettetDato(DateTime.now());

        soknadId = soknadRepository.opprettSoknad(soknad);
        assertThat(soknadId, greaterThan(0L));
    }

    private void lagreData(String key, String value) {
        soknadRepository.lagreFaktum(soknadId, new Faktum(soknadId, key, value, "BRUKERREGISTRERT"));
    }
}
