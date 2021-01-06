package no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl;


import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.SubjectHandler;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.barn.PdlBarn;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.common.AdressebeskyttelseDto;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.common.BostedsadresseDto;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.common.EndringDto;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.common.FoedselDto;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.common.FolkeregistermetadataDto;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.common.FolkeregisterpersonstatusDto;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.common.KontaktadresseDto;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.common.MetadataDto;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.common.NavnDto;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.common.SivilstandDto;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.common.StatsborgerskapDto;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.common.VegadresseDto;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.ektefelle.PdlEktefelle;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.person.PdlPerson;
import org.mockito.invocation.InvocationOnMock;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PdlConsumerMock {

    private static final String EKTEFELLE_FNR = "11111111111";

    private static Map<String, PdlPerson> pdlPersonResponses = new HashMap<>();
    private static Map<String, PdlEktefelle> pdlEktefelleResponses = new HashMap<>();
    private static Map<String, PdlBarn> pdlBarnResponses = new HashMap<>();

    public static PdlPerson getOrCreateCurrentPdlPersonResponse(InvocationOnMock invocationOnMock) {
        PdlPerson response = pdlPersonResponses.get(SubjectHandler.getUserId());
        if (response == null) {
            response = defaultPerson();
            pdlPersonResponses.put(SubjectHandler.getUserId(), response);
        }

        return response;
    }

    public static PdlEktefelle getOrCreateCurrentPdlEktefelleResponse(InvocationOnMock invocationOnMock) {
        var ektefelleFnr = (String) invocationOnMock.getArgument(0);
        PdlEktefelle response = pdlEktefelleResponses.get(ektefelleFnr);
        if (response == null) {
            response = defaultEktefelle();
            pdlEktefelleResponses.put(ektefelleFnr, response);
        }

        return response;
    }

    public static PdlBarn getOrCreateCurrentPdlBarnResponse(InvocationOnMock invocationOnMock) {
        var barnFnr = (String) invocationOnMock.getArgument(0);
        PdlBarn response = pdlBarnResponses.get(barnFnr);
        if (response == null) {
            response = defaultBarn();
            pdlBarnResponses.put(barnFnr, response);
        }

        return response;
    }

    private static PdlPerson defaultPerson() {
        return new PdlPerson(
                singletonList(new AdressebeskyttelseDto(AdressebeskyttelseDto.Gradering.UGRADERT)),
                singletonList(new BostedsadresseDto(null, new VegadresseDto("123123", "GATEVEIEN", 1, "A", null, "0690", "0301", null), null, null)),
                emptyList(), // ingen oppholdsadresse
                singletonList(new KontaktadresseDto(KontaktadresseDto.Type.INNLAND, null, new VegadresseDto(null, "midlertidig adresse gate", 1, "D", null, "0471", "Oslo", null), null, null)),
                emptyList(), // ingen familierelasjoner for mockperson
                singletonList(new NavnDto("rask", "jule", "mat", defaultMetadata(), defaultFolkeregisterMetadata())),
                singletonList(new SivilstandDto(
                        SivilstandDto.SivilstandType.GIFT,
                        EKTEFELLE_FNR,
                        defaultMetadata(),
                        defaultFolkeregisterMetadata())),
                singletonList(new StatsborgerskapDto("NOR"))
        );
    }

    private static PdlEktefelle defaultEktefelle() {
        return new PdlEktefelle(
                singletonList(new AdressebeskyttelseDto(AdressebeskyttelseDto.Gradering.UGRADERT)),
                singletonList(new BostedsadresseDto(null, new VegadresseDto("123123", "GATEVEIEN", 1, "A", null, "0690", "0301", null), null, null)),
                singletonList(new FoedselDto(LocalDate.of(1970, 1, 1))),
                singletonList(new NavnDto("ektefelle", "mellomnavn", "etternavn", defaultMetadata(), defaultFolkeregisterMetadata()))
        );
    }

    private static PdlBarn defaultBarn() {
        return new PdlBarn(
                singletonList(new AdressebeskyttelseDto(AdressebeskyttelseDto.Gradering.UGRADERT)),
                singletonList(new BostedsadresseDto(null, new VegadresseDto("123123", "GATEVEIEN", 1, "A", null, "0690", "0301", null), null, null)),
                singletonList(new FolkeregisterpersonstatusDto("bosatt")),
                singletonList(new FoedselDto(LocalDate.of(LocalDate.now().getYear() - 10, 1, 1))),
                singletonList(new NavnDto("barn", "mellomnavn", "etternavn", defaultMetadata(), defaultFolkeregisterMetadata()))
        );
    }

    private static MetadataDto defaultMetadata() {
        return new MetadataDto("FREG", singletonList(new EndringDto("FREG", LocalDateTime.now().minusDays(15), null, null, null)));
    }

    private static FolkeregistermetadataDto defaultFolkeregisterMetadata() {
        return new FolkeregistermetadataDto(LocalDateTime.now().minusMonths(1), null, null, "FREG");
    }

    public PdlConsumer pdlConsumerMock() {
        PdlConsumer mock = mock(PdlConsumer.class);

        when(mock.hentPerson(any()))
                .thenAnswer(PdlConsumerMock::getOrCreateCurrentPdlPersonResponse);
        when(mock.hentEktefelle(any()))
                .thenAnswer(PdlConsumerMock::getOrCreateCurrentPdlEktefelleResponse);
        when(mock.hentBarn(any()))
                .thenAnswer(PdlConsumerMock::getOrCreateCurrentPdlBarnResponse);

        return mock;
    }
}
