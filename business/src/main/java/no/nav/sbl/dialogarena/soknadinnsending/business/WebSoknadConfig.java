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

public class WebSoknadConfig {
    private String skjemaNavn = "";

    public static final String DAGPENGER_ORDINAER = "dagpengerOrdinaer";
    public static final String DAGPENGER_GJENOPPTAK = "dagpengerGjenopptak";
    public static final String FORELDREPENGER = "foreldrepenger";
    public static final String AAP = "aap";

    private static final String BOLK_PERSONALIA_CLASS = "PersonaliaService";
    private static final String BOLK_BARN_CLASS = "BarnService";

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

    private static final Map<String, List<String>> SOKNAD_BOLKER = new HashMap<String, List<String>> () {{
       put(DAGPENGER_ORDINAER, Arrays.asList(BOLK_PERSONALIA_CLASS, BOLK_BARN_CLASS));
       put(DAGPENGER_GJENOPPTAK, Arrays.asList(BOLK_PERSONALIA_CLASS, BOLK_BARN_CLASS));
       put(AAP, Arrays.asList(BOLK_PERSONALIA_CLASS, BOLK_BARN_CLASS));
       put(FORELDREPENGER, Arrays.asList(BOLK_PERSONALIA_CLASS, BOLK_BARN_CLASS));
    }};

    public WebSoknadConfig(Long soknadId, SoknadRepository repository) { //må ta inn behandlingsid eller søknad for å hente rett skjemanummer?
        String skjemanummer = repository.hentSoknadType(soknadId);
        System.out.println(skjemanummer);
        if (SKJEMANAVN.containsKey(skjemanummer)) {
            skjemaNavn = SKJEMANAVN.get(skjemanummer);
        }
    }

    public WebSoknadConfig(String skjemanummer) {
        if(SKJEMANAVN.containsKey(skjemanummer)){
            skjemaNavn = SKJEMANAVN.get(skjemanummer);
        }
    }

    public String getSoknadTypePrefix () {
        if (SOKNAD_TYPE_PREFIX_MAP.containsKey(skjemaNavn)) {
            return SOKNAD_TYPE_PREFIX_MAP.get(skjemaNavn);
        }
        else{ return ""; }
    }

    public String getSoknadUrl () {
        if (SOKNAD_URL_FASIT_RESSURS.containsKey(skjemaNavn)) {
            return System.getProperty(SOKNAD_URL_FASIT_RESSURS.get(skjemaNavn));
        }
        else{ return ""; }
    }

    public String getSoknadFortsettUrl () {
        if (SOKNAD_FORTSETT_URL_FASIT_RESSURS.containsKey(skjemaNavn)) {
            return System.getProperty(SOKNAD_FORTSETT_URL_FASIT_RESSURS.get(skjemaNavn));
        }
        else{ return ""; }
    }

    public SoknadStruktur hentStruktur () {
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

    public List<BolkService> getSoknadBolker (List<BolkService> alleBolker) {
        List<BolkService> soknadBolker = new ArrayList<>();
        if (SOKNAD_BOLKER.containsKey(skjemaNavn)) {
            for(BolkService bolk : alleBolker){
                if(SOKNAD_BOLKER.get(skjemaNavn).contains(bolk.getClass().getSimpleName())){
                    soknadBolker.add(bolk);
                }
            }
        }
        return soknadBolker;
    }


}
