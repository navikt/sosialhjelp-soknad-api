package no.nav.sbl.dialogarena.rest.ressurser.familie;

import no.nav.modig.core.context.StaticSubjectHandler;
import no.nav.sbl.dialogarena.rest.ressurser.LegacyHelper;
import no.nav.sbl.dialogarena.sikkerhet.Tilgangskontroll;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.FaktaService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadService;
import no.nav.sbl.sosialhjelp.soknadunderbehandling.SoknadUnderArbeidRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import static no.nav.modig.core.context.SubjectHandler.SUBJECTHANDLER_KEY;
import static no.nav.sbl.dialogarena.sendsoknad.domain.oidc.OidcFeatureToggleUtils.IS_RUNNING_WITH_OIDC;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ForsorgerpliktRessursUtenOidcTest {

    @Mock
    private LegacyHelper legacyHelper;

    @Mock
    private Tilgangskontroll tilgangskontroll;

    @Mock
    private SoknadService soknadService;

    @Mock
    private FaktaService faktaService;

    @Mock
    private SoknadUnderArbeidRepository soknadUnderArbeidRepository;

    @InjectMocks
    private ForsorgerpliktRessurs forsorgerpliktRessurs = spy(new ForsorgerpliktRessurs());

    @InjectMocks
    private ForsorgerpliktRessursTest forsorgerpliktRessursTest;

    @Before
    public void setUp() {
        System.setProperty(SUBJECTHANDLER_KEY, StaticSubjectHandler.class.getName());
        System.setProperty(IS_RUNNING_WITH_OIDC, "false");
    }

    @After
    public void tearDown() {
        System.clearProperty(SUBJECTHANDLER_KEY);
    }

    @Test
    public void getForsorgerpliktSkalReturnereTomForsorgerplikt(){
        forsorgerpliktRessursTest.getForsorgerpliktSkalReturnereTomForsorgerplikt();
    }

    @Test
    public void getForsorgerpliktSkalReturnereEtBarnSomErFolkeregistrertSammenOgHarDeltBosted(){
        forsorgerpliktRessursTest.getForsorgerpliktSkalReturnereEtBarnSomErFolkeregistrertSammenOgHarDeltBosted();
    }

    @Test
    public void getForsorgerpliktSkalReturnereEtBarnSomIkkeErFolkeregistrertSammenMenHarSamvarsgrad(){
        forsorgerpliktRessursTest.getForsorgerpliktSkalReturnereEtBarnSomIkkeErFolkeregistrertSammenMenHarSamvarsgrad();
    }

    @Test
    public void getForsorgerpliktSkalReturnereToBarn(){
        forsorgerpliktRessursTest.getForsorgerpliktSkalReturnereToBarn();
    }

    @Test
    public void getForsorgerpliktSkalReturnereEtBarnOgBarnebidrag(){
        forsorgerpliktRessursTest.getForsorgerpliktSkalReturnereEtBarnOgBarnebidrag();
    }

    @Test
    public void getForsorgerpliktSkalReturnereEtBarnMedDiskresjonskode(){
        forsorgerpliktRessursTest.getForsorgerpliktSkalReturnereEtBarnMedDiskresjonskode();
    }

    @Test
    public void putForsorgerpliktSkalSetteBarnebidrag(){
        forsorgerpliktRessursTest.putForsorgerpliktSkalSetteBarnebidrag();
    }

    @Test
    public void putForsorgerpliktSkalKunneSetteBarnebidragForBarnMedDiskresjonskode(){
        forsorgerpliktRessursTest.putForsorgerpliktSkalKunneSetteBarnebidragForBarnMedDiskresjonskode();
    }

    @Test
    public void putForsorgerpliktSkalSetteHarDeltBostedOgSamvarsgradPaaToBarn(){
        forsorgerpliktRessursTest.putForsorgerpliktSkalSetteHarDeltBostedOgSamvarsgradPaaToBarn();
    }
}
