package no.nav.sbl.dialogarena.rest.ressurser.bosituasjon;

import no.nav.sbl.dialogarena.rest.ressurser.LegacyHelper;
import no.nav.sbl.dialogarena.rest.ressurser.bosituasjon.BosituasjonRessurs.BosituasjonFrontend;
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
import no.nav.sbl.soknadsosialhjelp.soknad.bosituasjon.JsonBosituasjon;
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

import static no.nav.sbl.dialogarena.sendsoknad.domain.oidc.OidcFeatureToggleUtils.IS_RUNNING_WITH_OIDC;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class BosituasjonRessursTest {

    private static final String BEHANDLINGSID = "123";

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
    private BosituasjonRessurs bosituasjonRessurs;

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
    public void getBosituasjonSkalReturnereBosituasjonMedBotypeOgAntallPersonerLikNull(){
        when(legacyHelper.hentSoknad(anyString(), anyString(), anyBoolean())).thenReturn(
                createJsonInternalSoknadWithBosituasjon(null, null));

        final BosituasjonFrontend bosituasjonFrontend = bosituasjonRessurs.hentBosituasjon(BEHANDLINGSID);

        assertThat(bosituasjonFrontend.botype, nullValue());
        assertThat(bosituasjonFrontend.antallPersoner, nullValue());
    }

    @Test
    public void getBosituasjonSkalReturnereBosituasjonMedBotypeOgAntallPersoner(){
        when(legacyHelper.hentSoknad(anyString(), anyString(), anyBoolean())).thenReturn(
                createJsonInternalSoknadWithBosituasjon(JsonBosituasjon.Botype.EIER, 2));

        final BosituasjonFrontend bosituasjonFrontend = bosituasjonRessurs.hentBosituasjon(BEHANDLINGSID);

        assertThat(bosituasjonFrontend.botype, is(JsonBosituasjon.Botype.EIER));
        assertThat(bosituasjonFrontend.antallPersoner, is(2));
    }

    @Test
    public void putBosituasjonSkalSetteBosituasjon(){
        ignoreTilgangskontrollAndLegacyUpdate();
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                Optional.of(createJsonInternalSoknadWithBosituasjon(JsonBosituasjon.Botype.LEIER, 2)));

        final BosituasjonFrontend bosituasjonFrontend = new BosituasjonFrontend()
                .withBotype(JsonBosituasjon.Botype.ANNET)
                .withAntallPersoner(3);
        bosituasjonRessurs.updateBosituasjon(BEHANDLINGSID, bosituasjonFrontend);

        final SoknadUnderArbeid soknadUnderArbeid = catchSoknadUnderArbeidSentToOppdaterSoknadsdata();
        final JsonBosituasjon bosituasjon = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getBosituasjon();
        assertThat(bosituasjon.getKilde(), is(JsonKildeBruker.BRUKER));
        assertThat(bosituasjon.getBotype(), is(JsonBosituasjon.Botype.ANNET));
        assertThat(bosituasjon.getAntallPersoner(), is(3));
    }

    @Test
    public void putBosituasjonSkalSetteAntallPersonerLikNull(){
        ignoreTilgangskontrollAndLegacyUpdate();
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                Optional.of(createJsonInternalSoknadWithBosituasjon(null, 2)));

        final BosituasjonFrontend bosituasjonFrontend = new BosituasjonFrontend();
        bosituasjonRessurs.updateBosituasjon(BEHANDLINGSID, bosituasjonFrontend);

        final SoknadUnderArbeid soknadUnderArbeid = catchSoknadUnderArbeidSentToOppdaterSoknadsdata();
        final JsonBosituasjon bosituasjon = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getBosituasjon();
        assertThat(bosituasjon.getKilde(), is(JsonKildeBruker.BRUKER));
        assertThat(bosituasjon.getBotype(), nullValue());
        assertThat(bosituasjon.getAntallPersoner(), nullValue());
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

    private SoknadUnderArbeid createJsonInternalSoknadWithBosituasjon(JsonBosituasjon.Botype botype, Integer antallPersoner) {
        return new SoknadUnderArbeid()
                .withJsonInternalSoknad(new JsonInternalSoknad()
                        .withSoknad(new JsonSoknad()
                                .withData(new JsonData()
                                        .withBosituasjon(new JsonBosituasjon()
                                                .withKilde(JsonKildeBruker.BRUKER)
                                                .withBotype(botype)
                                                .withAntallPersoner(antallPersoner)
                                        )
                                )
                        )
                );
    }
}
