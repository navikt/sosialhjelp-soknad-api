//package no.nav.sosialhjelp.soknad.business.db.repositories.opplastetvedlegg;
//
//import no.nav.sosialhjelp.soknad.business.db.RepositoryTestSupport;
//import no.nav.sosialhjelp.soknad.config.DbTestConfig;
//import no.nav.sosialhjelp.soknad.db.repositories.opplastetvedlegg.OpplastetVedleggRepository;
//import no.nav.sosialhjelp.soknad.domain.OpplastetVedlegg;
//import no.nav.sosialhjelp.soknad.domain.VedleggType;
//import no.nav.sosialhjelp.soknad.vedlegg.VedleggUtils;
//import org.junit.jupiter.api.AfterEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.test.context.ContextConfiguration;
//import org.springframework.test.context.junit.jupiter.SpringExtension;
//
//import javax.inject.Inject;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//@ExtendWith(SpringExtension.class)
//@ContextConfiguration(classes = {DbTestConfig.class})
//@ActiveProfiles("test")
//class BatchOpplastetVedleggRepositoryJdbcTest {
//
//    private static final String EIER = "12345678901";
//    private static final byte[] DATA = {1, 2, 3, 4};
//    private static final String SHA512 = VedleggUtils.INSTANCE.getSha512FromByteArray(DATA);
//    private static final String TYPE = "bostotte|annetboutgift";
//    private static final String TYPE2 = "dokumentasjon|aksjer";
//    private static final Long SOKNADID = 1L;
//    private static final Long SOKNADID2 = 2L;
//    private static final Long SOKNADID3 = 3L;
//    private static final String FILNAVN = "dokumentasjon.pdf";
//
//    @Inject
//    private OpplastetVedleggRepository opplastetVedleggRepository;
//
//    @Inject
//    private BatchOpplastetVedleggRepository batchOpplastetVedleggRepository;
//
//    @Inject
//    private RepositoryTestSupport soknadRepositoryTestSupport;
//
//    @AfterEach
//    public void tearDown() {
//        soknadRepositoryTestSupport.getJdbcTemplate().update("delete from OPPLASTET_VEDLEGG");
//        soknadRepositoryTestSupport.getJdbcTemplate().update("delete from SOKNAD_UNDER_ARBEID");
//    }
//
//    @Test
//    void slettAlleVedleggForSoknadSletterAlleOpplastedeVedleggForGittSoknadId() {
//        final String uuid = opprettOpplastetVedleggOgLagreIDb(lagOpplastetVedlegg(), EIER);
//        final String uuidSammeSoknadOgEier = opprettOpplastetVedleggOgLagreIDb(lagOpplastetVedlegg(EIER, TYPE, SOKNADID), EIER);
//        final String uuidSammeEierOgAnnenSoknad = opprettOpplastetVedleggOgLagreIDb(lagOpplastetVedlegg(EIER, TYPE2, SOKNADID3), EIER);
//
//        batchOpplastetVedleggRepository.slettAlleVedleggForSoknad(SOKNADID);
//
//        assertThat(opplastetVedleggRepository.hentVedlegg(uuid, EIER)).isEmpty();
//        assertThat(opplastetVedleggRepository.hentVedlegg(uuidSammeSoknadOgEier, EIER)).isEmpty();
//        assertThat(opplastetVedleggRepository.hentVedlegg(uuidSammeEierOgAnnenSoknad, EIER)).isPresent();
//    }
//
//    private OpplastetVedlegg lagOpplastetVedlegg(String eier, String type, Long soknadId) {
//        return new OpplastetVedlegg()
//                .withEier(eier)
//                .withVedleggType(new VedleggType(type))
//                .withData(DATA)
//                .withSoknadId(soknadId)
//                .withFilnavn(FILNAVN)
//                .withSha512(SHA512);
//    }
//
//    private OpplastetVedlegg lagOpplastetVedlegg() {
//        return lagOpplastetVedlegg(EIER, TYPE, SOKNADID);
//    }
//
//    private String opprettOpplastetVedleggOgLagreIDb(OpplastetVedlegg opplastetVedlegg, String eier) {
//        return opplastetVedleggRepository.opprettVedlegg(opplastetVedlegg, eier);
//    }
//}