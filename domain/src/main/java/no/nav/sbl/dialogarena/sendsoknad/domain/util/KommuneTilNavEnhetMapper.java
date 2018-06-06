package no.nav.sbl.dialogarena.sendsoknad.domain.util;

import static java.util.Arrays.asList;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.springframework.util.CollectionUtils.isEmpty;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;

import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;


public class KommuneTilNavEnhetMapper {

    private static final Logger log = LoggerFactory.getLogger(KommuneTilNavEnhetMapper.class);

    private static final Map<String, String> TEST_ORGANISASJONSNUMMER = new ImmutableMap.Builder<String, String>()
            .put("0701","910940066")
            .put("1209", "910230158")
            .put("1210", "910230158")
            .put("0312", "910229699")
            .put("1247", "910230182")
            .put("0315", "811213322")
            .put("0328", "910229702")
            .put("0327", "910589792")
            .put("0314", "910565338")
            .put("0318", "910309935")
            .put("0319", "910723499")
            .build();
    private static final Map<String, Map<String, Boolean>> FEATURES_FOR_ENHET = new HashMap<>();

    public static String getTestOrganisasjonsnummer(String enhetNr) {
        return TEST_ORGANISASJONSNUMMER.get(enhetNr);
    }

    public static Map<String, Boolean> getFeaturesForEnhet(String enhetNr) {
        final Map<String, Boolean> featuresAndDefaults = new HashMap<>(defaultFeatures());
        final Map<String, Boolean> featuresForEnhet = FEATURES_FOR_ENHET.get(enhetNr);
        if (!isEmpty(featuresForEnhet)) {
            featuresAndDefaults.putAll(FEATURES_FOR_ENHET.get(enhetNr));
        }
        return featuresAndDefaults;
    }

    private static Map<String, Boolean> defaultFeatures() {
        final Map<String, Boolean> features = new HashMap<>();
        features.put("ettersendelse", true);
        return features;
    }

    public static Soknadsmottaker getSoknadsmottaker(WebSoknad webSoknad) {
        final Faktum nyttFaktum = webSoknad.getFaktumMedKey("soknadsmottaker");
        final Map<String, String> properties = nyttFaktum.getProperties();
        if (nyttFaktum != null && !StringUtils.isEmpty(properties.get("sosialOrgnr"))) {
            final String sosialOrgnr = properties.get("sosialOrgnr");
            final String enhetsnavn = properties.get("enhetsnavn");
            final String kommunenavn = properties.get("kommunenavn");
            if (StringUtils.isEmpty(enhetsnavn)) {
                throw new IllegalStateException("Mangler enhetsnavn.");
            }
            if (StringUtils.isEmpty(kommunenavn)) {
                throw new IllegalStateException("Mangler kommunenavn.");
            }
            
            return new Soknadsmottaker(sosialOrgnr, enhetsnavn, kommunenavn);
        }
        
        final NavEnhet navEnhet = getNavEnhetFromWebSoknad(webSoknad);
        return new Soknadsmottaker(navEnhet.getOrgnummer(), "NAV " + navEnhet.getKontornavn(), navEnhet.getKommunenavn());
    }
    
    public static final class Soknadsmottaker {
        private final String sosialOrgnr;
        private final String enhetsnavn;
        private final String kommunenavn;
        
        
        public Soknadsmottaker(String sosialOrgnr, String enhetsnavn, String kommunenavn) {
            this.sosialOrgnr = sosialOrgnr;
            this.enhetsnavn = enhetsnavn;
            this.kommunenavn = kommunenavn;
        }
        
        
        public String getSosialOrgnr() {
            return sosialOrgnr;
        }
        
        public String getEnhetsnavn() {
            return enhetsnavn;
        }
        
        public String getKommunenavn() {
            return kommunenavn;
        }
    }

    // Alt er deprecated under -- fjernes 2 uker etter ny versjon (grunnet gamle lagrede søknader):

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

    private static final List<String> TEST_DIGISOS_KOMMUNER = Collections.unmodifiableList(asList("0701", "0703", "0717", "1201", "0301", "1247"));

    private static final List<String> PROD_DIGISOS_KOMMUNER = Collections.unmodifiableList(asList("0701", "0703", "0717", "1201", "0301", "1247"));

    private static final Map<String, String> TEST_KOMMUNENAVN = new ImmutableMap.Builder<String, String>()
            .put("oslo", "Oslo")
            .put("bergen", "Bergen")
            .build();

    private static final Map<String, String> PROD_KOMMUNENAVN = new ImmutableMap.Builder<String, String>()
            .put("oslo", "Oslo")
            .put("bergen", "Bergen")
            .build();


    private static final Map<String, NavEnhet> TEST_ORGNR = new ImmutableMap.Builder<String, NavEnhet>()
            .put("horten", new NavEnhet("Horten", null, "910940066"))
            .put("bergenhus", new NavEnhet("Bergenhus", "bergen", "910230158", Collections.singletonMap("ettersendelse", false)))
            .put("ytrebygda", new NavEnhet("Ytrebygda", "bergen", "910230158", Collections.singletonMap("ettersendelse", false)))
            .put("frogner", new NavEnhet("Frogner", "oslo", "910229699"))
            .put("askoy", new NavEnhet("Askøy", null, "910230182"))
            .put("grunerlokka", new NavEnhet("Grünerløkka", "oslo", "811213322"))
            .put("grorud", new NavEnhet("Grorud", "oslo", "910229702"))
            .put("stovner", new NavEnhet("Stovner", "oslo", "910589792"))
            .put("sagene", new NavEnhet("Sagene", "oslo", "910565338"))
            .put("nordstrand", new NavEnhet("Nordstrand", "oslo", "910309935"))
            .put("sondreNordstrand", new NavEnhet("Søndre Nordstrand", "oslo", "910723499"))
            .build();

    private static final Map<String, NavEnhet> PROD_ORGNR = new ImmutableMap.Builder<String, NavEnhet>()
            .put("horten",          new NavEnhet("Horten", null,                "974605171"))
            .put("askoy",           new NavEnhet("Askøy", null,                 "974600889"))

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
        return isProduction() ? PROD_KOMMUNENAVN : TEST_KOMMUNENAVN;
    }
    
    private static NavEnhet getNavEnhetFromWebSoknad(WebSoknad webSoknad) {       
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
