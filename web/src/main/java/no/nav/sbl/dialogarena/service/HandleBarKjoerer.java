package no.nav.sbl.dialogarena.service;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.Options;
import no.bekk.bekkopen.person.Fodselsnummer;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;
import org.apache.commons.collections15.Predicate;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.IOException;
import java.util.*;

import static no.bekk.bekkopen.person.FodselsnummerValidator.getFodselsnummer;
import static no.nav.modig.lang.collections.IterUtils.on;
import static no.nav.sbl.dialogarena.service.HandlebarsUtils.*;
import static org.apache.commons.lang3.ArrayUtils.reverse;
import static org.apache.commons.lang3.StringUtils.join;
import static org.apache.commons.lang3.StringUtils.split;

@SuppressWarnings({"PMD.TooManyMethods", "PMD.ExcessiveClassLength"})
public class HandleBarKjoerer implements HtmlGenerator, HandlebarRegistry {

    private Map<String, Helper> helpers = new HashMap<>();

    public String fyllHtmlMalMedInnhold(WebSoknad soknad, String file) throws IOException {
        return getHandlebars().compile(file).apply(soknad);
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

        handlebars.registerHelper("forFaktumHvisSant", generateforFaktumHvisSantHelper());
        handlebars.registerHelper("forBarnefakta", generateForBarnefaktaHelper());
        handlebars.registerHelper("formatterFodelsDato", generateFormatterFodselsdatoHelper());
        handlebars.registerHelper("forPerioder", generateHelperForPeriodeTidsromFakta());
        handlebars.registerHelper("hvisFlereErTrue", generateHvisFlereSomStarterMedErTrueHelper());
        handlebars.registerHelper("skalViseRotasjonTurnusSporsmaal", generateSkalViseRotasjonTurnusSporsmaalHelper());

        return handlebars;
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