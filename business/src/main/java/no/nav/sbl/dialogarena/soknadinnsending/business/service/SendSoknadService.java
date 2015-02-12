package no.nav.sbl.dialogarena.soknadinnsending.business.service;

import no.nav.sbl.dialogarena.soknadinnsending.business.domain.DelstegStatus;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.oppsett.SoknadStruktur;

import java.util.Map;

public interface SendSoknadService {

    public interface Create {

    }

    public interface Read {

    }

    public interface Update {

    }

    public interface Delete {

    }

    String startSoknad(String navSoknadId);

    WebSoknad hentSoknad(long soknadId);

    String hentSoknadEier(Long soknadId);

    WebSoknad hentSoknadMedBehandlingsId(String behandlingsId);

    void sendSoknad(long soknadId, byte[] outputStream);

    void avbrytSoknad(Long soknadId);

    void settDelsteg(Long soknadId, DelstegStatus delstegStatus);

    SoknadStruktur hentSoknadStruktur(Long soknadId);
    SoknadStruktur hentSoknadStruktur(String skjemanummer);

    Map<String,String> hentInnsendtDatoForOpprinneligSoknad(String behandlingsId);

}