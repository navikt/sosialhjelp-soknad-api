package no.nav.sbl.dialogarena.rest.ressurser;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

import no.nav.modig.core.context.SubjectHandler;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.soknadinnsending.business.WebSoknadConfig;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.VedleggService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadService;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.sosialhjelp.midlertidig.WebSoknadConverter;

@Component
public class LegacyHelper {
    
    @Inject
    private VedleggService vedleggService;

    @Inject
    private SoknadService soknadService;
    
    @Inject
    private WebSoknadConfig webSoknadConfig;

    @Inject
    private WebSoknadConverter webSoknadConverter;
    

    public JsonInternalSoknad hentSoknad(String behandlingsId) {
        final WebSoknad webSoknad = soknadService.hentSoknad(behandlingsId, true, true);
        webSoknad.fjernFaktaSomIkkeSkalVaereSynligISoknaden(webSoknadConfig.hentStruktur(webSoknad.getskjemaNummer()));
        vedleggService.leggTilKodeverkFelter(webSoknad.hentPaakrevdeVedlegg());

        final JsonInternalSoknad soknad = webSoknadConverter.mapWebSoknadTilJsonSoknadInternal(webSoknad);
        final String loggedInUser = SubjectHandler.getSubjectHandler().getUid();
        if (!soknad.getSoknad().getData().getPersonalia().getPersonIdentifikator().getVerdi().equals(loggedInUser)) {
            throw new RuntimeException("Feillagrede brukerdata for søknad: " + behandlingsId);
        }
        return soknad;
    }
}
