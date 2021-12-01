package no.nav.sosialhjelp.soknad.web.sikkerhet;

import no.nav.sosialhjelp.soknad.business.db.repositories.soknadmetadata.SoknadMetadataRepository;
import no.nav.sosialhjelp.soknad.business.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository;
import no.nav.sosialhjelp.soknad.business.domain.SoknadMetadata;
import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeid;
import no.nav.sosialhjelp.soknad.domain.model.exception.AuthorizationException;
import no.nav.sosialhjelp.soknad.domain.model.oidc.SubjectHandler;
import no.nav.sosialhjelp.soknad.personalia.person.PersonService;
import no.nav.sosialhjelp.soknad.personalia.person.dto.Gradering;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Objects;
import java.util.Optional;

import static no.nav.sosialhjelp.soknad.web.utils.XsrfGenerator.sjekkXsrfToken;
import static org.slf4j.LoggerFactory.getLogger;

@Component
public class Tilgangskontroll {

    private static final Logger logger = getLogger(Tilgangskontroll.class);

    private final SoknadMetadataRepository soknadMetadataRepository;
    private final SoknadUnderArbeidRepository soknadUnderArbeidRepository;
    private final PersonService personService;

    public Tilgangskontroll(SoknadMetadataRepository soknadMetadataRepository, SoknadUnderArbeidRepository soknadUnderArbeidRepository, PersonService personService) {
        this.soknadMetadataRepository = soknadMetadataRepository;
        this.soknadUnderArbeidRepository = soknadUnderArbeidRepository;
        this.personService = personService;
    }

    public void verifiserAtBrukerKanEndreSoknad(String behandlingsId) {
        final HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        sjekkXsrfToken(request.getHeader("X-XSRF-TOKEN"), behandlingsId);
        verifiserBrukerHarTilgangTilSoknad(behandlingsId);
    }

    public void verifiserBrukerHarTilgangTilSoknad(String behandlingsId) {
        Optional<SoknadUnderArbeid> soknadUnderArbeidOptional = soknadUnderArbeidRepository.hentSoknadOptional(behandlingsId, SubjectHandler.getUserId());
        String eier;
        if (soknadUnderArbeidOptional.isPresent()) {
            eier = soknadUnderArbeidOptional.get().getEier();
        } else {
            throw new AuthorizationException("Bruker har ikke tilgang til søknaden.");
        }

        verifiserAtInnloggetBrukerErEierAvSoknad(eier);
    }

    public void verifiserBrukerHarTilgangTilMetadata(String behandlingsId) {
        String eier = "undefined";
        try {
            SoknadMetadata metadata = soknadMetadataRepository.hent(behandlingsId);
            eier = metadata.fnr;
        } catch (Exception e) {
            logger.warn("Kunne ikke avgjøre hvem som eier søknad med behandlingsId {} -> Ikke tilgang.", behandlingsId, e);
        }
        verifiserAtInnloggetBrukerErEierAvSoknad(eier);
    }

    private void verifiserAtInnloggetBrukerErEierAvSoknad(String eier) {
        if (Objects.isNull(eier)) {
            throw new AuthorizationException("Søknaden har ingen eier");
        }
        var fnr = SubjectHandler.getUserId();
        if (!Objects.equals(fnr, eier)) {
            throw new AuthorizationException("Fnr stemmer ikke overens med eieren til søknaden");
        }
        verifiserAtBrukerIkkeHarAdressebeskyttelse(fnr);
    }

    public void verifiserAtBrukerHarTilgang() {
        var fnr = SubjectHandler.getUserId();
        if (Objects.isNull(fnr)) {
            throw new AuthorizationException("Ingen tilgang når fnr ikke er satt");
        }
        verifiserAtBrukerIkkeHarAdressebeskyttelse(fnr);
    }

    private void verifiserAtBrukerIkkeHarAdressebeskyttelse(String ident) {
        var adressebeskyttelse = personService.hentAdressebeskyttelse(ident);
        if (Gradering.FORTROLIG.equals(adressebeskyttelse) || Gradering.STRENGT_FORTROLIG.equals(adressebeskyttelse) || Gradering.STRENGT_FORTROLIG_UTLAND.equals(adressebeskyttelse)) {
            throw new AuthorizationException("Bruker har ikke tilgang til søknaden.");
        }
    }
}