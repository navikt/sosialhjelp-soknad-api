package no.nav.sosialhjelp.soknad.web.rest.ressurser.begrunnelse;

import no.nav.sbl.soknadsosialhjelp.soknad.begrunnelse.JsonBegrunnelse;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKildeBruker;
import no.nav.sosialhjelp.soknad.business.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository;
import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeid;
import no.nav.sosialhjelp.soknad.domain.model.exception.AuthorizationException;
import no.nav.sosialhjelp.soknad.domain.model.oidc.StaticSubjectHandlerService;
import no.nav.sosialhjelp.soknad.domain.model.oidc.SubjectHandler;
import no.nav.sosialhjelp.soknad.web.sikkerhet.Tilgangskontroll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static no.nav.sosialhjelp.soknad.business.service.soknadservice.SoknadService.createEmptyJsonInternalSoknad;
import static no.nav.sosialhjelp.soknad.web.rest.ressurser.begrunnelse.BegrunnelseRessurs.BegrunnelseFrontend;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BegrunnelseRessursTest {

    private static final String BEHANDLINGSID = "123";
    private static final String EIER = "123456789101";
    private static final String SOKER_FORDI = "Jeg søker fordi...";
    private static final String SOKER_OM = "Jeg søker om...";

    @Mock
    private SoknadUnderArbeidRepository soknadUnderArbeidRepository;

    @Mock
    private Tilgangskontroll tilgangskontroll;

    @InjectMocks
    private BegrunnelseRessurs begrunnelseRessurs;

    @BeforeEach
    public void setUp() {
        System.setProperty("environment.name", "test");
        SubjectHandler.setSubjectHandlerService(new StaticSubjectHandlerService());
    }

    @AfterEach
    public void tearDown() {
        SubjectHandler.resetOidcSubjectHandlerService();
        System.clearProperty("environment.name");
    }

    @Test
    void getBegrunnelseSkalReturnereBegrunnelseMedTommeStrenger(){
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                createJsonInternalSoknadWithBegrunnelse("", ""));

        final BegrunnelseFrontend begrunnelseFrontend = begrunnelseRessurs.hentBegrunnelse(BEHANDLINGSID);

        assertThat(begrunnelseFrontend.hvaSokesOm).isBlank();
        assertThat(begrunnelseFrontend.hvorforSoke).isBlank();
    }

    @Test
    void getBegrunnelseSkalReturnereBegrunnelse(){
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                createJsonInternalSoknadWithBegrunnelse(SOKER_OM, SOKER_FORDI));

        final BegrunnelseFrontend begrunnelseFrontend = begrunnelseRessurs.hentBegrunnelse(BEHANDLINGSID);

        assertThat(begrunnelseFrontend.hvaSokesOm).isEqualTo(SOKER_OM);
        assertThat(begrunnelseFrontend.hvorforSoke).isEqualTo(SOKER_FORDI);
    }

    @Test
    void putBegrunnelseSkalSetteBegrunnelse(){
        doNothing().when(tilgangskontroll).verifiserAtBrukerKanEndreSoknad(anyString());
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                createJsonInternalSoknadWithBegrunnelse("", ""));

        final BegrunnelseFrontend begrunnelseFrontend = new BegrunnelseFrontend()
                .withHvaSokesOm(SOKER_OM)
                .withHvorforSoke(SOKER_FORDI);
        begrunnelseRessurs.updateBegrunnelse(BEHANDLINGSID, begrunnelseFrontend);

        final SoknadUnderArbeid soknadUnderArbeid = catchSoknadUnderArbeidSentToOppdaterSoknadsdata();
        final JsonBegrunnelse begrunnelse = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getBegrunnelse();
        assertThat(begrunnelse.getKilde()).isEqualTo(JsonKildeBruker.BRUKER);
        assertThat(begrunnelse.getHvaSokesOm()).isEqualTo(SOKER_OM);
        assertThat(begrunnelse.getHvorforSoke()).isEqualTo(SOKER_FORDI);
    }

    @Test
    void getBegrunnelseSkalKasteAuthorizationExceptionVedManglendeTilgang() {
        doThrow(new AuthorizationException("Not for you my friend")).when(tilgangskontroll).verifiserAtBrukerHarTilgang();

        assertThatExceptionOfType(AuthorizationException.class)
                .isThrownBy(() -> begrunnelseRessurs.hentBegrunnelse(BEHANDLINGSID));

        verifyNoInteractions(soknadUnderArbeidRepository);
    }

    @Test
    void putBegrunnelseSkalKasteAuthorizationExceptionVedManglendeTilgang() {
        doThrow(new AuthorizationException("Not for you my friend")).when(tilgangskontroll).verifiserAtBrukerKanEndreSoknad(BEHANDLINGSID);

        var begrunnelseFrontend = new BegrunnelseFrontend().withHvaSokesOm(SOKER_OM).withHvorforSoke(SOKER_FORDI);

        assertThatExceptionOfType(AuthorizationException.class)
                .isThrownBy(() -> begrunnelseRessurs.updateBegrunnelse(BEHANDLINGSID, begrunnelseFrontend));

        verifyNoInteractions(soknadUnderArbeidRepository);
    }

    private SoknadUnderArbeid catchSoknadUnderArbeidSentToOppdaterSoknadsdata() {
        ArgumentCaptor<SoknadUnderArbeid> argument = ArgumentCaptor.forClass(SoknadUnderArbeid.class);
        verify(soknadUnderArbeidRepository).oppdaterSoknadsdata(argument.capture(), anyString());
        return argument.getValue();
    }

    private SoknadUnderArbeid createJsonInternalSoknadWithBegrunnelse(String hvaSokesOm, String hvorforSoke) {
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
        soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getBegrunnelse()
                .withHvaSokesOm(hvaSokesOm)
                .withHvorforSoke(hvorforSoke);
        return soknadUnderArbeid;
    }
}
