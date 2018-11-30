package no.nav.sbl.sosialhjelp.pdf.helpers;

import com.github.jknack.handlebars.Options;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.oppsett.TekstStruktur;
import no.nav.sbl.sosialhjelp.pdf.oppsummering.OppsummeringsFaktum;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

import static no.nav.sbl.sosialhjelp.pdf.HandlebarsUtils.finnWebSoknad;
import static no.nav.sbl.sosialhjelp.pdf.HandlebarsUtils.getOppsummeringsFaktum;
import static no.nav.sbl.sosialhjelp.pdf.HandlebarsUtils.lagItererbarRespons;

@Component
public class ForInfotekstHelper extends RegistryAwareHelper<Object>{

    public static final String NAVN = "forInfotekst";

    @Override
    public CharSequence apply(Object obj, Options options) throws IOException {
        OppsummeringsFaktum oppsummeringsFaktum = getOppsummeringsFaktum(options.context);
        WebSoknad webSoknad = finnWebSoknad(options.context);

        if (oppsummeringsFaktum != null && webSoknad != null) {
            List<TekstStruktur> tekster = oppsummeringsFaktum.struktur.getInfotekster(webSoknad, oppsummeringsFaktum.faktum);
            return tekster.isEmpty() ? options.inverse(this) : lagItererbarRespons(options, tekster);
        } else {
            return options.inverse(this);
        }
    }

    @Override
    public String getNavn() {
        return NAVN;
    }

    @Override
    public String getBeskrivelse() {
        return "Itererer over alle infotekster med gyldig constraint på faktumstrukturen";
    }
}
