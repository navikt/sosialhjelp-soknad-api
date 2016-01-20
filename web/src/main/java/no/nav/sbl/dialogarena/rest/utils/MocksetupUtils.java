package no.nav.sbl.dialogarena.rest.utils;

import no.nav.sbl.dialogarena.sendsoknad.mockmodul.brukerprofil.BrukerprofilMock;
import no.nav.sbl.dialogarena.sendsoknad.mockmodul.person.PersonMock;
import no.nav.sbl.dialogarena.sendsoknad.mockmodul.person.PersonPortTypeMock;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLBruker;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLMidlertidigPostadresse;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLMidlertidigPostadresseUtland;
import no.nav.tjeneste.virksomhet.person.v1.informasjon.Diskresjonskoder;
import no.nav.tjeneste.virksomhet.person.v1.informasjon.Person;

import static no.nav.sbl.dialogarena.sendsoknad.mockmodul.brukerprofil.BrukerprofilMock.Adressetyper;
import static no.nav.sbl.dialogarena.sendsoknad.mockmodul.brukerprofil.BrukerprofilMock.Adressetyper.*;
import static no.nav.sbl.dialogarena.sendsoknad.mockmodul.brukerprofil.BrukerprofilMock.POSTTYPE_UTENLANDSK;


public class MocksetupUtils {
    private final static String VALG_UTENLANDSK = "UTENLANDSK";
    private final static String VALG_NORSK = "NORSK";
    private final static String VALG_INGEN = "INGEN";

    private static PersonPortTypeMock personPortTypeMock = PersonMock.getInstance().getPersonPortTypeMock();
    private static BrukerprofilMock brukerprofilMock = BrukerprofilMock.getInstance();

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

        XMLBruker bruker = getBrukerFraBrukerprofil();
        String primarType = bruker.getGjeldendePostadresseType().getValue();
        fields.setPrimarAdressetype(primarType.equalsIgnoreCase(POSTTYPE_UTENLANDSK) ? VALG_UTENLANDSK : VALG_NORSK);

        XMLMidlertidigPostadresse postadresse = bruker.getMidlertidigPostadresse();
        if(postadresse == null) {
            fields.setSekundarAdressetype(VALG_INGEN);
        } else if(XMLMidlertidigPostadresseUtland.class.isAssignableFrom(postadresse.getClass())) {
            fields.setSekundarAdressetype(VALG_UTENLANDSK);
        } else {
            fields.setSekundarAdressetype(VALG_NORSK);
        }

        return fields;
    }

    public static void settPostadressetype(String type){
        XMLBruker bruker = getBrukerFraBrukerprofil();
        brukerprofilMock.settPostadresse(bruker, mapValueTilAdressetype(type));
    }

    public static void settSekundarAdressetype(String type) {
        XMLBruker bruker = getBrukerFraBrukerprofil();
        brukerprofilMock.settSekundarAdresse(bruker, mapValueTilAdressetype(type));
    }

    private static XMLBruker getBrukerFraBrukerprofil(){
        return brukerprofilMock.getBrukerprofilPortTypeMock().getPerson();
    }

    private static Adressetyper mapValueTilAdressetype(String type){
        if(type.equals(VALG_NORSK)){
            return NORSK;
        } else if(type.equals(VALG_UTENLANDSK)) {
            return UTENLANDSK;
        } else {
            return INGEN;
        }
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
