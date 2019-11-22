package no.nav.sbl.dialogarena.rest.utils;

import no.nav.sbl.dialogarena.sendsoknad.mockmodul.person.Person2Mock;
import no.nav.sbl.dialogarena.sendsoknad.mockmodul.person.PersonMock;
import no.nav.sbl.dialogarena.sendsoknad.mockmodul.person.PersonPortTypeMock;
import no.nav.tjeneste.virksomhet.person.v1.informasjon.Diskresjonskoder;
import no.nav.tjeneste.virksomhet.person.v1.informasjon.Person;



public class MocksetupUtils {
    private final static String VALG_UTENLANDSK = "UTENLANDSK";
    private final static String VALG_NORSK = "NORSK";
    private final static String VALG_INGEN = "INGEN";

    private static PersonPortTypeMock personPortTypeMock = new PersonMock().personPortTypeMock();
    private static Person2Mock brukerprofilMock = new Person2Mock();

    public static Diskresjonskoder getDiskresjonskode() {
        Diskresjonskoder diskresjonskoder = new Diskresjonskoder();
        diskresjonskoder.setValue("6");
        return diskresjonskoder;
    }

    public static MocksetupFields getMocksetupFields() {
        MocksetupFields fields = new MocksetupFields();

        Person person = personPortTypeMock.getPerson();
        fields.setKode6(person.getDiskresjonskode() != null);
        String statsborgerskap = person.getStatsborgerskap().getLand().getValue().toUpperCase();
        fields.setStatsborgerskap(statsborgerskap);

        fields.setPrimarAdressetype( VALG_NORSK);
        return fields;
    }


    public static class MocksetupFields {
        private Boolean kode6;
        private String statsborgerskap;
        private String primarAdressetype;
        private String sekundarAdressetype;

        public Boolean getKode6() {
            return kode6;
        }

        public void setKode6(Boolean kode6) {
            this.kode6 = kode6;
        }

        public String getSekundarAdressetype() {
            return sekundarAdressetype;
        }

        public void setSekundarAdressetype(String sekundarAdressetype) {
            this.sekundarAdressetype = sekundarAdressetype;
        }

        public String getPrimarAdressetype() {
            return primarAdressetype;
        }

        public void setPrimarAdressetype(String primarAdressetype) {
            this.primarAdressetype = primarAdressetype;
        }

        public String getStatsborgerskap() {
            return statsborgerskap;
        }

        public void setStatsborgerskap(String statsborgerskap) {
            this.statsborgerskap = statsborgerskap;
        }


    }
}
