package no.nav.sbl.dialogarena.soknadinnsending.business.db;


import no.nav.modig.lang.option.Optional;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.DelstegStatus;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;

import java.util.List;

public interface SoknadRepository {

    Long opprettSoknad(WebSoknad soknad);

    WebSoknad hentSoknad(Long id);

    WebSoknad hentSoknadMedData(Long id);

    List<Faktum> hentAlleBrukerData(Long soknadId);

    Optional<WebSoknad> plukkSoknadTilMellomlagring();

    void leggTilbake(WebSoknad webSoknad);

    List<WebSoknad> hentListe(String aktorId);

    Long lagreFaktum(long soknadId, Faktum faktum);

    Long lagreFaktum(long soknadId, Faktum faktum, Boolean systemFaktum);

    WebSoknad hentMedBehandlingsId(String behandlingsId);

    Faktum hentFaktum(Long soknadId, Long faktumId);

    List<Faktum> hentSystemFaktumList(Long soknadId, String key);

    void settSistLagretTidspunkt(Long soknadId);

    void slettBrukerFaktum(Long soknadId, Long faktumId);

    void slettSoknad(long soknadId);

    String hentSoknadType(Long soknadId);

    Boolean isVedleggPaakrevd(Long soknadId, String key, String value, String dependOnValue);

    void settDelstegstatus(Long soknadId, DelstegStatus status);

    List<Faktum> hentBarneFakta(Long soknadId, Long faktumId);

    void populerFraStruktur(WebSoknad soknad);
}
