package no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon;


import no.nav.metrics.Event;
import no.nav.metrics.MetricsFactory;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.transformer.AlternativRepresentasjonTransformer;
import no.nav.sbl.dialogarena.sendsoknad.domain.transformer.foreldrepenger.engangsstonad.ForeldrepengerEngangsstonadTilXml;
import org.springframework.context.MessageSource;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

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

    public static final List<String> FORSTEGANGSSOKNADER = asList("NAV 14-05.06", "NAV 14-05.09");
    public static final List<String> ENGANGSSTONADER = asList("NAV 14-05.07", "NAV 14-05.08");
    public static final List<String> ENDRING_OVERFORING = asList("NAV 14-05.10");

    public List<String> getSkjemanummer() {
        return Stream.concat(
                ENDRING_OVERFORING.stream(),
                Stream.concat(FORSTEGANGSSOKNADER.stream(),
                        ENGANGSSTONADER.stream())
        ).collect(Collectors.toList());
    }

    @Override
    public List<AlternativRepresentasjonTransformer> getTransformers(MessageSource messageSource, WebSoknad soknad) {
        if (ENGANGSSTONADER.contains(soknad.getskjemaNummer())) {
            Event event = MetricsFactory.createEvent("soknad.alternativrepresentasjon.aktiv");
            event.addTagToReport("skjemanummer", soknad.getskjemaNummer());
            event.addTagToReport("soknadstype", getSoknadTypePrefix());
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
}

