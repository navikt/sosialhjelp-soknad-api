package no.nav.sbl.dialogarena.service;

import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.Options;
import no.bekk.bekkopen.person.Fodselsnummer;
import no.nav.sbl.dialogarena.kodeverk.Kodeverk;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Vedlegg;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;
import no.nav.sbl.dialogarena.soknadinnsending.business.util.DagpengerUtils;
import org.apache.commons.collections15.Predicate;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.util.*;

import static no.bekk.bekkopen.person.FodselsnummerValidator.getFodselsnummer;
import static no.nav.modig.lang.collections.IterUtils.on;
import static org.apache.commons.lang3.ArrayUtils.reverse;
import static org.apache.commons.lang3.StringUtils.join;
import static org.apache.commons.lang3.StringUtils.split;
import static org.slf4j.LoggerFactory.getLogger;


@SuppressWarnings({"PMD.TooManyMethods", "PMD.ExcessiveClassLength"})
public class HandleBarKjoerer implements HtmlGenerator, HandlebarRegistry {

    public static final Locale NO_LOCALE = new Locale("nb", "no");

    @Inject
    private Kodeverk kodeverk;

    private Map<String, Helper> helpers = new HashMap<>();

    @Inject
    @Named("navMessageSource")
    private MessageSource navMessageSource;

    private String soknadTypePrefix;

    public String fyllHtmlMalMedInnhold(WebSoknad soknad, String file) throws IOException {
        this.soknadTypePrefix = soknad.getSoknadPrefix();
        return getHandlebars().compile(file).apply(soknad);

    }

    @Override
    public void registrerHelper(String name, Helper helper){
        helpers.put(name, helper);
    }


    private Handlebars getHandlebars() {
        Handlebars handlebars = new Handlebars();

        for (Map.Entry<String, Helper> helper : helpers.entrySet()) {
            handlebars.registerHelper(helper.getKey(), helper.getValue());
        }

        handlebars.registerHelper("adresse", generateAdresseHelper());
        handlebars.registerHelper("forFaktum", generateForFaktumHelper());
        handlebars.registerHelper("forFaktumHvisSant", generateforFaktumHvisSantHelper());
        handlebars.registerHelper("forFakta", generateForFaktaHelper());
        handlebars.registerHelper("forBarnefakta", generateForBarnefaktaHelper());
        handlebars.registerHelper("forFaktaMedPropertySattTilTrue", generateForFaktaMedPropTrueHelper());
        handlebars.registerHelper("formatterFodelsDato", generateFormatterFodselsdatoHelper());
        handlebars.registerHelper("formatterLangDato", generateFormatterLangDatoHelper());
        handlebars.registerHelper("hvisSant", generateHvisSantHelper());
        handlebars.registerHelper("hvisEttersending", generateHvisEttersendingHelper());
        handlebars.registerHelper("hvisMindre", generateHvisMindreHelper());
        handlebars.registerHelper("hvisMer", generateHvisMerHelper());
        handlebars.registerHelper("hvisLik", generateHvisLikHelper());
        handlebars.registerHelper("hvisIkkeTom", generateHvisIkkeTomHelper());
        handlebars.registerHelper("hentTekst", generateHentTekstHelper());
        handlebars.registerHelper("hentTekstMedFaktumParameter", generateHentTekstMedFaktumParameterHelper());
        handlebars.registerHelper("hentLand", generateHentLandHelper());
        handlebars.registerHelper("forVedlegg", generateForVedleggHelper());
        handlebars.registerHelper("forPerioder", generateHelperForPeriodeTidsromFakta());
        handlebars.registerHelper("hentSkjemanummer", generateHentSkjemanummerHelper());
        handlebars.registerHelper("hentFaktumValue", generateHentFaktumValueHelper());
        handlebars.registerHelper("hvisFlereErTrue", generateHvisFlereSomStarterMedErTrueHelper());
        handlebars.registerHelper("sendtInnInfo", generateSendtInnInfoHelper());
        handlebars.registerHelper("forInnsendteVedlegg", generateForInnsendteVedleggHelper());
        handlebars.registerHelper("forIkkeInnsendteVedlegg", generateForIkkeInnsendteVedleggHelper());
        handlebars.registerHelper("hvisHarIkkeInnsendteDokumenter", generateHvisHarIkkeInnsendteDokumenterHelper());
        handlebars.registerHelper("concat", generateConcatStringHelper());
        handlebars.registerHelper("skalViseRotasjonTurnusSporsmaal", generateSkalViseRotasjonTurnusSporsmaalHelper());
        handlebars.registerHelper("hvisLikCmsTekst", generateHvisLikCmsTekstHelper());
        handlebars.registerHelper("toLowerCase", generateToLowerCaseHelper());
        handlebars.registerHelper("hvisKunStudent", generateHvisKunStudentHelper());
        handlebars.registerHelper("harBarnetInntekt", generateHarBarnetInntektHelper());

        return handlebars;
    }

    private Helper<String> generateAdresseHelper() {
        return new Helper<String>() {
            @Override
            public CharSequence apply(String adresse, Options options) throws IOException {
                String[] adresselinjer = adresse.split("\n");

                StringBuilder resultAdresse = new StringBuilder();
                for (String adresselinje : adresselinjer) {
                    resultAdresse.append("<p>").append(adresselinje).append("</p>");
                }

                return resultAdresse.toString();
            }
        };
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

                DateTimeFormatter dt = DateTimeFormat.forPattern("d. MMMM yyyy', klokken' HH.mm").withLocale(NO_LOCALE);

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
                List<Faktum> fakta = soknad.getFaktaSomStarterMed(o);

                int size = on(fakta).filter(new Predicate<Faktum>() {
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
                WebSoknad soknad = finnWebSoknad(options.context);
                if (soknad.erDagpengeSoknad()) {
                    return DagpengerUtils.getSkjemanummer(soknad);
                }
                return soknad.getskjemaNummer();
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

    private Helper<String> generateHentTekstMedFaktumParameterHelper() {
        return new Helper<String>() {
            @Override
            public CharSequence apply(String key, Options options) throws IOException {
                WebSoknad soknad = finnWebSoknad(options.context);
                Faktum faktum = soknad.getFaktumMedKey(options.param(0).toString());
                return getCmsTekst(key, new Object[]{faktum.getValue()}, new Locale("nb", "NO"));
            }
        };
    }

    private Helper<String> generateHentTekstHelper() {
        return new Helper<String>() {
            @Override
            public CharSequence apply(String key, Options options) throws IOException {
                return getCmsTekst(key, options.params, NO_LOCALE);
            }
        };
    }

    private Helper<String> generateConcatStringHelper() {
        return new Helper<String>() {
            @Override
            public CharSequence apply(String first, Options options) throws IOException {
                StringBuilder builder = new StringBuilder(first);
                for (Object string : options.params) {
                    builder.append(string);
                }
                return builder.toString();
            }
        };
    }

    private String getCmsTekst(String key, Object[] parameters, Locale locale) {
        try {
            return navMessageSource.getMessage(soknadTypePrefix + "." + key, parameters, locale);
        } catch (NoSuchMessageException e) {
            try {
                return navMessageSource.getMessage(key, parameters, locale);
            } catch (NoSuchMessageException e2) {
                return String.format("KEY MANGLER: [%s]", key);
            }
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
                try {
                    Double grense = Double.parseDouble(((String) options.param(0)).replace(',', '.'));
                    Double verdi = Double.parseDouble(value.replace(',', '.'));
                    if (verdi > grense) {
                        return options.fn(this);
                    } else {
                        return options.inverse(this);
                    }
                } catch (NumberFormatException e) {
                    getLogger(HandleBarKjoerer.class).error("Kunne ikke parse input til double", e);
                    return options.fn(this);
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

    private Helper<Object> generateHvisEttersendingHelper() {
        return new Helper<Object>() {
            @Override
            public CharSequence apply(Object o, Options options) throws IOException {
                WebSoknad soknad = finnWebSoknad(options.context);
                if (soknad.erEttersending()) {
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

    private Helper<String> generateHentFaktumValueHelper() {
        return new Helper<String>() {
            @Override
            public CharSequence apply(String key, Options options) throws IOException {
                WebSoknad soknad = finnWebSoknad(options.context);
                Faktum faktum = soknad.getFaktumMedKey(key);
                return faktum.getValue();
            }
        };
    }

    private Helper<Object> generateHelperForPeriodeTidsromFakta() {
        return new Helper<Object>() {
            @Override
            public CharSequence apply(Object context, Options options) throws IOException {
                WebSoknad soknad = finnWebSoknad(options.context);
                List<Faktum> fakta = soknad.getFaktaSomStarterMed("perioder.tidsrom");
                List<Faktum> sortertFaktaEtterDato = on(fakta).collect(new Comparator<Faktum>() {
                    @Override
                    public int compare(Faktum o1, Faktum o2) {
                        DateTimeFormatter dt = DateTimeFormat.forPattern("yyyy-MM-dd").withLocale(NO_LOCALE);
                        DateTime fradatoForstePeriode = dt.parseDateTime(o2.getProperties().get("fradato"));
                        DateTime fradatoAndrePeriode = dt.parseDateTime(o1.getProperties().get("fradato"));
                        return fradatoAndrePeriode.compareTo(fradatoForstePeriode);
                    }
                });
                if (sortertFaktaEtterDato.isEmpty()) {
                    return options.inverse(this);
                } else {
                    return lagItererbarRespons(options, sortertFaktaEtterDato);
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

    private Helper<String> generateForBarnefaktaHelper() {
        return new Helper<String>() {
            @Override
            public CharSequence apply(String key, Options options) throws IOException {
                WebSoknad soknad = finnWebSoknad(options.context);
                Faktum parentFaktum = finnFaktum(options.context);
                List<Faktum> fakta = soknad.getFaktaMedKeyOgParentFaktum(key, parentFaktum.getFaktumId());
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

    private Helper<String> generateforFaktumHvisSantHelper() {
        return new Helper<String>() {
            @Override
            public CharSequence apply(String o, Options options) throws IOException {
                WebSoknad soknad = finnWebSoknad(options.context);
                Faktum faktum = soknad.getFaktumMedKey(o);

                if (faktum != null && faktum.getValue() != null && faktum.getValue().equals("true")) {
                    return options.fn(faktum);
                } else {
                    return options.inverse(this);
                }
            }
        };
    }

    public static WebSoknad finnWebSoknad(Context context) {
        if (context == null) {
            return null;
        } else if (context.model() instanceof WebSoknad) {
            return (WebSoknad) context.model();
        } else {
            return finnWebSoknad(context.parent());
        }
    }

    private static Faktum finnFaktum(Context context) {
        if (context == null) {
            return null;
        } else if (context.model() instanceof Faktum) {
            return (Faktum) context.model();
        } else {
            return finnFaktum(context.parent());
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

    private Helper<Object> generateHvisLikCmsTekstHelper() {
        return new Helper<Object>() {
            @Override
            public CharSequence apply(Object value, Options options) throws IOException {
                if (value != null && getCmsTekst(options.param(0).toString(), new Object[]{}, NO_LOCALE).equalsIgnoreCase(value.toString())) {
                    return options.fn(this);
                }
                return options.inverse(this);
            }
        };
    }

    private Helper<Object> generateToLowerCaseHelper() {
        return new Helper<Object>() {
            @Override
            public CharSequence apply(Object value, Options options) throws IOException {
                return value.toString().toLowerCase();
            }
        };
    }

    private Helper<Object> generateHvisKunStudentHelper() {
        return new Helper<Object>() {
            @Override
            public CharSequence apply(Object context, Options options) throws IOException {
                WebSoknad soknad = finnWebSoknad(options.context);

                Faktum iArbeidFaktum = soknad.getFaktumMedKey("navaerendeSituasjon.iArbeid");
                Faktum sykmeldtFaktum = soknad.getFaktumMedKey("navaerendeSituasjon.sykmeldt");
                Faktum arbeidsledigFaktum = soknad.getFaktumMedKey("navaerendeSituasjon.arbeidsledig");
                Faktum forstegangstjenesteFaktum = soknad.getFaktumMedKey("navaerendeSituasjon.forstegangstjeneste");
                Faktum annetFaktum = soknad.getFaktumMedKey("navaerendeSituasjon.annet");

                Faktum[] fakta = {iArbeidFaktum, sykmeldtFaktum, arbeidsledigFaktum, forstegangstjenesteFaktum, annetFaktum};

                for (Faktum faktum : fakta) {
                    if (faktum != null && "true".equals(faktum.getValue())) {
                        return options.inverse(this);
                    }
                }

                return options.fn(this);
            }
        };
    }

    private Helper<Object> generateHarBarnetInntektHelper() {
        return new Helper<Object>() {
            @Override
            public CharSequence apply(Object key, Options options) throws IOException {
                WebSoknad soknad = finnWebSoknad(options.context);
                Faktum parentFaktum = finnFaktum(options.context);

                Faktum harInntekt = soknad.getFaktaMedKeyOgParentFaktum("barn.harinntekt", parentFaktum.getFaktumId()).get(0);

                if (harInntekt != null && "true".equals(harInntekt.getValue())) {
                    Faktum sumInntekt = soknad.getFaktaMedKeyOgParentFaktum("barn.inntekt", parentFaktum.getFaktumId()).get(0);
                    return options.fn(sumInntekt);
                } else {
                    return options.inverse(this);
                }
            }
        };
    }

}