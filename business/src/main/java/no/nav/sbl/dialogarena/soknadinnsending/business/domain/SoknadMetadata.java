package no.nav.sbl.dialogarena.soknadinnsending.business.domain;

import no.nav.sbl.dialogarena.sendsoknad.domain.SoknadInnsendingStatus;
import no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.SoknadType;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.StringReader;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SoknadMetadata {
    public Long id;
    public String behandlingsId, tilknyttetBehandlingsId, fnr, skjema, orgnr, navEnhet, fiksForsendelseId;
    public HovedskjemaMetadata hovedskjema;
    public VedleggMetadataListe vedlegg = new VedleggMetadataListe();
    public SoknadType type;
    public SoknadInnsendingStatus status;
    public LocalDateTime opprettetDato, sistEndretDato, innsendtDato;

    @XmlRootElement
    public static class FilData {
        public String filUuid;
        public String filnavn;
        public String mimetype;
        public String filStorrelse;
    }

    @XmlRootElement
    public static class VedleggMetadataListe {
        public List<VedleggMetadata> vedleggListe = new ArrayList<>();
    }

    @XmlRootElement
    public static class VedleggMetadata extends FilData {
        public Vedlegg.Status status;
        public String skjema;
        public String tillegg;
    }

    @XmlRootElement
    public static class HovedskjemaMetadata extends FilData {
        public List<FilData> alternativRepresentasjon = new ArrayList<>();
    }

    public static class JAXB {
        public static final JAXBContext CONTEXT;

        static {
            try {
                CONTEXT = JAXBContext.newInstance(
                        FilData.class,
                        VedleggMetadata.class,
                        VedleggMetadataListe.class,
                        HovedskjemaMetadata.class
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
