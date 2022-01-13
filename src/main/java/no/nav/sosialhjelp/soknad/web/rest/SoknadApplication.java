package no.nav.sosialhjelp.soknad.web.rest;

import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import no.nav.sosialhjelp.soknad.api.dialog.SistInnsendteSoknadRessurs;
import no.nav.sosialhjelp.soknad.api.dittnav.DittNavMetadataRessurs;
import no.nav.sosialhjelp.soknad.api.featuretoggle.FeatureToggleRessurs;
import no.nav.sosialhjelp.soknad.api.informasjon.InformasjonRessurs;
import no.nav.sosialhjelp.soknad.api.innsyn.SoknadOversiktRessurs;
import no.nav.sosialhjelp.soknad.api.minesaker.MineSakerMetadataRessurs;
import no.nav.sosialhjelp.soknad.api.nedetid.NedetidRessurs;
import no.nav.sosialhjelp.soknad.api.saksoversikt.SaksoversiktMetadataOidcRessurs;
import no.nav.sosialhjelp.soknad.arbeid.ArbeidRessurs;
import no.nav.sosialhjelp.soknad.begrunnelse.BegrunnelseRessurs;
import no.nav.sosialhjelp.soknad.bosituasjon.BosituasjonRessurs;
import no.nav.sosialhjelp.soknad.common.mdc.MdcFilter;
import no.nav.sosialhjelp.soknad.common.rest.feil.ApplicationExceptionMapper;
import no.nav.sosialhjelp.soknad.common.rest.feil.ThrowableMapper;
import no.nav.sosialhjelp.soknad.common.rest.provider.JsonToTextPlainBodyWriter;
import no.nav.sosialhjelp.soknad.common.rest.provider.SoknadObjectMapperProvider;
import no.nav.sosialhjelp.soknad.ettersending.EttersendingRessurs;
import no.nav.sosialhjelp.soknad.health.InternalRessurs;
import no.nav.sosialhjelp.soknad.innsending.SoknadActions;
import no.nav.sosialhjelp.soknad.innsending.SoknadRessurs;
import no.nav.sosialhjelp.soknad.inntekt.andreinntekter.UtbetalingRessurs;
import no.nav.sosialhjelp.soknad.inntekt.formue.FormueRessurs;
import no.nav.sosialhjelp.soknad.inntekt.husbanken.BostotteRessurs;
import no.nav.sosialhjelp.soknad.inntekt.navutbetalinger.SystemregistrertInntektRessurs;
import no.nav.sosialhjelp.soknad.inntekt.skattbarinntekt.SkattbarInntektRessurs;
import no.nav.sosialhjelp.soknad.inntekt.studielan.StudielanRessurs;
import no.nav.sosialhjelp.soknad.inntekt.verdi.VerdiRessurs;
import no.nav.sosialhjelp.soknad.navenhet.NavEnhetRessurs;
import no.nav.sosialhjelp.soknad.okonomiskeopplysninger.OkonomiskeOpplysningerRessurs;
import no.nav.sosialhjelp.soknad.oppsummering.OppsummeringRessurs;
import no.nav.sosialhjelp.soknad.personalia.adresse.AdresseRessurs;
import no.nav.sosialhjelp.soknad.personalia.basispersonalia.BasisPersonaliaRessurs;
import no.nav.sosialhjelp.soknad.personalia.familie.ForsorgerpliktRessurs;
import no.nav.sosialhjelp.soknad.personalia.familie.SivilstatusRessurs;
import no.nav.sosialhjelp.soknad.personalia.kontonummer.KontonummerRessurs;
import no.nav.sosialhjelp.soknad.personalia.telefonnummer.TelefonnummerRessurs;
import no.nav.sosialhjelp.soknad.utdanning.UtdanningRessurs;
import no.nav.sosialhjelp.soknad.utgifter.BarneutgiftRessurs;
import no.nav.sosialhjelp.soknad.utgifter.BoutgiftRessurs;
import no.nav.sosialhjelp.soknad.vedlegg.OpplastetVedleggRessurs;
import no.nav.sosialhjelp.soknad.web.oidc.OidcResourceFilteringFeature;
import no.nav.sosialhjelp.soknad.web.sikkerhet.CORSFilter;
import no.nav.sosialhjelp.soknad.web.sikkerhet.HeaderFilter;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Jersey 2 config
 */
public class SoknadApplication extends ResourceConfig {

    private static final Logger logger = LoggerFactory.getLogger(SoknadApplication.class);

    public SoknadApplication() {
        // JacksonJaxbJsonProvider registreres manuelt for å unngå å dra inn Jacksons egne ExceptionMappers, som
        // returnerer litt for mye informasjon i sine feilmeldinger. Desse ExceptionMappers har @Provider-annotationer
        // og blir automatisk trukket inn hvis du tar tar inn hele Jackson-pakken for JSON.
        packages("no.nav.sosialhjelp.soknad.web.rest");
        // interne
        register(SoknadRessurs.class);
        register(SoknadActions.class);
        register(InternalRessurs.class);
        register(FeatureToggleRessurs.class);
        register(NedetidRessurs.class);
        register(NavEnhetRessurs.class);
        register(OppsummeringRessurs.class);
        register(ArbeidRessurs.class);
        register(StudielanRessurs.class);
        register(BostotteRessurs.class);
        register(FormueRessurs.class);
        register(VerdiRessurs.class);
        register(SystemregistrertInntektRessurs.class);
        register(UtbetalingRessurs.class);
        register(SkattbarInntektRessurs.class);
        register(TelefonnummerRessurs.class);
        register(KontonummerRessurs.class);
        register(InformasjonRessurs.class);
        register(AdresseRessurs.class);
        register(BasisPersonaliaRessurs.class);
        register(BegrunnelseRessurs.class);
        register(BosituasjonRessurs.class);
        register(ForsorgerpliktRessurs.class);
        register(SivilstatusRessurs.class);
        register(UtdanningRessurs.class);
        register(BarneutgiftRessurs.class);
        register(BoutgiftRessurs.class);
        register(OpplastetVedleggRessurs.class);
        register(OkonomiskeOpplysningerRessurs.class);
        register(EttersendingRessurs.class);

        // eksterne
        register(DittNavMetadataRessurs.class);
        register(MineSakerMetadataRessurs.class);
        register(SistInnsendteSoknadRessurs.class);
        register(SoknadOversiktRessurs.class);
        register(SaksoversiktMetadataOidcRessurs.class);

        register(JacksonJaxbJsonProvider.class);
        register(MultiPartFeature.class);
        register(JsonToTextPlainBodyWriter.class);
        register(SoknadObjectMapperProvider.class);

        register(ApplicationExceptionMapper.class);
        register(ThrowableMapper.class);

        // Filters
        register(HeaderFilter.class);
        register(CORSFilter.class);
        register(MdcFilter.class);

        register(OidcResourceFilteringFeature.class);

        logger.info("Starter Jersey");
    }
}
