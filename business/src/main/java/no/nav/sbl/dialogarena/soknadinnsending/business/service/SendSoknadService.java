package no.nav.sbl.dialogarena.soknadinnsending.business.service;

import no.nav.sbl.dialogarena.soknadinnsending.business.domain.DelstegStatus;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.oppsett.SoknadStruktur;

import java.util.List;

public interface SendSoknadService {

    String startSoknad(String navSoknadId);

    WebSoknad hentSoknad(long soknadId);

    String hentSoknadEier(Long soknadId);

    Long hentSoknadMedBehandlingsId(String behandlingsId);

    Faktum lagreSoknadsFelt(Long soknadId, Faktum faktum);

    Long lagreSystemFaktum(Long soknadId, Faktum faktum, String uniqueProperty);

    void sendSoknad(long soknadId, byte[] outputStream);

    void avbrytSoknad(Long soknadId);

    List<Faktum> hentFakta(Long soknadId);

    void slettBrukerFaktum(Long soknadId, Long faktumId);

    void settDelsteg(Long soknadId, DelstegStatus delstegStatus);

    SoknadStruktur hentSoknadStruktur(Long soknadId);

    String hentInnsendtDatoForOpprinneligSoknad(String behandlingsId);
}