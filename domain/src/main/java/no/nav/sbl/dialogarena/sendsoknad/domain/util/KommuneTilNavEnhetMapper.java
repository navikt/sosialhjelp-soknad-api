package no.nav.sbl.dialogarena.sendsoknad.domain.util;

import static java.util.Arrays.asList;
import static org.apache.commons.lang3.StringUtils.isEmpty;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;

import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;

public class KommuneTilNavEnhetMapper {

    private static final Logger log = LoggerFactory.getLogger(KommuneTilNavEnhetMapper.class);
    
    public static class NavEnhet {

        private String kontornavn;
        private String kommune;
        private String orgnummer;

        NavEnhet(String kontornavn, String kommune, String orgnummer) {
            this.kontornavn = kontornavn;
            this.kommune = kommune;
            this.orgnummer = orgnummer;
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
            final String kommunenavn = kommunenavnMapper.get(kommune);
            if (kommunenavn == null) {
                log.error("Mangler navn på kommune: " + kommune);
                return kommune;
            }
            return kommunenavn;
        }
    }
    

    private static final List<String> TEST_DIGISOS_KOMMUNER = Collections.unmodifiableList(asList("0701", "0703", "0717", "1201", "0301"));
    
    private static final List<String> PROD_DIGISOS_KOMMUNER = Collections.unmodifiableList(asList("0701", "0703", "0717", "1201", "0301"));

    private static final Map<String, String> kommunenavnMapper = new ImmutableMap.Builder<String, String>()
            .put("oslo", "Oslo")
            .put("bergen", "Bergen")
            .build();
    
    private static final Map<String, NavEnhet> TEST_ORGNR = new ImmutableMap.Builder<String, NavEnhet>()
            .put("horten", new NavEnhet("Horten", null, "910940066"))
            .put("bergenhus", new NavEnhet("Bergenhus", "bergen", "910230158"))
            .put("ytrebygda", new NavEnhet("Ytrebygda", "bergen", "910230158"))
            .put("frogner", new NavEnhet("Frogner", "oslo", "910229699"))
            .put("askoy", new NavEnhet( "Askøy", null, "910230182"))
            .put("grunerlokka", new NavEnhet("Grünerløkka", "oslo", "811213322"))
            .put("grorud", new NavEnhet("Grorud", "oslo", "910229702"))
            .build();


    private static final Map<String, NavEnhet> PROD_ORGNR = new ImmutableMap.Builder<String, NavEnhet>()
            .put("horten", new NavEnhet("Horten", null, "974605171"))
            .put("bergenhus", new NavEnhet("Bergenhus", "bergen", "976830563"))
            .put("ytrebygda", new NavEnhet("Ytrebygda", "bergen", "976830652"))
            .put("gamleoslo", new NavEnhet("Gamle Oslo", "oslo", "974778742")) 
            //.put("frogner", new NavEnhet("Frogner", "oslo", "874778702")) //OK
            //.put("grunerlokka", new NavEnhet("Grünerløkka", "oslo", "870534612")) //OK
            //.put("grorud", new NavEnhet("Grorud", "oslo", "974778866")) //OK
            .build();

    private static final Map<String, NavEnhet> mapper = velgMapperUtFraMiljo();


    /**
     * Angir kommune som søker må være bosatt i for å kunne komme inn på løsningen.
     * @return Liste med kommunenumre.
     */
    public static List<String> getDigisoskommuner() {
        return isProduction() ? PROD_DIGISOS_KOMMUNER : TEST_DIGISOS_KOMMUNER;
    }
    
    public static Map<String, NavEnhet> getNavEnheter() {
        return mapper;
    }

    public static Map<String, String> getKommunerMedBydeler() {
        return kommunenavnMapper;
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
