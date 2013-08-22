package no.nav.sbl.dialogarena.dokumentinnsending.domain;

import org.joda.time.DateTime;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static no.nav.modig.lang.collections.IterUtils.on;
import static no.nav.sbl.dialogarena.dokumentinnsending.domain.Dokument.Type.EKSTERNT_VEDLEGG;
import static no.nav.sbl.dialogarena.dokumentinnsending.domain.Dokument.Type.EKSTRA_VEDLEGG;
import static no.nav.sbl.dialogarena.dokumentinnsending.domain.Dokument.Type.NAV_VEDLEGG;
import static no.nav.sbl.dialogarena.dokumentinnsending.domain.Dokument.avType;


@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class DokumentSoknad implements Serializable {

    public String brukerbehandlingId;
    public String ident;
    public String soknadsId;
    public SoknadStatus status;
    public String soknadTittel;
    public Skjema hovedskjema;
    public List<Dokument> vedlegg;
    public BrukerBehandlingType brukerBehandlingType;
    public DateTime sistEndret;
    public DateTime innsendtDato;

    public DokumentSoknad() {
        this.vedlegg = new ArrayList<>();
    }

    public DokumentSoknad(String ident, String soknadsId) {
        this.ident = ident;
        this.soknadsId = soknadsId;
        this.vedlegg = new ArrayList<>();
    }

    public String getSkjemaNavn() {
        return hovedskjema != null ? hovedskjema.getNavn() : "Ukjent skjema";
    }

    public List<Dokument> finnVedleggAvType(Dokument.Type type) {
        return on(vedlegg).filter(avType(type)).collect();
    }

    public List<Dokument> getDokumenter() {
        List<Dokument> dokumenter = new ArrayList<>();

        if (er(BrukerBehandlingType.DOKUMENT_BEHANDLING)) {
            dokumenter.add(hovedskjema);
        }
        dokumenter.addAll(finnVedleggAvType(NAV_VEDLEGG));
        dokumenter.addAll(finnVedleggAvType(EKSTERNT_VEDLEGG));
        dokumenter.addAll(finnVedleggAvType(EKSTRA_VEDLEGG));
        return dokumenter;
    }

    public void leggTilVedlegg(Dokument nyttVedlegg) {
        vedlegg.add(nyttVedlegg);
    }

    public void leggTilVedlegg(Iterable<? extends Dokument> nyeVedlegg) {
        for (Dokument nyttvedlegg : nyeVedlegg) {
            this.vedlegg.add(nyttvedlegg);
        }
    }

    public boolean er(BrukerBehandlingType type) {
        return brukerBehandlingType.equals(type);
    }
}