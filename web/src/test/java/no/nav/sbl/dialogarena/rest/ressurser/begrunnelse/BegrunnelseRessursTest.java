package no.nav.sbl.dialogarena.rest.ressurser.begrunnelse;

import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.StaticSubjectHandlerService;
import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.SubjectHandler;
import no.nav.sbl.dialogarena.sikkerhet.Tilgangskontroll;
import no.nav.sbl.soknadsosialhjelp.soknad.begrunnelse.JsonBegrunnelse;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKildeBruker;
import no.nav.sbl.sosialhjelp.domain.SoknadUnderArbeid;
import no.nav.sbl.sosialhjelp.soknadunderbehandling.SoknadUnderArbeidRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static no.nav.sbl.dialogarena.rest.ressurser.begrunnelse.BegrunnelseRessurs.BegrunnelseFrontend;
import static no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadService.createEmptyJsonInternalSoknad;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class BegrunnelseRessursTest {

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

    @Before
    public void setUp() {
        System.setProperty("environment.name", "test");
        SubjectHandler.setSubjectHandlerService(new StaticSubjectHandlerService());
    }

    @After
    public void tearDown() {
        SubjectHandler.resetOidcSubjectHandlerService();
        System.clearProperty("environment.name");
    }

    @Test
    public void getBegrunnelseSkalReturnereBegrunnelseMedTommeStrenger(){
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                createJsonInternalSoknadWithBegrunnelse("", ""));

        final BegrunnelseFrontend begrunnelseFrontend = begrunnelseRessurs.hentBegrunnelse(BEHANDLINGSID);

        assertThat(begrunnelseFrontend.hvaSokesOm, is(""));
        assertThat(begrunnelseFrontend.hvorforSoke, is(""));
    }

    @Test
    public void getBegrunnelseSkalReturnereBegrunnelse(){
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                createJsonInternalSoknadWithBegrunnelse(SOKER_OM, SOKER_FORDI));

        final BegrunnelseFrontend begrunnelseFrontend = begrunnelseRessurs.hentBegrunnelse(BEHANDLINGSID);

        assertThat(begrunnelseFrontend.hvaSokesOm, is(SOKER_OM));
        assertThat(begrunnelseFrontend.hvorforSoke, is(SOKER_FORDI));
    }

    @Test
    public void putBegrunnelseSkalSetteBegrunnelse(){
        doNothing().when(tilgangskontroll).verifiserAtBrukerKanEndreSoknad(anyString());
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                createJsonInternalSoknadWithBegrunnelse("", ""));

        final BegrunnelseFrontend begrunnelseFrontend = new BegrunnelseFrontend()
                .withHvaSokesOm(SOKER_OM)
                .withHvorforSoke(SOKER_FORDI);
        begrunnelseRessurs.updateBegrunnelse(BEHANDLINGSID, begrunnelseFrontend);

        final SoknadUnderArbeid soknadUnderArbeid = catchSoknadUnderArbeidSentToOppdaterSoknadsdata();
        final JsonBegrunnelse begrunnelse = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getBegrunnelse();
        assertThat(begrunnelse.getKilde(), is(JsonKildeBruker.BRUKER));
        assertThat(begrunnelse.getHvaSokesOm(), is(SOKER_OM));
        assertThat(begrunnelse.getHvorforSoke(), is(SOKER_FORDI));
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
