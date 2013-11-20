package no.nav.sbl.dialogarena.soknadinnsending.business.db;


import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Vedlegg;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;

import java.io.InputStream;
import java.util.List;

public interface SoknadRepository {
	
    Long opprettSoknad(WebSoknad soknad);

    WebSoknad hentSoknad(Long id);

    WebSoknad hentSoknadMedData(Long id);

    List<Faktum> hentAlleBrukerData(Long soknadId);
    
	void avslutt(WebSoknad soknad);

	void avbryt(Long soknad);

	List<WebSoknad> hentListe(String aktorId);

	void lagreFaktum(long soknadId, Faktum faktum);

	WebSoknad hentMedBehandlingsId(String behandlingsId);

	String opprettBehandling();

    Long lagreVedlegg(final Vedlegg vedlegg);


    void slettVedlegg(Long soknadId, Long vedleggId);

    List<Vedlegg> hentVedleggForFaktum(Long soknadId, Long faktum);

    InputStream hentVedlegg(Long soknadI, Long vedleggId);
}
