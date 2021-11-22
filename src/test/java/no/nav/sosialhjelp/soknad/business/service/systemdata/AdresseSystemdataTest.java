package no.nav.sosialhjelp.soknad.business.service.systemdata;

import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonAdresse;
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonAdresseValg;
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonGateAdresse;
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonMatrikkelAdresse;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeid;
import no.nav.sosialhjelp.soknad.person.PersonService;
import no.nav.sosialhjelp.soknad.person.domain.Bostedsadresse;
import no.nav.sosialhjelp.soknad.person.domain.Matrikkeladresse;
import no.nav.sosialhjelp.soknad.person.domain.Oppholdsadresse;
import no.nav.sosialhjelp.soknad.person.domain.Person;
import no.nav.sosialhjelp.soknad.person.domain.Vegadresse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static java.util.Collections.emptyList;
import static no.nav.sosialhjelp.soknad.business.service.soknadservice.SoknadService.createEmptyJsonInternalSoknad;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdresseSystemdataTest {

    private static final String EIER = "12345678901";
    private static final Vegadresse DEFAULT_VEGADRESSE = new Vegadresse("gateveien", 1, "A", "", "0123", "poststed", "0301", "H0101", "123456");
    private static final Vegadresse ANNEN_VEGADRESSE = new Vegadresse("en annen sti", 32, null, null, "0456", "oslo", "0302", null, null);

    @Mock
    private PersonService personService;

    @InjectMocks
    private AdresseSystemdata adresseSystemdata;

    @Test
    void skalOppdatereFolkeregistrertAdresse_vegadresse_fraPdl() {
        var soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));

        var personWithBostedsadresseVegadresse = createPersonWithBostedsadresse(new Bostedsadresse("", DEFAULT_VEGADRESSE, null));
        when(personService.hentPerson(anyString())).thenReturn(personWithBostedsadresseVegadresse);

        adresseSystemdata.updateSystemdataIn(soknadUnderArbeid, "");

        var folkeregistrertAdresse = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia().getFolkeregistrertAdresse();
        var bostedsadresseVegadresse = personWithBostedsadresseVegadresse.getBostedsadresse().getVegadresse();

        assertThat(folkeregistrertAdresse.getKilde()).isEqualTo(JsonKilde.SYSTEM);
        assertThat(folkeregistrertAdresse.getType()).isEqualTo(JsonAdresse.Type.GATEADRESSE);
        assertThatVegadresseIsCorrectlyConverted(bostedsadresseVegadresse, folkeregistrertAdresse);
    }

    @Test
    void skalOppdatereFolkeregistrertAdresse_matrikkeladresse_fraPdl() {
        var soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
        var personWithBostedsadresseMatrikkeladresse = createPersonWithBostedsadresse(
                new Bostedsadresse(
                        "",
                        null,
                        new Matrikkeladresse(
                                "matrikkelId",
                                "postnummer",
                                "poststed",
                                "tilleggsnavn",
                                "kommunenummer",
                                "bruksenhetsnummer"
                        )
                )
        );
        when(personService.hentPerson(anyString())).thenReturn(personWithBostedsadresseMatrikkeladresse);

        adresseSystemdata.updateSystemdataIn(soknadUnderArbeid, "");

        var folkeregistrertAdresse = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia().getFolkeregistrertAdresse();

        assertThat(folkeregistrertAdresse.getKilde()).isEqualTo(JsonKilde.SYSTEM);
        assertThat(folkeregistrertAdresse.getType()).isEqualTo(JsonAdresse.Type.MATRIKKELADRESSE);
        var matrikkeladresse = (JsonMatrikkelAdresse) folkeregistrertAdresse;
        var bostedsadresse = personWithBostedsadresseMatrikkeladresse.getBostedsadresse().getMatrikkeladresse();
        assertThat(matrikkeladresse.getBruksnummer()).isEqualTo(bostedsadresse.getBruksenhetsnummer());
        assertThat(matrikkeladresse.getKommunenummer()).isEqualTo(bostedsadresse.getKommunenummer());
    }

    @Test
    void skalOppdatereOppholdsadresseOgPostAdresseMedMidlertidigAdresse_kontaktadresse_fraPdl() {
        var soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
        soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia()
                .withOppholdsadresse(new JsonAdresse().withAdresseValg(JsonAdresseValg.MIDLERTIDIG))
                .withPostadresse(new JsonAdresse().withAdresseValg(JsonAdresseValg.MIDLERTIDIG));

        var personWithOppholdsadresse = createPersonWithBostedsadresseOgOppholdsadresse(
                new Bostedsadresse("", DEFAULT_VEGADRESSE, null),
                new Oppholdsadresse("", ANNEN_VEGADRESSE)
        );
        when(personService.hentPerson(anyString())).thenReturn(personWithOppholdsadresse);

        adresseSystemdata.updateSystemdataIn(soknadUnderArbeid, "");

        var folkeregistrertAdresse = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia().getFolkeregistrertAdresse();
        var oppholdsadresse = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia().getOppholdsadresse();
        var postadresse = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia().getPostadresse();

        assertThat(folkeregistrertAdresse.getKilde()).isEqualTo(JsonKilde.SYSTEM);
        assertThat(oppholdsadresse.getKilde()).isEqualTo(JsonKilde.SYSTEM);
        assertThat(postadresse.getKilde()).isEqualTo(JsonKilde.SYSTEM);

        assertThat(folkeregistrertAdresse.getType()).isEqualTo(JsonAdresse.Type.GATEADRESSE);
        assertThat(oppholdsadresse.getType()).isEqualTo(JsonAdresse.Type.GATEADRESSE);
        assertThat(postadresse.getType()).isEqualTo(JsonAdresse.Type.GATEADRESSE);

        var bostedsadresseVegadresse = personWithOppholdsadresse.getBostedsadresse().getVegadresse();
        var oppholdsadresseVegadresse = personWithOppholdsadresse.getOppholdsadresse().getVegadresse();

        assertThatVegadresseIsCorrectlyConverted(bostedsadresseVegadresse, folkeregistrertAdresse);
        assertThatVegadresseIsCorrectlyConverted(oppholdsadresseVegadresse, oppholdsadresse);
        assertThatVegadresseIsCorrectlyConverted(oppholdsadresseVegadresse, postadresse);
    }

    @Test
    void skalOppdatereOppholdsadresseOgPostAdresseMedFolkeregistrertAdresse_fraPdl() {
        var soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
        soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia()
                .withOppholdsadresse(new JsonAdresse().withAdresseValg(JsonAdresseValg.FOLKEREGISTRERT))
                .withPostadresse(new JsonAdresse().withAdresseValg(JsonAdresseValg.FOLKEREGISTRERT));

        var personWithBostedsadresseVegadresse = createPersonWithBostedsadresse(new Bostedsadresse("", DEFAULT_VEGADRESSE, null));
        when(personService.hentPerson(anyString())).thenReturn(personWithBostedsadresseVegadresse);

        adresseSystemdata.updateSystemdataIn(soknadUnderArbeid, "");

        var folkeregistrertAdresse = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia().getFolkeregistrertAdresse();
        var oppholdsadresse = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia().getOppholdsadresse();
        var postadresse = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia().getPostadresse();

        assertThat(folkeregistrertAdresse.getKilde()).isEqualTo(JsonKilde.SYSTEM);
        assertThat(oppholdsadresse.getKilde()).isEqualTo(JsonKilde.SYSTEM);
        assertThat(postadresse.getKilde()).isEqualTo(JsonKilde.SYSTEM);
        assertThat(folkeregistrertAdresse)
                .isEqualTo(oppholdsadresse.withAdresseValg(null))
                .isEqualTo(postadresse);
    }

    @Test
    void skalIkkeOppdatereOppholdsadresseEllerPostAdresseDersomAdresseValgErNull_fraPdl() {
        var soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
        soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia()
                .withOppholdsadresse(new JsonAdresse())
                .withPostadresse(new JsonAdresse());

        var personWithBostedsadresseVegadresse = createPersonWithBostedsadresse(new Bostedsadresse("", DEFAULT_VEGADRESSE, null));
        when(personService.hentPerson(anyString())).thenReturn(personWithBostedsadresseVegadresse);

        adresseSystemdata.updateSystemdataIn(soknadUnderArbeid, "");

        var oppholdsadresse = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia().getOppholdsadresse();
        var postadresse = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia().getPostadresse();

        assertThat(postadresse.getAdresseValg()).isNull();
        assertThat(postadresse.getType()).isNull();
        assertThat(oppholdsadresse.getAdresseValg()).isNull();
        assertThat(oppholdsadresse.getType()).isNull();
    }

    private void assertThatVegadresseIsCorrectlyConverted(Vegadresse vegadresse, JsonAdresse jsonAdresse) {
        var gateAdresse = (JsonGateAdresse) jsonAdresse;
        assertThat(gateAdresse.getBolignummer()).isEqualTo(vegadresse.getBruksenhetsnummer());
        assertThat(gateAdresse.getGatenavn()).isEqualTo(vegadresse.getAdressenavn());
        assertThat(gateAdresse.getHusbokstav()).isEqualTo(vegadresse.getHusbokstav());
        assertThat(gateAdresse.getHusnummer()).isEqualTo(vegadresse.getHusnummer().toString());
        assertThat(gateAdresse.getKommunenummer()).isEqualTo(vegadresse.getKommunenummer());
        assertThat(gateAdresse.getLandkode()).isEqualTo("NOR");
        assertThat(gateAdresse.getPostnummer()).isEqualTo(vegadresse.getPostnummer());
        assertThat(gateAdresse.getPoststed()).isEqualTo(vegadresse.getPoststed());
    }

    private Person createPersonWithBostedsadresse(Bostedsadresse bostedsadresse) {
        return new Person("fornavn", "mellomnavn", "etternavn", EIER, "ugift", emptyList(), null, bostedsadresse, null, null);
    }

    private Person createPersonWithBostedsadresseOgOppholdsadresse(Bostedsadresse bostedsadresse, Oppholdsadresse oppholdsadresse) {
        return new Person("fornavn", "mellomnavn", "etternavn", EIER, "ugift", emptyList(), null, bostedsadresse, oppholdsadresse, null);
    }
}
