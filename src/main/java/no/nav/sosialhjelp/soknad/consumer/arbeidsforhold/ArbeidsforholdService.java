package no.nav.sosialhjelp.soknad.consumer.arbeidsforhold;

import no.nav.sosialhjelp.soknad.consumer.arbeidsforhold.dto.ArbeidsforholdDto;
import no.nav.sosialhjelp.soknad.consumer.arbeidsforhold.dto.OrganisasjonDto;
import no.nav.sosialhjelp.soknad.consumer.arbeidsforhold.dto.PeriodeDto;
import no.nav.sosialhjelp.soknad.consumer.arbeidsforhold.dto.PersonDto;
import no.nav.sosialhjelp.soknad.domain.model.Arbeidsforhold;
import no.nav.sosialhjelp.soknad.organisasjon.OrganisasjonService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;


@Component
public class ArbeidsforholdService {

    private static final Logger log = LoggerFactory.getLogger(ArbeidsforholdService.class);

    private final ArbeidsforholdConsumer arbeidsforholdConsumer;
    private final OrganisasjonService organisasjonService;

    public ArbeidsforholdService(ArbeidsforholdConsumer arbeidsforholdConsumer, OrganisasjonService organisasjonService) {
        this.arbeidsforholdConsumer = arbeidsforholdConsumer;
        this.organisasjonService = organisasjonService;
    }

    public List<Arbeidsforhold> hentArbeidsforhold(String fnr) {
        var arbeidsforholdDtos = arbeidsforholdConsumer.finnArbeidsforholdForArbeidstaker(fnr);
        if (arbeidsforholdDtos == null) {
            return null;
        }
        log.info("Hentet {} arbeidsforhold fra aareg", arbeidsforholdDtos.size());

        return arbeidsforholdDtos.stream()
                .map(this::mapToDomain)
                .collect(Collectors.toList());
    }

    public Arbeidsforhold mapToDomain(ArbeidsforholdDto dto) {
        Arbeidsforhold result = new Arbeidsforhold();
        result.edagId = dto.getNavArbeidsforholdId();
        result.orgnr = null;
        result.arbeidsgivernavn = "";

        if (dto.getArbeidsgiver() instanceof OrganisasjonDto) {
            result.orgnr = ((OrganisasjonDto) dto.getArbeidsgiver()).getOrganisasjonsnummer();
            result.arbeidsgivernavn = organisasjonService.hentOrgNavn(result.orgnr);
        } else if (dto.getArbeidsgiver() instanceof PersonDto) {
            result.arbeidsgivernavn = "Privatperson";
        }

        PeriodeDto periode = dto.getAnsettelsesperiode().getPeriode();
        result.fom = periode.getFom().format(DateTimeFormatter.ISO_LOCAL_DATE);
        result.tom = periode.getTom() != null ? periode.getTom().format(DateTimeFormatter.ISO_LOCAL_DATE) : null;

        dto.getArbeidsavtaler()
                .forEach(arbeidsavtale -> {
                    result.harFastStilling = true;
                    result.fastStillingsprosent += (long) arbeidsavtale.getStillingsprosent(); // Hvorfor er "fastStillingsprosent" long?
                });
        return result;
    }
}
