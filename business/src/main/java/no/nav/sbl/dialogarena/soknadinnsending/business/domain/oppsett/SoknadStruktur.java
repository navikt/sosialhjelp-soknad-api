package no.nav.sbl.dialogarena.soknadinnsending.business.domain.oppsett;

import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import org.apache.commons.collections15.Predicate;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static no.nav.modig.lang.collections.IterUtils.on;

@XmlRootElement(name = "soknad")
public class SoknadStruktur implements Serializable {

    private List<SoknadFaktum> fakta = new ArrayList<>();
    private List<SoknadVedlegg> vedlegg = new ArrayList<>();
    private List<String> vedleggReferanser = new ArrayList<>();
    private String temaKode;

    @XmlAttribute
    public String getTemaKode() {
        return temaKode;
    }

    public void setTemaKode(String temaKode) {
        this.temaKode = temaKode;
    }

    @XmlElement(name = "faktum")
    public List<SoknadFaktum> getFakta() {
        return fakta;
    }

    public void setFakta(List<SoknadFaktum> fakta) {
        this.fakta = fakta;
    }

    @XmlElement(name = "vedlegg")
    public List<SoknadVedlegg> getVedlegg() {
        return vedlegg;
    }

    public void setVedlegg(List<SoknadVedlegg> vedlegg) {
        this.vedlegg = vedlegg;
    }

    @XmlElement(name = "vedleggsreferanse")
    public List<String> getVedleggReferanser() { return vedleggReferanser; }

    public void setVedleggReferanser(List<String> vedleggReferanser) {
        this.vedleggReferanser = vedleggReferanser;
    }

    @Override
    public String toString() {
        return new StringBuilder("SoknadStruktur{")
                .append(", fakta=").append(fakta)
                .append(", vedlegg=").append(vedlegg)
                .append(", vedleggreferanser=").append(vedleggReferanser)
                .append('}')
                .toString();
    }

    public List<SoknadVedlegg> vedleggFor(final Faktum faktum) {
        return on(vedlegg).filter(new Predicate<SoknadVedlegg>() {
            @Override
            public boolean evaluate(SoknadVedlegg soknadVedlegg) {
                return soknadVedlegg.getFaktum().getId().equals(faktum.getKey());
            }
        }).filter(new Predicate<SoknadVedlegg>() {
            @Override
            public boolean evaluate(SoknadVedlegg soknadVedlegg) {
                return soknadVedlegg.harFilterProperty(faktum);
            }
        }).collect();

    }

    public List<SoknadVedlegg> vedleggForSkjemanrMedTillegg(final String skjemaNr, final String tillegg) {
        return on(vedlegg).filter(new Predicate<SoknadVedlegg>() {
            @Override
            public boolean evaluate(SoknadVedlegg soknadVedlegg) {
                if(soknadVedlegg.getSkjemaNummer().equals(skjemaNr)) {
                    String skjemaTillegg = soknadVedlegg.getSkjemanummerTillegg();
                    return Objects.equals(skjemaTillegg, tillegg);
                }
                return false;
            }
        }).collect();

    }

    public SoknadFaktum finnFaktum(final String key) {
        for (SoknadFaktum soknadFaktum : fakta) {
            if (soknadFaktum.getId().equals(key)) {
                return soknadFaktum;
            }
        }
        return null;
    }
}
