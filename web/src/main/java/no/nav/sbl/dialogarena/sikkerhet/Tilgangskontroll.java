package no.nav.sbl.dialogarena.sikkerhet;

import no.nav.sbl.dialogarena.sendsoknad.domain.exception.AuthorizationException;
import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.OidcFeatureToggleUtils;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknadmetadata.SoknadMetadataRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.SoknadMetadata;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadService;
import no.nav.sbl.sosialhjelp.domain.SoknadUnderArbeid;
import no.nav.sbl.sosialhjelp.soknadunderbehandling.SoknadUnderArbeidRepository;
import org.slf4j.Logger;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import java.util.Objects;
import java.util.Optional;

import static no.nav.sbl.dialogarena.sikkerhet.XsrfGenerator.sjekkXsrfToken;
import static org.slf4j.LoggerFactory.getLogger;

@Named("tilgangskontroll")
public class Tilgangskontroll {

    private static final Logger logger = getLogger(Tilgangskontroll.class);

    @Inject
    private SoknadService soknadService;
    @Inject
    private SoknadMetadataRepository soknadMetadataRepository;
    @Inject
    private SoknadUnderArbeidRepository soknadUnderArbeidRepository;

    public void verifiserAtBrukerKanEndreSoknad(String behandlingsId) {
        final HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        sjekkXsrfToken(request.getHeader("X-XSRF-TOKEN"), behandlingsId);
        verifiserBrukerHarTilgangTilSoknad(behandlingsId);
    }

    public void verifiserBrukerHarTilgangTilSoknad(String behandlingsId) {
        String aktoerId = "undefined";

        Optional<SoknadUnderArbeid> soknadUnderArbeid = soknadUnderArbeidRepository.hentSoknad(behandlingsId, OidcFeatureToggleUtils.getUserId());
        if (soknadUnderArbeid.isPresent()) {
            aktoerId = soknadUnderArbeid.get().getEier();
        } else {
            try {
                WebSoknad soknad = soknadService.hentSoknad(behandlingsId, false, false);
                aktoerId = soknad.getAktoerId();
            } catch (Exception e) {
                logger.warn("Kunne ikke avgjøre hvem som eier søknad med behandlingsId {} -> Ikke tilgang.", behandlingsId, e);
            }
        }

        verifiserTilgangMotPep(aktoerId);
    }

    public void verifiserBrukerHarTilgangTilMetadata(String behandlingsId) {
        String aktoerId = "undefined";
        try {
            SoknadMetadata metadata = soknadMetadataRepository.hent(behandlingsId);
            aktoerId = metadata.fnr;
        } catch (Exception e) {
            logger.warn("Kunne ikke avgjøre hvem som eier søknad med behandlingsId {} -> Ikke tilgang.", behandlingsId, e);
        }
        verifiserTilgangMotPep(aktoerId);
    }

    public void verifiserTilgangMotPep(String eier) {
        if (Objects.isNull(eier)) {
            throw new AuthorizationException("Søknaden har ingen eier");
        }
        String aktorId = OidcFeatureToggleUtils.getUserId();
        if (!Objects.equals(aktorId, eier)) {
            throw new AuthorizationException("AktørId stemmer ikke overens med eieren til søknaden");
        }
    }
}