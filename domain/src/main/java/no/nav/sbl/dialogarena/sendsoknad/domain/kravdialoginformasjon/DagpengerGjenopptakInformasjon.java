package no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon;


import no.nav.metrics.Event;
import no.nav.metrics.MetricsFactory;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.transformer.AlternativRepresentasjonTransformer;
import no.nav.sbl.dialogarena.sendsoknad.domain.transformer.dagpenger.ordinaer.DagpengerTilJson;
import org.springframework.context.MessageSource;

import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

@SuppressWarnings("squid:")
public class DagpengerGjenopptakInformasjon extends KravdialogInformasjon.DefaultOppsett {

    private static List<String> skjemanummer = asList("NAV 04-16.03", "NAV 04-16.04");

    public String getSoknadTypePrefix() {
        return "dagpenger.gjenopptak";
    }

    public String getSoknadUrlKey() {
        return "soknad.dagpenger.gjenopptak.path";
    }

    public String getFortsettSoknadUrlKey() {
        return "soknad.dagpenger.fortsett.path";
    }

    public String getStrukturFilnavn() {
        return "dagpenger/dagpenger_gjenopptak.xml";
    }

    public List<String> getSkjemanummer() {
        return skjemanummer;
    }

    public List<String> getSoknadBolker(WebSoknad soknad) {
        return asList(BOLK_PERSONALIA, BOLK_BARN);
    }

    @Override
    public boolean brukerNyOppsummering() {
        return true;
    }

    @Override
    public boolean skalSendeMedFullSoknad() {
        return true;
    }

    @Override
    public String getBundleName() {
        return "dagpenger";
    }

    public static boolean erDagpengerGjenopptak(String skjema) {
        return skjemanummer.contains(skjema);
    }

    @Override
    public List<AlternativRepresentasjonTransformer> getTransformers(MessageSource messageSource, WebSoknad soknad) {
        Event event = MetricsFactory.createEvent("soknad.alternativrepresentasjon.aktiv");
        event.addTagToReport("skjemanummer", soknad.getskjemaNummer());
        event.addTagToReport("soknadstype", getSoknadTypePrefix());
        event.report();

        return singletonList(new DagpengerTilJson(getSoknadTypePrefix()));
    }
}
