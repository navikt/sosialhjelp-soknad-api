package no.nav.sbl.dialogarena.soknadinnsending.business;


import no.nav.sbl.dialogarena.sendsoknad.domain.Steg;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.KravdialogInformasjon;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.KravdialogInformasjonHolder;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad.SoknadRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.inject.Named;

@Component
public class WebSoknadConfig {

    private static final Logger LOG = LoggerFactory.getLogger(WebSoknadConfig.class);

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

    private KravdialogInformasjon finnSkjemaConfig(Long soknadId) {
        String skjemanummer = repository.hentSoknadType(soknadId);
        return kravdialogInformasjonHolder.hentKonfigurasjon(skjemanummer);
    }

    public Steg[] getStegliste(Long soknadId) {
        KravdialogInformasjon skjemaConfig = finnSkjemaConfig(soknadId);
        return skjemaConfig.getStegliste();
    }
}
