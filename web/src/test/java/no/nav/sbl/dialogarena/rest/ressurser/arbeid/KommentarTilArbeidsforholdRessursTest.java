package no.nav.sbl.dialogarena.rest.ressurser.arbeid;

import no.nav.modig.core.context.StaticSubjectHandler;
import no.nav.sbl.dialogarena.rest.ressurser.LegacyHelper;
import no.nav.sbl.dialogarena.rest.ressurser.arbeid.KommentarTilArbeidsforholdRessurs;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sikkerhet.Tilgangskontroll;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.FaktaService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadService;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonData;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.arbeid.JsonArbeid;
import no.nav.sbl.soknadsosialhjelp.soknad.arbeid.JsonKommentarTilArbeidsforhold;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKildeBruker;
import no.nav.sbl.sosialhjelp.domain.SoknadUnderArbeid;
import no.nav.sbl.sosialhjelp.soknadunderbehandling.SoknadUnderArbeidRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Optional;

import static no.nav.sbl.dialogarena.rest.ressurser.arbeid.KommentarTilArbeidsforholdRessurs.*;
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
public class KommentarTilArbeidsforholdRessursTest {

    private static final String BEHANDLINGSID = "123";
    private static final String KOMMENTAR = "Min kommentar til mine arbeidsforhold.";

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
    private KommentarTilArbeidsforholdRessurs kommentarTilArbeidsforholdRessurs;

    @Before
    public void setUp() {
        System.setProperty("no.nav.modig.core.context.subjectHandlerImplementationClass", StaticSubjectHandler.class.getName());
    }

    @Test
    public void getKommentarTilArbeidsforholdSkalReturnereKommentarTilArbeidsforholdLikNull(){
        when(legacyHelper.hentSoknad(anyString(), anyString())).thenReturn(
                createJsonInternalSoknadWithoutKommentarTilArbeidsforhold());

        final KommentarTilArbeidsforholdFrontend kommentarTilArbeidsforholdFrontend = kommentarTilArbeidsforholdRessurs.hentKommentarTilArbeidsforhold(BEHANDLINGSID);

        assertThat(kommentarTilArbeidsforholdFrontend.kommentarTilArbeidsforhold, nullValue());
    }

    @Test
    public void getKommentarTilArbeidsforholdSkalReturnereKommentarTilArbeidsforhold(){
        when(legacyHelper.hentSoknad(anyString(), anyString())).thenReturn(
                createJsonInternalSoknadWithKommentarTilArbeidsforhold(KOMMENTAR));

        final KommentarTilArbeidsforholdFrontend kommentarTilArbeidsforholdFrontend = kommentarTilArbeidsforholdRessurs.hentKommentarTilArbeidsforhold(BEHANDLINGSID);

        assertThat(kommentarTilArbeidsforholdFrontend.kommentarTilArbeidsforhold, is(KOMMENTAR));
    }

    @Test
    public void putKommentarTilArbeidsforholdSkalLageNyJsonKommentarTilArbeidsforholdDersomDenVarNull(){
        ignoreTilgangskontrollAndLegacyUpdate();
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                Optional.of(createJsonInternalSoknadWithoutKommentarTilArbeidsforhold()));

        final KommentarTilArbeidsforholdFrontend kommentarTilArbeidsforholdFrontend = new KommentarTilArbeidsforholdFrontend()
                .withKommentarTilArbeidsforhold(KOMMENTAR);
        kommentarTilArbeidsforholdRessurs.updateKommentarTilArbeidsforhold(BEHANDLINGSID, kommentarTilArbeidsforholdFrontend);

        final SoknadUnderArbeid soknadUnderArbeid = catchSoknadUnderArbeidSentToOppdaterSoknadsdata();
        final JsonKommentarTilArbeidsforhold kommentarTilArbeidsforhold = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getArbeid().getKommentarTilArbeidsforhold();
        assertThat(kommentarTilArbeidsforhold.getKilde(), is(JsonKildeBruker.BRUKER));
        assertThat(kommentarTilArbeidsforhold.getVerdi(), is(KOMMENTAR));
    }

    @Test
    public void putKommentarTilArbeidsforholdSkalOppdatereKommentarTilArbeidsforhold(){
        ignoreTilgangskontrollAndLegacyUpdate();
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                Optional.of(createJsonInternalSoknadWithKommentarTilArbeidsforhold("")));

        final KommentarTilArbeidsforholdFrontend kommentarTilArbeidsforholdFrontend = new KommentarTilArbeidsforholdFrontend()
                .withKommentarTilArbeidsforhold(KOMMENTAR);
        kommentarTilArbeidsforholdRessurs.updateKommentarTilArbeidsforhold(BEHANDLINGSID, kommentarTilArbeidsforholdFrontend);

        final SoknadUnderArbeid soknadUnderArbeid = catchSoknadUnderArbeidSentToOppdaterSoknadsdata();
        final JsonKommentarTilArbeidsforhold kommentarTilArbeidsforhold = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getArbeid().getKommentarTilArbeidsforhold();
        assertThat(kommentarTilArbeidsforhold.getKilde(), is(JsonKildeBruker.BRUKER));
        assertThat(kommentarTilArbeidsforhold.getVerdi(), is(KOMMENTAR));
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

    private SoknadUnderArbeid createJsonInternalSoknadWithKommentarTilArbeidsforhold(String kommentar) {
        return new SoknadUnderArbeid()
                .withJsonInternalSoknad(new JsonInternalSoknad()
                        .withSoknad(new JsonSoknad()
                                .withData(new JsonData()
                                        .withArbeid(new JsonArbeid()
                                                .withKommentarTilArbeidsforhold(new JsonKommentarTilArbeidsforhold()
                                                        .withKilde(JsonKildeBruker.BRUKER)
                                                        .withVerdi(kommentar)
                                                )
                                        )
                                )
                        )
                );
    }

    private SoknadUnderArbeid createJsonInternalSoknadWithoutKommentarTilArbeidsforhold() {
        return new SoknadUnderArbeid()
                .withJsonInternalSoknad(new JsonInternalSoknad()
                        .withSoknad(new JsonSoknad()
                                .withData(new JsonData()
                                        .withArbeid(new JsonArbeid())
                                )
                        )
                );
    }
}
