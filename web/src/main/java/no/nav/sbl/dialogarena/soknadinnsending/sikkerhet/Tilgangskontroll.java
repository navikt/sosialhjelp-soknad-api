package no.nav.sbl.dialogarena.soknadinnsending.sikkerhet;

import no.nav.modig.security.tilgangskontroll.URN;
import no.nav.modig.security.tilgangskontroll.policy.attributes.values.StringValue;
import no.nav.modig.security.tilgangskontroll.policy.enrichers.EnvironmentRequestEnricher;
import no.nav.modig.security.tilgangskontroll.policy.enrichers.SecurityContextRequestEnricher;
import no.nav.modig.security.tilgangskontroll.policy.pdp.DecisionPoint;
import no.nav.modig.security.tilgangskontroll.policy.pdp.picketlink.PicketLinkDecisionPoint;
import no.nav.modig.security.tilgangskontroll.policy.pep.EnforcementPoint;
import no.nav.modig.security.tilgangskontroll.policy.pep.PEPImpl;
import no.nav.modig.security.tilgangskontroll.policy.request.attributes.SubjectAttribute;
import no.nav.sbl.dialogarena.soknadinnsending.SoknadInnsendingConfig;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.aktor.AktorIdService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.henvendelse.HenvendelseConnector;

import javax.inject.Inject;
import javax.inject.Named;

import static java.util.Arrays.asList;
import static no.nav.modig.core.context.SubjectHandler.getSubjectHandler;
import static no.nav.modig.security.tilgangskontroll.utils.AttributeUtils.ownerId;
import static no.nav.modig.security.tilgangskontroll.utils.AttributeUtils.resourceId;
import static no.nav.modig.security.tilgangskontroll.utils.AttributeUtils.resourceType;
import static no.nav.modig.security.tilgangskontroll.utils.RequestUtils.forRequest;

/**
 * Tilgangskontrollimplementasjon som bruker EnforcementPoint fra modig.security
 */
@Named("tilgangskontroll")
public class Tilgangskontroll {

    @Inject
    private HenvendelseConnector soknadService;
    @Inject
    private AktorIdService aktorIdService;

    private final EnforcementPoint pep;

    public Tilgangskontroll() {
        DecisionPoint pdp = new PicketLinkDecisionPoint(SoknadInnsendingConfig.class.getResource("/security/policyConfig.xml"));
        pep = new PEPImpl(pdp);
        ((PEPImpl) pep).setRequestEnrichers(asList(new EnvironmentRequestEnricher(), new SecurityContextRequestEnricher()));
    }

    public void verifiserBrukerHarTilgangTilSoknad(Long soknadId) {
        String eier = soknadService.hentSoknadEier(soknadId);
        String aktorId = aktorIdService.hentAktorIdForFno(getSubjectHandler().getUid());

        SubjectAttribute aktorSubjectId = new SubjectAttribute(new URN("urn:nav:ikt:tilgangskontroll:xacml:subject:aktor-id"), new StringValue(aktorId));

        pep.assertAccess(
                forRequest(
                        resourceType("Soknad"),
                        resourceId(soknadId.toString()),
                        ownerId(eier),
                        aktorSubjectId));
    }
}