package no.nav.sbl.dialogarena.sikkerhet;

import no.nav.sbl.dialogarena.sendsoknad.domain.exception.AuthorizationException;

import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.SubjectHandler;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknadmetadata.SoknadMetadataRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.SoknadMetadata;
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
    private SoknadMetadataRepository soknadMetadataRepository;
    @Inject
    private SoknadUnderArbeidRepository soknadUnderArbeidRepository;

    @Inject
    private SubjectHandler subjectHandler;

    public void verifiserAtBrukerKanEndreSoknad(String behandlingsId) {
        final HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String oidcToken = subjectHandler.getOIDCTokenAsString();
        sjekkXsrfToken(request.getHeader("X-XSRF-TOKEN"), behandlingsId, oidcToken);
        verifiserBrukerHarTilgangTilSoknad(behandlingsId);
    }

    public void verifiserBrukerHarTilgangTilSoknad(String behandlingsId) {
        Optional<SoknadUnderArbeid> soknadUnderArbeidOptional = soknadUnderArbeidRepository.hentSoknadOptional(behandlingsId, subjectHandler.getUserId());
        String aktoerId;
        if (soknadUnderArbeidOptional.isPresent()) {
            aktoerId = soknadUnderArbeidOptional.get().getEier();
        } else {
            throw new AuthorizationException("Bruker har ikke tilgang til søknaden.");
        }

        verifiserTilgang(aktoerId);
    }

    public void verifiserBrukerHarTilgangTilMetadata(String behandlingsId) {
        String aktoerId = "undefined";
        try {
            SoknadMetadata metadata = soknadMetadataRepository.hent(behandlingsId);
            aktoerId = metadata.fnr;
        } catch (Exception e) {
            logger.warn("Kunne ikke avgjøre hvem som eier søknad med behandlingsId {} -> Ikke tilgang.", behandlingsId, e);
        }
        verifiserTilgang(aktoerId);
    }

    public void verifiserTilgang(String eier) {
        if (Objects.isNull(eier)) {
            throw new AuthorizationException("Søknaden har ingen eier");
        }
        String aktorId = subjectHandler.getUserId();
        if (!Objects.equals(aktorId, eier)) {
            throw new AuthorizationException("AktørId stemmer ikke overens med eieren til søknaden");
        }
    }
}