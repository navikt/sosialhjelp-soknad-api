package no.nav.sbl.sosialhjelp.soknadunderbehandling;

import no.nav.sbl.dialogarena.sendsoknad.domain.util.ServiceUtils;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.DbTestConfig;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.RepositoryTestSupport;
import no.nav.sbl.sosialhjelp.domain.OpplastetVedlegg;
import no.nav.sbl.sosialhjelp.domain.VedleggType;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {DbTestConfig.class})
public class OpplastetVedleggRepositoryJdbcTest {

    private static final String EIER = "12345678901";
    private static final String EIER2 = "22222222222";
    private static final byte[] DATA = {1, 2, 3, 4};
    private static final String SHA512 = ServiceUtils.getSha512FromByteArray(DATA);
    private static final String TYPE = "bostotte|annetboutgift";
    private static final String TYPE2 = "dokumentasjon|aksjer";
    private static final Long SOKNADID = 1L;
    private static final Long SOKNADID2 = 2L;
    private static final Long SOKNADID3 = 3L;
    private static final String FILNAVN = "dokumentasjon.pdf";

    @Inject
    private OpplastetVedleggRepository opplastetVedleggRepository;

    @Inject
    private RepositoryTestSupport soknadRepositoryTestSupport;

    @After
    public void tearDown() {
        soknadRepositoryTestSupport.getJdbcTemplate().update("delete from OPPLASTET_VEDLEGG");
        soknadRepositoryTestSupport.getJdbcTemplate().update("delete from SOKNAD_UNDER_ARBEID");
    }

    @Test
    public void opprettVedleggOppretterOpplastetVedleggIDatabasen() {
        OpplastetVedlegg opplastetVedlegg = lagOpplastetVedlegg();

        String uuidFraDb = opplastetVedleggRepository.opprettVedlegg(opplastetVedlegg, EIER);

        assertThat(uuidFraDb, is(opplastetVedlegg.getUuid()));
    }

    @Test
    public void hentVedleggHenterOpplastetVedleggSomFinnesForGittUuidOgEier() {
        final String uuid = opprettOpplastetVedleggOgLagreIDb(lagOpplastetVedlegg(), EIER);

        OpplastetVedlegg opplastetVedleggFraDb = opplastetVedleggRepository.hentVedlegg(uuid, EIER).get();

        assertThat(opplastetVedleggFraDb.getUuid(), is(uuid));
        assertThat(opplastetVedleggFraDb.getEier(), is(EIER));
        assertThat(opplastetVedleggFraDb.getVedleggType().getSammensattType(), is(TYPE));
        assertThat(opplastetVedleggFraDb.getData(), is(DATA));
        assertThat(opplastetVedleggFraDb.getSoknadId(), is(SOKNADID));
        assertThat(opplastetVedleggFraDb.getFilnavn(), is(FILNAVN));
        assertThat(opplastetVedleggFraDb.getSha512(), is(SHA512));
    }

    @Test
    public void hentVedleggForSoknadHenterAlleVedleggForGittSoknadUnderArbeidId() {
        final String uuid = opprettOpplastetVedleggOgLagreIDb(lagOpplastetVedlegg(), EIER);
        final String uuidSammeSoknadOgEier = opprettOpplastetVedleggOgLagreIDb(lagOpplastetVedlegg(EIER, TYPE2, SOKNADID), EIER);
        opprettOpplastetVedleggOgLagreIDb(lagOpplastetVedlegg(EIER2, TYPE2, SOKNADID2), EIER2);
        opprettOpplastetVedleggOgLagreIDb(lagOpplastetVedlegg(EIER, TYPE, SOKNADID3), EIER);

        List<OpplastetVedlegg> opplastedeVedlegg = opplastetVedleggRepository.hentVedleggForSoknad(SOKNADID, EIER);

        assertThat(opplastedeVedlegg.size(), is(2));
        assertThat(opplastedeVedlegg.get(0).getUuid(), is(uuid));
        assertThat(opplastedeVedlegg.get(1).getUuid(), is(uuidSammeSoknadOgEier));
    }

    @Test
    public void slettVedleggSletterOpplastetVedleggMedGittUuidOgEier() {
        final String uuid = opprettOpplastetVedleggOgLagreIDb(lagOpplastetVedlegg(), EIER);

        opplastetVedleggRepository.slettVedlegg(uuid, EIER);

        assertThat(opplastetVedleggRepository.hentVedlegg(uuid, EIER).isPresent(), is(false));
    }

    @Test
    public void slettAlleVedleggForSoknadSletterAlleOpplastedeVedleggForGittSoknadIdOgEier() {
        final String uuid = opprettOpplastetVedleggOgLagreIDb(lagOpplastetVedlegg(), EIER);
        final String uuidSammeSoknadOgEier = opprettOpplastetVedleggOgLagreIDb(lagOpplastetVedlegg(EIER, TYPE, SOKNADID), EIER);
        final String uuidSammeEierOgAnnenSoknad = opprettOpplastetVedleggOgLagreIDb(lagOpplastetVedlegg(EIER, TYPE2, SOKNADID3), EIER);

        opplastetVedleggRepository.slettAlleVedleggForSoknad(SOKNADID, EIER);

        assertThat(opplastetVedleggRepository.hentVedlegg(uuid, EIER).isPresent(), is(false));
        assertThat(opplastetVedleggRepository.hentVedlegg(uuidSammeSoknadOgEier, EIER).isPresent(), is(false));
        assertThat(opplastetVedleggRepository.hentVedlegg(uuidSammeEierOgAnnenSoknad, EIER).isPresent(), is(true));
    }

    private OpplastetVedlegg lagOpplastetVedlegg(String eier, String type, Long soknadId) {
        return new OpplastetVedlegg()
                .withEier(eier)
                .withVedleggType(new VedleggType(type))
                .withData(DATA)
                .withSoknadId(soknadId)
                .withFilnavn(FILNAVN)
                .withSha512(SHA512);
    }

    private OpplastetVedlegg lagOpplastetVedlegg() {
        return lagOpplastetVedlegg(EIER, TYPE, SOKNADID);
    }

    private String opprettOpplastetVedleggOgLagreIDb(OpplastetVedlegg opplastetVedlegg, String eier) {
        return opplastetVedleggRepository.opprettVedlegg(opplastetVedlegg, eier);
    }
}