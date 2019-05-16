package no.nav.sbl.dialogarena.rest.ressurser.begrunnelse;

import no.nav.sbl.dialogarena.rest.ressurser.LegacyHelper;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.StaticSubjectHandlerService;
import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.SubjectHandler;
import no.nav.sbl.dialogarena.sikkerhet.Tilgangskontroll;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.FaktaService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadService;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonData;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknad;
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
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Optional;

import static no.nav.sbl.dialogarena.rest.ressurser.begrunnelse.BegrunnelseRessurs.BegrunnelseFrontend;
import static no.nav.sbl.dialogarena.sendsoknad.domain.oidc.OidcFeatureToggleUtils.IS_RUNNING_WITH_OIDC;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class BegrunnelseRessursTest {

    private static final String BEHANDLINGSID = "123";
    private static final String SOKER_FORDI = "Jeg søker fordi...";
    private static final String SOKER_OM = "Jeg søker om...";

    @Mock
    private LegacyHelper legacyHelper;

    @Mock
    private SoknadUnderArbeidRepository soknadUnderArbeidRepository;

    @Mock
    private Tilgangskontroll tilgangskontroll;

    @Mock
    private SoknadService soknadService;

    @Mock
    private FaktaService faktaService;

    @InjectMocks
    private BegrunnelseRessurs begrunnelseRessurs;

    @Before
    public void setUp() {
        SubjectHandler.setSubjectHandlerService(new StaticSubjectHandlerService());
        System.setProperty(IS_RUNNING_WITH_OIDC, "true");
    }

    @After
    public void tearDown() {
        SubjectHandler.resetOidcSubjectHandlerService();
        System.setProperty(IS_RUNNING_WITH_OIDC, "false");
    }

    @Test
    public void getBegrunnelseSkalReturnereBegrunnelseMedTommeStrenger(){
        when(legacyHelper.hentSoknad(anyString(), anyString(), anyBoolean())).thenReturn(
                createJsonInternalSoknadWithBegrunnelse("", ""));

        final BegrunnelseFrontend begrunnelseFrontend = begrunnelseRessurs.hentBegrunnelse(BEHANDLINGSID);

        assertThat(begrunnelseFrontend.hvaSokesOm, is(""));
        assertThat(begrunnelseFrontend.hvorforSoke, is(""));
    }

    @Test
    public void getBegrunnelseSkalReturnereBegrunnelse(){
        when(legacyHelper.hentSoknad(anyString(), anyString(), anyBoolean())).thenReturn(
                createJsonInternalSoknadWithBegrunnelse(SOKER_OM, SOKER_FORDI));

        final BegrunnelseFrontend begrunnelseFrontend = begrunnelseRessurs.hentBegrunnelse(BEHANDLINGSID);

        assertThat(begrunnelseFrontend.hvaSokesOm, is(SOKER_OM));
        assertThat(begrunnelseFrontend.hvorforSoke, is(SOKER_FORDI));
    }

    @Test
    public void putBegrunnelseSkalSetteBegrunnelse(){
        ignoreTilgangskontrollAndLegacyUpdate();
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                Optional.of(createJsonInternalSoknadWithBegrunnelse("", "")));

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

    private void ignoreTilgangskontrollAndLegacyUpdate() {
        doNothing().when(tilgangskontroll).verifiserAtBrukerKanEndreSoknad(anyString());
        when(soknadService.hentSoknad(anyString(), anyBoolean(), anyBoolean())).thenReturn(new WebSoknad());
        when(faktaService.hentFaktumMedKey(anyLong(), anyString())).thenReturn(new Faktum());
        when(faktaService.lagreBrukerFaktum(any(Faktum.class))).thenReturn(new Faktum());
    }

    private SoknadUnderArbeid createJsonInternalSoknadWithBegrunnelse(String hvaSokesOm, String hvorforSoke) {
        return new SoknadUnderArbeid()
                .withJsonInternalSoknad(new JsonInternalSoknad()
                        .withSoknad(new JsonSoknad()
                                .withData(new JsonData()
                                        .withBegrunnelse(new JsonBegrunnelse()
                                                .withHvaSokesOm(hvaSokesOm)
                                                .withHvorforSoke(hvorforSoke)
                                        )
                                )
                        )
                );
    }
}
