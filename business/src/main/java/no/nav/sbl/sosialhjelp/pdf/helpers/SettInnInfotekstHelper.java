package no.nav.sbl.sosialhjelp.pdf.helpers;


import com.github.jknack.handlebars.Options;
import no.nav.sbl.sosialhjelp.pdf.CmsTekst;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

import static no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.SosialhjelpInformasjon.BUNDLE_NAME;
import static no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.SosialhjelpInformasjon.SOKNAD_TYPE_PREFIX;
import static no.nav.sbl.sosialhjelp.pdf.HandlebarContext.SPRAK;

@Component
public class SettInnInfotekstHelper extends RegistryAwareHelper<String> implements TextWithTitle {

    @Inject
    private CmsTekst cmsTekst;

    @Override
    public String getNavn() {
        return "settInnInfotekst";
    }

    @Override
    public String getBeskrivelse() {
        return "Returner html kode for å vise infotekst";
    }

    @Override
    public CharSequence apply(String key, Options options) {
        String infotekst = TextWithTitle.getText(key, options, SOKNAD_TYPE_PREFIX, BUNDLE_NAME, cmsTekst);
        
        if (infotekst == null) {
            return "";
        }
        
        final String infotekstTittel = this.cmsTekst.getCmsTekst("infotekst.oppsummering.tittel", options.params, SOKNAD_TYPE_PREFIX, BUNDLE_NAME, SPRAK);
        
        return TextWithTitle.createHtmlLayout(infotekst, infotekstTittel);
    }
}