package no.nav.sbl.dialogarena.soknadinnsending.business.domain;

import no.nav.sbl.dialogarena.sendsoknad.domain.SoknadInnsendingStatus;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.SoknadType;
import no.nav.sbl.dialogarena.soknadinnsending.business.util.JAXBHelper;
import no.nav.sbl.sosialhjelp.domain.Vedleggstatus;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@Deprecated
public class SoknadMetadata {
    public Long id;
    public String behandlingsId, tilknyttetBehandlingsId, fnr, skjema, orgnr, navEnhet, fiksForsendelseId;
    public VedleggMetadataListe vedlegg = new VedleggMetadataListe();
    public SoknadType type;
    public SoknadInnsendingStatus status;

    @XmlRootElement
    public static class FilData {
        public String filnavn; // Må være der i 90 dager etter prodsetting for å kunne mappe tidligere soknader med filnavn i VedleggMetadataListe
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

    public final static JAXBHelper JAXB = new JAXBHelper(
            FilData.class,
            VedleggMetadata.class,
            VedleggMetadataListe.class

    );
}
