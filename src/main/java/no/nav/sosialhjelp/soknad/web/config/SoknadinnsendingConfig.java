package no.nav.sosialhjelp.soknad.web.config;

import no.nav.sosialhjelp.soknad.api.dialog.DialogApiConfig;
import no.nav.sosialhjelp.soknad.api.dittnav.DittNavConfig;
import no.nav.sosialhjelp.soknad.api.featuretoggle.FeatureToggleConfig;
import no.nav.sosialhjelp.soknad.api.informasjon.InformasjonConfig;
import no.nav.sosialhjelp.soknad.api.innsyn.SoknadOversiktConfig;
import no.nav.sosialhjelp.soknad.api.minesaker.MineSakerConfig;
import no.nav.sosialhjelp.soknad.api.nedetid.NedetidConfig;
import no.nav.sosialhjelp.soknad.api.saksoversikt.SaksoversiktConfig;
import no.nav.sosialhjelp.soknad.begrunnelse.BegrunnelseConfig;
import no.nav.sosialhjelp.soknad.bosituasjon.BosituasjonConfig;
import no.nav.sosialhjelp.soknad.business.BusinessConfig;
import no.nav.sosialhjelp.soknad.consumer.ConsumerConfig;
import no.nav.sosialhjelp.soknad.ettersending.EttersendingConfig;
import no.nav.sosialhjelp.soknad.health.HealthConfig;
import no.nav.sosialhjelp.soknad.health.selftest.SelftestService;
import no.nav.sosialhjelp.soknad.innsending.InnsendingConfig;
import no.nav.sosialhjelp.soknad.inntekt.InntektConfig;
import no.nav.sosialhjelp.soknad.metrics.MetricsConfig;
import no.nav.sosialhjelp.soknad.okonomiskeopplysninger.OkonomiskeOpplysningerConfig;
import no.nav.sosialhjelp.soknad.personalia.adresse.AdresseConfig;
import no.nav.sosialhjelp.soknad.personalia.basispersonalia.BasisPersonaliaConfig;
import no.nav.sosialhjelp.soknad.personalia.familie.FamilieConfig;
import no.nav.sosialhjelp.soknad.scheduled.SchedulerConfig;
import no.nav.sosialhjelp.soknad.tilgangskontroll.TilgangskontrollConfig;
import no.nav.sosialhjelp.soknad.utdanning.UtdanningConfig;
import no.nav.sosialhjelp.soknad.utgifter.UtgifterConfig;
import no.nav.sosialhjelp.soknad.vedlegg.VedleggConfig;
import no.nav.sosialhjelp.soknad.web.oidc.OidcTokenValidatorConfig;
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
        SelftestService.class,
        InnsendingConfig.class,
        InntektConfig.class,
        DittNavConfig.class,
        MineSakerConfig.class,
        DialogApiConfig.class,
        SoknadOversiktConfig.class,
        InformasjonConfig.class,
        AdresseConfig.class,
        BasisPersonaliaConfig.class,
        FamilieConfig.class,
        BegrunnelseConfig.class,
        BosituasjonConfig.class,
        UtdanningConfig.class,
        UtgifterConfig.class,
        VedleggConfig.class,
        OkonomiskeOpplysningerConfig.class,
        EttersendingConfig.class,
        HealthConfig.class,
        FeatureToggleConfig.class,
        NedetidConfig.class,
        SaksoversiktConfig.class,
        InnsendingConfig.class,
        TilgangskontrollConfig.class,
        SchedulerConfig.class,
        MetricsConfig.class
})
@ComponentScan(basePackages = "no.nav.sosialhjelp.soknad.web.rest")
public class SoknadinnsendingConfig {

}