package no.nav.sbl.dialogarena.print.helper;


public class JsonTestData {

    public static String hentWebSoknadHtml() {
        return "<html>\n" +
                "<head>\n" +
                "    <link rel=\"stylesheet\" type=\"text/css\" href=\"style.css\" />\n" +
                "</head>\n" +
                "<body>\n" +
                "\n" +
                "<h3>{{skjemaNummer}}</h3>\n" +
                "<div>\n" +
                "   Oppsummering... {{soknadId}}\n" +
                "</div>\n" +
                "\n" +
                "</body>\n" +
                "</html>";
    }

    public static String hentWebSoknadJson() {
        return "{\"soknadId\":188,\"skjemaNummer\":\"Dagpenger\",\"brukerBehandlingId\":\"07cc33d3-ca01-4824-8924-7bac27ca8428\",\"fakta\":{},\"status\":\"UNDER_ARBEID\",\"aktoerId\":\"***REMOVED***\",\"opprettetDato\":{\"year\":2013,\"era\":1,\"dayOfWeek\":3,\"dayOfYear\":324,\"dayOfMonth\":20,\"weekyear\":2013,\"yearOfEra\":2013,\"hourOfDay\":13,\"weekOfWeekyear\":47,\"monthOfYear\":11,\"yearOfCentury\":13,\"centuryOfEra\":20,\"millisOfSecond\":314,\"millisOfDay\":47947314,\"secondOfMinute\":7,\"secondOfDay\":47947,\"minuteOfHour\":19,\"minuteOfDay\":799,\"zone\":{\"fixed\":false,\"uncachedZone\":{\"cachable\":true,\"fixed\":false,\"id\":\"Europe/Prague\"},\"id\":\"Europe/Prague\"},\"millis\":1384949947314,\"chronology\":{\"zone\":{\"fixed\":false,\"uncachedZone\":{\"cachable\":true,\"fixed\":false,\"id\":\"Europe/Prague\"},\"id\":\"Europe/Prague\"}},\"afterNow\":false,\"beforeNow\":true,\"equalNow\":false},\"delstegStatus\":\"OPPRETTET\"}";
    }

}
