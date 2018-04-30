package no.nav.sbl.dialogarena.service.helpers;

import com.github.jknack.handlebars.Options;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.ForeldrepengerInformasjon;
import org.joda.time.DateTime;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static no.nav.sbl.dialogarena.service.HandlebarsUtils.finnWebSoknad;

@Component
public class VisInformasjonOmPerioderHelper extends RegistryAwareHelper<Object>{

    public static final String NAVN = "visInformasjonOmPerioder";
    public static final DateTime TIDSPUNKT_HVOR_FORELDREPENGEPERIODEFEILEN_BLE_RETTET = new DateTime(2018, 04, 17, 16, 00);
    public static final DateTime SISTE_DAG_I_FEBRUAR = new DateTime(2018, 2, 28, 0, 0);
    public static final DateTime FORSTE_JULI = new DateTime(2018, 7, 1, 00, 00);

    @Override
    public CharSequence apply(Object key, Options options) throws IOException {
        WebSoknad soknad = finnWebSoknad(options.context);
        if (ForeldrepengerInformasjon.FORSTEGANGSSOKNADER.contains(soknad.getskjemaNummer()) && soknad.getOpprettetDato().isBefore(TIDSPUNKT_HVOR_FORELDREPENGEPERIODEFEILEN_BLE_RETTET)) {

            try {
                DateTime dato = DateTime.parse(soknad.getFaktumMedKey("barnet.dato").getValue());
                if (dato.isAfter(SISTE_DAG_I_FEBRUAR) && dato.isBefore(FORSTE_JULI)) {
                    return options.fn(this);
                } else {
                    return options.inverse(this);
                }
            }catch (Exception e){
                return options.fn(this);
            }
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
        return "Sjekker om en søknad er opprettet før en hardkodet dato og er en førstegangssøknad for foreldrepenger";
    }
}
