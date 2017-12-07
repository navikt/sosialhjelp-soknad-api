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

public class InnsendtSoknad {

    private String behandlingsId;
    private DateTime dato;
    private List<Vedlegg> innsendteVedlegg;
    private List<Vedlegg> ikkeInnsendteVedlegg;
    private String temakode;
    private String tittel;
    private String tittelCmsKey;
    private final DateTimeFormatter datoFormatter;
    private final DateTimeFormatter klokkeslettFormatter;
    private String navenhet;
    private String orgnummer;

    public InnsendtSoknad(){
        this(new Locale("nb_NO"));
    }
    public InnsendtSoknad(Locale sprak) {
        datoFormatter = DateTimeFormat.forPattern("d. MMMM yyyy").withLocale(sprak);
        klokkeslettFormatter = DateTimeFormat.forPattern("HH.mm").withLocale(sprak);
    }

    public InnsendtSoknad medBehandlingId(String behandlingsId) {
        this.behandlingsId = behandlingsId;
        return this;
    }

    public InnsendtSoknad medTittelCmsKey(String tittelCmsKey) {
        this.tittelCmsKey = tittelCmsKey;
        return this;
    }

    public InnsendtSoknad medTittel(String tittel) {
        this.tittel = tittel;
        return this;
    }

    public InnsendtSoknad medTemakode(String temakode) {
        this.temakode = temakode;
        return this;
    }

    public InnsendtSoknad medDato(DateTime avsluttetDato) {
        this.dato = avsluttetDato;
        return this;
    }

    public InnsendtSoknad medInnsendteVedlegg(List<Vedlegg> innsendteVedlegg) {
        this.innsendteVedlegg = innsendteVedlegg;
        return this;
    }

    public InnsendtSoknad medIkkeInnsendteVedlegg(List<Vedlegg> ikkeInnsendteVedlegg) {
        this.ikkeInnsendteVedlegg = ikkeInnsendteVedlegg;
        return this;
    }

    public InnsendtSoknad medNavenhet(String navenhet) {
        this.navenhet = navenhet;
        return this;
    }

    public InnsendtSoknad medOrgnummer(String orgnummer) {
        this.orgnummer = orgnummer;
        return this;
    }

    public String getBehandlingsId() {
        return behandlingsId;
    }

    public String getDato() {
        return datoFormatter.print(this.dato);
    }

    public String getKlokkeslett() {
        return klokkeslettFormatter.print(this.dato);
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

    public String getTittel() {
        return tittel;
    }

    public String getTittelCmsKey() {
        return tittelCmsKey;
    }

    public String getNavenhet() {
        return navenhet;
    }

    public String getOrgnummer() {
        return orgnummer;
    }
}
