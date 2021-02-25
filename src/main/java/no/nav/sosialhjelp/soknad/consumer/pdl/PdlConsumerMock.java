package no.nav.sosialhjelp.soknad.consumer.pdl;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import no.nav.sosialhjelp.soknad.consumer.pdl.person.PdlBarn;
import no.nav.sosialhjelp.soknad.consumer.pdl.person.PdlEktefelle;
import no.nav.sosialhjelp.soknad.consumer.pdl.person.PdlPerson;
import no.nav.sosialhjelp.soknad.consumer.pdl.person.dto.AdressebeskyttelseDto;
import no.nav.sosialhjelp.soknad.consumer.pdl.person.dto.BostedsadresseDto;
import no.nav.sosialhjelp.soknad.consumer.pdl.person.dto.EndringDto;
import no.nav.sosialhjelp.soknad.consumer.pdl.person.dto.FoedselDto;
import no.nav.sosialhjelp.soknad.consumer.pdl.person.dto.FolkeregistermetadataDto;
import no.nav.sosialhjelp.soknad.consumer.pdl.person.dto.FolkeregisterpersonstatusDto;
import no.nav.sosialhjelp.soknad.consumer.pdl.person.dto.ForelderBarnRelasjonDto;
import no.nav.sosialhjelp.soknad.consumer.pdl.person.dto.KontaktadresseDto;
import no.nav.sosialhjelp.soknad.consumer.pdl.person.dto.MetadataDto;
import no.nav.sosialhjelp.soknad.consumer.pdl.person.dto.NavnDto;
import no.nav.sosialhjelp.soknad.consumer.pdl.person.dto.SivilstandDto;
import no.nav.sosialhjelp.soknad.consumer.pdl.person.dto.StatsborgerskapDto;
import no.nav.sosialhjelp.soknad.consumer.pdl.person.dto.VegadresseDto;
import no.nav.sosialhjelp.soknad.domain.model.oidc.SubjectHandler;
import org.mockito.invocation.InvocationOnMock;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    public static void setPerson(PdlMockResponse response) {
        var pdlPerson = createPdlPerson(response);
        pdlPersonResponses.put(response.getPerson().getIdent(), pdlPerson);
    }

    public static void setEktefelle(PdlMockEktefelle ektefelle) {
        var pdlEktefelle = createPdlEktefelle(ektefelle);
        pdlEktefelleResponses.put(ektefelle.getIdent(), pdlEktefelle);
    }

    public static void setBarn(PdlMockBarn barn) {
        var pdlBarn = createPdlBarn(barn);
        pdlBarnResponses.put(barn.getIdent(), pdlBarn);
    }

    private static PdlPerson createPdlPerson(PdlMockResponse response) {
        return new PdlPerson(
                singletonList(new AdressebeskyttelseDto(AdressebeskyttelseDto.Gradering.UGRADERT)),
                singletonList(defaultBostedsadresse()),
                emptyList(), // ingen oppholdsadresse
                singletonList(new KontaktadresseDto("Innland", null, new VegadresseDto(null, "midlertidig adresse gate", 1, "D", null, "0471", "Oslo", null), null, null)),
                response.getBarn().isEmpty() ? null : forelderBarnRelasjoner(response.getBarn()), // ingen forelderBarnRelasjoner for mockperson
                singletonList(new NavnDto(
                        response.getPerson().getFornavn(),
                        response.getPerson().getMellomnavn(),
                        response.getPerson().getEtternavn(),
                        defaultMetadata(),
                        defaultFolkeregisterMetadata())),
                singletonList(new SivilstandDto(
                        SivilstandDto.SivilstandType.valueOf(response.getPerson().getSivilstand()),
                        response.getEktefelle() != null ? response.getEktefelle().getIdent() : null,
                        defaultMetadata(),
                        defaultFolkeregisterMetadata())),
                singletonList(new StatsborgerskapDto(response.getPerson().getStatsborgerskap()))
        );
    }

    private static PdlEktefelle createPdlEktefelle(PdlMockEktefelle pdlMockEktefelle) {
        return new PdlEktefelle(
                singletonList(new AdressebeskyttelseDto(AdressebeskyttelseDto.Gradering.valueOf(pdlMockEktefelle.getAdressebeskyttelse()))),
                singletonList(pdlMockEktefelle.harSammeBostedsadresse ? defaultBostedsadresse() : annenBostedsadresse()),
                singletonList(new FoedselDto(LocalDate.of(1970, 1, 1))),
                singletonList(new NavnDto(
                        pdlMockEktefelle.getFornavn(),
                        pdlMockEktefelle.getMellomnavn(),
                        pdlMockEktefelle.getEtternavn(),
                        defaultMetadata(),
                        defaultFolkeregisterMetadata()))
        );
    }

    private static PdlBarn createPdlBarn(PdlMockBarn pdlMockBarn) {
        return new PdlBarn(
                singletonList(new AdressebeskyttelseDto(AdressebeskyttelseDto.Gradering.UGRADERT)),
                singletonList(pdlMockBarn.harSammeBostedsadresse ? defaultBostedsadresse() : annenBostedsadresse()),
                singletonList(new FolkeregisterpersonstatusDto(pdlMockBarn.isErDoed() ? "doed" : "bosatt")),
                singletonList(findFoedselsdatoFromIdent(pdlMockBarn.getIdent())),
                singletonList(new NavnDto(
                        pdlMockBarn.getFornavn(),
                        pdlMockBarn.getMellomnavn(),
                        pdlMockBarn.getEtternavn(),
                        defaultMetadata(),
                        defaultFolkeregisterMetadata()))
        );
    }

    private static FoedselDto findFoedselsdatoFromIdent(String ident) {
        try {
            return new FoedselDto(LocalDate.parse(ident.substring(0, 6), DateTimeFormatter.ofPattern("ddMMyy")));
        } catch (Exception e) {
            return new FoedselDto(LocalDate.of(LocalDate.now().getYear() - 10, 1, 1));
        }
    }

    private static BostedsadresseDto defaultBostedsadresse() {
        return new BostedsadresseDto(null, new VegadresseDto("123123", "GATEVEIEN", 1, "A", null, "0690", "0301", null), null, null);
    }

    private static BostedsadresseDto annenBostedsadresse() {
        return new BostedsadresseDto(null, new VegadresseDto("999999", "Karl Johans gate", 1, "A", null, "1111", "0301", null), null, null);
    }

    private static List<ForelderBarnRelasjonDto> forelderBarnRelasjoner(List<PdlMockBarn> barn) {
        if (barn == null) {
            return emptyList();
        }
        return barn.stream()
                .map(pdlMockBarn -> new ForelderBarnRelasjonDto(pdlMockBarn.getIdent(), "BARN", "MOR"))
                .collect(Collectors.toList());
    }

    private static PdlPerson defaultPerson() {
        return new PdlPerson(
                singletonList(new AdressebeskyttelseDto(AdressebeskyttelseDto.Gradering.UGRADERT)),
                singletonList(new BostedsadresseDto(null, new VegadresseDto("123123", "GATEVEIEN", 1, "A", null, "0690", "0301", null), null, null)),
                emptyList(), // ingen oppholdsadresse
                singletonList(new KontaktadresseDto("Innland", null, new VegadresseDto(null, "midlertidig adresse gate", 1, "D", null, "0471", "Oslo", null), null, null)),
                emptyList(), // ingen forelderBarnRelasjoner for mockperson
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

    public static class PdlMockResponse {
        private final PdlMockPerson person;
        private final PdlMockEktefelle ektefelle;
        private final List<PdlMockBarn> barn;

        @JsonCreator
        public PdlMockResponse(
                @JsonProperty("person") PdlMockPerson person,
                @JsonProperty("ektefelle") PdlMockEktefelle ektefelle,
                @JsonProperty("barn") List<PdlMockBarn> barn
        ) {
            this.person = person;
            this.ektefelle = ektefelle;
            this.barn = barn;
        }

        public PdlMockPerson getPerson() {
            return person;
        }

        public PdlMockEktefelle getEktefelle() {
            return ektefelle;
        }

        public List<PdlMockBarn> getBarn() {
            return barn;
        }
    }

    public static class PdlMockPerson {
        private final String ident;
        private final String fornavn;
        private final String mellomnavn;
        private final String etternavn;
        private final String statsborgerskap;
        private final String sivilstand;

        @JsonCreator
        public PdlMockPerson(
                @JsonProperty("ident") String ident,
                @JsonProperty("fornavn") String fornavn,
                @JsonProperty("mellomnavn") String mellomnavn,
                @JsonProperty("etternavn") String etternavn,
                @JsonProperty("statsborgerskap") String statsborgerskap,
                @JsonProperty("sivilstand") String sivilstand
        ) {
            this.ident = ident;
            this.fornavn = fornavn;
            this.mellomnavn = mellomnavn;
            this.etternavn = etternavn;
            this.statsborgerskap = statsborgerskap;
            this.sivilstand = sivilstand;
        }

        public String getIdent() {
            return ident;
        }

        public String getFornavn() {
            return fornavn;
        }

        public String getMellomnavn() {
            return mellomnavn;
        }

        public String getEtternavn() {
            return etternavn;
        }

        public String getStatsborgerskap() {
            return statsborgerskap;
        }

        public String getSivilstand() {
            return sivilstand;
        }
    }

    public static class PdlMockEktefelle {
        private final String ident;
        private final String fornavn;
        private final String mellomnavn;
        private final String etternavn;
        private final boolean harSammeBostedsadresse;
        private final String adressebeskyttelse;

        @JsonCreator
        public PdlMockEktefelle(
                @JsonProperty("ident") String ident,
                @JsonProperty("fornavn") String fornavn,
                @JsonProperty("mellomnavn") String mellomnavn,
                @JsonProperty("etternavn") String etternavn,
                @JsonProperty("harSammeBostedsadresse") boolean harSammeBostedsadresse,
                @JsonProperty("adressebeskyttelse") String adressebeskyttelse
        ) {
            this.ident = ident;
            this.fornavn = fornavn;
            this.mellomnavn = mellomnavn;
            this.etternavn = etternavn;
            this.harSammeBostedsadresse = harSammeBostedsadresse;
            this.adressebeskyttelse = adressebeskyttelse;
        }

        public String getIdent() {
            return ident;
        }

        public String getFornavn() {
            return fornavn;
        }

        public String getMellomnavn() {
            return mellomnavn;
        }

        public String getEtternavn() {
            return etternavn;
        }

        public boolean isHarSammeBostedsadresse() {
            return harSammeBostedsadresse;
        }

        public String getAdressebeskyttelse() {
            return adressebeskyttelse;
        }
    }

    public static class PdlMockBarn {
        private final String ident;
        private final String fornavn;
        private final String mellomnavn;
        private final String etternavn;
        private final boolean harSammeBostedsadresse;
        private final boolean erDoed;

        @JsonCreator
        public PdlMockBarn(
                @JsonProperty("ident") String ident,
                @JsonProperty("fornavn") String fornavn,
                @JsonProperty("mellomnavn") String mellomnavn,
                @JsonProperty("etternavn") String etternavn,
                @JsonProperty("harSammeBostedsadresse") boolean harSammeBostedsadresse,
                @JsonProperty("erDoed") boolean erDoed
        ) {
            this.ident = ident;
            this.fornavn = fornavn;
            this.mellomnavn = mellomnavn;
            this.etternavn = etternavn;
            this.harSammeBostedsadresse = harSammeBostedsadresse;
            this.erDoed = erDoed;
        }

        public String getIdent() {
            return ident;
        }

        public String getFornavn() {
            return fornavn;
        }

        public String getMellomnavn() {
            return mellomnavn;
        }

        public String getEtternavn() {
            return etternavn;
        }

        public boolean isHarSammeBostedsadresse() {
            return harSammeBostedsadresse;
        }

        public boolean isErDoed() {
            return erDoed;
        }
    }
}
