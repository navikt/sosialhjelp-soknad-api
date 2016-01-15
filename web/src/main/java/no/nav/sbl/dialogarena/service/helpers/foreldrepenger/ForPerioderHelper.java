package no.nav.sbl.dialogarena.service.helpers.foreldrepenger;

import com.github.jknack.handlebars.Options;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.service.helpers.RegistryAwareHelper;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;

import static no.nav.modig.lang.collections.IterUtils.on;
import static no.nav.sbl.dialogarena.service.HandlebarsUtils.*;

@Component
public class ForPerioderHelper extends RegistryAwareHelper<Object> {

    @Override
    public String getNavn() {
        return "forPerioder";
    }

    @Override
    public String getBeskrivelse() {
        return "Henter perioder for foreldrepenger og sorterer dem etter fradato";
    }

    @Override
    public CharSequence apply(Object context, Options options) throws IOException {
        WebSoknad soknad = finnWebSoknad(options.context);
        List<Faktum> fakta = soknad.getFaktaSomStarterMed("perioder.tidsrom");
        List<Faktum> sortertFaktaEtterDato = on(fakta).collect(new Comparator<Faktum>() {
            @Override
            public int compare(Faktum o1, Faktum o2) {
                DateTimeFormatter dt = DateTimeFormat.forPattern("yyyy-MM-dd").withLocale(NO_LOCALE);
                DateTime fradatoForstePeriode = dt.parseDateTime(o2.getProperties().get("fradato"));
                DateTime fradatoAndrePeriode = dt.parseDateTime(o1.getProperties().get("fradato"));
                return fradatoAndrePeriode.compareTo(fradatoForstePeriode);
            }
        });
        if (sortertFaktaEtterDato.isEmpty()) {
            return options.inverse();
        } else {
            return lagItererbarRespons(options, sortertFaktaEtterDato);
        }
    }
}
