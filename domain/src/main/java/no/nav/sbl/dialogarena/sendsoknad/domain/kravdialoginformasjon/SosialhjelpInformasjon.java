package no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon;


import no.nav.metrics.Event;
import no.nav.metrics.MetricsFactory;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.transformer.AlternativRepresentasjonTransformer;
import no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp.SosialhjelpTilXml;
import org.springframework.context.MessageSource;

import java.util.Arrays;
import java.util.List;

import static java.util.Collections.singletonList;

public class SosialhjelpInformasjon extends KravdialogInformasjon.DefaultOppsett {

    public String getSoknadTypePrefix() {
        return "soknadsosialhjelp";
    }

    public String getSoknadUrlKey() {
        return "soknadsosialhjelp.path";
    }

    public String getFortsettSoknadUrlKey() {
        return "soknadsosialhjelp.fortsett.path";
    }

    public List<String> getSoknadBolker(WebSoknad soknad) {
        return Arrays.asList(BOLK_PERSONALIA);
    }

    public String getStrukturFilnavn() {
        return "sosialhjelp/sosialhjelp.xml";
    }

    //TODO ta i bruk riktig skjemanummer
    public List<String> getSkjemanummer() {
        return Arrays.asList("NAV 35-18.01");
    }

    @Override
    public List<AlternativRepresentasjonTransformer> getTransformers(MessageSource messageSource, WebSoknad soknad) {

        Event event = MetricsFactory.createEvent("soknad.alternativrepresentasjon.aktiv");
        event.addTagToReport("skjemanummer", soknad.getskjemaNummer());
        event.addTagToReport("soknadstype", getSoknadTypePrefix());
        event.report();
        return singletonList(new SosialhjelpTilXml(messageSource));
    }

    @Override
    public String getBundleName() {
        return "soknadsosialhjelp";
    }

    @Override
    public boolean brukerNyOppsummering() {
        return true;
    }

    @Override
    public boolean brukerEnonicLedetekster() {
        return false;
    }

    @Override
    public SoknadType getSoknadstype() {
        return SoknadType.SEND_SOKNAD_KOMMUNAL;
    }
}