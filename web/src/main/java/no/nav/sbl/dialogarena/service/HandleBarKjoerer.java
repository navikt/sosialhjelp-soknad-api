package no.nav.sbl.dialogarena.service;

import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.Options;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.context.FieldValueResolver;
import com.github.jknack.handlebars.context.JavaBeanValueResolver;
import com.github.jknack.handlebars.context.MapValueResolver;
import com.github.jknack.handlebars.context.MethodValueResolver;
import no.bekk.bekkopen.person.Fodselsnummer;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.oppsett.SoknadStruktur;

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

    public String fyllHtmlMalMedInnhold(WebSoknad soknad, String file) throws IOException {
        return getHandlebars()
                .compile(file)
                .apply(Context.newBuilder(soknad).build());
    }

    @Override
    public String fyllHtmlMalMedInnholdNew(WebSoknad soknad, SoknadStruktur soknadStruktur, String file) throws IOException {
        return getHandlebars()
                .infiniteLoops(true)
                .compile(file)
                .apply(Context.newBuilder(new OppsummeringsContext(soknad, soknadStruktur))
                        .resolver(
                                JavaBeanValueResolver.INSTANCE,
                                FieldValueResolver.INSTANCE,
                                MapValueResolver.INSTANCE,
                                MethodValueResolver.INSTANCE
                        ).build());
    }

    @Override
    public void registrerHelper(String name, Helper helper) {
        helpers.put(name, helper);
    }

    private Handlebars getHandlebars() {
        Handlebars handlebars = new Handlebars();

        for (Map.Entry<String, Helper> helper : helpers.entrySet()) {
            handlebars.registerHelper(helper.getKey(), helper.getValue());
        }

        handlebars.registerHelper("formatterFodelsDato", generateFormatterFodselsdatoHelper());
        handlebars.registerHelper("skalViseRotasjonTurnusSporsmaal", generateSkalViseRotasjonTurnusSporsmaalHelper());
        handlebars.registerHelper("dynamiskPartial", new Helper<OppsummeringsContext.OppsummeringsFaktum>() {
            @Override
            public CharSequence apply(OppsummeringsContext.OppsummeringsFaktum context, Options options) throws IOException {
                options.partial("skjema/generisk/default", options.handlebars.compile("skjema/generisk/default"));
                options.partial("skjema/generisk/checkboxGroup", options.handlebars.compile("skjema/generisk/checkboxGroup"));
                Template partial = options.partial("skjema/generisk/" + context.struktur.getType());
                if(partial != null) {
                    return partial.apply(context);
                } else {
                    return options.partial("skjema/generisk/default").apply(context);
                }
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