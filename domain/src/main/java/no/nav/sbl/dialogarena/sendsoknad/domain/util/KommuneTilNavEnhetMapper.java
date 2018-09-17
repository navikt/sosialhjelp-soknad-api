package no.nav.sbl.dialogarena.sendsoknad.domain.util;

import com.google.common.collect.ImmutableMap;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.apache.commons.lang3.StringUtils.isEmpty;

public class KommuneTilNavEnhetMapper {

    private static final Logger log = LoggerFactory.getLogger(KommuneTilNavEnhetMapper.class);

    public static class NavEnhet {

        private String kontornavn;
        private String kommune;
        private String orgnummer;
        private Map<String, Boolean> features;

        NavEnhet(String kontornavn, String kommune, String orgnummer) {
            this(kontornavn, kommune, orgnummer, Collections.emptyMap());
        }

        NavEnhet(String kontornavn, String kommune, String orgnummer, Map<String, Boolean> features) {
            this.kontornavn = kontornavn;
            this.kommune = kommune;
            this.orgnummer = orgnummer;

            final Map<String, Boolean> featuresAndDefaults = new HashMap<>(defaultFeatures());
            featuresAndDefaults.putAll(features);
            this.features = featuresAndDefaults;
        }

        public String getOrgnummer() {
            return orgnummer;
        }

        public String getNavn() {
            final String kommunenavntillegg = (kommune == null) ? "" : ", " + getKommunenavn() + " kommune";
            return "NAV " + kontornavn + kommunenavntillegg;
        }

        public String getKontornavn() {
            return kontornavn;
        }

        public String getKommune() {
            return kommune;
        }

        public String getKommunenavn() {
            if (kommune == null) {
                return null;
            }
            final String kommunenavn = getKommunerMedBydeler().get(kommune);
            if (kommunenavn == null) {
                log.error("Mangler navn på kommune: " + kommune);
                return kommune;
            }
            return kommunenavn;
        }

        public Map<String, Boolean> getFeatures() {
            return features;
        }
    }

    private static Map<String, Boolean> defaultFeatures() {
        final Map<String, Boolean> features = new HashMap<>();
        features.put("ettersendelse", true);
        return features;
    }


    private static final List<String> TEST_DIGISOS_KOMMUNER = Collections.unmodifiableList(asList("0701", "0703", "0717", "1201", "0301", "1247", "0219", "5001"));

    private static final List<String> PROD_DIGISOS_KOMMUNER = Collections.unmodifiableList(asList("0701", "0703", "0717", "0806", "1201", "0301", "1247"));

    private static final Map<String, String> TEST_KOMMUNER_MED_BYDELER = new ImmutableMap.Builder<String, String>()
            .put("oslo", "Oslo")
            .put("bergen", "Bergen")
            .put("trondheim", "Trondheim")
            .build();

    private static final Map<String, String> PROD_KOMMUNER_MED_BYDELER = new ImmutableMap.Builder<String, String>()
            .put("oslo", "Oslo")
            .put("bergen", "Bergen")
            .build();


    private static final Map<String, NavEnhet> TEST_ORGNR = new ImmutableMap.Builder<String, NavEnhet>()
            // Kommuner uten bydeler
            .put("horten", new NavEnhet("Horten", null, "910940066"))
            .put("askoy", new NavEnhet("Askøy", null, "910230182"))
            .put("barum", new NavEnhet("Bærum", null, "910230484"))

            // Kommuner med bydeler
            //Bergen
            .put("arna", new NavEnhet("Arna", "bergen", "910230530"))
            .put("bergenhus", new NavEnhet("Bergenhus", "bergen", "910230158"))
            .put("fana", new NavEnhet("Fana", "bergen", "910230506"))
            .put("fyllingsdalen", new NavEnhet("Fyllingsdalen", "bergen", "910230514"))
            .put("laksevag", new NavEnhet("Laksevåg", "bergen", "910230514"))
            .put("ytrebygda", new NavEnhet("Ytrebygda", "bergen", "910230506"))
            .put("arstad", new NavEnhet("Årstad", "bergen", "910230158"))
            .put("asane", new NavEnhet("Åsane", "bergen", "910230530"))

            // Oslo
            .put("frogner", new NavEnhet("Frogner", "oslo", "910229699"))
            .put("grunerlokka", new NavEnhet("Grünerløkka", "oslo", "811213322"))
            .put("grorud", new NavEnhet("Grorud", "oslo", "910229702"))
            .put("stovner", new NavEnhet("Stovner", "oslo", "910589792"))
            .put("sagene", new NavEnhet("Sagene", "oslo", "910565338"))
            .put("nordstrand", new NavEnhet("Nordstrand", "oslo", "910309935"))
            .put("sondreNordstrand", new NavEnhet("Søndre Nordstrand", "oslo", "910723499"))

            // Trondheim
            .put("falkenborg", new NavEnhet("Falkenborg", "trondheim", "910230646"))
            .put("lerkendal", new NavEnhet("Lerkendal", "trondheim", "910230611"))

            .build();

    private static final Map<String, NavEnhet> PROD_ORGNR = new ImmutableMap.Builder<String, NavEnhet>()
            .put("horten",          new NavEnhet("Horten", null,                "974605171"))
            .put("askoy",           new NavEnhet("Askøy", null,                 "974600889"))
            .put("skien",           new NavEnhet("Skien", null,                 "995509970"))

            .put("arna",            new NavEnhet("Arna", "bergen",              "976829786"))
            .put("bergenhus",       new NavEnhet("Bergenhus", "bergen",         "976830563"))
            .put("fana",            new NavEnhet("Fana", "bergen",              "976829948"))
            .put("fyllingsdalen",   new NavEnhet("Fyllingsdalen", "bergen",     "976830032"))
            .put("laksevag",        new NavEnhet("Laksevåg", "bergen",          "976830121"))
            .put("ytrebygda",       new NavEnhet("Ytrebygda", "bergen",         "976830652"))
            .put("arstad",          new NavEnhet("Årstad", "bergen",            "976830172"))
            .put("asane",           new NavEnhet("Åsane", "bergen",             "976830784"))

            .put("alna",            new NavEnhet("Alna", "oslo",                "970534644"))
            .put("bjerke",          new NavEnhet("Bjerke", "oslo",              "974778874"))
            .put("frogner",         new NavEnhet("Frogner", "oslo",             "874778702"))
            .put("gamleoslo",       new NavEnhet("Gamle Oslo", "oslo",          "974778742"))
            .put("grorud",          new NavEnhet("Grorud", "oslo",              "974778866"))
            .put("grunerlokka",     new NavEnhet("Grünerløkka", "oslo",         "870534612"))
            .put("nordreaker",      new NavEnhet("Nordre Aker", "oslo",         "974778882"))
            .put("nordstrand",      new NavEnhet("Nordstrand", "oslo",          "970534679"))
            .put("sagene",          new NavEnhet("Sagene", "oslo",              "974778726"))
            .put("sthanshaugen",    new NavEnhet("St.Hanshaugen", "oslo",       "971179686"))
            .put("stovner",         new NavEnhet("Stovner", "oslo",             "874778842"))
            .put("sondrenordstrand",new NavEnhet("Søndre Nordstrand", "oslo",   "972408875"))
            .put("ullern",          new NavEnhet("Ullern", "oslo",              "971022051"))
            .put("vestreaker",      new NavEnhet("Vestre Aker", "oslo",         "970145311"))
            .put("ostensjo",        new NavEnhet("Østensjø", "oslo",            "974778807"))
            .build();

    private static final Map<String, NavEnhet> mapper = velgMapperUtFraMiljo();


    /**
     * Angir kommune som søker må være bosatt i for å kunne komme inn på løsningen.
     *
     * @return Liste med kommunenumre.
     */
    public static List<String> getDigisoskommuner() {
        return isProduction() ? PROD_DIGISOS_KOMMUNER : TEST_DIGISOS_KOMMUNER;
    }

    public static Map<String, NavEnhet> getNavEnheter() {
        return mapper;
    }

    public static Map<String, String> getKommunerMedBydeler() {
        return isProduction() ? PROD_KOMMUNER_MED_BYDELER : TEST_KOMMUNER_MED_BYDELER;
    }

    public static NavEnhet getNavEnhetFromWebSoknad(WebSoknad webSoknad) {
        String key;
        if (webSoknad.getFaktumMedKey("personalia.bydel") == null || isEmpty(webSoknad.getFaktumMedKey("personalia.bydel").getValue())) {
            key = webSoknad.getFaktumMedKey("personalia.kommune").getValue();
        } else {
            key = webSoknad.getFaktumMedKey("personalia.bydel").getValue();
        }

        NavEnhet navEnhet = mapper.get(key);
        if (navEnhet == null) {
            throw new IllegalStateException("Innsendt kommune/bydel har ikke NAVEnhet");
        }
        return navEnhet;
    }

    private static Map<String, NavEnhet> velgMapperUtFraMiljo() {
        return isProduction() ? PROD_ORGNR : TEST_ORGNR;
    }

    private static boolean isProduction() {
        return "p".equals(System.getProperty("environment.name"));
    }
}
