package no.nav.sosialhjelp.soknad.web.rest.ressurser.bosituasjon;

import no.nav.sbl.soknadsosialhjelp.soknad.bosituasjon.JsonBosituasjon;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKildeBruker;
import no.nav.sosialhjelp.soknad.business.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository;
import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeid;
import no.nav.sosialhjelp.soknad.domain.model.exception.AuthorizationException;
import no.nav.sosialhjelp.soknad.domain.model.oidc.StaticSubjectHandlerService;
import no.nav.sosialhjelp.soknad.domain.model.oidc.SubjectHandler;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.bosituasjon.BosituasjonRessurs.BosituasjonFrontend;
import no.nav.sosialhjelp.soknad.web.sikkerhet.Tilgangskontroll;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static no.nav.sosialhjelp.soknad.business.service.soknadservice.SoknadService.createEmptyJsonInternalSoknad;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class BosituasjonRessursTest {

    private static final String BEHANDLINGSID = "123";
    private static final String EIER = "123456789101";

    @Mock
    private SoknadUnderArbeidRepository soknadUnderArbeidRepository;

    @Mock
    private Tilgangskontroll tilgangskontroll;

    @InjectMocks
    private BosituasjonRessurs bosituasjonRessurs;

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
    public void getBosituasjonSkalReturnereBosituasjonMedBotypeOgAntallPersonerLikNull(){
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                createJsonInternalSoknadWithBosituasjon(null, null));

        final BosituasjonFrontend bosituasjonFrontend = bosituasjonRessurs.hentBosituasjon(BEHANDLINGSID);

        assertThat(bosituasjonFrontend.botype).isNull();
        assertThat(bosituasjonFrontend.antallPersoner).isNull();
    }

    @Test
    public void getBosituasjonSkalReturnereBosituasjonMedBotypeOgAntallPersoner(){
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                createJsonInternalSoknadWithBosituasjon(JsonBosituasjon.Botype.EIER, 2));

        final BosituasjonFrontend bosituasjonFrontend = bosituasjonRessurs.hentBosituasjon(BEHANDLINGSID);

        assertThat(bosituasjonFrontend.botype).isEqualTo(JsonBosituasjon.Botype.EIER);
        assertThat(bosituasjonFrontend.antallPersoner).isEqualTo(2);
    }

    @Test
    public void putBosituasjonSkalSetteBosituasjon(){
        doNothing().when(tilgangskontroll).verifiserAtBrukerKanEndreSoknad(anyString());
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                createJsonInternalSoknadWithBosituasjon(JsonBosituasjon.Botype.LEIER, 2));

        final BosituasjonFrontend bosituasjonFrontend = new BosituasjonFrontend()
                .withBotype(JsonBosituasjon.Botype.ANNET)
                .withAntallPersoner(3);
        bosituasjonRessurs.updateBosituasjon(BEHANDLINGSID, bosituasjonFrontend);

        final SoknadUnderArbeid soknadUnderArbeid = catchSoknadUnderArbeidSentToOppdaterSoknadsdata();
        final JsonBosituasjon bosituasjon = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getBosituasjon();
        assertThat(bosituasjon.getKilde()).isEqualTo(JsonKildeBruker.BRUKER);
        assertThat(bosituasjon.getBotype()).isEqualTo(JsonBosituasjon.Botype.ANNET);
        assertThat(bosituasjon.getAntallPersoner()).isEqualTo(3);
    }

    @Test
    public void putBosituasjonSkalSetteAntallPersonerLikNull(){
        doNothing().when(tilgangskontroll).verifiserAtBrukerKanEndreSoknad(anyString());
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                createJsonInternalSoknadWithBosituasjon(null, 2));

        final BosituasjonFrontend bosituasjonFrontend = new BosituasjonFrontend();
        bosituasjonRessurs.updateBosituasjon(BEHANDLINGSID, bosituasjonFrontend);

        final SoknadUnderArbeid soknadUnderArbeid = catchSoknadUnderArbeidSentToOppdaterSoknadsdata();
        final JsonBosituasjon bosituasjon = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getBosituasjon();
        assertThat(bosituasjon.getKilde()).isEqualTo(JsonKildeBruker.BRUKER);
        assertThat(bosituasjon.getBotype()).isNull();
        assertThat(bosituasjon.getAntallPersoner()).isNull();
    }

    @Test(expected = AuthorizationException.class)
    public void getBosituasjonSkalKasteAuthorizationExceptionVedManglendeTilgang() {
        doThrow(new AuthorizationException("Not for you my friend")).when(tilgangskontroll).verifiserAtBrukerHarTilgang();

        bosituasjonRessurs.hentBosituasjon(BEHANDLINGSID);

        verifyNoInteractions(soknadUnderArbeidRepository);
    }

    @Test(expected = AuthorizationException.class)
    public void putBosituasjonSkalKasteAuthorizationExceptionVedManglendeTilgang() {
        doThrow(new AuthorizationException("Not for you my friend")).when(tilgangskontroll).verifiserAtBrukerKanEndreSoknad(BEHANDLINGSID);

        var bosituasjonFrontend = new BosituasjonFrontend();
        bosituasjonRessurs.updateBosituasjon(BEHANDLINGSID, bosituasjonFrontend);

        verifyNoInteractions(soknadUnderArbeidRepository);
    }

    private SoknadUnderArbeid catchSoknadUnderArbeidSentToOppdaterSoknadsdata() {
        ArgumentCaptor<SoknadUnderArbeid> argument = ArgumentCaptor.forClass(SoknadUnderArbeid.class);
        verify(soknadUnderArbeidRepository).oppdaterSoknadsdata(argument.capture(), anyString());
        return argument.getValue();
    }

    private SoknadUnderArbeid createJsonInternalSoknadWithBosituasjon(JsonBosituasjon.Botype botype, Integer antallPersoner) {
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
        soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getBosituasjon()
                .withKilde(JsonKildeBruker.BRUKER)
                .withBotype(botype)
                .withAntallPersoner(antallPersoner);
        return soknadUnderArbeid;
    }
}
