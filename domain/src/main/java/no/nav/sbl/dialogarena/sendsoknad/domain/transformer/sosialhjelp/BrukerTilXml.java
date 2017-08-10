package no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp;

import no.nav.melding.domene.brukerdialog.soeknadsskjemasosialhjelp.v1.XMLSoknadsosialhjelp;
import no.nav.melding.domene.brukerdialog.soeknadsskjemasosialhjelp.v1.XMLPersonalia;
import no.nav.melding.domene.brukerdialog.soeknadsskjemasosialhjelp.v1.XMLPersonalia.Person.*;
import no.nav.melding.domene.brukerdialog.soeknadsskjemasosialhjelp.v1.XMLPersonalia.Person;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;

import java.util.Map;
import java.util.function.Function;

import static no.nav.melding.domene.brukerdialog.soeknadsskjemasosialhjelp.v1.XMLKilde.SYSTEM;


public class BrukerTilXml implements Function<WebSoknad, XMLPersonalia> {

    @Override
    public XMLPersonalia apply(WebSoknad webSoknad) {
        String personnummer = webSoknad.getAktoerId();

        XMLPersonalia.Person.PersonIdentifikator personIdentifikator = new PersonIdentifikator()
                .withKilde(SYSTEM)
                .withValue(personnummer);

        Map<String, String> personaliaProperties = webSoknad.getFaktumMedKey("personalia").getProperties();

        Navn navn = new Navn()
                .withKilde(SYSTEM)
                .withValue(personaliaProperties.get("navn"));

        Statsborgerskap statsborgerskap = new Statsborgerskap()
                .withKilde(SYSTEM)
                .withValue(personaliaProperties.get("statsborgerskap"));

        EpostAdresse epostAdresse = new EpostAdresse()
                .withKilde(SYSTEM)
                .withValue(personaliaProperties.get("epost") == null ? "ingen epost" : personaliaProperties.get("epost"));

        return new XMLPersonalia().withPerson(new Person()
                .withPersonIdentifikator(personIdentifikator)
                .withNavn(navn)
                .withStatsborgerskap(statsborgerskap)
                .withEpostAdresse(epostAdresse)
        );
    }
}