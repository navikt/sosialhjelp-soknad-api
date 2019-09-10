package no.nav.sbl.dialogarena.soknadinnsending.business.domain;

import no.nav.sbl.dialogarena.sendsoknad.domain.SoknadInnsendingStatus;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.SoknadType;
import no.nav.sbl.dialogarena.soknadinnsending.business.util.JAXBHelper;
import no.nav.sbl.sosialhjelp.domain.Vedleggstatus;

import javax.xml.bind.annotation.XmlRootElement;
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
        public Vedleggstatus status;
        public String skjema;
        public String tillegg;
    }

    @XmlRootElement
    public static class HovedskjemaMetadata extends FilData {
        public List<FilData> alternativRepresentasjon = new ArrayList<>();
    }
    public final static JAXBHelper JAXB = new JAXBHelper(
            FilData.class,
            VedleggMetadata.class,
            VedleggMetadataListe.class,
            HovedskjemaMetadata.class

    );
}
