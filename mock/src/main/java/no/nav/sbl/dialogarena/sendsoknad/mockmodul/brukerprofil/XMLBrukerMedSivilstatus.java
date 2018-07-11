package no.nav.sbl.dialogarena.sendsoknad.mockmodul.brukerprofil;

import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLBruker;

public class XMLBrukerMedSivilstatus extends XMLBruker {

    private String sivilstatus;

    XMLBruker superbruker;

    public XMLBrukerMedSivilstatus(XMLBruker person, String sivilstatus) {
        super();
        superbruker = person;
        this.sivilstatus = sivilstatus;
    }

    public XMLBrukerMedSivilstatus() {
        super();
        sivilstatus = null;
    }

    public void setSivilstatus(String sivilstatus) {
        this.sivilstatus = sivilstatus;

    }

    public String getSivilstatus() {
        return sivilstatus;
    }
}
