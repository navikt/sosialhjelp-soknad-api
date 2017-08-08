package no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp;

import no.nav.melding.domene.brukerdialog.soeknadsskjemasosialhjelp.v1.XMLSoknadsosialhjelp;
import no.nav.melding.domene.brukerdialog.soeknadsskjemasosialhjelp.v1.XMLSoknadsosialhjelp.Personalia;
import no.nav.melding.domene.brukerdialog.soeknadsskjemasosialhjelp.v1.XMLSoknadsosialhjelp.Personalia.Person.*;
import no.nav.melding.domene.brukerdialog.soeknadsskjemasosialhjelp.v1.XMLSoknadsosialhjelp.Personalia.Person;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;

import java.math.BigInteger;
import java.util.Map;
import java.util.function.Function;

import static no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp.Kilde.SYSTEM;


public class BrukerTilXml implements Function<WebSoknad, Personalia> {

    @Override
    public XMLSoknadsosialhjelp.Personalia apply(WebSoknad webSoknad) {
        String personnummer = webSoknad.getAktoerId();

        XMLSoknadsosialhjelp.Personalia.Person.PersonIdentifikator personIdentifikator = new PersonIdentifikator()
                .withKilde(SYSTEM.toString())
                .withValue(new BigInteger(personnummer));

        Map<String, String> personaliaProperties = webSoknad.getFaktumMedKey("personalia").getProperties();

        Navn navn = new Navn()
                .withKilde(SYSTEM.toString())
                .withValue(personaliaProperties.get("navn"));

        Statsborgerskap statsborgerskap = new Statsborgerskap()
                .withKilde(SYSTEM.toString())
                .withValue(personaliaProperties.get("statsborgerskap"));

        EpostAdresse epostAdresse = new EpostAdresse()
                .withKilde(SYSTEM.toString())
                .withValue(personaliaProperties.get("epost").isEmpty() ? "ingen" : personaliaProperties.get("epost"));

        return new Personalia().withPerson(new Person()
                .withPersonIdentifikator(personIdentifikator)
                .withNavn(navn)
                .withStatsborgerskap(statsborgerskap)
                .withEpostAdresse(epostAdresse)
        );
    }
}