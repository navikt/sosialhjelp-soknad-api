package no.nav.sosialhjelp.soknad.business.service.soknadservice;

import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sosialhjelp.soknad.business.SoknadServiceIntegrationTestContext;
import no.nav.sosialhjelp.soknad.business.db.repositories.soknadmetadata.SoknadMetadataRepository;
import no.nav.sosialhjelp.soknad.business.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository;
import no.nav.sosialhjelp.soknad.business.domain.SoknadMetadata;
import no.nav.sosialhjelp.soknad.business.pdfmedpdfbox.SosialhjelpPdfGenerator;
import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeid;
import no.nav.sosialhjelp.soknad.domain.model.oidc.StaticSubjectHandlerService;
import no.nav.sosialhjelp.soknad.domain.model.oidc.SubjectHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.inject.Inject;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
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

    @BeforeEach
    public void setUp() {
        System.setProperty("environment.name", "test");
        SubjectHandler.setSubjectHandlerService(new StaticSubjectHandlerService());
        when(soknadMetadataRepository.hent(anyString())).thenReturn(new SoknadMetadata());
        when(soknadUnderArbeidRepository.hentSoknadOptional(anyString(), anyString())).thenReturn(Optional.of(new SoknadUnderArbeid().withBehandlingsId(EN_BEHANDLINGSID).withVersjon(0L)));
        when(sosialhjelpPdfGenerator.generate(any(JsonInternalSoknad.class), anyBoolean())).thenReturn(new byte[]{1, 2, 3});
    }

    @AfterEach
    public void tearDown() {
        SubjectHandler.resetOidcSubjectHandlerService();
        System.clearProperty("environment.name");
    }

    @Test
    public void avbrytSoknadSletterSoknadenFraLokalDb() {
        soknadService.avbrytSoknad(EN_BEHANDLINGSID);

        verify(soknadUnderArbeidRepository).slettSoknad(any(SoknadUnderArbeid.class), anyString());
    }

}