package no.nav.sbl.dialogarena.soknadinnsending.business.person;


import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;

import java.util.List;

public interface BolkService {
    String tilbyrBolk();

    List<Faktum> genererSystemFakta(String fodselsnummer, Long soknadId);

}
