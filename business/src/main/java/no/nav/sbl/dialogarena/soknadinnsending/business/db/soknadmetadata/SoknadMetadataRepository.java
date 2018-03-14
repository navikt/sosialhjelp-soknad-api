package no.nav.sbl.dialogarena.soknadinnsending.business.db.soknadmetadata;

import no.nav.sbl.dialogarena.soknadinnsending.business.domain.SoknadMetadata;

public interface SoknadMetadataRepository {

    Long hentNesteId();

    void opprett(SoknadMetadata metadata);

    void oppdater(SoknadMetadata metadata);

    SoknadMetadata hent(String behandlingsId);


}
