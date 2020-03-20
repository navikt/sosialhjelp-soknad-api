package no.nav.sbl.dialogarena.soknadinnsending.consumer.arbeidsforhold;

import no.nav.sbl.dialogarena.soknadinnsending.consumer.arbeidsforhold.dto.ArbeidsforholdDto;

import java.util.List;

public interface ArbeidsforholdConsumer {

    void ping();

    List<ArbeidsforholdDto> finnArbeidsforholdForArbeidstaker(String fodselsnummer);
}
