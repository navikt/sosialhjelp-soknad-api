package no.nav.sbl.dialogarena.soknadinnsending.business.service;

import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;

import java.util.List;

public interface SendSoknadService {

    Long startSoknad(String navSoknadId);

    WebSoknad hentSoknad(long soknadId);

    Faktum lagreSoknadsFelt(Long soknadId, Faktum faktum);

    Faktum lagreSystemSoknadsFelt(Long soknadId, String key, String value);

    void sendSoknad(long soknadId);

    List<Long> hentMineSoknader(String aktorId);

    void avbrytSoknad(Long soknadId);

    void endreInnsendingsvalg(Long soknadId, Faktum faktum);

    List<Faktum> hentFakta(Long soknadId);

    void slettBrukerFaktum(Long soknadId, Long faktumId);
}