package no.nav.sbl.dialogarena.soknadinnsending.business.arbeid;

import no.nav.sbl.dialogarena.soknadinnsending.business.domain.dto.Land;
import org.joda.time.Interval;

import java.util.ArrayList;
import java.util.List;

public class Arbeidsforhold {
    public String orgnr;
    public String arbridsgiverNavn;
    public Land land;
    public Long fom;
    public Long tom;
    public List<Utenlandsopphold> utenlandsopphold = new ArrayList<>();
    static class Utenlandsopphold{
        public Long fom;
        public Long tom;
        public String land;
    }

}
