package no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon;


import no.nav.metrics.Event;
import no.nav.metrics.MetricsFactory;
import no.nav.sbl.dialogarena.sendsoknad.domain.Steg;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.transformer.AlternativRepresentasjonTransformer;
import no.nav.sbl.dialogarena.sendsoknad.domain.transformer.refusjondagligreise.RefusjonDagligreiseTilXml;
import org.springframework.context.MessageSource;

import java.util.Arrays;
import java.util.List;

import static no.nav.sbl.dialogarena.sendsoknad.domain.Steg.OPPSUMMERING;
import static no.nav.sbl.dialogarena.sendsoknad.domain.Steg.SOKNAD;

public class SoknadRefusjonDagligreise extends KravdialogInformasjon.DefaultOppsett {
    public static final String VEDTAKPERIODER = "vedtakperioder";

    public String getSoknadTypePrefix() {
        return "soknadrefusjondagligreise";
    }

    public String getSoknadUrlKey() {
        return "soknadrefusjondagligreise.path";
    }

    public String getFortsettSoknadUrlKey() {
        return "soknadrefusjondagligreise.path";
    }

    public List<String> getSoknadBolker(WebSoknad soknad) {
        return Arrays.asList(BOLK_PERSONALIA, VEDTAKPERIODER);
    }

    public String getStrukturFilnavn() {
        return "refusjondagligreise.xml";
    }

    public List<String> getSkjemanummer() {
        return Arrays.asList("NAV 11-12.10", "NAV 11-12.11");
    }

    @Override
    public List<AlternativRepresentasjonTransformer> getTransformers(MessageSource messageSource, WebSoknad soknad) {
        AlternativRepresentasjonTransformer tilleggsstonaderTilXml = new RefusjonDagligreiseTilXml();
        Event event = MetricsFactory.createEvent("soknad.alternativrepresentasjon.aktiv");
        event.addTagToReport("skjemanummer", soknad.getskjemaNummer());
        event.report();
        return Arrays.asList(tilleggsstonaderTilXml);
    }

    @Override
    public String getBundleName() {
        return "refusjondagligreise";
    }

    @Override
    public Steg[] getStegliste() {
        return new Steg[]{SOKNAD, OPPSUMMERING};
    }

}
