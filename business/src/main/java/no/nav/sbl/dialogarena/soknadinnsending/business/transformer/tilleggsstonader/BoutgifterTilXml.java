package no.nav.sbl.dialogarena.soknadinnsending.business.transformer.tilleggsstonader;

import com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl;
import no.nav.melding.virksomhet.soeknadsskjema.v1.soeknadsskjema.Boutgifter;
import no.nav.melding.virksomhet.soeknadsskjema.v1.soeknadsskjema.Periode;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;
import org.apache.commons.collections15.Transformer;
import org.joda.time.DateTime;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

public class BoutgifterTilXml implements Transformer<WebSoknad, Boutgifter> {

    private Boutgifter boutgifter = new Boutgifter();

    @Override
    public Boutgifter transform(WebSoknad webSoknad) {
        aarsakTilBoutgifter(webSoknad);
        periodeTilBoutgifter(webSoknad);
        kommunestotteTilBoutgifter(webSoknad);
        samlingTilBoutgifter(webSoknad); // TODO: Mangler sluttdato
        adresseUtgifterTilBoutgifter(webSoknad);
        //medisinskeaarsakerTilBoutgifter(webSoknad); // TODO: Finnes ikke i boutgifter
        utbetalingsdatoTilBoutgifter(webSoknad);

        return boutgifter;
    }

    private void utbetalingsdatoTilBoutgifter(WebSoknad webSoknad) {
        Faktum utbetalingsdatoFaktum = webSoknad.getFaktumMedKey("bostotte.utbetalingsdato");
        if (utbetalingsdatoFaktum != null && utbetalingsdatoFaktum.hasValue()) {
            boutgifter.setOensketUtbetalingsdag(new BigInteger(utbetalingsdatoFaktum.getValue()));
        }
    }

    private void adresseUtgifterTilBoutgifter(WebSoknad webSoknad) {
        Faktum aktivitetstedFaktum = webSoknad.getFaktumMedKey("bostotte.adresseutgifter.aktivitetsadresse");

        if (aktivitetstedFaktum != null && aktivitetstedFaktum.hasEgenskap("utgift")) {
            boutgifter.setBoutgifterAktivitetsted(new BigInteger(aktivitetstedFaktum.getProperties().get("utgift")));
        }

        Faktum hjemstedsaddresse = webSoknad.getFaktumMedKey("bostotte.adresseutgifter.hjemstedsaddresse");
        if (hjemstedsaddresse != null && hjemstedsaddresse.hasEgenskap("utgift")) {
            boutgifter.setBoutgifterHjemstedAktuell(new BigInteger(hjemstedsaddresse.getProperties().get("utgift")));
        }

        Faktum opphorte = webSoknad.getFaktumMedKey("bostotte.adresseutgifter.opphorte");
        if (opphorte != null && opphorte.hasEgenskap("utgift")) {
            boutgifter.setBoutgifterHjemstedOpphoert(new BigInteger(opphorte.getProperties().get("utgift")));
        }
    }

    private void samlingTilBoutgifter(WebSoknad webSoknad) {
        List<Faktum> samlingFakta = webSoknad.getFaktaMedKey("bostotte.samling");
        if (samlingFakta != null) {
            for (Faktum samlingFaktum : samlingFakta) {
                DateTime startDato = DateTime.parse(samlingFaktum.getProperties().get("fom"));
                boutgifter.getSamlingsdato().add(new XMLGregorianCalendarImpl(startDato.toGregorianCalendar()));
            }
        }
    }

    private void kommunestotteTilBoutgifter(WebSoknad webSoknad) {
        Faktum kommunestotteFaktum = webSoknad.getFaktumMedKey("bostotte.kommunestotte");
        if (kommunestotteFaktum != null && kommunestotteFaktum.hasValue()) {
            boutgifter.setMottarBostoette(Boolean.valueOf(kommunestotteFaktum.getValue()));
            if (kommunestotteFaktum.hasEgenskap("utgift")) {
                boutgifter.setBostoetteBeloep(new BigInteger(kommunestotteFaktum.getProperties().get("utgift")));
            }
        }
    }

    private void aarsakTilBoutgifter(WebSoknad webSoknad) {
        Faktum aarsakFaktum = webSoknad.getFaktumMedKey("bostotte.aarsak");
        if (aarsakFaktum != null && aarsakFaktum.hasValue()) {
            boutgifter.setHarFasteBoutgifter(aarsakFaktum.getValue() == "fasteboutgifter");
            boutgifter.setHarBoutgifterVedSamling(aarsakFaktum.getValue() == "samling");
        }
    }

    private void periodeTilBoutgifter(WebSoknad webSoknad) {
        Faktum periodeFaktum = webSoknad.getFaktumMedKey("bostotte.periode");

        if (periodeFaktum != null) {
            Map<String, String> properties = periodeFaktum.getProperties();
            Periode periode = new Periode();
            periode.setFom(new XMLGregorianCalendarImpl(DateTime.parse(properties.get("fom")).toGregorianCalendar()));
            String tom = properties.get("tom");
            if (tom != null) {
                periode.setTom(new XMLGregorianCalendarImpl(DateTime.parse(tom).toGregorianCalendar()));
            }

            boutgifter.setPeriode(periode);
        }
    }

}
