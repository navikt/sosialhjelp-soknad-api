package no.nav.sbl.dialogarena.rest.utils;

import no.nav.sbl.dialogarena.sendsoknad.mockmodul.person.PersonPortTypeMock;
import no.nav.tjeneste.virksomhet.person.v1.informasjon.Diskresjonskoder;
import no.nav.tjeneste.virksomhet.person.v1.informasjon.Person;
import no.nav.tjeneste.virksomhet.person.v1.informasjon.Statsborgerskap;


public class MockdataUtils {
    public static Diskresjonskoder getDiskresjonskode() {
        Diskresjonskoder diskresjonskoder = new Diskresjonskoder();
        diskresjonskoder.setValue("6");
        return diskresjonskoder;
    }

    public static void setLandPaaStatsborgerskap(Statsborgerskap statsborgerskap, String landkode) {
        statsborgerskap.getLand().setValue(landkode);
    }

    public static MockdataFields getMockdataFields(PersonPortTypeMock portTypeMock) {
        Person person = portTypeMock.getPerson();

        Boolean erNorskStatsborger = person.getStatsborgerskap().getLand().getValue().equalsIgnoreCase("NOR");

        MockdataFields fields = new MockdataFields();
        fields.setKode6(person.getDiskresjonskode() != null);
        fields.setUtenlandskStatsborger(!erNorskStatsborger);
        return fields;
    }

    public static class MockdataFields {
        private Boolean kode6;
        private Boolean utenlandskStatsborger;

        public Boolean getKode6() {
            return kode6;
        }

        public void setKode6(Boolean kode6) {
            this.kode6 = kode6;
        }


        public Boolean getUtenlandskStatsborger() {
            return utenlandskStatsborger;
        }

        public void setUtenlandskStatsborger(Boolean utenlandskStatsborger) {
            this.utenlandskStatsborger = utenlandskStatsborger;
        }


    }
}
