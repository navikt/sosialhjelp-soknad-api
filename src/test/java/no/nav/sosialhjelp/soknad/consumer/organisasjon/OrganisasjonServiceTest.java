package no.nav.sosialhjelp.soknad.consumer.organisasjon;

import no.nav.sosialhjelp.soknad.client.organisasjon.OrganisasjonClient;
import no.nav.sosialhjelp.soknad.client.organisasjon.dto.NavnDto;
import no.nav.sosialhjelp.soknad.client.organisasjon.dto.OrganisasjonNoekkelinfoDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrganisasjonServiceTest {

    @Mock
    private OrganisasjonClient organisasjonClient;

    @InjectMocks
    private OrganisasjonService service;

    private String orgnr = "12345";

    @Test
    void skalHentOrgNavnMedNullINavnelinjer() {
        when(organisasjonClient.hentOrganisasjonNoekkelinfo(anyString())).thenReturn(createOrgNoekkelinfoResponseWithNulls());

        String orgNavn = service.hentOrgNavn(orgnr);

        assertThat(orgNavn).isEqualTo("Testesen A/S, andre linje");
    }

    @Test
    void skalHentOrgNavnMedTommeStringsINavnelinjer() {
        when(organisasjonClient.hentOrganisasjonNoekkelinfo(anyString())).thenReturn(createOrgNoekkelinfoResponseWithEmptyStrings());

        String orgNavn = service.hentOrgNavn(orgnr);

        assertThat(orgNavn).isEqualTo("Testesen A/S, andre linje");
    }

    @Test
    void skalReturnereTomStringHvisOrgnrErNull() {
        String orgNavn = service.hentOrgNavn(null);

        assertThat(orgNavn).isBlank();
    }

    @Test
    void skalReturnereOrgNrSomOrgNavnHvisNoekkelinfoErNull() {
        when(organisasjonClient.hentOrganisasjonNoekkelinfo(anyString())).thenReturn(null);

        String orgNavn = service.hentOrgNavn(orgnr);

        assertThat(orgNavn).isEqualTo(orgnr);
    }

    @Test
    void skalReturnereOrgNrSomOrgNavnHvisConsumerKasterFeil() {
        when(organisasjonClient.hentOrganisasjonNoekkelinfo(anyString())).thenThrow(new RuntimeException("noe feil"));

        String orgNavn = service.hentOrgNavn(orgnr);

        assertThat(orgNavn).isEqualTo(orgnr);
    }

    private OrganisasjonNoekkelinfoDto createOrgNoekkelinfoResponseWithNulls() {
        NavnDto navn = new NavnDto("Testesen A/S", "andre linje", null, null, null);
        return new OrganisasjonNoekkelinfoDto(navn, orgnr);
    }

    private OrganisasjonNoekkelinfoDto createOrgNoekkelinfoResponseWithEmptyStrings() {
        NavnDto navn = new NavnDto("Testesen A/S", "andre linje", "", "", "");
        return new OrganisasjonNoekkelinfoDto(navn, orgnr);
    }
}