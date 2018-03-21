package no.nav.sbl.dialogarena.soknadinnsending.business.batch.oppgave;

import no.nav.sbl.dialogarena.soknadinnsending.business.batch.oppgave.fiks.FiksData;
import no.nav.sbl.dialogarena.soknadinnsending.business.batch.oppgave.fiks.FiksResultat;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.StringReader;
import java.io.StringWriter;
import java.time.LocalDateTime;

public class Oppgave {
    public Long id;
    public String behandlingId;
    public String type;
    public Status status;
    public int steg;
    public FiksData oppgaveData;
    public FiksResultat oppgaveResultat;
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

    public static class JAXB {

        public static final JAXBContext CONTEXT;

        static { // TODO dra ut
            try {
                CONTEXT = JAXBContext.newInstance(
                        FiksData.class,
                        FiksResultat.class
                );
            } catch (JAXBException e) {
                throw new RuntimeException(e);
            }
        }

        public static String marshal(Object jaxbelement) {
            try {
                StringWriter writer = new StringWriter();
                Marshaller marshaller = CONTEXT.createMarshaller();
                marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
                marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);
                marshaller.marshal(jaxbelement, new StreamResult(writer));
                return writer.toString();
            } catch (JAXBException e) {
                throw new RuntimeException(e);
            }
        }

        public static <T> T unmarshal(String melding, Class<T> elementClass) {
            try {
                JAXBElement<T> value = CONTEXT.createUnmarshaller().unmarshal(new StreamSource(new StringReader(melding)), elementClass);
                return value.getValue();
            } catch (JAXBException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
