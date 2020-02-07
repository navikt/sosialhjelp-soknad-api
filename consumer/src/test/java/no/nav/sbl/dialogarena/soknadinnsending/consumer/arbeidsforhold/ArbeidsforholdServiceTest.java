package no.nav.sbl.dialogarena.soknadinnsending.consumer.arbeidsforhold;

import no.nav.sbl.dialogarena.sendsoknad.domain.Arbeidsforhold;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.arbeidsforhold.dto.*;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.organisasjon.OrganisasjonService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;
import static java.util.Arrays.stream;
import static java.util.Collections.singletonList;
import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ArbeidsforholdServiceTest {

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

    @Before
    public void setUp() {
        when(organisasjonService.hentOrgNavn(anyString())).thenReturn(orgNavn);
    }

    @Test
    public void skalMappeDtoTilArbeidsforhold() {
        when(arbeidsforholdConsumer.finnArbeidsforholdForArbeidstaker(fnr)).thenReturn(singletonList(createArbeidsforhold(true, fom, tom)));

        List<Arbeidsforhold> arbeidsforholdList = service.hentArbeidsforhold(fnr);
        Arbeidsforhold arbeidsforhold = arbeidsforholdList.get(0);

        assertEquals(orgNavn, arbeidsforhold.arbeidsgivernavn);
        assertTrue(arbeidsforhold.harFastStilling);
        assertEquals(100L, arbeidsforhold.fastStillingsprosent.longValue());
        assertEquals(orgnr, arbeidsforhold.orgnr);
        assertEquals(fom.format(ISO_LOCAL_DATE), arbeidsforhold.fom);
        assertEquals(tom.format(ISO_LOCAL_DATE), arbeidsforhold.tom);
        assertEquals(1337L, arbeidsforhold.edagId.longValue());
    }

    @Test
    public void skalSetteArbeidsgivernavnTilOrgnrHvisArbeidsgiverErOrganisasjon() {
        when(arbeidsforholdConsumer.finnArbeidsforholdForArbeidstaker(fnr)).thenReturn(singletonList(createArbeidsforhold(false, fom, tom)));

        List<Arbeidsforhold> arbeidsforholdList = service.hentArbeidsforhold(fnr);
        Arbeidsforhold arbeidsforhold = arbeidsforholdList.get(0);

        assertEquals("Privatperson", arbeidsforhold.arbeidsgivernavn);
        assertNull(arbeidsforhold.orgnr);
    }

    @Test
    public void skalAddereStillingsprosentFraArbeidsavtaler() {
        when(arbeidsforholdConsumer.finnArbeidsforholdForArbeidstaker(fnr))
                .thenReturn(singletonList(createArbeidsforholdMedFlereArbeidsavtaler(12.3, 45.6)));

        List<Arbeidsforhold> arbeidsforholdList = service.hentArbeidsforhold(fnr);
        Arbeidsforhold arbeidsforhold = arbeidsforholdList.get(0);

        // desimaler strippes fra double til long
        assertEquals(57L, arbeidsforhold.fastStillingsprosent.longValue());
    }

    @Test
    public void ansettelsesperiodeTomKanVæreNull() {
        when(arbeidsforholdConsumer.finnArbeidsforholdForArbeidstaker(fnr)).thenReturn(singletonList(createArbeidsforhold(true, fom, null)));

        List<Arbeidsforhold> arbeidsforholdList = service.hentArbeidsforhold(fnr);
        Arbeidsforhold arbeidsforhold = arbeidsforholdList.get(0);

        assertEquals(orgNavn, arbeidsforhold.arbeidsgivernavn);
        assertTrue(arbeidsforhold.harFastStilling);
        assertEquals(100L, arbeidsforhold.fastStillingsprosent.longValue());
        assertEquals(orgnr, arbeidsforhold.orgnr);
        assertEquals(fom.format(ISO_LOCAL_DATE), arbeidsforhold.fom);
        assertNull(arbeidsforhold.tom);
        assertEquals(1337L, arbeidsforhold.edagId.longValue());
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