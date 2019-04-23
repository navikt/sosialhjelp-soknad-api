package no.nav.sbl.dialogarena.sendsoknad.domain.util;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

public class LocalDateTimeAdapter extends XmlAdapter<String, OffsetDateTime> {

    @Override
    public OffsetDateTime unmarshal(String v) throws Exception {
        return OffsetDateTime.from(DateTimeFormatter.ISO_DATE.parse(v));
    }

    @Override
    public String marshal(OffsetDateTime v) throws Exception {
        return v.toString();
    }
}
