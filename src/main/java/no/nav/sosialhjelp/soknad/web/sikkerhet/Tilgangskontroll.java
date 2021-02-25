package no.nav.sosialhjelp.soknad.web.sikkerhet;

import no.nav.sosialhjelp.soknad.business.db.soknadmetadata.SoknadMetadataRepository;
import no.nav.sosialhjelp.soknad.business.domain.SoknadMetadata;
import no.nav.sosialhjelp.soknad.business.soknadunderbehandling.SoknadUnderArbeidRepository;
import no.nav.sosialhjelp.soknad.consumer.pdl.PdlService;
import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeid;
import no.nav.sosialhjelp.soknad.domain.model.exception.AuthorizationException;
import no.nav.sosialhjelp.soknad.domain.model.oidc.SubjectHandler;
import org.slf4j.Logger;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import java.util.Objects;
import java.util.Optional;

import static no.nav.sosialhjelp.soknad.consumer.pdl.person.PdlPersonMapper.KODE_6;
import static no.nav.sosialhjelp.soknad.consumer.pdl.person.PdlPersonMapper.KODE_7;
import static no.nav.sosialhjelp.soknad.web.sikkerhet.XsrfGenerator.sjekkXsrfToken;
import static org.slf4j.LoggerFactory.getLogger;

@Named("tilgangskontroll")
public class Tilgangskontroll {

    private static final Logger logger = getLogger(Tilgangskontroll.class);

    private final SoknadMetadataRepository soknadMetadataRepository;
    private final SoknadUnderArbeidRepository soknadUnderArbeidRepository;
    private final PdlService pdlService;

    public Tilgangskontroll(SoknadMetadataRepository soknadMetadataRepository, SoknadUnderArbeidRepository soknadUnderArbeidRepository, PdlService pdlService) {
        this.soknadMetadataRepository = soknadMetadataRepository;
        this.soknadUnderArbeidRepository = soknadUnderArbeidRepository;
        this.pdlService = pdlService;
    }

    public void verifiserAtBrukerKanEndreSoknad(String behandlingsId) {
        final HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        sjekkXsrfToken(request.getHeader("X-XSRF-TOKEN"), behandlingsId);
        verifiserBrukerHarTilgangTilSoknad(behandlingsId);
    }

    public void verifiserBrukerHarTilgangTilSoknad(String behandlingsId) {
        Optional<SoknadUnderArbeid> soknadUnderArbeidOptional = soknadUnderArbeidRepository.hentSoknadOptional(behandlingsId, SubjectHandler.getUserId());
        String fnr;
        if (soknadUnderArbeidOptional.isPresent()) {
            fnr = soknadUnderArbeidOptional.get().getEier();
        } else {
            throw new AuthorizationException("Bruker har ikke tilgang til søknaden.");
        }

        verifiserTilgang(fnr);
    }

    public void verifiserBrukerHarTilgangTilMetadata(String behandlingsId) {
        String fnr = "undefined";
        try {
            SoknadMetadata metadata = soknadMetadataRepository.hent(behandlingsId);
            fnr = metadata.fnr;
        } catch (Exception e) {
            logger.warn("Kunne ikke avgjøre hvem som eier søknad med behandlingsId {} -> Ikke tilgang.", behandlingsId, e);
        }
        verifiserTilgang(fnr);
    }

    private void verifiserTilgang(String eier) {
        if (Objects.isNull(eier)) {
            throw new AuthorizationException("Søknaden har ingen eier");
        }
        var fnr = SubjectHandler.getUserId();
        if (!Objects.equals(fnr, eier)) {
            throw new AuthorizationException("Fnr stemmer ikke overens med eieren til søknaden");
        }
        verifiserAtBrukerIkkeHarDiskresjonskode(fnr);
    }

    public void verifiserAtBrukerHarTilgang() {
        var fnr = SubjectHandler.getUserId();
        if (Objects.isNull(fnr)) {
            throw new AuthorizationException("Ingen tilgang når fnr ikke er satt");
        }
        verifiserAtBrukerIkkeHarDiskresjonskode(fnr);
    }

    private void verifiserAtBrukerIkkeHarDiskresjonskode(String ident) {
        var person = pdlService.hentPerson(ident);
        if (person.getDiskresjonskode() != null && (person.getDiskresjonskode().equals(KODE_6) || person.getDiskresjonskode().equals(KODE_7))) {
            throw new AuthorizationException("Bruker har ikke tilgang til søknaden.");
        }
    }
}