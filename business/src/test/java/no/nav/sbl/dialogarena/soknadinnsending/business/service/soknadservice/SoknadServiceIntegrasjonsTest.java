package no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice;

import no.nav.modig.core.context.ThreadLocalSubjectHandler;
import no.nav.sbl.dialogarena.soknadinnsending.business.SoknadServiceIntegrationTestContext;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknadmetadata.SoknadMetadataRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.SoknadMetadata;
import no.nav.sbl.sosialhjelp.domain.SoknadUnderArbeid;
import no.nav.sbl.sosialhjelp.soknadunderbehandling.SoknadUnderArbeidRepository;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import java.util.Optional;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = SoknadServiceIntegrationTestContext.class)
public class SoknadServiceIntegrasjonsTest {
    private final String EN_BEHANDLINGSID = "EN_BEHANDLINGSID";

    @Inject
    private SoknadService soknadService;

    @Inject
    private SoknadMetadataRepository soknadMetadataRepository;

    @Inject
    private SoknadUnderArbeidRepository soknadUnderArbeidRepository;

    @BeforeClass
    public static void beforeClass() {
        System.setProperty("soknad.feature.foreldrepenger.alternativrepresentasjon.enabled", "true");
    }

    @Before
    public void setUp() {
        System.setProperty("no.nav.modig.core.context.subjectHandlerImplementationClass", ThreadLocalSubjectHandler.class.getName());
        when(soknadMetadataRepository.hent(anyString())).thenReturn(new SoknadMetadata());
        when(soknadUnderArbeidRepository.hentSoknadOptional(anyString(), anyString())).thenReturn(Optional.of(new SoknadUnderArbeid().withVersjon(0L)));
    }

    @Test
    public void avbrytSoknadSletterSoknadenFraLokalDb() {
        soknadService.avbrytSoknad(EN_BEHANDLINGSID);

        verify(soknadUnderArbeidRepository).slettSoknad(any(SoknadUnderArbeid.class), anyString());
    }

}