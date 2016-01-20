package no.nav.sbl.dialogarena.service;

import com.github.jknack.handlebars.*;
import com.github.jknack.handlebars.context.*;
import no.bekk.bekkopen.person.*;
import no.nav.sbl.dialogarena.common.kodeverk.*;
import no.nav.sbl.dialogarena.sendsoknad.domain.*;
import no.nav.sbl.dialogarena.sendsoknad.domain.oppsett.*;
import no.nav.sbl.dialogarena.service.oppsummering.*;
import no.nav.sbl.dialogarena.soknadinnsending.business.*;

import javax.inject.*;
import java.io.*;
import java.util.*;

import static no.bekk.bekkopen.person.FodselsnummerValidator.*;
import static org.apache.commons.lang3.ArrayUtils.reverse;
import static org.apache.commons.lang3.StringUtils.*;

@SuppressWarnings({"PMD.TooManyMethods", "PMD.ExcessiveClassLength"})
public class HandleBarKjoerer implements HtmlGenerator, HandlebarRegistry {

    private Map<String, Helper> helpers = new HashMap<>();
    @Inject
    private Kodeverk kodeverk;

    @Inject
    private WebSoknadConfig webSoknadConfig;

    public String fyllHtmlMalMedInnhold(WebSoknad soknad, String file) throws IOException {
        return getHandlebars()
                .compile(file)
                .apply(Context.newBuilder(soknad).build());
    }

    @Override
    public String fyllHtmlMalMedInnhold(WebSoknad soknad) throws IOException {
        SoknadStruktur soknadStruktur = webSoknadConfig.hentStruktur(soknad.getskjemaNummer());
        return getHandlebars()
                .infiniteLoops(true)
                .compile("/skjema/generisk")
                .apply(Context.newBuilder(new OppsummeringsContext(soknad, soknadStruktur, false))
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

    private Integer finnNivaa(Context context) {
        if(context == null){return 3;}
        return (context.get("sporsmal") != null? 1:0) + finnNivaa(context.parent());
    }

    private Handlebars getHandlebars() {
        Handlebars handlebars = new Handlebars();

        for (Map.Entry<String, Helper> helper : helpers.entrySet()) {
            handlebars.registerHelper(helper.getKey(), helper.getValue());
        }

        handlebars.registerHelper("formatterFodelsDato", generateFormatterFodselsdatoHelper());
        handlebars.registerHelper("skalViseRotasjonTurnusSporsmaal", generateSkalViseRotasjonTurnusSporsmaalHelper());
        handlebars.registerHelper("finnNiva", new Helper<Object>() {
            @Override
            public CharSequence apply(Object context, Options options) throws IOException {
                return "" + finnNivaa(options.context.parent());
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