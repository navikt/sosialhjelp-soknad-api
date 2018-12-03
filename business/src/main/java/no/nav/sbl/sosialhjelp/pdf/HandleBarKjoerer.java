package no.nav.sbl.sosialhjelp.pdf;

import com.github.jknack.handlebars.*;
import com.github.jknack.handlebars.cache.ConcurrentMapTemplateCache;
import com.github.jknack.handlebars.context.*;
import no.bekk.bekkopen.person.Fodselsnummer;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.oppsett.SoknadStruktur;
import no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp.SosialhjelpTilJson;
import no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp.SosialhjelpVedleggTilJson;
import no.nav.sbl.dialogarena.soknadinnsending.business.WebSoknadConfig;
import no.nav.sbl.dialogarena.soknadsosialhjelp.message.NavMessageSource;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.internal.JsonSoknadsmottaker;
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedleggSpesifikasjon;
import no.nav.sbl.sosialhjelp.domain.SoknadUnderArbeid;
import no.nav.sbl.sosialhjelp.pdf.oppsummering.OppsummeringsContext;

import javax.inject.Inject;
import java.io.IOException;
import java.util.*;

import static no.bekk.bekkopen.person.FodselsnummerValidator.getFodselsnummer;
import static org.apache.commons.lang3.ArrayUtils.reverse;
import static org.apache.commons.lang3.StringUtils.join;
import static org.apache.commons.lang3.StringUtils.split;
import static org.slf4j.LoggerFactory.getLogger;

@SuppressWarnings({"PMD.TooManyMethods", "PMD.ExcessiveClassLength"})
public class HandleBarKjoerer implements HtmlGenerator, HandlebarRegistry {

    private Map<String, Helper> helpers = new HashMap<>();

    @Inject
    private WebSoknadConfig webSoknadConfig;
    
    @Inject
    private NavMessageSource messageSource;
    
    @Inject
    private WebSoknadConfig config;

    public String fyllHtmlMalMedInnhold(WebSoknad soknad, String file) throws IOException {
        return getHandlebars()
                .compile(file)
                .apply(Context.newBuilder(soknad).build());
    }

    public String fyllHtmlMalMedInnhold(WebSoknad soknad) throws IOException {
        return fyllHtmlMalMedInnhold(soknad, false);
    }

    public String fyllHtmlMalMedInnhold(JsonInternalSoknad jsonInternalSoknad) throws IOException {
        return fyllHtmlMalMedInnhold(jsonInternalSoknad, false);
    }

    public String fyllHtmlMalMedInnhold(JsonInternalSoknad jsonInternalSoknad, boolean utvidetSoknad) throws IOException {
        final HandlebarContext context = new HandlebarContext(jsonInternalSoknad, utvidetSoknad, false);
        return getHandlebars()
                .infiniteLoops(true)
                .compile("/skjema/ny_generisk")
                .apply(Context.newBuilder(context)
                        .resolver(
                                JavaBeanValueResolver.INSTANCE,
                                FieldValueResolver.INSTANCE,
                                MapValueResolver.INSTANCE,
                                MethodValueResolver.INSTANCE
                        )
                        .build());
    }
    
    @Override
    public String fyllHtmlMalMedInnhold(WebSoknad soknad, boolean utvidetSoknad) throws IOException {
        try {
            //oppsummering saksbehandlerpdf, innsending
            SoknadStruktur soknadStruktur = webSoknadConfig.hentStruktur(soknad.getskjemaNummer());
            OppsummeringsContext context = new OppsummeringsContext(soknad, soknadStruktur, utvidetSoknad);
            return getHandlebars()
                    .infiniteLoops(true)
                    .compile("/skjema/generisk")
                    .apply(Context.newBuilder(context)
                            .resolver(
                                    JavaBeanValueResolver.INSTANCE,
                                    FieldValueResolver.INSTANCE,
                                    MapValueResolver.INSTANCE,
                                    MethodValueResolver.INSTANCE
                            )
                            .build());
        }catch (IllegalArgumentException e){
            getLogger(HandleBarKjoerer.class).warn("catch IllegalArgumentException " + e.getMessage()
                    + " -  Søknad med skjemanr: " + soknad.getskjemaNummer() + "har faktum med ugyldig datoverdi."
                    + " -  BehandlingId: " + soknad.getBrukerBehandlingId());
            throw e;
        }
    }
    
    @Override
    public String genererHtmlForPdf(WebSoknad soknad, String file, boolean erEttersending) throws IOException {
        final JsonInternalSoknad internalSoknad = legacyGenererJsonInternalSoknad(soknad);
        return genererHtmlForPdf(internalSoknad, file, erEttersending);
    }
    
    @Override
    public String genererHtmlForPdf(JsonInternalSoknad internalSoknad, String file, boolean erEttersending) throws IOException {
        final HandlebarContext context = new HandlebarContext(internalSoknad, false, erEttersending);
        
        return getHandlebars()
                .compile(file)
                .apply(Context.newBuilder(context).build());
    }

    @Override
    public String genererHtmlForPdf(WebSoknad soknad, boolean utvidetSoknad) throws IOException {
        try {
            //saksbehandlerpdf underveis
            final JsonInternalSoknad internalSoknad = legacyGenererJsonInternalSoknad(soknad);
            return genererHtmlForPdf(internalSoknad, utvidetSoknad);
        }catch (IllegalArgumentException e){
                    getLogger(HandleBarKjoerer.class).warn("catch IllegalArgumentException " + e.getMessage()
                    + " -  Søknad med skjemanr: " + soknad.getskjemaNummer() + "har faktum med ugyldig datoverdi."
                    + " -  BehandlingId: " + soknad.getBrukerBehandlingId());
            throw e;
        }
    }
            
    @Override
    public String genererHtmlForPdf(JsonInternalSoknad internalSoknad, boolean utvidetSoknad) throws IOException {
        final HandlebarContext context = new HandlebarContext(internalSoknad, utvidetSoknad, false);
        
        return getHandlebars()
                .infiniteLoops(true)
                .compile("/skjema/ny_generisk")
                .apply(Context.newBuilder(context)
                        .resolver(
                                JavaBeanValueResolver.INSTANCE,
                                FieldValueResolver.INSTANCE,
                                MapValueResolver.INSTANCE,
                                MethodValueResolver.INSTANCE
                        )
                        .build());
    }

    private JsonInternalSoknad legacyGenererJsonInternalSoknad(WebSoknad soknad) {
        final SosialhjelpTilJson sosialhjelpTilJson = new SosialhjelpTilJson(messageSource);
        soknad.fjernFaktaSomIkkeSkalVaereSynligISoknaden(config.hentStruktur(soknad.getskjemaNummer()));
        
        final SosialhjelpVedleggTilJson sosialhjelpVedleggTilJson = new SosialhjelpVedleggTilJson();
        
        final JsonSoknad jsonSoknad = sosialhjelpTilJson.toJsonSoknad(soknad);
        final JsonVedleggSpesifikasjon jsonVedlegg = sosialhjelpVedleggTilJson.toJsonVedleggSpesifikasjon(soknad);
        final JsonInternalSoknad internalSoknad = new JsonInternalSoknad()
                .withSoknad(jsonSoknad)
                .withVedlegg(jsonVedlegg)
                .withMottaker(new JsonSoknadsmottaker()
                        .withNavEnhetsnavn("TODO TESTNAVN SLETTES FØR PROD"));
        return internalSoknad;
    }

    @Override
    public void registrerHelper(String name, Helper helper) {
        helpers.put(name, helper);
    }

    private Handlebars getHandlebars() {
        Handlebars handlebars = new Handlebars()
                .with(new ConcurrentMapTemplateCache());

        for (Map.Entry<String, Helper> helper : helpers.entrySet()) {
            handlebars.registerHelper(helper.getKey(), helper.getValue());
        }

        handlebars.registerHelper("formatterFodelsDato", generateFormatterFodselsdatoHelper());
        handlebars.registerHelper("skalViseRotasjonTurnusSporsmaal", generateSkalViseRotasjonTurnusSporsmaalHelper());
        handlebars.registerHelper("inc", new Helper<String>() {
            @Override
            public CharSequence apply(String counter, Options options) throws IOException {
                return "" + (Integer.parseInt(counter) + 1);
            }
        });

        return handlebars;
    }

    @Deprecated
    private Helper<String> generateFormatterFodselsdatoHelper() {
        return new Helper<String>() {
            @Override
            public CharSequence apply(String s, Options options) throws IOException {
                if (s.length() == 11) {
                    Fodselsnummer fnr = getFodselsnummer(s);
                    return fnr.getDayInMonth() + "." + fnr.getMonth() + "." + fnr.getBirthYear();
                } else {
                    String[] datoSplit = split(s, "-");
                    reverse(datoSplit);
                    return join(datoSplit, ".");
                }
            }
        };
    }

    private Helper<Object> generateSkalViseRotasjonTurnusSporsmaalHelper() {
        return new Helper<Object>() {
            private boolean faktumSkalIkkeHaRotasjonssporsmaal(Faktum faktum) {
                List<String> verdierSomGenerererSporsmaal = Arrays.asList(
                        "Permittert",
                        "Sagt opp av arbeidsgiver",
                        "Kontrakt utgått",
                        "Sagt opp selv",
                        "Redusert arbeidstid"
                );
                return !verdierSomGenerererSporsmaal.contains(faktum.getProperties().get("type"));
            }

            @Override
            public CharSequence apply(Object value, Options options) throws IOException {
                if (!(options.context.model() instanceof Faktum)) {
                    return options.inverse(this);
                }

                Faktum faktum = (Faktum) options.context.model();
                if (faktumSkalIkkeHaRotasjonssporsmaal(faktum)) {
                    return options.inverse(this);
                } else {
                    return options.fn(this);
                }
            }
        };
    }

}