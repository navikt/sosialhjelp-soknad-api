package no.nav.sbl.dialogarena.rest.ressurser.familie;

import no.nav.modig.core.context.StaticSubjectHandler;
import no.nav.sbl.dialogarena.rest.ressurser.LegacyHelper;
import no.nav.sbl.dialogarena.rest.ressurser.NavnFrontend;
import no.nav.sbl.dialogarena.rest.ressurser.familie.ForsorgerpliktRessurs.ForsorgerpliktFrontend;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sikkerhet.Tilgangskontroll;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.FaktaService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadService;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonData;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKildeBruker;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonNavn;
import no.nav.sbl.soknadsosialhjelp.soknad.familie.*;
import no.nav.sbl.sosialhjelp.domain.SoknadUnderArbeid;
import no.nav.sbl.sosialhjelp.soknadunderbehandling.SoknadUnderArbeidRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ForsorgerpliktRessursTest {

    private static final String BEHANDLINGSID = "123";
    private static final JsonBarn JSON_BARN = new JsonBarn()
            .withNavn(new JsonNavn()
                    .withFornavn("Amadeus")
                    .withMellomnavn("Wolfgang")
                    .withEtternavn("Mozart"))
            .withFodselsdato("1756-01-27")
            .withPersonIdentifikator("***REMOVED***");

    private static final ForsorgerpliktRessurs.BarnFrontend BARN_FRONTEND = new ForsorgerpliktRessurs.BarnFrontend()
            .withNavn(new NavnFrontend("Amadeus", "Wolfgang", "Mozart"))
            .withFodselsdato("1756-01-27")
            .withPersonIdentifikator("***REMOVED***");

    @Mock
    private LegacyHelper legacyHelper;

    @InjectMocks
    private ForsorgerpliktRessurs forsorgerpliktRessurs;

    @Mock
    private Tilgangskontroll tilgangskontroll;

    @Mock
    private SoknadService soknadService;

    @Mock
    private FaktaService faktaService;

    @Mock
    private SoknadUnderArbeidRepository soknadUnderArbeidRepository;

    @Before
    public void setUp() {
        System.setProperty("no.nav.modig.core.context.subjectHandlerImplementationClass", StaticSubjectHandler.class.getName());
    }

    @Test
    public void getForsorgerpliktSkalReturnereTomForsorgerplikt(){
        when(legacyHelper.hentSoknad(anyString(), anyString())).thenReturn(
                createJsonInternalSoknadWithForsorgerplikt(null, null, null));

        final ForsorgerpliktFrontend forsorgerpliktFrontend = forsorgerpliktRessurs.hentForsorgerplikt(BEHANDLINGSID);

        assertThat(forsorgerpliktFrontend.harForsorgerplikt, nullValue());
        assertThat(forsorgerpliktFrontend.barnebidrag, nullValue());
        assertThat(forsorgerpliktFrontend.ansvarFrontends, nullValue());
    }

    @Test
    public void getForsorgerpliktSkalReturnereKunBrukerdefinertStatus(){
    }

    @Test
    public void getForsorgerpliktSkalReturnereBrukerdefinertEktefelleRiktigKonvertert(){
    }

    @Test
    public void getForsorgerpliktSkalReturnereSystemdefinertEktefelleRiktigKonvertert(){

    }

    @Test
    public void getForsorgerpliktSkalReturnereSystemdefinertEktefelleMedDiskresjonskode(){
    }

    @Test
    public void putForsorgerpliktSkalSetteStatusGiftOgEktefelle(){
        ignoreTilgangskontrollAndLegacyUpdate();
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                Optional.of(createJsonInternalSoknadWithForsorgerplikt(null, null, null)));
        
    }



    private SoknadUnderArbeid catchSoknadUnderArbeidSentToOppdaterSoknadsdata() {
        ArgumentCaptor<SoknadUnderArbeid> argument = ArgumentCaptor.forClass(SoknadUnderArbeid.class);
        verify(soknadUnderArbeidRepository, atLeastOnce()).oppdaterSoknadsdata(argument.capture(), anyString());
        return argument.getValue();
    }

    private void ignoreTilgangskontrollAndLegacyUpdate() {
        doNothing().when(tilgangskontroll).verifiserAtBrukerKanEndreSoknad(anyString());
        when(soknadService.hentSoknad(anyString(), anyBoolean(), anyBoolean())).thenReturn(new WebSoknad());
        when(faktaService.hentFaktumMedKey(anyLong(), anyString())).thenReturn(new Faktum());
        when(faktaService.lagreBrukerFaktum(any(Faktum.class))).thenReturn(new Faktum());
    }

    private SoknadUnderArbeid createJsonInternalSoknadWithForsorgerplikt(Boolean harForsorgerplikt, JsonBarnebidrag.Verdi barnebidrag, List<JsonAnsvar> ansvars) {
        return new SoknadUnderArbeid()
                .withJsonInternalSoknad(new JsonInternalSoknad()
                        .withSoknad(new JsonSoknad()
                                .withData(new JsonData()
                                        .withFamilie(new JsonFamilie()
                                                .withForsorgerplikt(new JsonForsorgerplikt()
                                                        .withHarForsorgerplikt(harForsorgerplikt == null ? null :
                                                                new JsonHarForsorgerplikt()
                                                                        .withKilde(JsonKilde.SYSTEM)
                                                                        .withVerdi(harForsorgerplikt))
                                                        .withBarnebidrag(barnebidrag == null ? null :
                                                                new JsonBarnebidrag()
                                                                        .withKilde(JsonKildeBruker.BRUKER)
                                                                        .withVerdi(barnebidrag))
                                                        .withAnsvar(ansvars)
                                                )
                                        )
                                )
                        )
                );
    }

}
