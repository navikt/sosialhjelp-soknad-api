package no.nav.sosialhjelp.soknad.business.service.systemdata;

import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonAdresse;
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonAdresseValg;
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonGateAdresse;
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonMatrikkelAdresse;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sosialhjelp.soknad.consumer.pdl.person.PersonService;
import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeid;
import no.nav.sosialhjelp.soknad.domain.model.Bostedsadresse;
import no.nav.sosialhjelp.soknad.domain.model.Kontaktadresse;
import no.nav.sosialhjelp.soknad.domain.model.Matrikkeladresse;
import no.nav.sosialhjelp.soknad.domain.model.Oppholdsadresse;
import no.nav.sosialhjelp.soknad.domain.model.Person;
import no.nav.sosialhjelp.soknad.domain.model.Vegadresse;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static no.nav.sosialhjelp.soknad.business.service.soknadservice.SoknadService.createEmptyJsonInternalSoknad;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AdresseSystemdataTest {

    private static final String EIER = "12345678901";
    private static final Vegadresse DEFAULT_VEGADRESSE = new Vegadresse("gateveien", 1, "A", "", "0123", "poststed", "0301", "H0101", "123456");
    private static final Vegadresse ANNEN_VEGADRESSE = new Vegadresse("en annen sti", 32, null, null, "0456", "oslo", "0302", null, null);

    @Mock
    private PersonService personService;

    @InjectMocks
    private AdresseSystemdata adresseSystemdata;

    @Test
    public void skalOppdatereFolkeregistrertAdresse_vegadresse_fraPdl() {
        var soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));

        var personWithBostedsadresseVegadresse = createPersonWithBostedsadresseVegadresse();
        when(personService.hentPerson(anyString())).thenReturn(personWithBostedsadresseVegadresse);

        adresseSystemdata.updateSystemdataIn(soknadUnderArbeid, "");

        var folkeregistrertAdresse = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia().getFolkeregistrertAdresse();
        var bostedsadresseVegadresse = personWithBostedsadresseVegadresse.getBostedsadresse().getVegadresse();

        assertThat(folkeregistrertAdresse.getKilde()).isEqualTo(JsonKilde.SYSTEM);
        assertThat(folkeregistrertAdresse.getType()).isEqualTo(JsonAdresse.Type.GATEADRESSE);
        assertThatVegadresseIsCorrectlyConverted(bostedsadresseVegadresse, folkeregistrertAdresse);
    }

    @Test
    public void skalOppdatereFolkeregistrertAdresse_matrikkeladresse_fraPdl() {
        var soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
        var personWithBostedsadresseMatrikkeladresse = createPersonWithBostedsadresseMatrikkeladresse();
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
    public void skalOppdatereOppholdsadresseOgPostAdresseMedMidlertidigAdresse_kontaktadresse_fraPdl() {
        var soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
        soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia()
                .withOppholdsadresse(new JsonAdresse().withAdresseValg(JsonAdresseValg.MIDLERTIDIG))
                .withPostadresse(new JsonAdresse().withAdresseValg(JsonAdresseValg.MIDLERTIDIG));

        var personWithOppholdsadresse = createPersonWithOppholdsadresseVegadresse();
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
    public void skalOppdatereOppholdsadresseOgPostAdresseMedFolkeregistrertAdresse_fraPdl() {
        var soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
        soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia()
                .withOppholdsadresse(new JsonAdresse().withAdresseValg(JsonAdresseValg.FOLKEREGISTRERT))
                .withPostadresse(new JsonAdresse().withAdresseValg(JsonAdresseValg.FOLKEREGISTRERT));

        var personWithBostedsadresseVegadresse = createPersonWithBostedsadresseVegadresse();
        when(personService.hentPerson(anyString())).thenReturn(personWithBostedsadresseVegadresse);

        adresseSystemdata.updateSystemdataIn(soknadUnderArbeid, "");

        var folkeregistrertAdresse = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia().getFolkeregistrertAdresse();
        var oppholdsadresse = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia().getOppholdsadresse();
        var postadresse = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia().getPostadresse();

        assertThat(folkeregistrertAdresse.getKilde()).isEqualTo(JsonKilde.SYSTEM);
        assertThat(oppholdsadresse.getKilde()).isEqualTo(JsonKilde.SYSTEM);
        assertThat(postadresse.getKilde()).isEqualTo(JsonKilde.SYSTEM);
        assertThat(folkeregistrertAdresse).isEqualTo(oppholdsadresse.withAdresseValg(null));
        assertThat(folkeregistrertAdresse).isEqualTo(postadresse);
    }

    @Test
    public void skalIkkeOppdatereOppholdsadresseEllerPostAdresseDersomAdresseValgErNull_fraPdl() {
        var soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
        soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia()
                .withOppholdsadresse(new JsonAdresse())
                .withPostadresse(new JsonAdresse());

        var personWithBostedsadresseVegadresse = createPersonWithBostedsadresseVegadresse();
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

    private Person createPersonWithBostedsadresseVegadresse() {
        return new Person()
                .withBostedsadresse(new Bostedsadresse("", DEFAULT_VEGADRESSE, null));
    }

    private Person createPersonWithBostedsadresseMatrikkeladresse() {
        return new Person()
                .withBostedsadresse(
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
    }

    private Person createPersonWithKontaktadresseVegadresse() {
        return new Person()
                .withBostedsadresse(new Bostedsadresse("", DEFAULT_VEGADRESSE, null))
                .withKontaktadresse(new Kontaktadresse("", ANNEN_VEGADRESSE));
    }

    private Person createPersonWithOppholdsadresseVegadresse() {
        return new Person()
                .withBostedsadresse(new Bostedsadresse("", DEFAULT_VEGADRESSE, null))
                .withOppholdsadresse(new Oppholdsadresse("", ANNEN_VEGADRESSE));
    }
}
