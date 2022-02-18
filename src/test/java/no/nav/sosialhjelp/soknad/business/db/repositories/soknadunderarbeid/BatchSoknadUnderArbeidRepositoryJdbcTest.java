package no.nav.sosialhjelp.soknad.business.db.repositories.soknadunderarbeid;

import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sosialhjelp.soknad.business.db.RepositoryTestSupport;
import no.nav.sosialhjelp.soknad.business.db.repositories.opplastetvedlegg.BatchOpplastetVedleggRepository;
import no.nav.sosialhjelp.soknad.business.db.repositories.opplastetvedlegg.OpplastetVedleggRepository;
import no.nav.sosialhjelp.soknad.config.DbTestConfig;
import no.nav.sosialhjelp.soknad.domain.OpplastetVedlegg;
import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeid;
import no.nav.sosialhjelp.soknad.domain.VedleggType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.inject.Inject;
import java.time.LocalDateTime;

import static no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeidStatus.UNDER_ARBEID;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {DbTestConfig.class})
@ActiveProfiles("test")
class BatchSoknadUnderArbeidRepositoryJdbcTest {

    private static final String EIER = "12345678901";
    private static final String BEHANDLINGSID = "1100020";
    private static final String TILKNYTTET_BEHANDLINGSID = "4567";
    private static final JsonInternalSoknad JSON_INTERNAL_SOKNAD = new JsonInternalSoknad();

    @Inject
    private RepositoryTestSupport soknadRepositoryTestSupport;

    @Inject
    private SoknadUnderArbeidRepository soknadUnderArbeidRepository;

    @Inject
    private OpplastetVedleggRepository opplastetVedleggRepository;

    @Inject
    private BatchSoknadUnderArbeidRepository batchSoknadUnderArbeidRepository;

    @Inject
    private BatchOpplastetVedleggRepository batchOpplastetVedleggRepository;

    @AfterEach
    public void tearDown() {
        soknadRepositoryTestSupport.getJdbcTemplate().update("delete from SOKNAD_UNDER_ARBEID");
        soknadRepositoryTestSupport.getJdbcTemplate().update("delete from OPPLASTET_VEDLEGG");
    }

    @Test
    void hentSoknaderForBatchSkalFinneGamleSoknader() {
        var skalIkkeSlettes = lagSoknadUnderArbeid(BEHANDLINGSID, 13);
        var skalIkkeSlettesId = soknadUnderArbeidRepository.opprettSoknad(skalIkkeSlettes, EIER);

        var skalSlettes = lagSoknadUnderArbeid("annen_behandlingsid", 14);
        var skalSlettesId = soknadUnderArbeidRepository.opprettSoknad(skalSlettes, EIER);

        var soknader = batchSoknadUnderArbeidRepository.hentGamleSoknadUnderArbeidForBatch();

        assertThat(soknader).hasSize(1);
        assertThat(soknader.get(0)).isEqualTo(skalSlettesId);
    }

    @Test
    void slettSoknadGittSoknadUnderArbeidIdSkalSletteSoknad() {
        var soknadUnderArbeid = lagSoknadUnderArbeid(BEHANDLINGSID, 15);
        var soknadUnderArbeidId = soknadUnderArbeidRepository.opprettSoknad(soknadUnderArbeid, EIER);
        soknadUnderArbeid.setSoknadId(soknadUnderArbeidId);
        var opplastetVedleggUuid = opplastetVedleggRepository.opprettVedlegg(lagOpplastetVedlegg(soknadUnderArbeidId), EIER);

        batchSoknadUnderArbeidRepository.slettSoknad(soknadUnderArbeid.getSoknadId());

        assertThat(soknadUnderArbeidRepository.hentSoknad(soknadUnderArbeidId, EIER)).isEmpty();
        assertThat(opplastetVedleggRepository.hentVedlegg(opplastetVedleggUuid, EIER)).isEmpty();
    }

    private SoknadUnderArbeid lagSoknadUnderArbeid(String behandlingsId, int antallDagerSiden) {
        return new SoknadUnderArbeid().withVersjon(1L)
                .withBehandlingsId(behandlingsId)
                .withTilknyttetBehandlingsId(TILKNYTTET_BEHANDLINGSID)
                .withEier(EIER)
                .withJsonInternalSoknad(JSON_INTERNAL_SOKNAD)
                .withStatus(UNDER_ARBEID)
                .withOpprettetDato(LocalDateTime.now().minusDays(antallDagerSiden).minusMinutes(5))
                .withSistEndretDato(LocalDateTime.now().minusDays(antallDagerSiden).minusMinutes(5));
    }

    private OpplastetVedlegg lagOpplastetVedlegg(Long soknadId) {
        return new OpplastetVedlegg()
                .withEier(EIER)
                .withVedleggType(new VedleggType("bostotte|annetboutgift"))
                .withData(new byte[]{1, 2, 3})
                .withSoknadId(soknadId)
                .withFilnavn("dokumentasjon.pdf")
                .withSha512("aaa");
    }

}
