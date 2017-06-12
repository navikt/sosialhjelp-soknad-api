package no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon;


import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.transformer.AlternativRepresentasjonTransformer;
import no.nav.sbl.dialogarena.sendsoknad.domain.transformer.foreldrepenger.engangsstonad.ForeldrepengerEngangsstonadTilXml;
import org.springframework.context.MessageSource;

import java.util.Arrays;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import no.nav.metrics.Event;
import no.nav.metrics.MetricsFactory;

public class ForeldrepengerInformasjon extends KravdialogInformasjon.DefaultOppsett {

    public static final List<String> STONADSTYPER_PERSONALIA = asList("overforing", "engangsstonadMor", "engangsstonadFar", "endringMor", "endringFar");

    public String getSoknadTypePrefix() {
        return "foreldresoknad";
    }

    public String getSoknadUrlKey() {
        return "foreldresoknad.path";
    }

    public String getFortsettSoknadUrlKey() {
        return "foreldresoknad.fortsett.path";
    }

    public String getStrukturFilnavn() {
        return "foreldrepenger/foreldrepenger.xml";
    }

    public List<String> getSkjemanummer() {
        return asList("NAV 14-05.06", "NAV 14-05.07", "NAV 14-05.08", "NAV 14-05.09", "NAV 14-05.10");
    }

    @Override
    public List<AlternativRepresentasjonTransformer> getTransformers(MessageSource messageSource, WebSoknad soknad) {
        List<String> engangsstonadSkjemanummerListe = Arrays.asList("NAV 14-05.07","NAV 14-05.08");
        if (alternativRepresentasjonAktivert() && engangsstonadSkjemanummerListe.contains(soknad.getskjemaNummer())) {
            Event event = MetricsFactory.createEvent("soknad.foreldrepenger.alternativrepresentasjon.aktiv");
            event.report();

            return singletonList(new ForeldrepengerEngangsstonadTilXml(messageSource));
        } else {
            return emptyList();
        }
    }

    @Override
    public String getBundleName() {
        return "foreldrepenger";
    }

    public List<String> getSoknadBolker(WebSoknad soknad) {
        Faktum stonadstype = soknad.getFaktumMedKey("soknadsvalg.stonadstype");
        if (stonadstype != null && STONADSTYPER_PERSONALIA.contains(stonadstype.getValue())) {
            return asList(BOLK_PERSONALIA);
        } else {
            return asList(BOLK_PERSONALIA, BOLK_BARN, BOLK_ARBEIDSFORHOLD);
        }
    }

    @Override
    public boolean brukerEnonicLedetekster() {
        return false;
    }

    private boolean alternativRepresentasjonAktivert() {
        return Boolean.valueOf(System.getProperty("soknad.feature.foreldrepenger.alternativrepresentasjon.enabled", "false"));
    }
}

