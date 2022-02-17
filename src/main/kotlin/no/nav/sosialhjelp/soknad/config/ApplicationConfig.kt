package no.nav.sosialhjelp.soknad.config

import no.nav.sosialhjelp.soknad.api.dialog.DialogApiConfig
import no.nav.sosialhjelp.soknad.api.dittnav.DittNavConfig
import no.nav.sosialhjelp.soknad.api.featuretoggle.FeatureToggleConfig
import no.nav.sosialhjelp.soknad.api.informasjon.InformasjonConfig
import no.nav.sosialhjelp.soknad.api.innsyn.SoknadOversiktConfig
import no.nav.sosialhjelp.soknad.api.minesaker.MineSakerConfig
import no.nav.sosialhjelp.soknad.api.nedetid.NedetidConfig
import no.nav.sosialhjelp.soknad.api.saksoversikt.SaksoversiktConfig
import no.nav.sosialhjelp.soknad.begrunnelse.BegrunnelseConfig
import no.nav.sosialhjelp.soknad.bosituasjon.BosituasjonConfig
import no.nav.sosialhjelp.soknad.business.pdfmedpdfbox.PdfGenerator
import no.nav.sosialhjelp.soknad.business.pdfmedpdfbox.SosialhjelpPdfGenerator
import no.nav.sosialhjelp.soknad.client.ClientConfig
import no.nav.sosialhjelp.soknad.client.tokenx.TokendingsConfig
import no.nav.sosialhjelp.soknad.common.ServiceUtils
import no.nav.sosialhjelp.soknad.common.filter.FilterConfig
import no.nav.sosialhjelp.soknad.common.oidc.OidcTokenValidatorConfig
import no.nav.sosialhjelp.soknad.common.rest.feil.ApplicationExceptionMapper
import no.nav.sosialhjelp.soknad.common.rest.feil.ThrowableMapper
import no.nav.sosialhjelp.soknad.common.systemdata.SystemdataUpdater
import no.nav.sosialhjelp.soknad.ettersending.EttersendingConfig
import no.nav.sosialhjelp.soknad.health.HealthConfig
import no.nav.sosialhjelp.soknad.health.selftest.SelftestService
import no.nav.sosialhjelp.soknad.innsending.InnsendingConfig
import no.nav.sosialhjelp.soknad.inntekt.InntektConfig
import no.nav.sosialhjelp.soknad.metrics.MetricsConfig
import no.nav.sosialhjelp.soknad.okonomiskeopplysninger.OkonomiskeOpplysningerConfig
import no.nav.sosialhjelp.soknad.pdf.PdfUtils
import no.nav.sosialhjelp.soknad.pdf.TextHelpers
import no.nav.sosialhjelp.soknad.personalia.adresse.AdresseConfig
import no.nav.sosialhjelp.soknad.personalia.basispersonalia.BasisPersonaliaConfig
import no.nav.sosialhjelp.soknad.personalia.familie.FamilieConfig
import no.nav.sosialhjelp.soknad.scheduled.SchedulerConfig
import no.nav.sosialhjelp.soknad.tekster.TextService
import no.nav.sosialhjelp.soknad.tilgangskontroll.TilgangskontrollConfig
import no.nav.sosialhjelp.soknad.utdanning.UtdanningConfig
import no.nav.sosialhjelp.soknad.utgifter.UtgifterConfig
import no.nav.sosialhjelp.soknad.vedlegg.VedleggConfig
import no.nav.sosialhjelp.soknad.web.config.ContentConfig
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.EnableAspectJAutoProxy
import org.springframework.context.annotation.Import

@EnableAspectJAutoProxy
@Configuration
@Import(
    ClientConfig::class,
    ContentConfig::class,
    OidcTokenValidatorConfig::class,
    SelftestService::class,
    InnsendingConfig::class,
    InntektConfig::class,
    DittNavConfig::class,
    MineSakerConfig::class,
    DialogApiConfig::class,
    SoknadOversiktConfig::class,
    InformasjonConfig::class,
    AdresseConfig::class,
    BasisPersonaliaConfig::class,
    FamilieConfig::class,
    BegrunnelseConfig::class,
    BosituasjonConfig::class,
    UtdanningConfig::class,
    UtgifterConfig::class,
    VedleggConfig::class,
    OkonomiskeOpplysningerConfig::class,
    EttersendingConfig::class,
    HealthConfig::class,
    FeatureToggleConfig::class,
    NedetidConfig::class,
    SaksoversiktConfig::class,
    InnsendingConfig::class,
    TilgangskontrollConfig::class,
    SchedulerConfig::class,
    MetricsConfig::class,
    ApplicationExceptionMapper::class,
    ThrowableMapper::class,
    ServiceUtils::class,
    TextService::class,
    SystemdataUpdater::class,
    DbConfig::class,
    PdfGenerator::class,
    TextHelpers::class,
    PdfUtils::class,
    SosialhjelpPdfGenerator::class,
    FilterConfig::class,
    TokendingsConfig::class
)
open class ApplicationConfig
