package no.nav.sosialhjelp.soknad.web.rest.ressurser.utgifter;

import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonForsorgerplikt;
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonHarForsorgerplikt;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtgift;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomibekreftelse;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.oversikt.JsonOkonomioversiktUtgift;
import no.nav.sosialhjelp.soknad.business.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository;
import no.nav.sosialhjelp.soknad.business.service.TextService;
import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeid;
import no.nav.sosialhjelp.soknad.domain.model.exception.AuthorizationException;
import no.nav.sosialhjelp.soknad.domain.model.oidc.StaticSubjectHandlerService;
import no.nav.sosialhjelp.soknad.domain.model.oidc.SubjectHandler;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.utgifter.BarneutgiftRessurs.BarneutgifterFrontend;
import no.nav.sosialhjelp.soknad.web.sikkerhet.Tilgangskontroll;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.BEKREFTELSE_BARNEUTGIFTER;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTGIFTER_ANNET_BARN;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTGIFTER_BARNEHAGE;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTGIFTER_BARN_FRITIDSAKTIVITETER;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTGIFTER_BARN_TANNREGULERING;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTGIFTER_SFO;
import static no.nav.sosialhjelp.soknad.business.service.soknadservice.SoknadService.createEmptyJsonInternalSoknad;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class BarneutgiftRessursTest {

    private static final String BEHANDLINGSID = "123";
    private static final String EIER = "123456789101";

    @Mock
    private SoknadUnderArbeidRepository soknadUnderArbeidRepository;

    @Mock
    private Tilgangskontroll tilgangskontroll;

    @Mock
    private TextService textService;

    @InjectMocks
    private BarneutgiftRessurs barneutgiftRessurs;

    @Before
    public void setUp() {
        System.setProperty("environment.name", "test");
        SubjectHandler.setSubjectHandlerService(new StaticSubjectHandlerService());
        when(textService.getJsonOkonomiTittel(anyString())).thenReturn("tittel");
    }

    @After
    public void tearDown() {
        SubjectHandler.resetOidcSubjectHandlerService();
        System.clearProperty("environment.name");
    }

    @Test
    public void getBarneutgifterSkalReturnereBekreftelseLikNullOgAltFalse() {
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER)));

        BarneutgifterFrontend barneutgifterFrontend = barneutgiftRessurs.hentBarneutgifter(BEHANDLINGSID);

        assertThat(barneutgifterFrontend.harForsorgerplikt).isFalse();
        assertThat(barneutgifterFrontend.bekreftelse).isNull();
        assertThat(barneutgifterFrontend.barnehage).isFalse();
        assertThat(barneutgifterFrontend.sfo).isFalse();
        assertThat(barneutgifterFrontend.tannregulering).isFalse();
        assertThat(barneutgifterFrontend.fritidsaktiviteter).isFalse();
        assertThat(barneutgifterFrontend.annet).isFalse();
    }

    @Test
    public void getBarneutgifterSkalReturnereHarForsorgerpliktLikFalseForPersonUtenBarn() {
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                createJsonInternalSoknadWithBarneutgifter(false, false, Collections.emptyList()));

        BarneutgifterFrontend barneutgifterFrontend = barneutgiftRessurs.hentBarneutgifter(BEHANDLINGSID);

        assertThat(barneutgifterFrontend.harForsorgerplikt).isFalse();
        assertThat(barneutgifterFrontend.bekreftelse).isNull();
        assertThat(barneutgifterFrontend.barnehage).isFalse();
        assertThat(barneutgifterFrontend.sfo).isFalse();
        assertThat(barneutgifterFrontend.tannregulering).isFalse();
        assertThat(barneutgifterFrontend.fritidsaktiviteter).isFalse();
        assertThat(barneutgifterFrontend.annet).isFalse();
    }

    @Test
    public void getBarneutgifterSkalReturnereBekreftelserLikTrue() {
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                createJsonInternalSoknadWithBarneutgifter(true, true, asList(UTGIFTER_BARNEHAGE, UTGIFTER_SFO, UTGIFTER_BARN_FRITIDSAKTIVITETER,
                        UTGIFTER_BARN_TANNREGULERING, UTGIFTER_ANNET_BARN)));

        BarneutgifterFrontend barneutgifterFrontend = barneutgiftRessurs.hentBarneutgifter(BEHANDLINGSID);

        assertThat(barneutgifterFrontend.harForsorgerplikt).isTrue();
        assertThat(barneutgifterFrontend.bekreftelse).isTrue();
        assertThat(barneutgifterFrontend.barnehage).isTrue();
        assertThat(barneutgifterFrontend.sfo).isTrue();
        assertThat(barneutgifterFrontend.tannregulering).isTrue();
        assertThat(barneutgifterFrontend.fritidsaktiviteter).isTrue();
        assertThat(barneutgifterFrontend.annet).isTrue();
    }

    @Test
    public void putBarneutgifterSkalSetteAltFalseDersomManVelgerHarIkkeBarneutgifter() {
        doNothing().when(tilgangskontroll).verifiserAtBrukerKanEndreSoknad(anyString());
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                createJsonInternalSoknadWithBarneutgifter(true, true, asList(UTGIFTER_BARNEHAGE, UTGIFTER_SFO,
                        UTGIFTER_BARN_FRITIDSAKTIVITETER, UTGIFTER_ANNET_BARN)));

        BarneutgifterFrontend barneutgifterFrontend = new BarneutgifterFrontend();
        barneutgifterFrontend.setBekreftelse(false);
        barneutgiftRessurs.updateBarneutgifter(BEHANDLINGSID, barneutgifterFrontend);

        SoknadUnderArbeid soknadUnderArbeid = catchSoknadUnderArbeidSentToOppdaterSoknadsdata();
        List<JsonOkonomibekreftelse> bekreftelser = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData()
                .getOkonomi().getOpplysninger().getBekreftelse();
        JsonOkonomibekreftelse barneutgiftBekreftelse = bekreftelser.get(0);
        List<JsonOkonomioversiktUtgift> oversiktBarneutgifter = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData()
                .getOkonomi().getOversikt().getUtgift();
        List<JsonOkonomiOpplysningUtgift> opplysningerBarneutgifter = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData()
                .getOkonomi().getOpplysninger().getUtgift();
        assertThat(barneutgiftBekreftelse.getVerdi()).isFalse();
        assertThat(oversiktBarneutgifter.isEmpty()).isTrue();
        assertThat(opplysningerBarneutgifter.isEmpty()).isTrue();
    }

    @Test
    public void putBarneutgifterSkalSetteNoenBekreftelser() {
        doNothing().when(tilgangskontroll).verifiserAtBrukerKanEndreSoknad(anyString());
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER)));

        BarneutgifterFrontend barneutgifterFrontend = new BarneutgifterFrontend();
        barneutgifterFrontend.setBekreftelse(true);
        barneutgifterFrontend.setBarnehage(true);
        barneutgifterFrontend.setSfo(true);
        barneutgifterFrontend.setTannregulering(false);
        barneutgifterFrontend.setFritidsaktiviteter(false);
        barneutgifterFrontend.setAnnet(false);
        barneutgiftRessurs.updateBarneutgifter(BEHANDLINGSID, barneutgifterFrontend);

        SoknadUnderArbeid soknadUnderArbeid = catchSoknadUnderArbeidSentToOppdaterSoknadsdata();
        List<JsonOkonomibekreftelse> bekreftelser = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData()
                .getOkonomi().getOpplysninger().getBekreftelse();
        JsonOkonomibekreftelse barneutgiftBekreftelse = bekreftelser.get(0);
        List<JsonOkonomioversiktUtgift> oversiktBarneutgifter = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData()
                .getOkonomi().getOversikt().getUtgift();
        List<JsonOkonomiOpplysningUtgift> opplysningerBarneutgifter = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData()
                .getOkonomi().getOpplysninger().getUtgift();
        assertThat(barneutgiftBekreftelse.getKilde()).isEqualTo(JsonKilde.BRUKER);
        assertThat(barneutgiftBekreftelse.getType()).isEqualTo(BEKREFTELSE_BARNEUTGIFTER);
        assertThat(barneutgiftBekreftelse.getVerdi()).isTrue();
        assertThat(oversiktBarneutgifter.stream().anyMatch(barneutgift -> barneutgift.getType().equals(UTGIFTER_BARNEHAGE))).isTrue();
        assertThat(oversiktBarneutgifter.stream().anyMatch(barneutgift -> barneutgift.getType().equals(UTGIFTER_SFO))).isTrue();
        assertThat(opplysningerBarneutgifter.stream().anyMatch(barneutgift -> barneutgift.getType().equals(UTGIFTER_BARN_FRITIDSAKTIVITETER))).isFalse();
        assertThat(opplysningerBarneutgifter.stream().anyMatch(barneutgift -> barneutgift.getType().equals(UTGIFTER_BARN_TANNREGULERING))).isFalse();
        assertThat(opplysningerBarneutgifter.stream().anyMatch(barneutgift -> barneutgift.getType().equals(UTGIFTER_ANNET_BARN))).isFalse();
    }

    @Test
    public void putBarneutgifterSkalSetteAlleBekreftelser() {
        doNothing().when(tilgangskontroll).verifiserAtBrukerKanEndreSoknad(anyString());
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER)));

        BarneutgifterFrontend barneutgifterFrontend = new BarneutgifterFrontend();
        barneutgifterFrontend.setBekreftelse(true);
        barneutgifterFrontend.setBarnehage(true);
        barneutgifterFrontend.setSfo(true);
        barneutgifterFrontend.setTannregulering(true);
        barneutgifterFrontend.setFritidsaktiviteter(true);
        barneutgifterFrontend.setAnnet(true);
        barneutgiftRessurs.updateBarneutgifter(BEHANDLINGSID, barneutgifterFrontend);

        SoknadUnderArbeid soknadUnderArbeid = catchSoknadUnderArbeidSentToOppdaterSoknadsdata();
        List<JsonOkonomibekreftelse> bekreftelser = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData()
                .getOkonomi().getOpplysninger().getBekreftelse();
        JsonOkonomibekreftelse barneutgiftBekreftelse = bekreftelser.get(0);
        List<JsonOkonomioversiktUtgift> oversiktBarneutgifter = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData()
                .getOkonomi().getOversikt().getUtgift();
        List<JsonOkonomiOpplysningUtgift> opplysningerBarneutgifter = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData()
                .getOkonomi().getOpplysninger().getUtgift();
        assertThat(barneutgiftBekreftelse.getKilde()).isEqualTo(JsonKilde.BRUKER);
        assertThat(barneutgiftBekreftelse.getType()).isEqualTo(BEKREFTELSE_BARNEUTGIFTER);
        assertThat(barneutgiftBekreftelse.getVerdi()).isTrue();
        assertThat(oversiktBarneutgifter.stream().anyMatch(barneutgift -> barneutgift.getType().equals(UTGIFTER_BARNEHAGE))).isTrue();
        assertThat(oversiktBarneutgifter.stream().anyMatch(barneutgift -> barneutgift.getType().equals(UTGIFTER_SFO))).isTrue();
        assertThat(opplysningerBarneutgifter.stream().anyMatch(barneutgift -> barneutgift.getType().equals(UTGIFTER_BARN_FRITIDSAKTIVITETER))).isTrue();
        assertThat(opplysningerBarneutgifter.stream().anyMatch(barneutgift -> barneutgift.getType().equals(UTGIFTER_BARN_TANNREGULERING))).isTrue();
        assertThat(opplysningerBarneutgifter.stream().anyMatch(barneutgift -> barneutgift.getType().equals(UTGIFTER_ANNET_BARN))).isTrue();
    }

    @Test(expected = AuthorizationException.class)
    public void getBarneutgifterSkalKasteAuthorizationExceptionVedManglendeTilgang() {
        doThrow(new AuthorizationException("Not for you my friend")).when(tilgangskontroll).verifiserAtBrukerHarTilgang();

        barneutgiftRessurs.hentBarneutgifter(BEHANDLINGSID);

        verifyNoInteractions(soknadUnderArbeidRepository);
    }

    @Test(expected = AuthorizationException.class)
    public void putBarneutgifterSkalKasteAuthorizationExceptionVedManglendeTilgang() {
        doThrow(new AuthorizationException("Not for you my friend")).when(tilgangskontroll).verifiserAtBrukerKanEndreSoknad(anyString());

        var barneutgifterFrontend = new BarneutgifterFrontend();
        barneutgiftRessurs.updateBarneutgifter(BEHANDLINGSID, barneutgifterFrontend);

        verifyNoInteractions(soknadUnderArbeidRepository);
    }

    private SoknadUnderArbeid catchSoknadUnderArbeidSentToOppdaterSoknadsdata() {
        ArgumentCaptor<SoknadUnderArbeid> argument = ArgumentCaptor.forClass(SoknadUnderArbeid.class);
        verify(soknadUnderArbeidRepository).oppdaterSoknadsdata(argument.capture(), anyString());
        return argument.getValue();
    }

    private SoknadUnderArbeid createJsonInternalSoknadWithBarneutgifter(Boolean harForsorgerplikt, Boolean harUtgifter, List<String> utgiftstyper) {
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
        List<JsonOkonomioversiktUtgift> oversiktUtgifter = new ArrayList<>();
        List<JsonOkonomiOpplysningUtgift> opplysningUtgifter = new ArrayList<>();
        for (String utgiftstype : utgiftstyper) {
            if (utgiftstype.equals(UTGIFTER_BARNEHAGE) || utgiftstype.equals(UTGIFTER_SFO)) {
                oversiktUtgifter.add(new JsonOkonomioversiktUtgift()
                        .withKilde(JsonKilde.BRUKER)
                        .withType(utgiftstype)
                        .withTittel("tittel"));
            } else if (utgiftstype.equals(UTGIFTER_BARN_FRITIDSAKTIVITETER) || utgiftstype.equals(UTGIFTER_BARN_TANNREGULERING)
                    || utgiftstype.equals(UTGIFTER_ANNET_BARN)) {
                opplysningUtgifter.add(new JsonOkonomiOpplysningUtgift()
                        .withKilde(JsonKilde.BRUKER)
                        .withType(utgiftstype)
                        .withTittel("tittel"));
            }
        }
        soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOpplysninger().setBekreftelse(asList(new JsonOkonomibekreftelse()
                .withKilde(JsonKilde.BRUKER)
                .withType(BEKREFTELSE_BARNEUTGIFTER)
                .withVerdi(harUtgifter)));
        soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOversikt().setUtgift(oversiktUtgifter);
        soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOpplysninger().setUtgift(opplysningUtgifter);
        soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getFamilie().setForsorgerplikt(new JsonForsorgerplikt()
                .withHarForsorgerplikt(new JsonHarForsorgerplikt().withKilde(JsonKilde.SYSTEM).withVerdi(harForsorgerplikt)));
        return soknadUnderArbeid;
    }
}
