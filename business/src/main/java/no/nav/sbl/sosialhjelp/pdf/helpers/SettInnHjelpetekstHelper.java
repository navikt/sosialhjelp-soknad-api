package no.nav.sbl.sosialhjelp.pdf.helpers;


import com.github.jknack.handlebars.Options;
import no.nav.sbl.sosialhjelp.pdf.CmsTekst;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

import static no.nav.sosialhjelp.soknad.domain.model.kravdialoginformasjon.SosialhjelpInformasjon.BUNDLE_NAME;
import static no.nav.sosialhjelp.soknad.domain.model.kravdialoginformasjon.SosialhjelpInformasjon.SOKNAD_TYPE_PREFIX;
import static no.nav.sbl.sosialhjelp.pdf.HandlebarContext.SPRAK;

@Component
public class SettInnHjelpetekstHelper extends RegistryAwareHelper<String> implements TextWithTitle {

    @Inject
    private CmsTekst cmsTekst;

    @Override
    public String getNavn() {
        return "settInnHjelpetekst";
    }

    @Override
    public String getBeskrivelse() {
        return "Returner html kode for å vise hjelpetekst";
    }

    @Override
    public CharSequence apply(String key, Options options) {
        String hjelpetekst = TextWithTitle.getText(key, options, SOKNAD_TYPE_PREFIX, BUNDLE_NAME, cmsTekst);
        
        if (hjelpetekst == null) {
            return "";
        }
        
        final String hjelpetekstTittel = this.cmsTekst.getCmsTekst("hjelpetekst.oppsummering.tittel", options.params, SOKNAD_TYPE_PREFIX, BUNDLE_NAME, SPRAK);
        
        return TextWithTitle.createHtmlLayout(hjelpetekst, hjelpetekstTittel);
    }
}
