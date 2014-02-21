package no.nav.sbl.dialogarena.print;

import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.Options;
import no.bekk.bekkopen.person.Fodselsnummer;
import no.nav.sbl.dialogarena.kodeverk.Kodeverk;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Vedlegg;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.WebSoknadUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.context.MessageSource;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import static no.bekk.bekkopen.person.FodselsnummerValidator.getFodselsnummer;
import static org.apache.commons.lang3.ArrayUtils.reverse;
import static org.apache.commons.lang3.StringUtils.join;
import static org.apache.commons.lang3.StringUtils.split;


public class HandleBarKjoerer {

    private Kodeverk kodeverk;
    private MessageSource navMessageSource;

    public HandleBarKjoerer(Kodeverk kodeverk, MessageSource navMessageSource) {
        this.kodeverk = kodeverk;
        this.navMessageSource = navMessageSource;
    }

    public String fyllHtmlMalMedInnhold(WebSoknad soknad, String file) throws IOException {
        return getHandlebars().compile(file)
                .apply(soknad);

    }

    private Handlebars getHandlebars() {
        Handlebars handlebars = new Handlebars();

        handlebars.registerHelper("forFaktum", generateForFaktumHelper());
        handlebars.registerHelper("forFakta", generateForFaktaHelper());
        handlebars.registerHelper("forFaktaMedPropertySattTilTrue", generateForFaktaMedPropTrueHelper());
        handlebars.registerHelper("formatterFodelsDato", generateFormatterFodselsdatoHelper());
        handlebars.registerHelper("formatterLangDato", generateFormatterLangDatoHelper());
        handlebars.registerHelper("hvisSant", generateHvisSantHelper());
        handlebars.registerHelper("hvisMindre", generateHvisMindreHelper());
        handlebars.registerHelper("hvisMer", generateHvisMerHelper());
        handlebars.registerHelper("hvisLik", generateHvisLikHelper());
        handlebars.registerHelper("hvisIkkeTom", generateHvisIkkeTomHelper());
        handlebars.registerHelper("hentTekst", generateHvisTekstHelper());
        handlebars.registerHelper("hentTekstMedParameter", generateHentTekstMedParameterHelper());
        handlebars.registerHelper("hentLand", generateHentLandHelper());
        handlebars.registerHelper("forVedlegg", generateForVedleggHelper());
        handlebars.registerHelper("hentSkjemanummer", generateHentSkjemanummerHelper());

        return handlebars;
    }

    private Helper<Object> generateHentSkjemanummerHelper() {
        return new Helper<Object>() {
            @Override
            public CharSequence apply(Object context, Options options) throws IOException {
                return WebSoknadUtils.getSkjemanummer(finnWebSoknad(options.context));
            }
        };
    }

    private Helper<Object> generateForVedleggHelper() {
        return new Helper<Object>() {
            @Override
            public CharSequence apply(Object context, Options options) throws IOException {
                WebSoknad soknad = finnWebSoknad(options.context);
                List<Vedlegg> vedlegg = soknad.getVedlegg();

                if (vedlegg.isEmpty()) {
                    return options.inverse(this);
                } else {
                    return lagItererbarRespons(options, vedlegg);
                }
            }
        };
    }

    private Helper<String> generateHentLandHelper() {
        return new Helper<String>() {
            @Override
            public CharSequence apply(String landKode, Options options) throws IOException {
                return kodeverk.getLand(landKode);
            }
        };
    }

    private Helper<String> generateHentTekstMedParameterHelper() {
        return new Helper<String>() {
            @Override
            public CharSequence apply(String key, Options options) throws IOException {
                return navMessageSource.getMessage(key, new Object[]{options.param(0)}, new Locale("nb", "NO"));
            }
        };
    }

    private Helper<String> generateHvisTekstHelper() {
        return new Helper<String>() {
            @Override
            public CharSequence apply(String key, Options options) throws IOException {
                return navMessageSource.getMessage(key, new Object[0], new Locale("nb", "NO"));
            }
        };
    }

    private Helper<Object> generateHvisIkkeTomHelper() {
        return new Helper<Object>() {
            @Override
            public CharSequence apply(Object value, Options options) throws IOException {
                if (value != null && !value.toString().isEmpty()) {
                    return options.fn(this);
                } else {
                    return options.inverse(this);
                }
            }
        };
    }

    private Helper<Object> generateHvisLikHelper() {
        return new Helper<Object>() {
            @Override
            public CharSequence apply(Object value, Options options) throws IOException {
                if (value != null && value.toString().equals(options.param(0))) {
                    return options.fn(this);
                } else {
                    return options.inverse(this);
                }
            }
        };
    }

    private Helper<String> generateHvisMerHelper() {
        return new Helper<String>() {
            @Override
            public CharSequence apply(String value, Options options) throws IOException {
                Integer grense = Integer.parseInt((String) options.param(0));
                Integer verdi = Integer.parseInt(value);
                if (verdi > grense) {
                    return options.fn(this);
                } else {
                    return options.inverse(this);
                }
            }
        };
    }

    private Helper<String> generateHvisMindreHelper() {
        return new Helper<String>() {
            @Override
            public CharSequence apply(String value, Options options) throws IOException {
                Integer grense = Integer.parseInt((String) options.param(0));
                Integer verdi = Integer.parseInt(value);
                if (verdi < grense) {
                    return options.fn(this);
                } else {
                    return options.inverse(this);
                }
            }
        };
    }

    private Helper<String> generateHvisSantHelper() {
        return new Helper<String>() {
            @Override
            public CharSequence apply(String value, Options options) throws IOException {
                if (value != null && value.equals("true")) {
                    return options.fn(this);
                } else {
                    return options.inverse(this);
                }
            }
        };
    }

    private Helper<String> generateFormatterLangDatoHelper() {
        return new Helper<String>() {
            @Override
            public CharSequence apply(String dato, Options options) throws IOException {
                Locale locale = new Locale("nb", "no");
                DateTimeFormatter dt = DateTimeFormat.forPattern("d. MMMM yyyy").withLocale(locale);
                if (StringUtils.isNotEmpty(dato)) {
                    return dt.print(DateTime.parse(dato));
                }
                return "";
            }
        };
    }

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

    private Helper<String> generateForFaktaMedPropTrueHelper() {
        return new Helper<String>() {
            @Override
            public CharSequence apply(String key, Options options) throws IOException {
                WebSoknad soknad = finnWebSoknad(options.context);
                List<Faktum> fakta = soknad.getFaktaMedKeyOgPropertyLikTrue(key, (String) options.param(0));
                if (fakta.isEmpty()) {
                    return options.inverse(this);
                } else {
                    return lagItererbarRespons(options, fakta);
                }
            }
        };
    }

    private Helper<String> generateForFaktaHelper() {
        return new Helper<String>() {
            @Override
            public CharSequence apply(String key, Options options) throws IOException {
                WebSoknad soknad = finnWebSoknad(options.context);
                List<Faktum> fakta = soknad.getFaktaMedKey(key);

                if (fakta.isEmpty()) {
                    return options.inverse(this);
                } else {
                    return lagItererbarRespons(options, fakta);
                }
            }
        };
    }

    private Helper<String> generateForFaktumHelper() {
        return new Helper<String>() {
            @Override
            public CharSequence apply(String o, Options options) throws IOException {
                WebSoknad soknad = finnWebSoknad(options.context);
                Faktum faktum = soknad.getFaktumMedKey(o);
                return options.fn(faktum);
            }
        };
    }

    private static WebSoknad finnWebSoknad(Context context) {
        if (context == null) {
            return null;
        } else if (context.model() instanceof WebSoknad) {
            return (WebSoknad) context.model();
        } else {
            return finnWebSoknad(context.parent());
        }
    }

    private static <T> String lagItererbarRespons(Options options, List<T> liste) throws IOException {
        Context parent = options.context;
        StringBuilder buffer = new StringBuilder();
        int index = 0;
        Iterator<T> iterator = liste.iterator();
        while (iterator.hasNext()) {
            Object element = iterator.next();
            boolean first = index == 0;
            boolean even = index % 2 == 0;
            boolean last = !iterator.hasNext();
            Context current = Context.newContext(parent, element)
                    .data("index", index)
                    .data("first", first ? "first" : "")
                    .data("last", last ? "last" : "")
                    .data("odd", even ? "" : "odd")
                    .data("even", even ? "even" : "");
            buffer.append(options.fn(current));
            index++;
        }
        return buffer.toString();
    }
}
