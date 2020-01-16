package no.nav.sbl.dialogarena.soknadinnsending.consumer.arbeidsforhold;

import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.binding.ArbeidsforholdV3;
import org.mockito.InjectMocks;
import org.mockito.Mock;

public class ArbeidsforholdServiceTest {

    @Mock
    private ArbeidsforholdV3 arbeidsforholdV3;

    @Mock
    private ArbeidsforholdConsumer arbeidsforholdConsumer;

    @Mock
    private ArbeidsforholdTransformer arbeidsforholdTransformer;

    @InjectMocks
    private ArbeidsforholdService service;


}