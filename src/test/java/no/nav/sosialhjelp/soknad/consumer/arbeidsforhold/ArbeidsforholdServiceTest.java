package no.nav.sosialhjelp.soknad.consumer.arbeidsforhold;

import no.nav.sosialhjelp.soknad.consumer.arbeidsforhold.dto.AnsettelsesperiodeDto;
import no.nav.sosialhjelp.soknad.consumer.arbeidsforhold.dto.ArbeidsavtaleDto;
import no.nav.sosialhjelp.soknad.consumer.arbeidsforhold.dto.ArbeidsforholdDto;
import no.nav.sosialhjelp.soknad.consumer.arbeidsforhold.dto.OrganisasjonDto;
import no.nav.sosialhjelp.soknad.consumer.arbeidsforhold.dto.PeriodeDto;
import no.nav.sosialhjelp.soknad.consumer.arbeidsforhold.dto.PersonDto;
import no.nav.sosialhjelp.soknad.domain.model.Arbeidsforhold;
import no.nav.sosialhjelp.soknad.organisasjon.OrganisasjonService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;
import static java.util.Arrays.stream;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ArbeidsforholdServiceTest {

    @Mock
    private ArbeidsforholdConsumer arbeidsforholdConsumer;

    @Mock
    private OrganisasjonService organisasjonService;

    @InjectMocks
    private ArbeidsforholdService service;

    private String fnr = "11111111111";
    private String orgnr = "orgnr";
    private String orgNavn = "Testbedriften A/S";
    private LocalDate fom = LocalDate.now().minusMonths(1);
    private LocalDate tom = LocalDate.now();

    @Test
    void skalMappeDtoTilArbeidsforhold() {
        when(arbeidsforholdConsumer.finnArbeidsforholdForArbeidstaker(fnr)).thenReturn(singletonList(createArbeidsforhold(true, fom, tom)));
        when(organisasjonService.hentOrgNavn(anyString())).thenReturn(orgNavn);

        List<Arbeidsforhold> arbeidsforholdList = service.hentArbeidsforhold(fnr);
        Arbeidsforhold arbeidsforhold = arbeidsforholdList.get(0);

        assertThat(arbeidsforhold.arbeidsgivernavn).isEqualTo(orgNavn);
        assertThat(arbeidsforhold.harFastStilling).isTrue();
        assertThat(arbeidsforhold.fastStillingsprosent.longValue()).isEqualTo(100L);
        assertThat(arbeidsforhold.orgnr).isEqualTo(orgnr);
        assertThat(arbeidsforhold.fom).isEqualTo(fom.format(ISO_LOCAL_DATE));
        assertThat(arbeidsforhold.tom).isEqualTo(tom.format(ISO_LOCAL_DATE));
        assertThat(arbeidsforhold.edagId.longValue()).isEqualTo(1337L);
    }

    @Test
    void skalSetteArbeidsgivernavnTilOrgnrHvisArbeidsgiverErOrganisasjon() {
        when(arbeidsforholdConsumer.finnArbeidsforholdForArbeidstaker(fnr)).thenReturn(singletonList(createArbeidsforhold(false, fom, tom)));

        List<Arbeidsforhold> arbeidsforholdList = service.hentArbeidsforhold(fnr);
        Arbeidsforhold arbeidsforhold = arbeidsforholdList.get(0);

        assertThat(arbeidsforhold.arbeidsgivernavn).isEqualTo("Privatperson");
        assertThat(arbeidsforhold.orgnr).isNull();
    }

    @Test
    void skalAddereStillingsprosentFraArbeidsavtaler() {
        when(arbeidsforholdConsumer.finnArbeidsforholdForArbeidstaker(fnr))
                .thenReturn(singletonList(createArbeidsforholdMedFlereArbeidsavtaler(12.3, 45.6)));
        when(organisasjonService.hentOrgNavn(anyString())).thenReturn(orgNavn);

        List<Arbeidsforhold> arbeidsforholdList = service.hentArbeidsforhold(fnr);
        Arbeidsforhold arbeidsforhold = arbeidsforholdList.get(0);

        // desimaler strippes fra double til long
        assertThat(arbeidsforhold.fastStillingsprosent.longValue()).isEqualTo(57L);
    }

    @Test
    void ansettelsesperiodeTomKanVæreNull() {
        when(arbeidsforholdConsumer.finnArbeidsforholdForArbeidstaker(fnr)).thenReturn(singletonList(createArbeidsforhold(true, fom, null)));
        when(organisasjonService.hentOrgNavn(anyString())).thenReturn(orgNavn);

        List<Arbeidsforhold> arbeidsforholdList = service.hentArbeidsforhold(fnr);
        Arbeidsforhold arbeidsforhold = arbeidsforholdList.get(0);

        assertThat(arbeidsforhold.arbeidsgivernavn).isEqualTo(orgNavn);
        assertThat(arbeidsforhold.harFastStilling).isTrue();
        assertThat(arbeidsforhold.fastStillingsprosent.longValue()).isEqualTo(100L);
        assertThat(arbeidsforhold.orgnr).isEqualTo(orgnr);
        assertThat(arbeidsforhold.fom).isEqualTo(fom.format(ISO_LOCAL_DATE));
        assertThat(arbeidsforhold.tom).isNull();
        assertThat(arbeidsforhold.edagId.longValue()).isEqualTo(1337L);
    }

    private ArbeidsforholdDto createArbeidsforhold(boolean erArbeidsgiverOrganisasjon, LocalDate fom, LocalDate tom) {
        AnsettelsesperiodeDto ansettelsesperiodeDto = new AnsettelsesperiodeDto(new PeriodeDto(fom, tom));
        ArbeidsavtaleDto arbeidsavtaleDto = createArbeidsavtale(100.0);
        return new ArbeidsforholdDto(
                ansettelsesperiodeDto,
                singletonList(arbeidsavtaleDto),
                "arbeidsforholdId",
                erArbeidsgiverOrganisasjon ? createArbeidsgiverOrganisasjon() : createArbeidsgiverPerson(),
                createArbeidstaker(),
                1337L);
    }

    private ArbeidsforholdDto createArbeidsforholdMedFlereArbeidsavtaler(double... stillingsprosenter) {
        AnsettelsesperiodeDto ansettelsesperiodeDto = new AnsettelsesperiodeDto(new PeriodeDto(fom, tom));
        List<ArbeidsavtaleDto> arbeidsavtaler = stream(stillingsprosenter)
                .boxed()
                .map(this::createArbeidsavtale)
                .collect(Collectors.toList());
        return new ArbeidsforholdDto(
                ansettelsesperiodeDto,
                arbeidsavtaler,
                "arbeidsforholdId",
                createArbeidsgiverOrganisasjon(),
                createArbeidstaker(),
                1337L);
    }

    private ArbeidsavtaleDto createArbeidsavtale(double stillingsprosent) {
        return new ArbeidsavtaleDto(stillingsprosent);
    }

    private OrganisasjonDto createArbeidsgiverOrganisasjon() {
        return new OrganisasjonDto(orgnr, "Organisasjon");
    }

    private PersonDto createArbeidsgiverPerson() {
        return new PersonDto("arbeidsgiver_fnr", "aktoerid", "Person");
    }

    private PersonDto createArbeidstaker() {
        return new PersonDto("arbeidstaker_fnr", "aktoerid", "Person");
    }
}