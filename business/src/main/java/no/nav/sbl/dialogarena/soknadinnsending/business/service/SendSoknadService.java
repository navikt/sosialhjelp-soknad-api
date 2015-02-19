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

    String startSoknad(String navSoknadId, String fnr);

    WebSoknad hentSoknad(String behandlingsId);

    WebSoknad hentSoknadMedFaktaOgVedlegg(long soknadId);
    WebSoknad hentSoknadMedFaktaOgVedlegg(String behandlingsId);

    String hentSoknadEier(Long soknadId);

    void sendSoknad(String behandlingsId, byte[] outputStream);

    void avbrytSoknad(String behandlingsId);

    void settDelsteg(String behandlingsId, DelstegStatus delstegStatus);

    SoknadStruktur hentSoknadStruktur(Long soknadId);
    SoknadStruktur hentSoknadStruktur(String skjemanummer);

    Map<String,String> hentInnsendtDatoForOpprinneligSoknad(String behandlingsId);

}