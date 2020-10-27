package no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl;


import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.SubjectHandler;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.person.AdressebeskyttelseDto;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.person.FoedselDto;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.person.NavnDto;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.person.PdlPerson;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.person.SivilstandDto;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.person.StatsborgerskapDto;
import org.mockito.invocation.InvocationOnMock;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PdlConsumerMock {

    private static Map<String, PdlPerson> responses = new HashMap<>();

    public static PdlPerson getOrCreateCurrentUserResponse(InvocationOnMock invocationOnMock) {
        PdlPerson response = responses.get(SubjectHandler.getUserId());
        if (response == null) {
            response = defaultPerson();
            responses.put(SubjectHandler.getUserId(), response);
        }

        return response;
    }

    private static PdlPerson defaultPerson() {
        PdlPerson person = new PdlPerson(
                singletonList(new AdressebeskyttelseDto(AdressebeskyttelseDto.Gradering.UGRADERT)),
                emptyList(), // ingen familierelasjoner
                singletonList(new FoedselDto(LocalDate.of(1970, 1, 1))),
                singletonList(new NavnDto("fornavn", "mellomnavn", "etternavn")),
                singletonList(new SivilstandDto(SivilstandDto.SivilstandType.GIFT, "annenFnr")),
                singletonList(new StatsborgerskapDto("NOR"))
        );
        return person;
    }

    public PdlConsumer pdlConsumerMock() {

        PdlConsumer mock = mock(PdlConsumer.class);

        when(mock.hentPerson(anyString()))
                .thenAnswer(PdlConsumerMock::getOrCreateCurrentUserResponse);

        return mock;
    }
}
