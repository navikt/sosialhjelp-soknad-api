package no.nav.sbl.dialogarena.soknadinnsending.business.kravdialoginformasjon;

import no.nav.sbl.dialogarena.soknadinnsending.business.domain.AlternativRepresentasjon;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Steg;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.AktivitetBetalingsplanService;
import no.nav.sbl.dialogarena.soknadinnsending.business.transformer.refusjondagligreise.RefusjonDagligreiseTilXml;
import org.apache.commons.collections15.Transformer;
import org.springframework.context.MessageSource;

import java.util.Arrays;
import java.util.List;

public class SoknadRefusjonDagligreise extends KravdialogInformasjon.DefaultOppsett {

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
        return Arrays.asList(BOLK_PERSONALIA, AktivitetBetalingsplanService.VEDTAKPERIODER);
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
