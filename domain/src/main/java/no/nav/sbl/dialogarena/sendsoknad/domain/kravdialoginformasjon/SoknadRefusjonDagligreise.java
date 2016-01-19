package no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon;


import no.nav.sbl.dialogarena.sendsoknad.domain.AlternativRepresentasjon;
import no.nav.sbl.dialogarena.sendsoknad.domain.Steg;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.transformer.refusjondagligreise.RefusjonDagligreiseTilXml;
import org.apache.commons.collections15.Transformer;
import org.springframework.context.MessageSource;

import java.util.Arrays;
import java.util.List;

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
    public List<Transformer<WebSoknad, AlternativRepresentasjon>> getTransformers(MessageSource messageSource) {
        Transformer<WebSoknad, AlternativRepresentasjon> tilleggsstonaderTilXml = new RefusjonDagligreiseTilXml();
        return Arrays.asList(tilleggsstonaderTilXml);
    }
    @Override
    public Steg[] getStegliste() {
        return new Steg[]{Steg.SOKNAD, Steg.OPPSUMMERING};
    }

}
