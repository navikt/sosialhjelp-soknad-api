package no.nav.sbl.dialogarena.soknadinnsending.business;


import no.nav.modig.core.exception.ApplicationException;
import no.nav.sbl.dialogarena.soknadinnsending.business.config.SoknadConfig;
import no.nav.sbl.dialogarena.soknadinnsending.business.config.SoknadConfigUtil;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad.SoknadRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.oppsett.SoknadStruktur;
import no.nav.sbl.dialogarena.soknadinnsending.business.person.BolkService;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;
import static javax.xml.bind.JAXBContext.newInstance;

public class WebSoknadConfig {
    private static SoknadRepository repository;
    public static final String BOLK_PERSONALIA = "Personalia";
    public static final String BOLK_BARN = "Barn";
    public static final String BOLK_ARBEIDSFORHOLD = "Arbeidsforhold";

    public WebSoknadConfig(SoknadRepository repository) { //må ta inn behandlingsid eller søknad for å hente rett skjemanummer?
        this.repository = repository;
    }

    public WebSoknadConfig() {
    }

    public String getSoknadTypePrefix (long soknadId) {
        SoknadConfig skjemaConfig = finnSkjemaConfig(soknadId);
        return skjemaConfig.getSoknadTypePrefix();
    }

    public String getSoknadUrl (long soknadId) {
        SoknadConfig skjemaConfig = finnSkjemaConfig(soknadId);
        return skjemaConfig.getSoknadUrl();
    }

    public String getFortsettSoknadUrl(long soknadId) {
        SoknadConfig skjemaConfig = finnSkjemaConfig(soknadId);
        return skjemaConfig.getFortsettSoknadUrl();
    }

    public SoknadStruktur hentStruktur (long soknadId) {
        SoknadConfig skjemaConfig = finnSkjemaConfig(soknadId);
        return hentStrukturForSkjemanavn(skjemaConfig);
    }

    public SoknadStruktur hentStruktur (String skjemaNummer) {
        SoknadConfig skjemaConfig = SoknadConfigUtil.getConfig(skjemaNummer);
        return hentStrukturForSkjemanavn(skjemaConfig);
    }

    private SoknadStruktur hentStrukturForSkjemanavn(SoknadConfig skjemaConfig) {
        String type = skjemaConfig.hentStruktur();
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
        SoknadConfig skjemaConfig = finnSkjemaConfig(soknadId);
        List<String> configBolker = skjemaConfig.getSoknadBolker();

        List<BolkService> soknadBolker = new ArrayList<>();
        for(BolkService bolk : alleBolker){
            if(configBolker.contains(bolk.tilbyrBolk())){
                soknadBolker.add(bolk);
            }
        }
        return soknadBolker;
    }

    private SoknadConfig finnSkjemaConfig(Long soknadId) {
        String skjemanummer = repository.hentSoknadType(soknadId);
        return SoknadConfigUtil.getConfig(skjemanummer);
    }
}
