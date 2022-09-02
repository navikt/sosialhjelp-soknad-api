package no.nav.sosialhjelp.soknad.app.config

import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider
import no.nav.sosialhjelp.soknad.api.dialog.SistInnsendteSoknadRessurs
import no.nav.sosialhjelp.soknad.api.featuretoggle.FeatureToggleRessurs
import no.nav.sosialhjelp.soknad.api.informasjon.InformasjonRessurs
import no.nav.sosialhjelp.soknad.api.innsyn.SoknadOversiktRessurs
import no.nav.sosialhjelp.soknad.api.minesaker.MineSakerMetadataRessurs
import no.nav.sosialhjelp.soknad.api.minside.MinSideMetadataRessurs
import no.nav.sosialhjelp.soknad.api.nedetid.NedetidRessurs
import no.nav.sosialhjelp.soknad.app.filter.CORSFilter
import no.nav.sosialhjelp.soknad.app.filter.HeaderFilter
import no.nav.sosialhjelp.soknad.app.filter.MdcFilter
import no.nav.sosialhjelp.soknad.app.health.InternalRessurs
import no.nav.sosialhjelp.soknad.app.oidc.OidcResourceFilteringFeature
import no.nav.sosialhjelp.soknad.app.rest.feil.ApplicationExceptionMapper
import no.nav.sosialhjelp.soknad.app.rest.feil.ThrowableMapper
import no.nav.sosialhjelp.soknad.app.rest.provider.JsonToTextPlainBodyWriter
import no.nav.sosialhjelp.soknad.app.rest.provider.SoknadObjectMapperProvider
import no.nav.sosialhjelp.soknad.arbeid.ArbeidRessurs
import no.nav.sosialhjelp.soknad.begrunnelse.BegrunnelseRessurs
import no.nav.sosialhjelp.soknad.bosituasjon.BosituasjonRessurs
import no.nav.sosialhjelp.soknad.ettersending.EttersendingRessurs
import no.nav.sosialhjelp.soknad.innsending.SoknadActions
import no.nav.sosialhjelp.soknad.innsending.SoknadRessurs
import no.nav.sosialhjelp.soknad.inntekt.andreinntekter.UtbetalingRessurs
import no.nav.sosialhjelp.soknad.inntekt.formue.FormueRessurs
import no.nav.sosialhjelp.soknad.inntekt.husbanken.BostotteRessurs
import no.nav.sosialhjelp.soknad.inntekt.navutbetalinger.SystemregistrertInntektRessurs
import no.nav.sosialhjelp.soknad.inntekt.skattbarinntekt.SkattbarInntektRessurs
import no.nav.sosialhjelp.soknad.inntekt.studielan.StudielanRessurs
import no.nav.sosialhjelp.soknad.inntekt.verdi.VerdiRessurs
import no.nav.sosialhjelp.soknad.migration.MigrationFeedRessurs
import no.nav.sosialhjelp.soknad.navenhet.NavEnhetRessurs
import no.nav.sosialhjelp.soknad.okonomiskeopplysninger.OkonomiskeOpplysningerRessurs
import no.nav.sosialhjelp.soknad.oppsummering.OppsummeringRessurs
import no.nav.sosialhjelp.soknad.personalia.adresse.AdresseRessurs
import no.nav.sosialhjelp.soknad.personalia.basispersonalia.BasisPersonaliaRessurs
import no.nav.sosialhjelp.soknad.personalia.familie.ForsorgerpliktRessurs
import no.nav.sosialhjelp.soknad.personalia.familie.SivilstatusRessurs
import no.nav.sosialhjelp.soknad.personalia.kontonummer.KontonummerRessurs
import no.nav.sosialhjelp.soknad.personalia.telefonnummer.TelefonnummerRessurs
import no.nav.sosialhjelp.soknad.utdanning.UtdanningRessurs
import no.nav.sosialhjelp.soknad.utgifter.BarneutgiftRessurs
import no.nav.sosialhjelp.soknad.utgifter.BoutgiftRessurs
import no.nav.sosialhjelp.soknad.vedlegg.OpplastetVedleggRessurs
import org.glassfish.jersey.media.multipart.MultiPartFeature
import org.glassfish.jersey.server.ResourceConfig
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Configuration

/**
 * Jersey 2 config
 */
@Configuration
open class SoknadApplication : ResourceConfig() {

    init {
        // JacksonJaxbJsonProvider registreres manuelt for å unngå å dra inn Jacksons egne ExceptionMappers, som
        // returnerer litt for mye informasjon i sine feilmeldinger. Desse ExceptionMappers har @Provider-annotationer
        // og blir automatisk trukket inn hvis du tar tar inn hele Jackson-pakken for JSON.

        // interne
        register(SoknadRessurs::class.java)
        register(SoknadActions::class.java)
        register(InternalRessurs::class.java)
        register(FeatureToggleRessurs::class.java)
        register(NedetidRessurs::class.java)
        register(NavEnhetRessurs::class.java)
        register(OppsummeringRessurs::class.java)
        register(ArbeidRessurs::class.java)
        register(StudielanRessurs::class.java)
        register(BostotteRessurs::class.java)
        register(FormueRessurs::class.java)
        register(VerdiRessurs::class.java)
        register(SystemregistrertInntektRessurs::class.java)
        register(UtbetalingRessurs::class.java)
        register(SkattbarInntektRessurs::class.java)
        register(TelefonnummerRessurs::class.java)
        register(KontonummerRessurs::class.java)
        register(InformasjonRessurs::class.java)
        register(AdresseRessurs::class.java)
        register(BasisPersonaliaRessurs::class.java)
        register(BegrunnelseRessurs::class.java)
        register(BosituasjonRessurs::class.java)
        register(ForsorgerpliktRessurs::class.java)
        register(SivilstatusRessurs::class.java)
        register(UtdanningRessurs::class.java)
        register(BarneutgiftRessurs::class.java)
        register(BoutgiftRessurs::class.java)
        register(OpplastetVedleggRessurs::class.java)
        register(OkonomiskeOpplysningerRessurs::class.java)
        register(EttersendingRessurs::class.java)

        // eksterne
        register(MinSideMetadataRessurs::class.java)
        register(MineSakerMetadataRessurs::class.java)
        register(SistInnsendteSoknadRessurs::class.java)
        register(SoknadOversiktRessurs::class.java)

        register(JacksonJaxbJsonProvider::class.java)
        register(MultiPartFeature::class.java)
        register(JsonToTextPlainBodyWriter::class.java)
        register(SoknadObjectMapperProvider::class.java)
        register(ApplicationExceptionMapper::class.java)
        register(ThrowableMapper::class.java)

        // Filters
        register(HeaderFilter::class.java)
        register(CORSFilter::class.java)
        register(MdcFilter::class.java)
        register(OidcResourceFilteringFeature::class.java)

        // Migration-api
        register(MigrationFeedRessurs::class.java)

        logger.info("Starter Jersey")
    }

    companion object {
        private val logger = LoggerFactory.getLogger(SoknadApplication::class.java)
    }
}
