package no.nav.sosialhjelp.soknad.business.service.systemdata;

import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonAdresse;
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonAdresseValg;
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonGateAdresse;
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonMatrikkelAdresse;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sosialhjelp.soknad.consumer.pdl.PdlService;
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
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AdresseSystemdataTest {

    private static final String EIER = "12345678901";
    private static final Vegadresse DEFAULT_VEGADRESSE = new Vegadresse("gateveien", 1, "A", "", "0123", "poststed", "0301", "H0101", "123456");
    private static final Vegadresse ANNEN_VEGADRESSE = new Vegadresse("en annen sti", 32, null, null, "0456", "oslo", "0302", null, null);

    @Mock
    private PdlService pdlService;

    @InjectMocks
    private AdresseSystemdata adresseSystemdata;

    @Test
    public void skalOppdatereFolkeregistrertAdresse_vegadresse_fraPdl() {
        var soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));

        var personWithBostedsadresseVegadresse = createPersonWithBostedsadresseVegadresse();
        when(pdlService.hentPerson(anyString())).thenReturn(personWithBostedsadresseVegadresse);

        adresseSystemdata.updateSystemdataIn(soknadUnderArbeid, "");

        var folkeregistrertAdresse = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia().getFolkeregistrertAdresse();
        var bostedsadresseVegadresse = personWithBostedsadresseVegadresse.getBostedsadresse().getVegadresse();

        assertThat(folkeregistrertAdresse.getKilde(), is(JsonKilde.SYSTEM));
        assertThat(folkeregistrertAdresse.getType(), is(JsonAdresse.Type.GATEADRESSE));
        assertThatVegadresseIsCorrectlyConverted(bostedsadresseVegadresse, folkeregistrertAdresse);
    }

    @Test
    public void skalOppdatereFolkeregistrertAdresse_matrikkeladresse_fraPdl() {
        var soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
        var personWithBostedsadresseMatrikkeladresse = createPersonWithBostedsadresseMatrikkeladresse();
        when(pdlService.hentPerson(anyString())).thenReturn(personWithBostedsadresseMatrikkeladresse);

        adresseSystemdata.updateSystemdataIn(soknadUnderArbeid, "");

        var folkeregistrertAdresse = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia().getFolkeregistrertAdresse();

        assertThat(folkeregistrertAdresse.getKilde(), is(JsonKilde.SYSTEM));
        assertThat(folkeregistrertAdresse.getType(), is(JsonAdresse.Type.MATRIKKELADRESSE));
        var matrikkeladresse = (JsonMatrikkelAdresse) folkeregistrertAdresse;
        var bostedsadresse = personWithBostedsadresseMatrikkeladresse.getBostedsadresse().getMatrikkeladresse();
        assertThat(matrikkeladresse.getBruksnummer(), is(bostedsadresse.getBruksenhetsnummer()));
        assertThat(matrikkeladresse.getKommunenummer(), is(bostedsadresse.getKommunenummer()));
    }

    @Test
    public void skalOppdatereOppholdsadresseOgPostAdresseMedMidlertidigAdresse_kontaktadresse_fraPdl() {
        var soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
        soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia()
                .withOppholdsadresse(new JsonAdresse().withAdresseValg(JsonAdresseValg.MIDLERTIDIG))
                .withPostadresse(new JsonAdresse().withAdresseValg(JsonAdresseValg.MIDLERTIDIG));

        var personWithOppholdsadresse = createPersonWithOppholdsadresseVegadresse();
        when(pdlService.hentPerson(anyString())).thenReturn(personWithOppholdsadresse);

        adresseSystemdata.updateSystemdataIn(soknadUnderArbeid, "");

        var folkeregistrertAdresse = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia().getFolkeregistrertAdresse();
        var oppholdsadresse = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia().getOppholdsadresse();
        var postadresse = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia().getPostadresse();

        assertThat(folkeregistrertAdresse.getKilde(), is(JsonKilde.SYSTEM));
        assertThat(oppholdsadresse.getKilde(), is(JsonKilde.SYSTEM));
        assertThat(postadresse.getKilde(), is(JsonKilde.SYSTEM));

        assertThat(folkeregistrertAdresse.getType(), is(JsonAdresse.Type.GATEADRESSE));
        assertThat(oppholdsadresse.getType(), is(JsonAdresse.Type.GATEADRESSE));
        assertThat(postadresse.getType(), is(JsonAdresse.Type.GATEADRESSE));

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
        when(pdlService.hentPerson(anyString())).thenReturn(personWithBostedsadresseVegadresse);

        adresseSystemdata.updateSystemdataIn(soknadUnderArbeid, "");

        var folkeregistrertAdresse = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia().getFolkeregistrertAdresse();
        var oppholdsadresse = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia().getOppholdsadresse();
        var postadresse = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia().getPostadresse();

        assertThat(folkeregistrertAdresse.getKilde(), is(JsonKilde.SYSTEM));
        assertThat(oppholdsadresse.getKilde(), is(JsonKilde.SYSTEM));
        assertThat(postadresse.getKilde(), is(JsonKilde.SYSTEM));
        assertThat(folkeregistrertAdresse.equals(oppholdsadresse.withAdresseValg(null)), is(true));
        assertThat(folkeregistrertAdresse.equals(postadresse), is(true));
    }

    @Test
    public void skalIkkeOppdatereOppholdsadresseEllerPostAdresseDersomAdresseValgErNull_fraPdl() {
        var soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
        soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia()
                .withOppholdsadresse(new JsonAdresse())
                .withPostadresse(new JsonAdresse());

        var personWithBostedsadresseVegadresse = createPersonWithBostedsadresseVegadresse();
        when(pdlService.hentPerson(anyString())).thenReturn(personWithBostedsadresseVegadresse);

        adresseSystemdata.updateSystemdataIn(soknadUnderArbeid, "");

        var oppholdsadresse = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia().getOppholdsadresse();
        var postadresse = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia().getPostadresse();

        assertThat(postadresse.getAdresseValg(), nullValue());
        assertThat(postadresse.getType(), nullValue());
        assertThat(oppholdsadresse.getAdresseValg(), nullValue());
        assertThat(oppholdsadresse.getType(), nullValue());
    }

    private void assertThatVegadresseIsCorrectlyConverted(Vegadresse vegadresse, JsonAdresse jsonAdresse) {
        var gateAdresse = (JsonGateAdresse) jsonAdresse;
        assertThat(gateAdresse.getBolignummer(), is(vegadresse.getBruksenhetsnummer()));
        assertThat(gateAdresse.getGatenavn(), is(vegadresse.getAdressenavn()));
        assertThat(gateAdresse.getHusbokstav(), is(vegadresse.getHusbokstav()));
        assertThat(gateAdresse.getHusnummer(), is(vegadresse.getHusnummer().toString()));
        assertThat(gateAdresse.getKommunenummer(), is(vegadresse.getKommunenummer()));
        assertThat(gateAdresse.getLandkode(), is("NOR"));
        assertThat(gateAdresse.getPostnummer(), is(vegadresse.getPostnummer()));
        assertThat(gateAdresse.getPoststed(), is(vegadresse.getPoststed()));

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
