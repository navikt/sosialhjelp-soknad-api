package no.nav.sbl.sosialhjelp.pdf;

import com.github.jknack.handlebars.*;
import com.github.jknack.handlebars.cache.ConcurrentMapTemplateCache;
import com.github.jknack.handlebars.context.*;
import no.bekk.bekkopen.person.Fodselsnummer;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;

import java.io.IOException;
import java.util.*;

import static no.bekk.bekkopen.person.FodselsnummerValidator.getFodselsnummer;
import static org.apache.commons.lang3.ArrayUtils.reverse;
import static org.apache.commons.lang3.StringUtils.join;
import static org.apache.commons.lang3.StringUtils.split;

public class HandleBarKjoerer implements HtmlGenerator, HandlebarRegistry {

    private Map<String, Helper> helpers = new HashMap<>();

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
    public String genererHtmlForPdf(JsonInternalSoknad internalSoknad, String file, boolean erEttersending) throws IOException {
        final HandlebarContext context = new HandlebarContext(internalSoknad, false, erEttersending);
        
        return getHandlebars()
                .compile(file)
                .apply(Context.newBuilder(context).build());
    }

    @Override
    public String fyllHtmlMalMedInnhold(JsonInternalSoknad internalSoknad, String file, boolean erEttersending, String eier) throws IOException {
        final HandlebarContext context = new HandlebarContext(internalSoknad, false, erEttersending, eier);

        return getHandlebars()
                .compile(file)
                .apply(Context.newBuilder(context).build());
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