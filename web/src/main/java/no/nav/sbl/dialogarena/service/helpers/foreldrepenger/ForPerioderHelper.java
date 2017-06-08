package no.nav.sbl.dialogarena.service.helpers.foreldrepenger;

import com.github.jknack.handlebars.Options;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.service.helpers.RegistryAwareHelper;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

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

        DateTimeFormatter dt = DateTimeFormat.forPattern("yyyy-MM-dd").withLocale(NO_LOCALE);

        List<Faktum> sortertFaktaEtterDato = finnWebSoknad(options.context)
                .getFaktaSomStarterMed("perioder.tidsrom")
                .stream()
                .sorted(Comparator.comparing(d -> dt.parseDateTime(d.getProperties().get("fradato"))))
                .collect(Collectors.toList());

        if (sortertFaktaEtterDato.isEmpty()) {
            return options.inverse();
        } else {
            return lagItererbarRespons(options, sortertFaktaEtterDato);
        }
    }
}
