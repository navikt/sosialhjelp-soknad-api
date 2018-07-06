package no.nav.sbl.dialogarena.service.helpers;

import com.github.jknack.handlebars.Options;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.KravdialogInformasjonHolder;
import no.nav.sbl.dialogarena.soknadsosialhjelp.message.NavMessageSource;
import no.nav.sbl.dialogarena.sendsoknad.domain.util.KommuneTilNavEnhetMapper;
import no.nav.sbl.dialogarena.service.CmsTekst;
import no.nav.sbl.dialogarena.service.HandlebarsUtils;
import no.nav.sbl.dialogarena.utils.UrlUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;
import java.util.Properties;
import java.util.stream.Collectors;

import static no.nav.sbl.dialogarena.sendsoknad.domain.util.KommuneTilNavEnhetMapper.getNavEnhetFromWebSoknad;
import static org.apache.commons.lang3.LocaleUtils.toLocale;

@Component
public class HentTekstForGDPRInfoHelper extends RegistryAwareHelper<String> {

    @Inject
    private CmsTekst cmsTekst;

    @Inject
    private NavMessageSource navMessageSource;

    @Inject
    private KravdialogInformasjonHolder kravdialogInformasjonHolder;

    private static final Logger LOG = LoggerFactory.getLogger(CmsTekst.class);


    @Override
    public String getNavn() {
        return "hentTekstForGDPRInfo";
    }

    @Override
    public String getBeskrivelse() {
        return "Henter tekst fra cms, prøver med søknadens prefix + key, før den prøver med bare keyen. Kan sende inn parametere. Formatterer tekst med variabel input (riktig NAV-kontor)";
    }

    @Override
    public CharSequence apply(String key, Options options) throws IOException {
        WebSoknad soknad = HandlebarsUtils.finnWebSoknad(options.context);
        Faktum sprakFaktum = soknad.getFaktumMedKey("skjema.sprak");
        String sprak = sprakFaktum == null ? "nb_NO" : sprakFaktum.getValue();

        String[] keys = {"soknasosialhjelp.oppsummering.hvorsendes", "soknadsosialhjelp.forstesiden.bekreftInfoModal.body"};

        final String bundleName = kravdialogInformasjonHolder.hentKonfigurasjon(soknad.getskjemaNummer()).getBundleName();

        String tekst = null;

        tekst = this.getCmsTekst(key, options.params, soknad.getSoknadPrefix(), bundleName, toLocale(sprak), soknad);


        return tekst != null ? tekst : "";
    }

    private String getCmsTekst(String key, Object[] parameters, String soknadTypePrefix, String bundleName, Locale locale, WebSoknad soknad) {
        Properties bundle = navMessageSource.getBundleFor(bundleName, locale);

        String tekst = bundle.getProperty(soknadTypePrefix + "." + key);

        if (tekst == null) {
            tekst = bundle.getProperty(key);
        }

        if (tekst == null) {
            LOG.debug(String.format("Fant ikke tekst til oppsummering for nokkel %s i bundelen %s", key, bundleName));
            return tekst;
        } else {


            String navenhet = null;

            if (soknad.erEttersending()) {
                navenhet = soknad.getValueForFaktum("ettersendelse.sendestil");
            } else {
                KommuneTilNavEnhetMapper.NavEnhet navEnhet = getNavEnhetFromWebSoknad(soknad);
                navenhet = navEnhet.getNavn();
            }


            tekst = erstattTekst(new String[]{"{navkontor}", "{navkontor:NAV-kontoret ditt}", "{navkontor:oppholdskommunen}"}, tekst, navenhet);

            return UrlUtils.endreHyperLenkerTilTekst(tekst);
        }
    }

    protected String erstattTekst(final String[] regex, final String input, final String replacement) {

        String returverdi = new String(input);
        for (int i = 0; i < regex.length; i++) {
            returverdi = erstattTekst(regex[i], returverdi, replacement);
        }
        return returverdi;
    }

    protected String erstattTekst(final String regex, final String input, final String replacement) {
        String[] lines = input.split("\n");
        for (int i = 0; i < lines.length; i++) {
            lines[i] = lines[i].replace(regex, replacement);

        }
        return Arrays.stream(lines).collect(Collectors.joining("\n"));

    }
}

