package no.nav.sosialhjelp.soknad.web.config;

import no.nav.sosialhjelp.soknad.api.dialog.DialogApiConfig;
import no.nav.sosialhjelp.soknad.api.dittnav.DittNavConfig;
import no.nav.sosialhjelp.soknad.api.minesaker.MineSakerConfig;
import no.nav.sosialhjelp.soknad.business.BusinessConfig;
import no.nav.sosialhjelp.soknad.consumer.ConsumerConfig;
import no.nav.sosialhjelp.soknad.inntekt.InntektConfig;
import no.nav.sosialhjelp.soknad.web.oidc.OidcTokenValidatorConfig;
import no.nav.sosialhjelp.soknad.web.selftest.SelftestService;
import no.nav.sosialhjelp.soknad.web.service.SaksoversiktMetadataService;
import no.nav.sosialhjelp.soknad.web.service.SoknadOversiktService;
import no.nav.sosialhjelp.soknad.web.sikkerhet.Tilgangskontroll;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;

@EnableAspectJAutoProxy
@Configuration
@Import({
        BusinessConfig.class,
        ConsumerConfig.class,
        ContentConfig.class,
        OidcTokenValidatorConfig.class,
        MetricsConfiguration.class,
        SaksoversiktMetadataService.class,
        SoknadOversiktService.class,
        SelftestService.class,
        Tilgangskontroll.class,
        InntektConfig.class,
        DittNavConfig.class,
        MineSakerConfig.class,
        DialogApiConfig.class
})
@ComponentScan(basePackages = "no.nav.sosialhjelp.soknad.web.rest")
public class SoknadinnsendingConfig {

}