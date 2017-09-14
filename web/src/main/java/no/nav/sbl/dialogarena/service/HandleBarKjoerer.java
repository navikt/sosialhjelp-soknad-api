package no.nav.sbl.dialogarena.service;

import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.Options;
import com.github.jknack.handlebars.cache.ConcurrentMapTemplateCache;
import com.github.jknack.handlebars.context.FieldValueResolver;
import com.github.jknack.handlebars.context.JavaBeanValueResolver;
import com.github.jknack.handlebars.context.MapValueResolver;
import com.github.jknack.handlebars.context.MethodValueResolver;
import no.bekk.bekkopen.person.Fodselsnummer;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.oppsett.SoknadStruktur;
import no.nav.sbl.dialogarena.service.oppsummering.OppsummeringsContext;
import no.nav.sbl.dialogarena.soknadinnsending.business.WebSoknadConfig;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static no.bekk.bekkopen.person.FodselsnummerValidator.getFodselsnummer;
import static org.apache.commons.lang3.ArrayUtils.reverse;
import static org.apache.commons.lang3.StringUtils.join;
import static org.apache.commons.lang3.StringUtils.split;

@SuppressWarnings({"PMD.TooManyMethods", "PMD.ExcessiveClassLength"})
public class HandleBarKjoerer implements HtmlGenerator, HandlebarRegistry {

    private Map<String, Helper> helpers = new HashMap<>();

    @Inject
    private WebSoknadConfig webSoknadConfig;

    public String fyllHtmlMalMedInnhold(WebSoknad soknad, String file) throws IOException {
        return getHandlebars()
                .compile(file)
                .apply(Context.newBuilder(soknad).build());
    }

    public String fyllHtmlMalMedInnhold(WebSoknad soknad) throws IOException {
        return fyllHtmlMalMedInnhold(soknad, false);
    }

    @Override
    public String fyllHtmlMalMedInnhold(WebSoknad soknad, boolean utvidetSoknad) throws IOException {
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