package no.nav.sbl.dialogarena.sikkerhet;

import no.nav.modig.security.tilgangskontroll.URN;
import no.nav.modig.security.tilgangskontroll.policy.attributes.values.StringValue;
import no.nav.modig.security.tilgangskontroll.policy.enrichers.EnvironmentRequestEnricher;
import no.nav.modig.security.tilgangskontroll.policy.pdp.DecisionPoint;
import no.nav.modig.security.tilgangskontroll.policy.pdp.picketlink.PicketLinkDecisionPoint;
import no.nav.modig.security.tilgangskontroll.policy.pep.EnforcementPoint;
import no.nav.modig.security.tilgangskontroll.policy.pep.PEPImpl;
import no.nav.modig.security.tilgangskontroll.policy.request.attributes.SubjectAttribute;
import no.nav.sbl.dialogarena.config.SikkerhetsConfig;
import no.nav.sbl.dialogarena.sendsoknad.domain.exception.AuthorizationException;
import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.OidcFeatureToggleUtils;
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

import static java.util.Arrays.asList;
import static no.nav.modig.security.tilgangskontroll.utils.AttributeUtils.*;
import static no.nav.modig.security.tilgangskontroll.utils.RequestUtils.forRequest;
import static no.nav.sbl.dialogarena.sikkerhet.XsrfGenerator.sjekkXsrfToken;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Tilgangskontrollimplementasjon som bruker EnforcementPoint fra modig.security
 */
@Named("tilgangskontroll")
public class Tilgangskontroll {

    private static final Logger logger = getLogger(Tilgangskontroll.class);

    public static final String URN_ENDEPUNKT = "urn:nav:ikt:tilgangskontroll:xacml:resource:endepunkt";

    private final EnforcementPoint pep;
    @Inject
    private SoknadMetadataRepository soknadMetadataRepository;
    @Inject
    private SoknadUnderArbeidRepository soknadUnderArbeidRepository;

    public Tilgangskontroll() {
        DecisionPoint pdp = new PicketLinkDecisionPoint(SikkerhetsConfig.class.getResource("/security/policyConfig.xml"));
        pep = new PEPImpl(pdp);
        ((PEPImpl) pep).setRequestEnrichers(asList(new EnvironmentRequestEnricher(), new SecurityContextRequestEnricher()));
    }
    
    public void verifiserAtBrukerKanEndreSoknad(String behandlingsId) {
        final HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        sjekkXsrfToken(request.getHeader("X-XSRF-TOKEN"), behandlingsId);
        verifiserBrukerHarTilgangTilSoknad(behandlingsId);
    }

    public void verifiserBrukerHarTilgangTilSoknad(String behandlingsId) {
        Optional<SoknadUnderArbeid> soknadUnderArbeidOptional = soknadUnderArbeidRepository.hentSoknadOptional(behandlingsId, OidcFeatureToggleUtils.getUserId());
        String aktoerId;
        if (soknadUnderArbeidOptional.isPresent()) {
            aktoerId = soknadUnderArbeidOptional.get().getEier();
        } else {
            throw new AuthorizationException("Bruker har ikke tilgang til søknaden.");
        }

        verifiserTilgangMotPep(aktoerId, behandlingsId);
    }

    public void verifiserBrukerHarTilgangTilMetadata(String behandlingsId) {
        String aktoerId = "undefined";
        try {
            SoknadMetadata metadata = soknadMetadataRepository.hent(behandlingsId);
            aktoerId = metadata.fnr;
        } catch (Exception e) {
            logger.warn("Kunne ikke avgjøre hvem som eier søknad med behandlingsId {} -> Ikke tilgang.", behandlingsId, e);
        }
        verifiserTilgangMotPep(aktoerId, behandlingsId);
    }

    public void verifiserTilgangMotPep(String eier, String behandlingsId) {
        if (Objects.isNull(eier)) {
            throw new AuthorizationException("");
        }
        String aktorId = OidcFeatureToggleUtils.getUserId();
        SubjectAttribute aktorSubjectId = new SubjectAttribute(new URN("urn:nav:ikt:tilgangskontroll:xacml:subject:aktor-id"), new StringValue(aktorId));

        try {
            pep.assertAccess(
                    forRequest(
                            resourceType("Soknad"),
                            resourceId(behandlingsId),
                            ownerId(eier),
                            aktorSubjectId));
        } catch (RuntimeException e) {
            throw new AuthorizationException(e.getMessage());
        }
    }
}