package no.nav.sbl.dialogarena.rest.utils;

import no.nav.sbl.dialogarena.sendsoknad.mockmodul.person.PersonPortTypeMock;
import no.nav.tjeneste.virksomhet.person.v1.informasjon.Diskresjonskoder;
import no.nav.tjeneste.virksomhet.person.v1.informasjon.Person;


public class MockdataUtils {
    public static Diskresjonskoder getDiskresjonskode() {
        Diskresjonskoder diskresjonskoder = new Diskresjonskoder();
        diskresjonskoder.setValue("6");
        return diskresjonskoder;
    }

    public static MockdataFields getMockdataFields(PersonPortTypeMock portTypeMock) {
        Person person = portTypeMock.getPerson();

        String statsborgerskap = person.getStatsborgerskap().getLand().getValue().toUpperCase();

        MockdataFields fields = new MockdataFields();
        fields.setKode6(person.getDiskresjonskode() != null);
        fields.setStatsborgerskap(statsborgerskap);
        return fields;
    }

    public static class MockdataFields {
        private Boolean kode6;
        private Boolean harSekundarAdresse;
        private String statsborgerskap;

        public Boolean getKode6() {
            return kode6;
        }

        public void setKode6(Boolean kode6) {
            this.kode6 = kode6;
        }


        public String getStatsborgerskap() {
            return statsborgerskap;
        }

        public void setStatsborgerskap(String statsborgerskap) {
            this.statsborgerskap = statsborgerskap;
        }


    }
}
