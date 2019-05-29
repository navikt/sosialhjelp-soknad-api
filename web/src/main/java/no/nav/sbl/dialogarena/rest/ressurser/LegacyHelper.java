package no.nav.sbl.dialogarena.rest.ressurser;

import no.nav.sbl.dialogarena.sendsoknad.domain.exception.AuthorizationException;
import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.OidcFeatureToggleUtils;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sikkerhet.Tilgangskontroll;
import no.nav.sbl.dialogarena.soknadinnsending.business.WebSoknadConfig;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.VedleggService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadService;
import no.nav.sbl.sosialhjelp.domain.SoknadUnderArbeid;
import no.nav.sbl.sosialhjelp.midlertidig.WebSoknadConverter;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.Objects;

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
    
    @Inject
    private Tilgangskontroll tilgangskontroll;

    public SoknadUnderArbeid hentSoknad(String behandlingsId, String eier, boolean medVedlegg) {
        final WebSoknad webSoknad = hentWebSoknad(behandlingsId, eier);

        final SoknadUnderArbeid soknad = webSoknadConverter.mapWebSoknadTilSoknadUnderArbeid(webSoknad, medVedlegg);
        if (!eier.equals(soknad.getJsonInternalSoknad().getSoknad().getData().getPersonalia().getPersonIdentifikator().getVerdi())) {
            throw new IllegalStateException("Feillagrede brukerdata for søknad: " + behandlingsId);
        }
        return soknad;
    }

    public WebSoknad hentWebSoknad(String behandlingsId, String eier) {
        if (Objects.isNull(eier)) {
            throw new AuthorizationException("");
        }

        /* Dette burde egentlig være unødvendig, men sjekker i tilfelle lesing av WebSoknad kan ha sideeffekter: */
        if (eier == null || !eier.equals(OidcFeatureToggleUtils.getUserId())) {
            throw new IllegalStateException("Har spurt på en annen bruker enn den som er pålogget. Dette er ikke støttet/tillatt.");
        }
        tilgangskontroll.verifiserBrukerHarTilgangTilSoknad(behandlingsId);

        final WebSoknad webSoknad = soknadService.hentSoknad(behandlingsId, true, true);
        if (!eier.equals(webSoknad.getAktoerId())) {
            throw new AuthorizationException("Ingen tilgang til angitt søknad for angitt bruker");
        }
        webSoknad.fjernFaktaSomIkkeSkalVaereSynligISoknaden(webSoknadConfig.hentStruktur(webSoknad.getskjemaNummer()));
        vedleggService.leggTilKodeverkFelter(webSoknad.hentPaakrevdeVedlegg());

        return webSoknad;
    }
}
