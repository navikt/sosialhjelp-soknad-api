package no.nav.sbl.dialogarena.soknadinnsending.business;


import no.nav.modig.core.exception.ApplicationException;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad.SoknadRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Steg;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.oppsett.SoknadStruktur;
import no.nav.sbl.dialogarena.soknadinnsending.business.kravdialoginformasjon.KravdialogInformasjon;
import no.nav.sbl.dialogarena.soknadinnsending.business.kravdialoginformasjon.KravdialogInformasjonHolder;
import no.nav.sbl.dialogarena.soknadinnsending.business.person.BolkService;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.inject.Named;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static java.lang.String.format;
import static javax.xml.bind.JAXBContext.newInstance;

@Component
public class WebSoknadConfig {

    @Inject
    @Named("soknadInnsendingRepository")
    private SoknadRepository repository;

    @Inject
    private KravdialogInformasjonHolder kravdialogInformasjonHolder;

    public String getSoknadTypePrefix(long soknadId) {
        KravdialogInformasjon skjemaConfig = finnSkjemaConfig(soknadId);
        return skjemaConfig.getSoknadTypePrefix();
    }

    public String getSoknadUrl(long soknadId) {
        KravdialogInformasjon skjemaConfig = finnSkjemaConfig(soknadId);
        return System.getProperty(skjemaConfig.getSoknadUrlKey());
    }

    public String getFortsettSoknadUrl(long soknadId) {
        KravdialogInformasjon skjemaConfig = finnSkjemaConfig(soknadId);
        return System.getProperty(skjemaConfig.getFortsettSoknadUrlKey());
    }

    @Deprecated
    public SoknadStruktur hentStruktur(long soknadId) {
        KravdialogInformasjon skjemaConfig = finnSkjemaConfig(soknadId);
        return hentStrukturForSkjemanavn(skjemaConfig);
    }

    public SoknadStruktur hentStruktur(String skjemaNummer) {
        KravdialogInformasjon skjemaConfig = kravdialogInformasjonHolder.hentKonfigurasjon(skjemaNummer);
        return hentStrukturForSkjemanavn(skjemaConfig);
    }

    private SoknadStruktur hentStrukturForSkjemanavn(KravdialogInformasjon skjemaConfig) {
        String type = skjemaConfig.getStrukturFilnavn();
        if (type == null || type.isEmpty()) {
            throw new ApplicationException("Fant ikke strukturdokument for skjema: " + skjemaConfig.getClass().getSimpleName());
        }

        try {
            Unmarshaller unmarshaller = newInstance(SoknadStruktur.class).createUnmarshaller();
            return (SoknadStruktur) unmarshaller.unmarshal(SoknadStruktur.class.getResourceAsStream(format("/soknader/%s", type)));
        } catch (JAXBException e) {
            throw new RuntimeException("Kunne ikke laste definisjoner. ", e);
        }
    }

    public List<BolkService> getSoknadBolker(WebSoknad soknad, Collection<BolkService> alleBolker) {
        KravdialogInformasjon skjemaConfig = kravdialogInformasjonHolder.hentKonfigurasjon(soknad.getskjemaNummer());
        List<String> configBolker = skjemaConfig.getSoknadBolker(soknad);

        List<BolkService> soknadBolker = new ArrayList<>();
        for (BolkService bolk : alleBolker) {
            if (configBolker.contains(bolk.tilbyrBolk())) {
                soknadBolker.add(bolk);
            }
        }
        return soknadBolker;
    }

    private KravdialogInformasjon finnSkjemaConfig(Long soknadId) {
        String skjemanummer = repository.hentSoknadType(soknadId);
        return kravdialogInformasjonHolder.hentKonfigurasjon(skjemanummer);
    }

    public Steg[] getStegliste(Long soknadId) {
        KravdialogInformasjon skjemaConfig = finnSkjemaConfig(soknadId);
        return skjemaConfig.getStegliste();
    }
}
