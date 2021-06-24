package no.nav.sosialhjelp.soknad.business.util;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.StringReader;
import java.io.StringWriter;

public class JAXBHelper {
    private final JAXBContext context;

    @SuppressWarnings("rawtypes")
    public JAXBHelper(Class... classes) {
        try {
            context = JAXBContext.newInstance(classes);
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }

    public String marshal(Object jaxbelement) {
        try {
            StringWriter writer = new StringWriter();
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);
            marshaller.marshal(jaxbelement, new StreamResult(writer));
            return writer.toString();
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> T unmarshal(String melding, Class<T> elementClass) {
        try {
            JAXBElement<T> value = context.createUnmarshaller().unmarshal(new StreamSource(new StringReader(melding)), elementClass);
            return value.getValue();
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }
}
