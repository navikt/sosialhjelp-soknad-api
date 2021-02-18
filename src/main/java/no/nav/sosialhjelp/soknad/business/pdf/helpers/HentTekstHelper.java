package no.nav.sosialhjelp.soknad.business.pdf.helpers;

import com.github.jknack.handlebars.Options;
import no.nav.sosialhjelp.soknad.business.pdf.CmsTekst;
import no.nav.sosialhjelp.soknad.business.pdf.UrlUtils;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

import static no.nav.sosialhjelp.soknad.business.pdf.HandlebarContext.SPRAK;
import static no.nav.sosialhjelp.soknad.domain.model.kravdialoginformasjon.SosialhjelpInformasjon.BUNDLE_NAME;
import static no.nav.sosialhjelp.soknad.domain.model.kravdialoginformasjon.SosialhjelpInformasjon.SOKNAD_TYPE_PREFIX;

@Component
public class HentTekstHelper extends RegistryAwareHelper<String> {

    @Inject
    private CmsTekst cmsTekst;

    @Override
    public String getNavn() {
        return "hentTekst";
    }

    @Override
    public String getBeskrivelse() {
        return "Henter tekst fra cms, prøver med søknadens prefix + key, før den prøver med bare keyen. Kan sende inn parametere.";
    }

    @Override
    public CharSequence apply(String key, Options options) {
        String tekst = this.cmsTekst.getCmsTekst(key, options.params, SOKNAD_TYPE_PREFIX, BUNDLE_NAME, SPRAK);

        String nyTekst = UrlUtils.endreHyperLenkerTilTekst(tekst);

        if (tekst != null && !tekst.equals(nyTekst)) {
            tekst = nyTekst;
        }
        return tekst != null ? tekst : "";
    }
}
