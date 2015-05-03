package no.nav.sbl.dialogarena.soknadinnsending.business;


import no.nav.modig.core.exception.ApplicationException;
import no.nav.sbl.dialogarena.soknadinnsending.business.config.AAPConfig;
import no.nav.sbl.dialogarena.soknadinnsending.business.config.DagpengerGjenopptakConfig;
import no.nav.sbl.dialogarena.soknadinnsending.business.config.DagpengerOrdinaerConfig;
import no.nav.sbl.dialogarena.soknadinnsending.business.config.ForeldrepengerConfig;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad.SoknadRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.oppsett.SoknadStruktur;
import no.nav.sbl.dialogarena.soknadinnsending.business.person.BolkService;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

import static java.lang.String.format;
import static javax.xml.bind.JAXBContext.newInstance;

public class WebSoknadConfig {
    private static SoknadRepository repository;

    public static final Object DAGPENGER_ORDINAER = new DagpengerOrdinaerConfig();
    public static final Object DAGPENGER_GJENOPPTAK = new DagpengerGjenopptakConfig();
    public static final Object FORELDREPENGER = new ForeldrepengerConfig();
    public static final Object AAP = new AAPConfig();

    public static final String BOLK_PERSONALIA = "Personalia";
    public static final String BOLK_BARN = "Barn";
    public static final String BOLK_ARBEIDSFORHOLD = "Arbeidsforhold";

    public static final Map<String, Object> SKJEMANAVN = new HashMap<String, Object>() {{
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


    public WebSoknadConfig(SoknadRepository repository) { //må ta inn behandlingsid eller søknad for å hente rett skjemanummer?
        this.repository = repository;
    }

    public WebSoknadConfig() {
    }

    public String getSoknadTypePrefix (long soknadId) {
        Object skjemaConfig = finnSkjemaConfig(soknadId);
        return invokeConfigMetoder("getSoknadTypePrefix", skjemaConfig).toString();
    }

    public String getSoknadUrl (long soknadId) {
        Object skjemaConfig = finnSkjemaConfig(soknadId);
        return invokeConfigMetoder("getSoknadUrl", skjemaConfig).toString();
    }

    public String getFortsettSoknadUrl(long soknadId) {
        Object skjemaConfig = finnSkjemaConfig(soknadId);
        return invokeConfigMetoder("getFortsettSoknadUrl", skjemaConfig).toString();
    }

    public SoknadStruktur hentStruktur (long soknadId) {
        Object skjemaConfig = finnSkjemaConfig(soknadId);
        return hentStrukturForSkjemanavn(skjemaConfig);
    }

    public SoknadStruktur hentStruktur (String skjemaNummer) {
        Object skjemaConfig = SKJEMANAVN.get(skjemaNummer);
        return hentStrukturForSkjemanavn(skjemaConfig);
    }

    private SoknadStruktur hentStrukturForSkjemanavn(Object skjemaConfig) {
        String type = invokeConfigMetoder("hentStruktur", skjemaConfig).toString();

        if (type == null || type.isEmpty()) {
            throw new ApplicationException("Fant ikke strukturdokument for skjema: " + skjemaConfig.getClass().getSimpleName());
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
        Object skjemaConfig = finnSkjemaConfig(soknadId);
        List<String> configBolker = (List<String>) invokeConfigMetoder("getSoknadBolker", skjemaConfig);

        List<BolkService> soknadBolker = new ArrayList<>();
        for(BolkService bolk : alleBolker){
            if(configBolker.contains(bolk.tilbyrBolk())){
                soknadBolker.add(bolk);
            }
        }

        return soknadBolker;
    }

    private Object finnSkjemaConfig(Long soknadId) {

        String skjemanummer = repository.hentSoknadType(soknadId);
        Object skjemaConfig = "";
        if (SKJEMANAVN.containsKey(skjemanummer)) {
            skjemaConfig = SKJEMANAVN.get(skjemanummer);
        }
        return skjemaConfig;
    }

    private Object invokeConfigMetoder(String metodenavn, Object skjemaConfig) {
        Object result = "";
        try {
            Method metode = skjemaConfig.getClass().getMethod(metodenavn, null);
            result = metode.invoke(skjemaConfig, null);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return result;
    }

}
