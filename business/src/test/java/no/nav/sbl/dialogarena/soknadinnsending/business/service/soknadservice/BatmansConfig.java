package no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice;

import no.nav.sbl.dialogarena.soknadinnsending.business.db.vedlegg.*;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.*;
import org.mockito.*;
import org.springframework.context.annotation.*;

import javax.inject.*;

@Configuration
@Import({BatmansDummyConfig.class})
public class BatmansConfig {

    @Bean
    public SoknadDataFletter soknadDataFletter() {
        return Mockito.mock(SoknadDataFletter.class);
    }

    @Bean
    public VedleggService vedleggService() {
        return new VedleggService();

    }

    @Bean
    @Named("vedleggRepository")
    public VedleggRepository vedleggRepository() {
        return new VedleggRepositoryJdbc();

    }

    @Bean
    public FaktaService faktaService() {
        return new FaktaService();
    }

}
