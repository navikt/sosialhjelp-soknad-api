package no.nav.sbl.dialogarena.service.helpers;

import com.github.jknack.handlebars.Options;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.KravdialogInformasjonHolder;
import no.nav.sbl.dialogarena.service.HandlebarsUtils;
import no.nav.sbl.dialogarena.service.oppsummering.OppsummeringsFaktum;
import no.nav.sbl.dialogarena.soknadsosialhjelp.message.NavMessageSource;
import no.nav.sbl.dialogarena.utils.UrlUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import static org.apache.commons.lang3.LocaleUtils.toLocale;

@Component
public class HentSvaralternativerHelper extends RegistryAwareHelper<String> {


    @Inject
    private NavMessageSource navMessageSource;

    private static final Logger LOG = LoggerFactory.getLogger(no.nav.sbl.dialogarena.service.helpers.HentSvaralternativerHelper.class);


    @Inject
    private KravdialogInformasjonHolder kravdialogInformasjonHolder;

    @Override
    public String getNavn() {
        return "hentSvaralternativer";
    }

    @Override
    public String getBeskrivelse() {
        return "Henter svaralternativer, dersom det er flere valgmuligheter. Prøver med søknadens prefix + key, før den prøver med bare keyen. Kan sende inn parametere.";
    }

    @Override
    public CharSequence apply(String key, Options options) throws IOException {
        WebSoknad soknad = HandlebarsUtils.finnWebSoknad(options.context);

        Faktum sprakFaktum = soknad.getFaktumMedKey("skjema.sprak");
        StringBuilder stringBuilder = new StringBuilder();

        if (options.context.model() instanceof OppsummeringsFaktum) {
            OppsummeringsFaktum oppsummeringsFaktum = (OppsummeringsFaktum) options.context.model();

            ArrayList<OppsummeringsFaktum> oppsummeringsFakta = new ArrayList<OppsummeringsFaktum>();

            finnFaktaForSvarAlternativer(oppsummeringsFaktum, oppsummeringsFakta
            );

            stringBuilder = skrivFakta(oppsummeringsFakta, options);

        }

        return stringBuilder.toString();
    }

    private StringBuilder skrivFakta(ArrayList<OppsummeringsFaktum> oppsummeringsFakta, Options options) {

        StringBuilder stringBuilder = new StringBuilder();
        WebSoknad soknad = HandlebarsUtils.finnWebSoknad(options.context);
        Faktum sprakFaktum = soknad.getFaktumMedKey("skjema.sprak");
        String sprak = sprakFaktum == null ? "nb_NO" : sprakFaktum.getValue();
        final String bundleName = kravdialogInformasjonHolder.hentKonfigurasjon(soknad.getskjemaNummer()).getBundleName();

        int antall = 1;

        for (OppsummeringsFaktum oppsummeringsFaktum : oppsummeringsFakta) {

            String tekst = hentTekst(oppsummeringsFaktum.key(), options.params, soknad.getSoknadPrefix(), bundleName, toLocale(sprak));

            String nyTekst = UrlUtils.endreHyperLenkerTilTekst(tekst);

            if (tekst != null && !tekst.equals(nyTekst)) {
                tekst = nyTekst;
            }

            stringBuilder.append(tekst != null ? antall++ + ") " + tekst + " " : "");
        }

        return stringBuilder;
    }

    private void finnFaktaForSvarAlternativer(OppsummeringsFaktum oppsummeringsFaktum, List<OppsummeringsFaktum> faktaSomSkalSkrivesUt) {

        if (oppsummeringsFaktum.barneFakta == null || oppsummeringsFaktum.barneFakta.isEmpty()) {
            faktaSomSkalSkrivesUt.add(oppsummeringsFaktum);

        } else {

            for (OppsummeringsFaktum barnefakta : oppsummeringsFaktum.barneFakta) {
                finnFaktaForSvarAlternativer(barnefakta, faktaSomSkalSkrivesUt);
            }
        }
    }

    private String hentTekst(String key, Object[] parameters, String soknadTypePrefix, String bundleName, Locale locale) {
        Properties bundle = navMessageSource.getBundleFor(bundleName, locale);

        String tekst = bundle.getProperty(soknadTypePrefix + "." + key);

        if (tekst == null) {
            tekst = bundle.getProperty(key);
        }

        if (tekst == null) {
            LOG.debug(String.format("Fant ikke tekst til oppsummering for nokkel %s i bundelen %s", key, bundleName));
            return tekst;
        } else {
            return MessageFormat.format(tekst, parameters);
        }
    }
}

