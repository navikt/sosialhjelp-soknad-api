package no.nav.sbl.sosialhjelp.pdf.helpers;

import com.github.jknack.handlebars.Options;
import no.nav.sbl.sosialhjelp.pdf.CmsTekst;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

import static no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.SosialhjelpInformasjon.BUNDLE_NAME;
import static no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.SosialhjelpInformasjon.SOKNAD_TYPE_PREFIX;

@Component
public class SettInnKnappTilgjengeligTekstHelper extends RegistryAwareHelper<String> implements TextWithTitle {

    @Inject
    private CmsTekst cmsTekst;

    @Override
    public String getNavn() {
        return "settInnKnappTilgjengeligTekst";
    }

    @Override
    public String getBeskrivelse() {
        return "Returner html kode for å vise knapp tilgjengelig";
    }

    @Override
    public CharSequence apply(String key, Options options) {
        String knapptekst = TextWithTitle.getText(key, options, SOKNAD_TYPE_PREFIX, BUNDLE_NAME, cmsTekst);

        if (knapptekst == null) {
            return "";
        }

        final String knapptekstTittel = "Knapp tilgjengelig:";

        return TextWithTitle.createHtmlLayout(knapptekst, knapptekstTittel);
    }
}