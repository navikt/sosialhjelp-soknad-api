package no.nav.sosialhjelp.soknad.business.domain;

import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedlegg;
import no.nav.sosialhjelp.soknad.db.repositories.JAXBHelper;
import no.nav.sosialhjelp.soknad.domain.SoknadMetadataInnsendingStatus;
import no.nav.sosialhjelp.soknad.domain.Vedleggstatus;
import no.nav.sosialhjelp.soknad.domain.model.kravdialoginformasjon.SoknadType;

import javax.xml.bind.annotation.XmlRootElement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SoknadMetadata {
    public Long id;
    public String behandlingsId, tilknyttetBehandlingsId, fnr, skjema, orgnr, navEnhet, fiksForsendelseId;
    public VedleggMetadataListe vedlegg = new VedleggMetadataListe();
    public SoknadType type;
    public SoknadMetadataInnsendingStatus status;
    public LocalDateTime opprettetDato, sistEndretDato, innsendtDato;
    public boolean lestDittNav;

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
        public JsonVedlegg.HendelseType hendelseType;
        public String hendelseReferanse;
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
