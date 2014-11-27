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
import no.nav.sbl.dialogarena.soknadinnsending.business.util.WebSoknadUtils;
import org.apache.commons.collections15.Predicate;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static no.bekk.bekkopen.person.FodselsnummerValidator.getFodselsnummer;
import static no.nav.modig.lang.collections.IterUtils.on;
import static org.apache.commons.lang3.ArrayUtils.reverse;
import static org.apache.commons.lang3.StringUtils.join;
import static org.apache.commons.lang3.StringUtils.split;


@Service
@SuppressWarnings("PMD.TooManyMethods")
public class HandleBarKjoerer implements HtmlGenerator {

    @Inject
    private Kodeverk kodeverk;

    @Inject
    @Named("navMessageSource")
    private MessageSource navMessageSource;

    private String soknadTypePrefix;

    public String fyllHtmlMalMedInnhold(WebSoknad soknad, String file) throws IOException {
        this.soknadTypePrefix = soknad.getSoknadPrefix();
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
        handlebars.registerHelper("hvisFlereErTrue", generateHvisFlereSomStarterMedErTrueHelper());
        handlebars.registerHelper("sendtInnInfo", generateSendtInnInfoHelper());
        handlebars.registerHelper("forInnsendteVedlegg", generateForInnsendteVedleggHelper());
        handlebars.registerHelper("forIkkeInnsendteVedlegg", generateForIkkeInnsendteVedleggHelper());
        handlebars.registerHelper("hvisHarIkkeInnsendteDokumenter", generateHvisHarIkkeInnsendteDokumenterHelper());

        return handlebars;
    }

    private Helper<Object> generateHvisHarIkkeInnsendteDokumenterHelper() {
        return new Helper<Object>() {
            @Override
            public CharSequence apply(Object o, Options options) throws IOException {
                WebSoknad soknad = finnWebSoknad(options.context);
                List<Vedlegg> vedlegg = soknad.getIkkeInnsendteVedlegg();
                if (vedlegg.isEmpty()) {
                    return options.inverse(this);
                } else {
                    return options.fn(this);
                }
            }
        };
    }

    private Helper<Object> generateForInnsendteVedleggHelper() {
        return new Helper<Object>() {
            @Override
            public CharSequence apply(Object o, Options options) throws IOException {
                WebSoknad soknad = finnWebSoknad(options.context);
                List<Vedlegg> vedlegg = soknad.getInnsendteVedlegg();
                if (vedlegg.isEmpty()) {
                    return options.inverse(this);
                } else {
                    return lagItererbarRespons(options, vedlegg);
                }
            }
        };
    }

    private Helper<Object> generateForIkkeInnsendteVedleggHelper() {
        return new Helper<Object>() {
            @Override
            public CharSequence apply(Object o, Options options) throws IOException {
                WebSoknad soknad = finnWebSoknad(options.context);
                List<Vedlegg> vedlegg = soknad.getIkkeInnsendteVedlegg();
                if (vedlegg.isEmpty()) {
                    return options.inverse(this);
                } else {
                    return lagItererbarRespons(options, vedlegg);
                }
            }
        };
    }

    private Helper<Object> generateSendtInnInfoHelper() {
        return new Helper<Object>() {
            @Override
            public CharSequence apply(Object o, Options options) throws IOException {
                WebSoknad soknad = finnWebSoknad(options.context);
                Map<String, String> infoMap = new HashMap<>();

                Locale locale = new Locale("nb", "no");
                DateTimeFormatter dt = DateTimeFormat.forPattern("d. MMMM yyyy").withLocale(locale);

                infoMap.put("sendtInn", String.valueOf(soknad.getInnsendteVedlegg().size()));
                infoMap.put("ikkeSendtInn", String.valueOf(soknad.getVedlegg().size()));
                infoMap.put("innsendtDato", dt.print(DateTime.now()));

                return options.fn(infoMap);
            }
        };
    }

    private Helper<String> generateHvisFlereSomStarterMedErTrueHelper() {
        return new Helper<String>() {
            @Override
            public CharSequence apply(String o, Options options) throws IOException {
                Integer grense = Integer.parseInt((String) options.param(0));

                WebSoknad soknad = finnWebSoknad(options.context);
                List<Faktum> faktaListe = soknad.getFaktaSomStarterMed(o);

                int size = on(faktaListe).filter(new Predicate<Faktum>() {
                    @Override
                    public boolean evaluate(Faktum faktum) {
                        String value = faktum.getValue();
                        return value != null && value.equals("true");
                    }
                }).collect().size();


                if (size > grense) {
                    return options.fn(this);
                } else {
                    return options.inverse(this);
                }
            }
        };
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
                return getCmsTekst(key, new Object[]{options.param(0)}, new Locale("nb", "NO"));
            }
        };
    }

    private Helper<String> generateHvisTekstHelper() {
        return new Helper<String>() {
            @Override
            public CharSequence apply(String key, Options options) throws IOException {
                return getCmsTekst(key, options.params, new Locale("nb", "NO"));
            }
        };
    }

    private String getCmsTekst(String key, Object[] parameters, Locale locale) {
        try {
            return navMessageSource.getMessage(soknadTypePrefix + "." + key, parameters, locale);
        } catch (NoSuchMessageException e) {
            return navMessageSource.getMessage(key, parameters, locale);
        }

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

                if (faktum == null || (faktum.getValue() == null && faktum.getProperties().isEmpty())) {
                    return options.inverse(this);
                } else {
                    return options.fn(faktum);
                }
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
