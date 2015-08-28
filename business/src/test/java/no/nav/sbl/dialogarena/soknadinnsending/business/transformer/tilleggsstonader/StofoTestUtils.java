package no.nav.sbl.dialogarena.soknadinnsending.business.transformer.tilleggsstonader;

import org.assertj.core.api.Condition;

import javax.xml.datatype.XMLGregorianCalendar;

public class StofoTestUtils {

    public static PeriodMatcher periodeMatcher(int year, int month, int day) {
        return new PeriodMatcher(year, month, day);
    }

    private static class PeriodMatcher extends Condition<XMLGregorianCalendar> {
        private int year, month, day;

        public PeriodMatcher(int year, int month, int day) {
            this.year = year;
            this.month = month;
            this.day = day;
        }

        @Override
        public boolean matches(XMLGregorianCalendar value) {
            return value.getYear() == year && value.getMonth() == month && value.getDay() == day;
        }
    }
}
