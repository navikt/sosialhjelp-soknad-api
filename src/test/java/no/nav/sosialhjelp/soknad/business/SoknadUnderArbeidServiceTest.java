package no.nav.sosialhjelp.soknad.business;

import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeid;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static java.time.LocalDateTime.now;

@ExtendWith(MockitoExtension.class)
class SoknadUnderArbeidServiceTest {
    private static final String EIER = "12345678910";
    private static final Long SOKNAD_UNDER_ARBEID_ID = 1L;
    private static final String BEHANDLINGSID = "1100001L";
    private static final String TILKNYTTET_BEHANDLINGSID = "1100002K";
    private static final LocalDateTime OPPRETTET_DATO = now().minusSeconds(50);
    private static final LocalDateTime SIST_ENDRET_DATO = now();

    @InjectMocks
    private SoknadUnderArbeidService soknadUnderArbeidService;

    @Test
    void settInnsendingstidspunktPaSoknadSkalHandtereEttersendelse() {
        soknadUnderArbeidService.settInnsendingstidspunktPaSoknad(lagSoknadUnderArbeidForEttersendelse());
    }

    private SoknadUnderArbeid lagSoknadUnderArbeidForEttersendelse() {
        return new SoknadUnderArbeid()
                .withSoknadId(SOKNAD_UNDER_ARBEID_ID)
                .withBehandlingsId(BEHANDLINGSID)
                .withTilknyttetBehandlingsId(TILKNYTTET_BEHANDLINGSID)
                .withEier(EIER)
                .withOpprettetDato(OPPRETTET_DATO)
                .withSistEndretDato(SIST_ENDRET_DATO);
    }
}