package no.nav.sbl.dialogarena.soknadinnsending.business.domain;

import no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;
import java.util.Locale;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)

public class FerdigSoknad {

    private DateTimeFormatter dt = DateTimeFormat.forPattern("d. MMMM yyyy' klokken' HH.mm").withLocale(new Locale("nb", "no"));

    private String behandlingsId;
    private DateTime dato;
    private List<Vedlegg> innsendteVedlegg;
    private List<Vedlegg> ikkeInnsendteVedlegg;
    private String temakode;
    private String soknadPrefix;

    public FerdigSoknad medBehandlingId(String behandlingsId) {
        this.behandlingsId = behandlingsId;
        return this;
    }

    public FerdigSoknad medSoknadPrefix(String prefix) {
        this.soknadPrefix = prefix;
        return this;
    }

    public FerdigSoknad medTemakode(String temakode) {
        this.temakode = temakode;
        return this;
    }

    public FerdigSoknad medDato(DateTime avsluttetDato) {
        this.dato = avsluttetDato;
        return this;
    }

    public FerdigSoknad medInnsendteVedlegg(List<Vedlegg> innsendteVedlegg) {
        this.innsendteVedlegg = innsendteVedlegg;
        return this;
    }

    public FerdigSoknad medIkkeInnsendteVedlegg(List<Vedlegg> ikkeInnsendteVedlegg) {
        this.ikkeInnsendteVedlegg = ikkeInnsendteVedlegg;
        return this;
    }

    public String getBehandlingsId() {
        return behandlingsId;
    }

    public String getDato() {
        return dt.print(this.dato);
    }

    public List<Vedlegg> getIkkeInnsendteVedlegg() {
        return ikkeInnsendteVedlegg;
    }

    public List<Vedlegg> getInnsendteVedlegg() {
        return innsendteVedlegg;
    }

    public String getTemakode() {
        return temakode;
    }

    public String getSoknadPrefix() {
        return soknadPrefix;
    }
}
