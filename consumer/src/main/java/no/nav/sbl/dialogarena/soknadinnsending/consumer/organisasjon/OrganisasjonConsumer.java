package no.nav.sbl.dialogarena.soknadinnsending.consumer.organisasjon;

import no.nav.sbl.dialogarena.soknadinnsending.consumer.organisasjon.dto.OrganisasjonNoekkelinfoDto;

public interface OrganisasjonConsumer {

    void ping();

    OrganisasjonNoekkelinfoDto hentOrganisasjonNoekkelinfo(String orgnr);

}
