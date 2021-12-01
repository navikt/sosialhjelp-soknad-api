package no.nav.sosialhjelp.soknad.web.sikkerhet;

import no.nav.sosialhjelp.soknad.business.db.repositories.soknadmetadata.SoknadMetadataRepository;
import no.nav.sosialhjelp.soknad.business.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository;
import no.nav.sosialhjelp.soknad.business.domain.SoknadMetadata;
import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeid;
import no.nav.sosialhjelp.soknad.domain.model.exception.AuthorizationException;
import no.nav.sosialhjelp.soknad.domain.model.oidc.StaticSubjectHandlerService;
import no.nav.sosialhjelp.soknad.domain.model.oidc.SubjectHandler;
import no.nav.sosialhjelp.soknad.personalia.person.PersonService;
import no.nav.sosialhjelp.soknad.personalia.person.dto.Gradering;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static no.nav.sosialhjelp.soknad.business.service.soknadservice.SoknadService.createEmptyJsonInternalSoknad;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TilgangskontrollTest {

    @InjectMocks
    private Tilgangskontroll tilgangskontroll;
    @Mock
    private SoknadMetadataRepository soknadMetadataRepository;
    @Mock
    private SoknadUnderArbeidRepository soknadUnderArbeidRepository;
    @Mock
    private PersonService personService;

    @BeforeEach
    public void setUp() {
        System.setProperty("environment.name", "test");
        SubjectHandler.setSubjectHandlerService(new StaticSubjectHandlerService());
    }

    @AfterEach
    public void tearDown() {
        SubjectHandler.resetOidcSubjectHandlerService();
        System.clearProperty("environment.name");
    }

    @Test
    void skalGiTilgangForBruker() {
        String userId = SubjectHandler.getUserId();
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withEier(userId).withJsonInternalSoknad(createEmptyJsonInternalSoknad(userId));
        when(soknadUnderArbeidRepository.hentSoknadOptional(anyString(), anyString())).thenReturn(Optional.of(soknadUnderArbeid));
        when(personService.hentAdressebeskyttelse(userId)).thenReturn(Gradering.UGRADERT);

        assertThatNoException().isThrownBy(() -> tilgangskontroll.verifiserBrukerHarTilgangTilSoknad("123"));
    }

    @Test
    void skalFeileForAndre() {
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad("other_user"));
        when(soknadUnderArbeidRepository.hentSoknadOptional(anyString(), anyString())).thenReturn(Optional.of(soknadUnderArbeid));

        assertThatExceptionOfType(AuthorizationException.class)
                .isThrownBy(() -> tilgangskontroll.verifiserBrukerHarTilgangTilSoknad("XXX"));
    }

    @Test
    void skalFeileOmSoknadenIkkeFinnes() {
        when(soknadUnderArbeidRepository.hentSoknadOptional(anyString(), anyString())).thenReturn(Optional.empty());

        assertThatExceptionOfType(AuthorizationException.class)
                .isThrownBy(() -> tilgangskontroll.verifiserBrukerHarTilgangTilSoknad("123"));
    }

    @Test
    void skalGiTilgangForBrukerMetadata() {
        var userId = SubjectHandler.getUserId();
        SoknadMetadata metadata = new SoknadMetadata();
        metadata.fnr = userId;
        when(soknadMetadataRepository.hent("123")).thenReturn(metadata);
        when(personService.hentAdressebeskyttelse(userId)).thenReturn(Gradering.UGRADERT);

        assertThatNoException().isThrownBy(() -> tilgangskontroll.verifiserBrukerHarTilgangTilMetadata("123"));
    }

    @Test
    void skalFeileForAndreMetadata() {
        SoknadMetadata metadata = new SoknadMetadata();
        metadata.fnr = "other_user";
        when(soknadMetadataRepository.hent("123")).thenReturn(metadata);

        assertThatExceptionOfType(AuthorizationException.class)
                .isThrownBy(() -> tilgangskontroll.verifiserBrukerHarTilgangTilMetadata("123"));
    }

    @Test
    void skalFeileHvisEierErNull() {
        assertThatExceptionOfType(AuthorizationException.class)
                .isThrownBy(() -> tilgangskontroll.verifiserBrukerHarTilgangTilSoknad(""));
    }

    @Test
    void skalFeileHvisBrukerHarAdressebeskyttelseStrengtFortrolig() {
        var userId = SubjectHandler.getUserId();
        when(personService.hentAdressebeskyttelse(userId)).thenReturn(Gradering.STRENGT_FORTROLIG);

        assertThatExceptionOfType(AuthorizationException.class)
                .isThrownBy(() -> tilgangskontroll.verifiserAtBrukerHarTilgang());
    }

    @Test
    void skalFeileHvisBrukerHarAdressebeskyttelseFortrolig() {
        var userId = SubjectHandler.getUserId();
        when(personService.hentAdressebeskyttelse(userId)).thenReturn(Gradering.FORTROLIG);

        assertThatExceptionOfType(AuthorizationException.class)
                .isThrownBy(() -> tilgangskontroll.verifiserAtBrukerHarTilgang());
    }

}
