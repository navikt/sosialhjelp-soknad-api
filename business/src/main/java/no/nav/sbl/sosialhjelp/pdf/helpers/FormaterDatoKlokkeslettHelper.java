package no.nav.sbl.sosialhjelp.pdf.helpers;

import com.github.jknack.handlebars.Options;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class FormaterDatoKlokkeslettHelper extends RegistryAwareHelper<String>{

    @Override
    public String getNavn() {
        return "formaterDatoKlokkeslett";
    }

    @Override
    public String getBeskrivelse() {
        return "Formaterer en innsendt dato og klokkeslett på et gitt format som også sendes inn";
    }

    @Override
    public CharSequence apply(String datoStreng, Options options) {
        if (datoStreng == null) {
            return "";
        }

        final ZonedDateTime zonedDate = ZonedDateTime.parse(datoStreng);
        final ZonedDateTime osloZonedDateTime = toOsloZonedDateTime(zonedDate);

        final String format = options.param(0);
        return formatDateTime(osloZonedDateTime, format);
    }

    private String formatDateTime(ZonedDateTime osloZonedDateTime, final String format) {
        final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(format);
        return osloZonedDateTime.format(dateFormatter);
    }

    private ZonedDateTime toOsloZonedDateTime(final ZonedDateTime zonedDate) {
        final ZoneId osloZone = ZoneId.of("Europe/Oslo");
        return zonedDate.withZoneSameInstant(osloZone);
    }
}
