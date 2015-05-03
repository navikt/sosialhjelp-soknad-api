package no.nav.sbl.dialogarena.soknadinnsending.business;


import no.nav.modig.core.exception.ApplicationException;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad.SoknadRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.oppsett.SoknadStruktur;
import no.nav.sbl.dialogarena.soknadinnsending.business.person.BolkService;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.util.*;

import static java.lang.String.format;
import static javax.xml.bind.JAXBContext.newInstance;

//Dette er et resultat av den første refaktureringen av søknads-configene. Det ligger lapp i backloggen på å splitte denne filen inn i én fil for hver søknad.
public class WebSoknadConfig {
    private static SoknadRepository repository;

    public static final String DAGPENGER_ORDINAER = "dagpengerOrdinaer";
    public static final String DAGPENGER_GJENOPPTAK = "dagpengerGjenopptak";
    public static final String FORELDREPENGER = "foreldrepenger";
    public static final String AAP = "aap";

    public static final String BOLK_PERSONALIA = "Personalia";
    public static final String BOLK_BARN = "Barn";
    public static final String BOLK_ARBEIDSFORHOLD = "Arbeidsforhold";

    public static final Map<String, String> SKJEMANAVN = new HashMap<String, String>() {{
        put("NAV 04-01.03", DAGPENGER_ORDINAER);
        put("NAV 04-01.04", DAGPENGER_ORDINAER);

        put("NAV 04-16.03", DAGPENGER_GJENOPPTAK);
        put("NAV 04-16.04", DAGPENGER_GJENOPPTAK);

        put("NAV 11-13.05", AAP);

        put("NAV 14-05.09", FORELDREPENGER);
        put("NAV 14-05.06", FORELDREPENGER);
        put("NAV 14-05.07", FORELDREPENGER);
        put("NAV 14-05.08", FORELDREPENGER);
    }};

    private static final Map<String, String> SOKNAD_TYPE_PREFIX_MAP = new HashMap<String, String>() {{
        put(AAP, "aap.ordinaer");
        put(DAGPENGER_ORDINAER, "dagpenger.ordinaer");
        put(DAGPENGER_GJENOPPTAK, "dagpenger.gjenopptak");
        put(FORELDREPENGER, "foreldresoknad");
    }};

    private static final Map<String, String> SOKNAD_URL_FASIT_RESSURS = new HashMap<String, String>() {{
        put(AAP, "soknad.aap.ordinaer.path");
        put(DAGPENGER_ORDINAER, "soknad.dagpenger.ordinaer.path");
        put(DAGPENGER_GJENOPPTAK, "soknad.dagpenger.gjenopptak.path");
        put(FORELDREPENGER, "foreldresoknad.path");
    }};

    private static final Map<String, String> SOKNAD_FORTSETT_URL_FASIT_RESSURS =  new HashMap<String, String> (){{
        put(AAP, "soknad.aap.fortsett.path");
        put(DAGPENGER_ORDINAER, "soknad.dagpenger.fortsett.path");
        put(DAGPENGER_GJENOPPTAK, "soknad.dagpenger.fortsett.path");
        put(FORELDREPENGER, "foreldresoknad.fortsett.path");
    }};

    private static final Map<String, String> STRUKTURDOKUEMENTER = new HashMap<String, String> (){{
        put(AAP, "aap_ordinaer.xml");
        put(DAGPENGER_ORDINAER, "dagpenger_ordinaer.xml");
        put(DAGPENGER_GJENOPPTAK, "dagpenger_gjenopptak.xml");
        put(FORELDREPENGER, "foreldresoknad.xml");
    }};

    public static final Map<String, List<String>> SOKNAD_BOLKER = new HashMap<String, List<String>> () {{
       put(DAGPENGER_ORDINAER, Arrays.asList( BOLK_PERSONALIA, BOLK_BARN));
       put(DAGPENGER_GJENOPPTAK, Arrays.asList(BOLK_PERSONALIA, BOLK_BARN));
       put(AAP, Arrays.asList(BOLK_PERSONALIA, BOLK_BARN));
       put(FORELDREPENGER, Arrays.asList(BOLK_PERSONALIA, BOLK_BARN, BOLK_ARBEIDSFORHOLD));
    }};

    public WebSoknadConfig(SoknadRepository repository) { //må ta inn behandlingsid eller søknad for å hente rett skjemanummer?
        this.repository = repository;
    }

    public WebSoknadConfig() {
    }

    public String getSoknadTypePrefix (long soknadId) {
        String skjemaNavn = finnSkjemaNavn(soknadId);
        if (SOKNAD_TYPE_PREFIX_MAP.containsKey(skjemaNavn)) {
            return SOKNAD_TYPE_PREFIX_MAP.get(skjemaNavn);
        }
        else{ return ""; }
    }

    public String getSoknadUrl (long soknadId) {
        String skjemaNavn = finnSkjemaNavn(soknadId);
        if (SOKNAD_URL_FASIT_RESSURS.containsKey(skjemaNavn)) {
            return System.getProperty(SOKNAD_URL_FASIT_RESSURS.get(skjemaNavn));
        }
        else{ return ""; }
    }

    public String getFortsettSoknadUrl(long soknadId) {
        String skjemaNavn = finnSkjemaNavn(soknadId);
        if (SOKNAD_FORTSETT_URL_FASIT_RESSURS.containsKey(skjemaNavn)) {
            return System.getProperty(SOKNAD_FORTSETT_URL_FASIT_RESSURS.get(skjemaNavn));
        }
        else{ return ""; }
    }

    public SoknadStruktur hentStruktur (long soknadId) {
        String skjemaNavn = finnSkjemaNavn(soknadId);
        return hentStrukturForSkjemanavn(skjemaNavn);
    }

    public SoknadStruktur hentStruktur (String skjemaNummer) {
        String skjemaNavn = SKJEMANAVN.get(skjemaNummer);
        return hentStrukturForSkjemanavn(skjemaNavn);
    }

    private SoknadStruktur hentStrukturForSkjemanavn(String skjemaNavn) {
        String type = STRUKTURDOKUEMENTER.get(skjemaNavn);

        if (type == null || type.isEmpty()) {
            throw new ApplicationException("Fant ikke strukturdokument for nav-skjemanummer: " + skjemaNavn);
        }

        try {
            Unmarshaller unmarshaller = newInstance(SoknadStruktur.class)
                    .createUnmarshaller();
            return (SoknadStruktur) unmarshaller.unmarshal(SoknadStruktur.class
                    .getResourceAsStream(format("/soknader/%s", type)));
        } catch (JAXBException e) {
            throw new RuntimeException("Kunne ikke laste definisjoner. ", e);
        }
    }


    public List<BolkService> getSoknadBolker (Long soknadId, List<BolkService> alleBolker) {
        String skjemaNavn = finnSkjemaNavn(soknadId);
        List<BolkService> soknadBolker = new ArrayList<>();
        if (SOKNAD_BOLKER.containsKey(skjemaNavn)) {
            for(BolkService bolk : alleBolker){
                if(SOKNAD_BOLKER.get(skjemaNavn).contains(bolk.tilbyrBolk())){
                    soknadBolker.add(bolk);
                }
            }
        }
        return soknadBolker;
    }

    private String finnSkjemaNavn(Long soknadId) {

        String skjemanummer = repository.hentSoknadType(soknadId);
        String skjemaNavn = "";
        if (SKJEMANAVN.containsKey(skjemanummer)) {
            skjemaNavn = SKJEMANAVN.get(skjemanummer);
        }
        return skjemaNavn;
    }

}
