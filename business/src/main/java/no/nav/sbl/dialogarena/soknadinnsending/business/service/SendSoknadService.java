package no.nav.sbl.dialogarena.soknadinnsending.business.service;

import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;

import java.io.OutputStream;
import java.util.List;

public interface SendSoknadService {

    String startSoknad(String navSoknadId);

    WebSoknad hentSoknad(long soknadId);
    
    Long hentSoknadMedBehandlinsId(String behandlingsId);

    Faktum lagreSoknadsFelt(Long soknadId, Faktum faktum);
    
    Faktum lagreSystemSoknadsFelt(Long soknadId, String key, String value);
    
    Long lagreSystemFaktum(Long soknadId, Faktum faktum, String uniqueProperty);

    Faktum lagreBarnSystemSoknadsFelt(Long soknadId, String string, String fnr, String json);

    void sendSoknad(long soknadId, byte[] outputStream);

    List<Long> hentMineSoknader(String aktorId);

    void avbrytSoknad(Long soknadId);

    void endreInnsendingsvalg(Long soknadId, Faktum faktum);

    List<Faktum> hentFakta(Long soknadId);

    void slettBrukerFaktum(Long soknadId, Long faktumId);

    WebSoknad hentSoknadMetaData(long soknadId);

    
}