package no.nav.sosialhjelp.soknad.web.sikkerhet;

import no.nav.sosialhjelp.soknad.business.db.soknadmetadata.SoknadMetadataRepository;
import no.nav.sosialhjelp.soknad.business.domain.SoknadMetadata;
import no.nav.sosialhjelp.soknad.business.soknadunderbehandling.SoknadUnderArbeidRepository;
import no.nav.sosialhjelp.soknad.consumer.pdl.person.PersonService;
import no.nav.sosialhjelp.soknad.consumer.pdl.person.dto.AdressebeskyttelseDto;
import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeid;
import no.nav.sosialhjelp.soknad.domain.model.exception.AuthorizationException;
import no.nav.sosialhjelp.soknad.domain.model.oidc.StaticSubjectHandlerService;
import no.nav.sosialhjelp.soknad.domain.model.oidc.SubjectHandler;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Optional;

import static no.nav.sosialhjelp.soknad.business.service.soknadservice.SoknadService.createEmptyJsonInternalSoknad;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TilgangskontrollTest {

    @InjectMocks
    private Tilgangskontroll tilgangskontroll;
    @Mock
    private SoknadMetadataRepository soknadMetadataRepository;
    @Mock
    private SoknadUnderArbeidRepository soknadUnderArbeidRepository;
    @Mock
    private PersonService personService;

    @Before
    public void setUp() {
        System.setProperty("environment.name", "test");
        SubjectHandler.setSubjectHandlerService(new StaticSubjectHandlerService());
    }

    @After
    public void tearDown() {
        SubjectHandler.resetOidcSubjectHandlerService();
        System.clearProperty("environment.name");
    }

    @Test
    public void skalGiTilgangForBruker() {
        String userId = SubjectHandler.getUserId();
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withEier(userId).withJsonInternalSoknad(createEmptyJsonInternalSoknad(userId));
        when(soknadUnderArbeidRepository.hentSoknadOptional(anyString(), anyString())).thenReturn(Optional.of(soknadUnderArbeid));
        when(personService.hentAdressebeskyttelse(userId)).thenReturn(AdressebeskyttelseDto.Gradering.UGRADERT);

        assertThatNoException().isThrownBy(() -> tilgangskontroll.verifiserBrukerHarTilgangTilSoknad("123"));
    }

    @Test(expected = AuthorizationException.class)
    public void skalFeileForAndre() {
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad("other_user"));
        when(soknadUnderArbeidRepository.hentSoknadOptional(anyString(), anyString())).thenReturn(Optional.of(soknadUnderArbeid));
        tilgangskontroll.verifiserBrukerHarTilgangTilSoknad("XXX");
    }

    @Test(expected = AuthorizationException.class)
    public void skalFeileOmSoknadenIkkeFinnes() {
        when(soknadUnderArbeidRepository.hentSoknadOptional(anyString(), anyString())).thenReturn(Optional.empty());
        tilgangskontroll.verifiserBrukerHarTilgangTilSoknad("123");
    }

    @Test
    public void skalGiTilgangForBrukerMetadata() {
        var userId = SubjectHandler.getUserId();
        SoknadMetadata metadata = new SoknadMetadata();
        metadata.fnr = userId;
        when(soknadMetadataRepository.hent("123")).thenReturn(metadata);
        when(personService.hentAdressebeskyttelse(userId)).thenReturn(AdressebeskyttelseDto.Gradering.UGRADERT);

        assertThatNoException().isThrownBy(() -> tilgangskontroll.verifiserBrukerHarTilgangTilMetadata("123"));
    }

    @Test(expected = AuthorizationException.class)
    public void skalFeileForAndreMetadata() {
        SoknadMetadata metadata = new SoknadMetadata();
        metadata.fnr = "other_user";
        when(soknadMetadataRepository.hent("123")).thenReturn(metadata);
        tilgangskontroll.verifiserBrukerHarTilgangTilMetadata("123");
    }

    @Test(expected = AuthorizationException.class)
    public void skalFeileHvisEierErNull() {
        tilgangskontroll.verifiserBrukerHarTilgangTilSoknad("");
    }

    @Test(expected = AuthorizationException.class)
    public void skalFeileHvisBrukerHarAdressebeskyttelseStrengtFortrolig() {
        var userId = SubjectHandler.getUserId();
        when(personService.hentAdressebeskyttelse(userId)).thenReturn(AdressebeskyttelseDto.Gradering.STRENGT_FORTROLIG);
        tilgangskontroll.verifiserAtBrukerHarTilgang();
    }

    @Test(expected = AuthorizationException.class)
    public void skalFeileHvisBrukerHarAdressebeskyttelseFortrolig() {
        var userId = SubjectHandler.getUserId();
        when(personService.hentAdressebeskyttelse(userId)).thenReturn(AdressebeskyttelseDto.Gradering.FORTROLIG);
        tilgangskontroll.verifiserAtBrukerHarTilgang();
    }

}
