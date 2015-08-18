package no.nav.sbl.dialogarena.soknadinnsending.business.domain.oppsett;

import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;
import org.apache.commons.collections15.Predicate;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static no.nav.modig.lang.collections.IterUtils.on;

@XmlRootElement(name = "soknad")
public class SoknadStruktur implements Serializable {

    private List<FaktumStruktur> fakta = new ArrayList<>();
    private List<VedleggForFaktumStruktur> vedlegg = new ArrayList<>();
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
    public List<FaktumStruktur> getFakta() {
        return fakta;
    }

    public void setFakta(List<FaktumStruktur> fakta) {
        this.fakta = fakta;
    }

    @XmlElement(name = "vedlegg")
    public List<VedleggForFaktumStruktur> getVedlegg() {
        return vedlegg;
    }

    public void setVedlegg(List<VedleggForFaktumStruktur> vedlegg) {
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

    public List<VedleggForFaktumStruktur> vedleggFor(final Faktum faktum) {
        return on(vedlegg).filter(new Predicate<VedleggForFaktumStruktur>() {
            @Override
            public boolean evaluate(VedleggForFaktumStruktur vedleggForFaktumStruktur) {
                return vedleggForFaktumStruktur.getFaktum().getId().equals(faktum.getKey());
            }
        }).filter(new Predicate<VedleggForFaktumStruktur>() {
            @Override
            public boolean evaluate(VedleggForFaktumStruktur vedleggForFaktumStruktur) {
                return vedleggForFaktumStruktur.harFilterProperty(faktum);
            }
        }).collect();

    }

    public List<VedleggForFaktumStruktur> vedleggForSkjemanrMedTillegg(final String skjemaNr, final String tillegg) {
        return on(vedlegg).filter(new Predicate<VedleggForFaktumStruktur>() {
            @Override
            public boolean evaluate(VedleggForFaktumStruktur vedleggForFaktumStruktur) {
                if(vedleggForFaktumStruktur.getSkjemaNummer().equals(skjemaNr)) {
                    String skjemaTillegg = vedleggForFaktumStruktur.getSkjemanummerTillegg();
                    return skjemaTillegg != null && skjemaTillegg.equals(tillegg);
                }
                return false;
            }
        }).collect();

    }

    public List<VedleggsGrunnlag> hentAlleMuligeVedlegg(WebSoknad soknad) {
        Map<String, VedleggsGrunnlag> muligeVedlegg = new HashMap<>();
        for (VedleggForFaktumStruktur vedleggForFaktumStruktur : getVedlegg()) {
            List<Faktum> faktaSomTriggerVedlegg = soknad.getFaktaMedKey(vedleggForFaktumStruktur.getFaktum().getId());

            if (vedleggForFaktumStruktur.getFlereTillatt()) {
                for (Faktum faktum : faktaSomTriggerVedlegg) {
                    String key = vedleggForFaktumStruktur.getSkjemaNummer() + vedleggForFaktumStruktur.getSkjemanummerTillegg() + faktum.getFaktumId();
                    muligeVedlegg.put(key, new VedleggsGrunnlag(soknad, soknad.finnVedleggSomMatcherForventning(vedleggForFaktumStruktur, faktum.getFaktumId())).medGrunnlag(vedleggForFaktumStruktur, faktum));
                }
            } else {
                String key = vedleggForFaktumStruktur.getSkjemaNummer() + vedleggForFaktumStruktur.getSkjemanummerTillegg();
                if (!muligeVedlegg.containsKey(key)) {
                    muligeVedlegg.put(key, new VedleggsGrunnlag(soknad, soknad.finnVedleggSomMatcherForventning(vedleggForFaktumStruktur, null)));
                }
                muligeVedlegg.get(key).medGrunnlag(vedleggForFaktumStruktur, faktaSomTriggerVedlegg);
            }
        }
        return new ArrayList<>(muligeVedlegg.values());
    }
}
