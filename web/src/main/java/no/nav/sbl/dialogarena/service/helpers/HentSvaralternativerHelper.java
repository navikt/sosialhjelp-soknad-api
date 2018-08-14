package no.nav.sbl.dialogarena.service.helpers;

import com.github.jknack.handlebars.Options;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.KravdialogInformasjonHolder;
import no.nav.sbl.dialogarena.service.CmsTekst;
import no.nav.sbl.dialogarena.service.HandlebarsUtils;
import no.nav.sbl.dialogarena.service.oppsummering.OppsummeringsFaktum;
import no.nav.sbl.dialogarena.utils.UrlUtils;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.LocaleUtils.toLocale;

@Component
public class HentSvaralternativerHelper extends RegistryAwareHelper<String> {

    @Inject
    private CmsTekst cmsTekst;

    @Inject
    private KravdialogInformasjonHolder kravdialogInformasjonHolder;

    @Override
    public String getNavn() {
        return "hentSvaralternativer";
    }

    @Override
    public String getBeskrivelse() {
        return "Henter svaralternativer fra cms, dersom det er flere valgmuligheter. Prøver med søknadens prefix + key, før den prøver med bare keyen. Kan sende inn parametere.";
    }

    @Override
    public CharSequence apply(String key, Options options) throws IOException {
        WebSoknad soknad = HandlebarsUtils.finnWebSoknad(options.context);

        Faktum sprakFaktum = soknad.getFaktumMedKey("skjema.sprak");

        String sprak = sprakFaktum == null ? "nb_NO" : sprakFaktum.getValue();

        final String bundleName = kravdialogInformasjonHolder.hentKonfigurasjon(soknad.getskjemaNummer()).getBundleName();


        /*StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Key : ").append(key).append("\n");*/

        /*     stringBuilder.append("HandlebarsUtils.finnFaktum(options.context).getKey() : " + HandlebarsUtils.finnFaktum(options.context).getKey()).append("\n").append("HandlebarsUtils.finnFaktum(options.context).getValue()  : " + HandlebarsUtils.finnFaktum(options.context).getValue());*/

        StringBuilder stringBuilder = new StringBuilder();

        if (options.context.model() instanceof OppsummeringsFaktum) {


            OppsummeringsFaktum oppsummeringsFaktum = (OppsummeringsFaktum) options.context.model();

            ArrayList<OppsummeringsFaktum> oppsummeringsFakta = new ArrayList<OppsummeringsFaktum>();

            finnFaktaForSvarAlternativer(oppsummeringsFaktum, oppsummeringsFakta
            );

            stringBuilder = skrivFakta(oppsummeringsFakta, options);

        }




        /*Faktum parentfaktum = soknad.getFaktumMedKey(key);
        Map<String, String> properties = parentfaktum.getProperties();
        stringBuilder.append("Antall properties : ").append(properties.size()).append("\n");

        for (String nokkel : properties.keySet()) {
            stringBuilder.append("Nøkkel : " + nokkel + ", verdi : " + properties.get(nokkel)).append("\n");
        }*/


        return stringBuilder.toString();


 /*       String tekst = this.cmsTekst.getCmsTekst(key, options.params, soknad.getSoknadPrefix(), bundleName, toLocale(sprak));


        String nyTekst = UrlUtils.endreHyperLenkerTilTekst(tekst);

        if (tekst != null && !tekst.equals(nyTekst)) {
            tekst = nyTekst;
        }
        return tekst != null ? tekst : "";*/
    }

    private StringBuilder skrivFakta(ArrayList<OppsummeringsFaktum> oppsummeringsFakta, Options options) {
        StringBuilder stringBuilder = new StringBuilder();
        WebSoknad soknad = HandlebarsUtils.finnWebSoknad(options.context);
        Faktum sprakFaktum = soknad.getFaktumMedKey("skjema.sprak");
        String sprak = sprakFaktum == null ? "nb_NO" : sprakFaktum.getValue();
        final String bundleName = kravdialogInformasjonHolder.hentKonfigurasjon(soknad.getskjemaNummer()).getBundleName();


        for (OppsummeringsFaktum oppsummeringsFaktum : oppsummeringsFakta) {

            String tekst = this.cmsTekst.getCmsTekst(oppsummeringsFaktum.key(), options.params, soknad.getSoknadPrefix(), bundleName, toLocale(sprak));


            String nyTekst = UrlUtils.endreHyperLenkerTilTekst(tekst);

            if (tekst != null && !tekst.equals(nyTekst)) {
                tekst = nyTekst;
            }
            stringBuilder.append(tekst != null ? tekst + "\n" : "");
        }


        return stringBuilder;
    }

    protected void finnFaktaForSvarAlternativer(OppsummeringsFaktum oppsummeringsFaktum, List<OppsummeringsFaktum> faktaSomSkalSkrivesUt) {

        if (oppsummeringsFaktum.barneFakta.isEmpty()) {
            faktaSomSkalSkrivesUt.add(oppsummeringsFaktum);

        } else {


            for (OppsummeringsFaktum barnefakta : oppsummeringsFaktum.barneFakta) {
                finnFaktaForSvarAlternativer(barnefakta, faktaSomSkalSkrivesUt);
            }
        }
    }

}

