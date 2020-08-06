package no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice;

import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.SubjectHandler;
import no.nav.sbl.dialogarena.soknadinnsending.business.SoknadServiceIntegrationTestContext;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknadmetadata.SoknadMetadataRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.SoknadMetadata;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.sosialhjelp.domain.SoknadUnderArbeid;
import no.nav.sbl.sosialhjelp.pdfmedpdfbox.SosialhjelpPdfGenerator;
import no.nav.sbl.sosialhjelp.soknadunderbehandling.SoknadUnderArbeidRepository;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
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

    @Inject
    private SosialhjelpPdfGenerator sosialhjelpPdfGenerator;

    @Inject
    private SubjectHandler subjectHandler;

    @BeforeClass
    public static void beforeClass() {
        System.setProperty("soknad.feature.foreldrepenger.alternativrepresentasjon.enabled", "true");
    }

    @Before
    public void setUp() {
        when(subjectHandler.getIdent()).thenReturn("123");
        when(soknadMetadataRepository.hent(anyString())).thenReturn(new SoknadMetadata());
        when(soknadUnderArbeidRepository.hentSoknadOptional(anyString(), anyString())).thenReturn(Optional.of(new SoknadUnderArbeid().withBehandlingsId(EN_BEHANDLINGSID).withVersjon(0L)));
        when(sosialhjelpPdfGenerator.generate(any(JsonInternalSoknad.class), anyBoolean())).thenReturn(new byte[]{1, 2, 3});
    }

    @Test
    public void avbrytSoknadSletterSoknadenFraLokalDb() {
        soknadService.avbrytSoknad(EN_BEHANDLINGSID);

        verify(soknadUnderArbeidRepository).slettSoknad(any(SoknadUnderArbeid.class), anyString());
    }

}