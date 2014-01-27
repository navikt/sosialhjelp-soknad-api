package no.nav.sbl.dialogarena.print;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.Options;
import no.bekk.bekkopen.person.Fodselsnummer;
import no.nav.sbl.dialogarena.kodeverk.Kodeverk;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Vedlegg;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;
import org.apache.wicket.model.StringResourceModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static no.bekk.bekkopen.person.FodselsnummerValidator.getFodselsnummer;
import static org.apache.commons.lang3.ArrayUtils.reverse;
import static org.apache.commons.lang3.StringUtils.join;
import static org.apache.commons.lang3.StringUtils.split;


public class HandleBarKjoerer {

    private Kodeverk kodeverk;

    private static final Logger LOGG = LoggerFactory.getLogger(HandleBarKjoerer.class);

    public HandleBarKjoerer(Kodeverk kodeverk) {
        this.kodeverk = kodeverk;
    }

    /**
     * Tar inn json og html på string-format. Htmlen er en Mustache-mal, med nøkler
     * som matcher json-nøklene.
     *
     * @return String Html med verdier fra Json-data
     */
    @SuppressWarnings("unchecked")
    public String hentHTML(String json, String html) {
        String ut = "";
        try {
            Map<String, Object> hashMap = new ObjectMapper().readValue(json, Map.class);
            ut = fyllHtmlStringMedInnhold(html, hashMap);
        } catch (IOException e) {
            LOGG.info("Kunne ikke legge json inn i mal", e);
        }
        return ut;
    }

    /**
     * Tar inn html i stringformat og data i en Map.
     * Html-stringen er en Mustache-mal, med nøkler som matcher nøklene i input-Map.
     *
     * @return String Html med verdier fra input Map
     */
    public String fyllHtmlStringMedInnhold(String input, Map<String, Object> hash) throws IOException {
        Handlebars handlebars = getHandlebars();
        return handlebars.compileInline(input).apply(hash);
    }

    /**
     * Tar inn json på string-format og navnet på en resource hbs-fil.
     * Hbs-fila er en Mustache-mal, med nøkler
     * som matcher json-nøklene.
     *
     * @return String Html med verdier fra Json-data
     */
    @SuppressWarnings("unchecked")
    public String fyllHtmlMalMedInnhold(String json, String hbsFil) {
        String ut = "";
        try {
            Map<String, Object> map = new ObjectMapper().readValue(json, Map.class);
            ut = fyllHtmlMalMedInnhold(hbsFil, map);
        } catch (IOException e) {
            LOGG.info("Kunne ikke legge json inn i mal", e);
        }
        return ut;
    }

    public String fyllHtmlMalMedInnhold(WebSoknad soknad, String file) throws IOException {
        return getHandlebars().compile(file)
                .apply(soknad);

    }

    /**
     * Tar inn navnet på en resource hbs-fil og data i en Map.
     * Hbs-fila er en Mustache-mal, med nøkler som matcher nøklene i input-Map.
     *
     * @return String Html med verdier fra input Map
     */
    public String fyllHtmlMalMedInnhold(String htmlFile, Map<String, Object> hash) throws IOException {
        return getHandlebars().compile(htmlFile)
                .apply(hash);
    }


    private Handlebars getHandlebars() {
        Context c = Context.newBuilder(new WebSoknad()).build();
        Handlebars handlebars = new Handlebars();
        handlebars.registerHelper("forFaktum", new Helper<String>() {
            @Override
            public CharSequence apply(String o, Options options) throws IOException {
                WebSoknad soknad = finnWebSoknad(options.context);
                Faktum faktum = soknad.getFakta().get(o);
                return options.fn(faktum);
            }
        });
        handlebars.registerHelper("forFakta", new Helper<String>() {
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
        });
        handlebars.registerHelper("forFaktaMedPropertySattTilTrue", new Helper<String>() {
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
        });

        handlebars.registerHelper("formatterFodelsDato", new Helper<String>() {
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
        });


        handlebars.registerHelper("forFaktaStarterMed", new Helper<String>() {
            @Override
            public CharSequence apply(String key, Options options) throws IOException {
                WebSoknad soknad = finnWebSoknad(options.context);
                List<Faktum> fakta = soknad.getFaktaSomStarterMed(key);
                return lagItererbarRespons(options, fakta);
            }
        });
        handlebars.registerHelper("forBarneFaktum", new Helper<String>() {
            @Override
            public CharSequence apply(String key, Options options) throws IOException {
                WebSoknad soknad = finnWebSoknad(options.context);
                Long parentFaktumId = options.param(0);
                Faktum faktum = soknad.getFaktaMedKeyOgParentFaktum(key, parentFaktumId).get(0);
                return options.fn(faktum);
            }
        });

        handlebars.registerHelper("hvisSant", new Helper<String>() {
            @Override
            public CharSequence apply(String value, Options options) throws IOException {
                if(value != null && value.equals("true")){
                    return options.fn(this);
                } else {
                    return options.inverse(this);
                }
            }
        });
        handlebars.registerHelper("hvisMindre", new Helper<String>() {
            @Override
            public CharSequence apply(String value, Options options) throws IOException {
                Integer grense = Integer.parseInt((String) options.param(0));
                Integer verdi = Integer.parseInt(value); 
                if(verdi < grense){
                    return options.fn(this);
                } else {
                    return options.inverse(this);
                }
            }
        });

        handlebars.registerHelper("hvisLik", new Helper<Object>() {
            @Override
            public CharSequence apply(Object value, Options options) throws IOException {
                if(value != null && value.toString().equals(options.param(0))){
                    return options.fn(this);
                } else {
                    return options.inverse(this);
                }
            }
        });

        handlebars.registerHelper("hentTekst", new Helper<String>() {
            @Override
            public CharSequence apply(String key, Options options) throws IOException {
                String tekst = new StringResourceModel(key, null).getString();
                return tekst;
            }
        });

        handlebars.registerHelper("hentLand", new Helper<String>() {
            @Override
            public CharSequence apply(String landKode, Options options) throws IOException {
                return kodeverk.getLand(landKode);
            }
        });

        handlebars.registerHelper("forVedlegg", new Helper<Object>() {
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
        });

        return handlebars;
    }

    private static WebSoknad finnWebSoknad(Context context) {
        if (context == null) {
            return null;
        } else if (context.model() instanceof WebSoknad) {
            return (WebSoknad)context.model();
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
