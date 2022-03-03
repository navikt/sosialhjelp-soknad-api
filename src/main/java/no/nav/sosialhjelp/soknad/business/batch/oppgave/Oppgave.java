package no.nav.sosialhjelp.soknad.business.batch.oppgave;

import no.nav.sosialhjelp.soknad.business.batch.oppgave.fiks.FiksData;
import no.nav.sosialhjelp.soknad.business.batch.oppgave.fiks.FiksResultat;
import no.nav.sosialhjelp.soknad.db.repositories.JAXBHelper;

import java.time.LocalDateTime;

public class Oppgave {
    public Long id;
    public String behandlingsId;
    public String type;
    public Status status;
    public int steg;
    public FiksData oppgaveData = new FiksData();
    public FiksResultat oppgaveResultat = new FiksResultat();
    public LocalDateTime opprettet;
    public LocalDateTime sistKjort;
    public LocalDateTime nesteForsok;
    public int retries;

    public void nesteSteg() {
        steg++;
    }

    public void ferdigstill() {
        this.status = Status.FERDIG;
    }


    public enum Status {
        KLAR, UNDER_ARBEID, FERDIG, FEILET
    }

    public final static JAXBHelper JAXB = new JAXBHelper(
            FiksData.class,
            FiksResultat.class
    );


}
