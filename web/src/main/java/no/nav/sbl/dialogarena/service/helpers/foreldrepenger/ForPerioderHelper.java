package no.nav.sbl.dialogarena.service.helpers.foreldrepenger;

import com.github.jknack.handlebars.Options;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.service.helpers.RegistryAwareHelper;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.function.BiFunction;

import static java.util.stream.Collectors.toList;
import static no.nav.sbl.dialogarena.service.HandlebarsUtils.*;

@Component
public class ForPerioderHelper extends RegistryAwareHelper<Object> {

    private final DateTimeFormatter dt = DateTimeFormat.forPattern("yyyy-MM-dd").withLocale(NO_LOCALE);

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

        List<Faktum> sortertFaktaEtterDato = finnWebSoknad(options.context)
                .getFaktaSomStarterMed("perioder.tidsrom")
                .stream()
                .sorted((f1, f2) -> compareFaktumFradato.apply(f1, f2))
                .collect(toList());

        if (sortertFaktaEtterDato.isEmpty()) {
            return options.inverse();
        } else {
            return lagItererbarRespons(options, sortertFaktaEtterDato);
        }
    }

    private BiFunction<Faktum, Faktum, Integer> compareFaktumFradato =
            (f1, f2) ->
                    dt.parseDateTime(f1.getProperties().get("fradato"))
                            .compareTo(dt.parseDateTime(f2.getProperties().get("fradato")));
}

