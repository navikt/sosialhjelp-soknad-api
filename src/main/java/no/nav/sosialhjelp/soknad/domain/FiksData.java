package no.nav.sosialhjelp.soknad.domain;

import com.migesok.jaxb.adapter.javatime.LocalDateTimeXmlAdapter;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.time.LocalDateTime;
import java.util.List;

@XmlRootElement
public class FiksData {

    public String behandlingsId;
    public String avsenderFodselsnummer;
    public String mottakerOrgNr;
    public String mottakerNavn;
    public List<DokumentInfo> dokumentInfoer;
    @XmlJavaTypeAdapter(LocalDateTimeXmlAdapter.class)
    public LocalDateTime innsendtDato;

    public String ettersendelsePa;

    @XmlRootElement
    @XmlType(name="fiksDokumentInfo")
    public static class DokumentInfo {
        public String uuid;
        public String filnavn;
        public String mimetype;

        public boolean ekskluderesFraPrint;

        public DokumentInfo() {
        }

        public DokumentInfo(String uuid, String filnavn, String mimetype) {
            this(uuid, filnavn, mimetype, false);
        }

        public DokumentInfo(String uuid, String filnavn, String mimetype, boolean ekskluderesFraPrint) {
            this.uuid = uuid;
            this.filnavn = filnavn;
            this.mimetype = mimetype;
            this.ekskluderesFraPrint = ekskluderesFraPrint;
        }
    }
}
