//package no.nav.sosialhjelp.soknad.business.db.repositories.opplastetvedlegg;
//
//import no.nav.sosialhjelp.soknad.business.db.RepositoryTestSupport;
//import no.nav.sosialhjelp.soknad.config.DbTestConfig;
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
//import java.util.List;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//@ExtendWith(SpringExtension.class)
//@ContextConfiguration(classes = {DbTestConfig.class})
//@ActiveProfiles("test")
//class OpplastetVedleggRepositoryJdbcTest {
//
//    private static final String EIER = "12345678901";
//    private static final String EIER2 = "22222222222";
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
//    private RepositoryTestSupport soknadRepositoryTestSupport;
//
//    @AfterEach
//    public void tearDown() {
//        soknadRepositoryTestSupport.getJdbcTemplate().update("delete from OPPLASTET_VEDLEGG");
//        soknadRepositoryTestSupport.getJdbcTemplate().update("delete from SOKNAD_UNDER_ARBEID");
//    }
//
//    @Test
//    void opprettVedleggOppretterOpplastetVedleggIDatabasen() {
//        OpplastetVedlegg opplastetVedlegg = lagOpplastetVedlegg();
//
//        String uuidFraDb = opplastetVedleggRepository.opprettVedlegg(opplastetVedlegg, EIER);
//
//        assertThat(uuidFraDb).isEqualTo(opplastetVedlegg.getUuid());
//    }
//
//    @Test
//    void hentVedleggHenterOpplastetVedleggSomFinnesForGittUuidOgEier() {
//        final String uuid = opprettOpplastetVedleggOgLagreIDb(lagOpplastetVedlegg(), EIER);
//
//        OpplastetVedlegg opplastetVedleggFraDb = opplastetVedleggRepository.hentVedlegg(uuid, EIER).get();
//
//        assertThat(opplastetVedleggFraDb.getUuid()).isEqualTo(uuid);
//        assertThat(opplastetVedleggFraDb.getEier()).isEqualTo(EIER);
//        assertThat(opplastetVedleggFraDb.getVedleggType().getSammensattType()).isEqualTo(TYPE);
//        assertThat(opplastetVedleggFraDb.getData()).isEqualTo(DATA);
//        assertThat(opplastetVedleggFraDb.getSoknadId()).isEqualTo(SOKNADID);
//        assertThat(opplastetVedleggFraDb.getFilnavn()).isEqualTo(FILNAVN);
//        assertThat(opplastetVedleggFraDb.getSha512()).isEqualTo(SHA512);
//    }
//
//    @Test
//    void hentVedleggForSoknadHenterAlleVedleggForGittSoknadUnderArbeidId() {
//        final String uuid = opprettOpplastetVedleggOgLagreIDb(lagOpplastetVedlegg(), EIER);
//        final String uuidSammeSoknadOgEier = opprettOpplastetVedleggOgLagreIDb(lagOpplastetVedlegg(EIER, TYPE2, SOKNADID), EIER);
//        opprettOpplastetVedleggOgLagreIDb(lagOpplastetVedlegg(EIER2, TYPE2, SOKNADID2), EIER2);
//        opprettOpplastetVedleggOgLagreIDb(lagOpplastetVedlegg(EIER, TYPE, SOKNADID3), EIER);
//
//        List<OpplastetVedlegg> opplastedeVedlegg = opplastetVedleggRepository.hentVedleggForSoknad(SOKNADID, EIER);
//
//        assertThat(opplastedeVedlegg).hasSize(2);
//        assertThat(opplastedeVedlegg.get(0).getUuid()).isEqualTo(uuid);
//        assertThat(opplastedeVedlegg.get(1).getUuid()).isEqualTo(uuidSammeSoknadOgEier);
//    }
//
//    @Test
//    void slettVedleggSletterOpplastetVedleggMedGittUuidOgEier() {
//        final String uuid = opprettOpplastetVedleggOgLagreIDb(lagOpplastetVedlegg(), EIER);
//
//        opplastetVedleggRepository.slettVedlegg(uuid, EIER);
//
//        assertThat(opplastetVedleggRepository.hentVedlegg(uuid, EIER)).isEmpty();
//    }
//
//    @Test
//    void slettAlleVedleggForSoknadSletterAlleOpplastedeVedleggForGittSoknadIdOgEier() {
//        final String uuid = opprettOpplastetVedleggOgLagreIDb(lagOpplastetVedlegg(), EIER);
//        final String uuidSammeSoknadOgEier = opprettOpplastetVedleggOgLagreIDb(lagOpplastetVedlegg(EIER, TYPE, SOKNADID), EIER);
//        final String uuidSammeEierOgAnnenSoknad = opprettOpplastetVedleggOgLagreIDb(lagOpplastetVedlegg(EIER, TYPE2, SOKNADID3), EIER);
//
//        opplastetVedleggRepository.slettAlleVedleggForSoknad(SOKNADID, EIER);
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