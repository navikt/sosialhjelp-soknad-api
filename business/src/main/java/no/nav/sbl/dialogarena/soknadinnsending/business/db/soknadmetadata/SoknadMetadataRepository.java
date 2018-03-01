package no.nav.sbl.dialogarena.soknadinnsending.business.db.soknadmetadata;

import no.nav.sbl.dialogarena.sendsoknad.domain.SoknadInnsendingStatus;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.SoknadType;

import java.time.LocalDateTime;

public interface SoknadMetadataRepository {

    Long hentNesteId();

    void opprett(SoknadMetadata metadata);

    void oppdater(SoknadMetadata metadata);

    SoknadMetadata hent(String behandlingsId);


    class SoknadMetadata {
        public Long id;
        public String behandlingsId, tilknyttetBehandlingsId, hovedskjema, vedlegg, orgnr, navEnhet, fiksForsendelseID;
        public SoknadType type;
        public SoknadInnsendingStatus status;
        public LocalDateTime opprettetDato, sisteEndretDato, innsendtDato;
    }

}
