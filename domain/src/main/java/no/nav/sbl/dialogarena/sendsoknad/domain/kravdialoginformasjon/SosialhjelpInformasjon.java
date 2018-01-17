package no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon;


import no.nav.metrics.Event;
import no.nav.metrics.MetricsFactory;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.transformer.AlternativRepresentasjonTransformer;
import no.nav.sbl.dialogarena.sendsoknad.domain.transformer.EkstraMetadataTransformer;
import no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp.FiksMetadataTransformer;
import no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp.SosialhjelpTilXml;
import no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp.SosialhjelpVedleggTilJson;
import org.springframework.context.MessageSource;

import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

public class SosialhjelpInformasjon extends KravdialogInformasjon.DefaultOppsett {

    public static final String SKJEMANUMMER = "NAV 35-18.01";

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
        return asList(BOLK_PERSONALIA, "SosialhjelpArbeidsforhold", "SosialhjelpKontakt");
    }

    public String getStrukturFilnavn() {
        return "sosialhjelp/sosialhjelp.xml";
    }

    public List<String> getSkjemanummer() {
        return asList(SKJEMANUMMER);
    }

    @Override
    public List<AlternativRepresentasjonTransformer> getTransformers(MessageSource messageSource, WebSoknad soknad) {

        Event event = MetricsFactory.createEvent("soknad.alternativrepresentasjon.aktiv");
        event.addTagToReport("skjemanummer", soknad.getskjemaNummer());
        event.addTagToReport("soknadstype", getSoknadTypePrefix());
        event.report();
        return asList(new SosialhjelpTilXml(messageSource), new SosialhjelpVedleggTilJson());
    }

    @Override
    public List<EkstraMetadataTransformer> getMetadataTransformers() {
        return singletonList(new FiksMetadataTransformer());
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
    public boolean skalSendeMedFullSoknad() {
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

    @Override
    public String getKvitteringTemplate() {
        return "/skjema/sosialhjelp/kvittering";
    }
}