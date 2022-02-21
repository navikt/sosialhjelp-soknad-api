package no.nav.sosialhjelp.soknad.business.db.repositories.soknadunderarbeid;

import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sosialhjelp.soknad.business.db.RepositoryTestSupport;
import no.nav.sosialhjelp.soknad.business.db.repositories.opplastetvedlegg.OpplastetVedleggRepository;
import no.nav.sosialhjelp.soknad.common.exceptions.SamtidigOppdateringException;
import no.nav.sosialhjelp.soknad.common.exceptions.SoknadLaastException;
import no.nav.sosialhjelp.soknad.config.DbTestConfig;
import no.nav.sosialhjelp.soknad.domain.OpplastetVedlegg;
import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeid;
import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeidStatus;
import no.nav.sosialhjelp.soknad.domain.VedleggType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.inject.Inject;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static java.time.LocalDateTime.now;
import static no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeidStatus.LAAST;
import static no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeidStatus.UNDER_ARBEID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {DbTestConfig.class})
@ActiveProfiles("test")
class SoknadUnderArbeidRepositoryJdbcTest {

    private static final String EIER = "12345678901";
    private static final String EIER2 = "22222222222";
    private static final String BEHANDLINGSID = "1100020";
    private static final String TILKNYTTET_BEHANDLINGSID = "4567";
    private static final LocalDateTime OPPRETTET_DATO = now().minusSeconds(50).truncatedTo(ChronoUnit.MILLIS);
    private static final LocalDateTime SIST_ENDRET_DATO = now().truncatedTo(ChronoUnit.MILLIS);
    private static final JsonInternalSoknad JSON_INTERNAL_SOKNAD = new JsonInternalSoknad();

    @Inject
    private RepositoryTestSupport soknadRepositoryTestSupport;

    @Inject
    private SoknadUnderArbeidRepository soknadUnderArbeidRepository;

    @Inject
    private OpplastetVedleggRepository opplastetVedleggRepository;

    @AfterEach
    public void tearDown() {
        soknadRepositoryTestSupport.getJdbcTemplate().update("delete from SOKNAD_UNDER_ARBEID");
        soknadRepositoryTestSupport.getJdbcTemplate().update("delete from OPPLASTET_VEDLEGG");
    }

    @Test
    void opprettSoknadOppretterSoknadUnderArbeidIDatabasen() {
        final Long soknadUnderArbeidId = soknadUnderArbeidRepository.opprettSoknad(lagSoknadUnderArbeid(BEHANDLINGSID), EIER);

        assertThat(soknadUnderArbeidId).isNotNull();
    }

    @Test
    void hentSoknadHenterSoknadUnderArbeidGittRiktigEierOgSoknadId() {
        final Long soknadUnderArbeidId = soknadUnderArbeidRepository.opprettSoknad(lagSoknadUnderArbeid(BEHANDLINGSID)
                .withJsonInternalSoknad(new JsonInternalSoknad()), EIER);

        SoknadUnderArbeid soknadUnderArbeid = soknadUnderArbeidRepository.hentSoknad(soknadUnderArbeidId, EIER).get();

        assertThat(soknadUnderArbeid.getSoknadId()).isNotNull();
        assertThat(soknadUnderArbeid.getVersjon()).isEqualTo(1L);
        assertThat(soknadUnderArbeid.getBehandlingsId()).isEqualTo(BEHANDLINGSID);
        assertThat(soknadUnderArbeid.getTilknyttetBehandlingsId()).isEqualTo(TILKNYTTET_BEHANDLINGSID);
        assertThat(soknadUnderArbeid.getEier()).isEqualTo(EIER);
        assertThat(soknadUnderArbeid.getJsonInternalSoknad()).isNotNull();
        assertThat(soknadUnderArbeid.getStatus()).isEqualTo(UNDER_ARBEID);
        assertThat(soknadUnderArbeid.getOpprettetDato()).isEqualTo(OPPRETTET_DATO);
        assertThat(soknadUnderArbeid.getSistEndretDato()).isEqualTo(SIST_ENDRET_DATO);
    }

    @Test
    void hentSoknadHenterIngenSoknadUnderArbeidHvisEiesAvAnnenBruker() {
        final Long soknadUnderArbeidId = soknadUnderArbeidRepository.opprettSoknad(lagSoknadUnderArbeid(BEHANDLINGSID), EIER);

        Optional<SoknadUnderArbeid> soknadUnderArbeid = soknadUnderArbeidRepository.hentSoknad(soknadUnderArbeidId, EIER2);

        assertThat(soknadUnderArbeid).isEmpty();
    }

    @Test
    void hentSoknadHenterSoknadUnderArbeidGittRiktigEierOgBehandlingsId() {
        final Long soknadUnderArbeidId = soknadUnderArbeidRepository.opprettSoknad(lagSoknadUnderArbeid(BEHANDLINGSID), EIER);

        SoknadUnderArbeid soknadUnderArbeid = soknadUnderArbeidRepository.hentSoknad(BEHANDLINGSID, EIER);

        assertThat(soknadUnderArbeid.getSoknadId()).isEqualTo(soknadUnderArbeidId);
        assertThat(soknadUnderArbeid.getBehandlingsId()).isEqualTo(BEHANDLINGSID);
        assertThat(soknadUnderArbeid.getEier()).isEqualTo(EIER);
    }

    @Test
    void oppdaterSoknadsdataOppdatererVersjonOgSistEndretDato() throws SamtidigOppdateringException {
        SoknadUnderArbeid soknadUnderArbeid = lagSoknadUnderArbeid(BEHANDLINGSID);
        final Long soknadUnderArbeidId = soknadUnderArbeidRepository.opprettSoknad(soknadUnderArbeid, EIER);
        soknadUnderArbeid.withSoknadId(soknadUnderArbeidId).withJsonInternalSoknad(JSON_INTERNAL_SOKNAD);

        soknadUnderArbeidRepository.oppdaterSoknadsdata(soknadUnderArbeid, EIER);

        SoknadUnderArbeid soknadUnderArbeidFraDb = soknadUnderArbeidRepository.hentSoknad(soknadUnderArbeidId, EIER).get();
        assertThat(soknadUnderArbeidFraDb.getVersjon()).isEqualTo(2L);
        assertThat(soknadUnderArbeidFraDb.getJsonInternalSoknad()).isEqualTo(JSON_INTERNAL_SOKNAD);
        assertThat(soknadUnderArbeidFraDb.getSistEndretDato()).isAfter(SIST_ENDRET_DATO);
    }

    @Test
    void oppdaterSoknadsdataKasterExceptionVedVersjonskonflikt() throws SamtidigOppdateringException {
        SoknadUnderArbeid soknadUnderArbeid = lagSoknadUnderArbeid(BEHANDLINGSID);
        final Long soknadUnderArbeidId = soknadUnderArbeidRepository.opprettSoknad(soknadUnderArbeid, EIER);
        soknadUnderArbeid.withSoknadId(soknadUnderArbeidId).withJsonInternalSoknad(JSON_INTERNAL_SOKNAD).withVersjon(5L);
        soknadUnderArbeid.withJsonInternalSoknad(soknadUnderArbeid.getJsonInternalSoknad().withAdditionalProperty("endret", true));

        assertThatExceptionOfType(SamtidigOppdateringException.class)
                .isThrownBy(() -> soknadUnderArbeidRepository.oppdaterSoknadsdata(soknadUnderArbeid, EIER));
    }

    @Test
    void oppdaterSoknadsdataKasterExceptionVedOppdateringAvLaastSoknad() throws SamtidigOppdateringException {
        SoknadUnderArbeid soknadUnderArbeid = lagSoknadUnderArbeid(BEHANDLINGSID).withStatus(LAAST);
        final Long soknadUnderArbeidId = soknadUnderArbeidRepository.opprettSoknad(soknadUnderArbeid, EIER);
        soknadUnderArbeid.withSoknadId(soknadUnderArbeidId).withJsonInternalSoknad(JSON_INTERNAL_SOKNAD);

        assertThatExceptionOfType(SoknadLaastException.class)
                .isThrownBy(() -> soknadUnderArbeidRepository.oppdaterSoknadsdata(soknadUnderArbeid, EIER));
    }

    @Test
    void oppdaterInnsendingStatusOppdatererStatusOgSistEndretDato() {
        SoknadUnderArbeid soknadUnderArbeid = lagSoknadUnderArbeid(BEHANDLINGSID);
        final Long soknadUnderArbeidId = soknadUnderArbeidRepository.opprettSoknad(soknadUnderArbeid, EIER);
        soknadUnderArbeid.withSoknadId(soknadUnderArbeidId).withStatus(SoknadUnderArbeidStatus.LAAST);

        soknadUnderArbeidRepository.oppdaterInnsendingStatus(soknadUnderArbeid, EIER);

        SoknadUnderArbeid soknadUnderArbeidFraDb = soknadUnderArbeidRepository.hentSoknad(soknadUnderArbeidId, EIER).get();
        assertThat(soknadUnderArbeidFraDb.getVersjon()).isEqualTo(1L);
        assertThat(soknadUnderArbeidFraDb.getStatus()).isEqualTo(SoknadUnderArbeidStatus.LAAST);
        assertThat(soknadUnderArbeidFraDb.getSistEndretDato()).isAfter(SIST_ENDRET_DATO);
    }

    @Test
    void slettSoknadSletterSoknadUnderArbeidFraDatabasen() {
        SoknadUnderArbeid soknadUnderArbeid = lagSoknadUnderArbeid(BEHANDLINGSID);
        final Long soknadUnderArbeidId = soknadUnderArbeidRepository.opprettSoknad(soknadUnderArbeid, EIER);
        soknadUnderArbeid.setSoknadId(soknadUnderArbeidId);
        final String opplastetVedleggUuid = opplastetVedleggRepository.opprettVedlegg(lagOpplastetVedlegg(soknadUnderArbeidId), EIER);

        soknadUnderArbeidRepository.slettSoknad(soknadUnderArbeid, EIER);

        assertThat(soknadUnderArbeidRepository.hentSoknad(soknadUnderArbeidId, EIER)).isEmpty();
        assertThat(opplastetVedleggRepository.hentVedlegg(opplastetVedleggUuid, EIER)).isEmpty();
    }

    private SoknadUnderArbeid lagSoknadUnderArbeid(String behandlingsId) {
        return new SoknadUnderArbeid().withVersjon(1L)
                .withBehandlingsId(behandlingsId)
                .withTilknyttetBehandlingsId(TILKNYTTET_BEHANDLINGSID)
                .withEier(EIER)
                .withJsonInternalSoknad(JSON_INTERNAL_SOKNAD)
                .withStatus(UNDER_ARBEID)
                .withOpprettetDato(OPPRETTET_DATO)
                .withSistEndretDato(SIST_ENDRET_DATO);
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