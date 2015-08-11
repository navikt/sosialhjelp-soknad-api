package no.nav.sbl.dialogarena.soknadinnsending.business.transformer.tilleggsstonader;

import com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl;
import no.nav.melding.virksomhet.soeknadsskjema.v1.soeknadsskjema.Maalgruppeinformasjon;
import no.nav.melding.virksomhet.soeknadsskjema.v1.soeknadsskjema.Maalgruppetyper;
import no.nav.melding.virksomhet.soeknadsskjema.v1.soeknadsskjema.Periode;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import org.apache.commons.collections15.Transformer;
import org.joda.time.DateTime;

import java.util.Map;

public class MaalgruppeTilXml implements Transformer<Faktum, Maalgruppeinformasjon> {
    @Override
    public Maalgruppeinformasjon transform(Faktum faktum) {
        Map<String, String> properties = faktum.getProperties();

        Maalgruppeinformasjon informasjon = new Maalgruppeinformasjon();
        informasjon.setPeriode(lagPeiode(properties));
        informasjon.setMaalgruppetype(lagType(properties));
        informasjon.setKilde(faktum.getType().toString()); //TODO: Usikker på om jeg har tolket kilde riktig.

        return informasjon;
    }

    private Maalgruppetyper lagType(Map<String, String> properties) {
        Maalgruppetyper type = new Maalgruppetyper();
        type.setKodeverksRef(properties.get("kodeverkVerdi"));
        return type;
    }

    private Periode lagPeiode(Map<String, String> properties) {
        Periode periode = new Periode();
        periode.setFom(new XMLGregorianCalendarImpl(DateTime.parse(properties.get("fom")).toGregorianCalendar()));
        String tom = properties.get("tom");
        if (tom != null) {
            periode.setTom(new XMLGregorianCalendarImpl(DateTime.parse(tom).toGregorianCalendar()));
        }
        return new Periode();
    }
}
