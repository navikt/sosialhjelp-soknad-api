// package no.nav.sosialhjelp.soknad.app.config
//
// import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider
// import no.nav.sosialhjelp.soknad.api.dittnav.DittNavMetadataRessurs
// import no.nav.sosialhjelp.soknad.api.featuretoggle.FeatureToggleRessurs
// import no.nav.sosialhjelp.soknad.api.informasjon.InformasjonRessurs
// import no.nav.sosialhjelp.soknad.api.innsyn.SoknadOversiktRessurs
// import no.nav.sosialhjelp.soknad.api.minesaker.MineSakerMetadataRessurs
// import no.nav.sosialhjelp.soknad.api.nedetid.NedetidRessurs
// import no.nav.sosialhjelp.soknad.app.filter.CORSFilter
// import no.nav.sosialhjelp.soknad.app.filter.HeaderFilter
// import no.nav.sosialhjelp.soknad.app.filter.MdcFilter
// import no.nav.sosialhjelp.soknad.app.health.InternalRessurs
// import no.nav.sosialhjelp.soknad.app.oidc.OidcResourceFilteringFeature
// import no.nav.sosialhjelp.soknad.app.rest.feil.ApplicationExceptionMapper
// import no.nav.sosialhjelp.soknad.app.rest.feil.ThrowableMapper
// import no.nav.sosialhjelp.soknad.app.rest.provider.JsonToTextPlainBodyWriter
// import no.nav.sosialhjelp.soknad.app.rest.provider.SoknadObjectMapperProvider
// import no.nav.sosialhjelp.soknad.arbeid.ArbeidRessurs
// import no.nav.sosialhjelp.soknad.begrunnelse.BegrunnelseRessurs
// import no.nav.sosialhjelp.soknad.bosituasjon.BosituasjonRessurs
// import no.nav.sosialhjelp.soknad.ettersending.EttersendingRessurs
// import no.nav.sosialhjelp.soknad.innsending.SoknadActions
// import no.nav.sosialhjelp.soknad.innsending.SoknadRessurs
// import no.nav.sosialhjelp.soknad.inntekt.andreinntekter.UtbetalingRessurs
// import no.nav.sosialhjelp.soknad.inntekt.formue.FormueRessurs
// import no.nav.sosialhjelp.soknad.inntekt.husbanken.BostotteRessurs
// import no.nav.sosialhjelp.soknad.inntekt.navutbetalinger.SystemregistrertInntektRessurs
// import no.nav.sosialhjelp.soknad.inntekt.skattbarinntekt.SkattbarInntektRessurs
// import no.nav.sosialhjelp.soknad.inntekt.studielan.StudielanRessurs
// import no.nav.sosialhjelp.soknad.inntekt.verdi.VerdiRessurs
// import no.nav.sosialhjelp.soknad.migration.MigrationFeedRessurs
// import no.nav.sosialhjelp.soknad.navenhet.NavEnhetRessurs
// import no.nav.sosialhjelp.soknad.okonomiskeopplysninger.OkonomiskeOpplysningerRessurs
// import no.nav.sosialhjelp.soknad.oppsummering.OppsummeringRessurs
// import no.nav.sosialhjelp.soknad.personalia.adresse.AdresseRessurs
// import no.nav.sosialhjelp.soknad.personalia.basispersonalia.BasisPersonaliaRessurs
// import no.nav.sosialhjelp.soknad.personalia.familie.ForsorgerpliktRessurs
// import no.nav.sosialhjelp.soknad.personalia.familie.SivilstatusRessurs
// import no.nav.sosialhjelp.soknad.personalia.kontonummer.KontonummerRessurs
// import no.nav.sosialhjelp.soknad.personalia.telefonnummer.TelefonnummerRessurs
// import no.nav.sosialhjelp.soknad.utdanning.UtdanningRessurs
// import no.nav.sosialhjelp.soknad.utgifter.BarneutgiftRessurs
// import no.nav.sosialhjelp.soknad.utgifter.BoutgiftRessurs
// import no.nav.sosialhjelp.soknad.vedlegg.OpplastetVedleggRessurs
// import org.glassfish.jersey.media.multipart.MultiPartFeature
// import org.glassfish.jersey.server.ResourceConfig
// import org.slf4j.LoggerFactory
// import org.springframework.context.annotation.Configuration
//
// /**
//  * Jersey 2 config
//  */
// @Configuration
// open class SoknadApplication : ResourceConfig() {
//
//    init {
//        // JacksonJaxbJsonProvider registreres manuelt for å unngå å dra inn Jacksons egne ExceptionMappers, som
//        // returnerer litt for mye informasjon i sine feilmeldinger. Desse ExceptionMappers har @Provider-annotationer
//        // og blir automatisk trukket inn hvis du tar tar inn hele Jackson-pakken for JSON.
//
//        // interne
//        this.register(SoknadRessurs::class.java)
//        this.register(SoknadActions::class.java)
//        this.register(InternalRessurs::class.java)
//        this.register(FeatureToggleRessurs::class.java)
//        this.register(NedetidRessurs::class.java)
//        this.register(NavEnhetRessurs::class.java)
//        this.register(OppsummeringRessurs::class.java)
//        this.register(ArbeidRessurs::class.java)
//        this.register(StudielanRessurs::class.java)
//        this.register(BostotteRessurs::class.java)
//        this.register(FormueRessurs::class.java)
//        this.register(VerdiRessurs::class.java)
//        this.register(SystemregistrertInntektRessurs::class.java)
//        this.register(UtbetalingRessurs::class.java)
//        this.register(SkattbarInntektRessurs::class.java)
//        this.register(TelefonnummerRessurs::class.java)
//        this.register(KontonummerRessurs::class.java)
//        this.register(InformasjonRessurs::class.java)
//        this.register(AdresseRessurs::class.java)
//        this.register(BasisPersonaliaRessurs::class.java)
//        this.register(BegrunnelseRessurs::class.java)
//        this.register(BosituasjonRessurs::class.java)
//        this.register(ForsorgerpliktRessurs::class.java)
//        this.register(SivilstatusRessurs::class.java)
//        this.register(UtdanningRessurs::class.java)
//        this.register(BarneutgiftRessurs::class.java)
//        this.register(BoutgiftRessurs::class.java)
//        this.register(OpplastetVedleggRessurs::class.java)
//        this.register(OkonomiskeOpplysningerRessurs::class.java)
//        this.register(EttersendingRessurs::class.java)
//
//        // eksterne
//        this.register(DittNavMetadataRessurs::class.java)
//        this.register(MineSakerMetadataRessurs::class.java)
//        this.register(SoknadOversiktRessurs::class.java)
//        this.register(JacksonJaxbJsonProvider::class.java)
//        this.register(MultiPartFeature::class.java)
//        this.register(JsonToTextPlainBodyWriter::class.java)
//        this.register(SoknadObjectMapperProvider::class.java)
//        this.register(ApplicationExceptionMapper::class.java)
//        this.register(ThrowableMapper::class.java)
//
//        // Filters
//        this.register(HeaderFilter::class.java)
//        this.register(CORSFilter::class.java)
//        this.register(MdcFilter::class.java)
//        this.register(OidcResourceFilteringFeature::class.java)
//
//        // Migration-api
//        this.register(MigrationFeedRessurs::class.java)
//
//        logger.info("Starter Jersey")
//    }
//
//    companion object {
//        private val logger = LoggerFactory.getLogger(SoknadApplication::class.java)
//    }
// }
