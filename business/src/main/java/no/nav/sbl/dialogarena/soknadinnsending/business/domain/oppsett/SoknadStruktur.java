package no.nav.sbl.dialogarena.soknadinnsending.business.domain.oppsett;

import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Vedlegg;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;
import org.apache.commons.collections15.Predicate;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
        return new ToStringBuilder(this)
                .append("fakta", fakta)
                .append("vedlegg", vedlegg)
                .append("vedleggReferanser", vedleggReferanser)
                .append("temaKode", temaKode)
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
                    return Objects.equals(skjemaTillegg, tillegg);
                }
                return false;
            }
        }).collect();

    }

    public List<VedleggsGrunnlag> hentAlleMuligeVedlegg(WebSoknad soknad) {
        Map<String, VedleggsGrunnlag> muligeVedlegg = new HashMap<>();

        for (VedleggForFaktumStruktur vedleggStruktur : getVedlegg()) {
            List<Faktum> faktaSomTriggerVedlegg = soknad.getFaktaMedKey(vedleggStruktur.getFaktum().getId());

            if (vedleggStruktur.getFlereTillatt()) {

                for (Faktum faktum : faktaSomTriggerVedlegg) {
                    Vedlegg brukervedlegg = soknad.finnVedleggSomMatcherForventning(vedleggStruktur, faktum.getFaktumId());

                    VedleggsGrunnlag vedleggsgrunnlag = new VedleggsGrunnlag(soknad, brukervedlegg)
                            .medGrunnlag(vedleggStruktur, faktum);

                    muligeVedlegg.put(createVedleggKey(vedleggStruktur, faktum), vedleggsgrunnlag);
                }
            } else {
                String key = createVedleggKey(vedleggStruktur);
                if (vedleggsgrunnlagFinnesIkke(muligeVedlegg, key)) {

                    Vedlegg brukervedlegg = soknad.finnVedleggSomMatcherForventning(vedleggStruktur, null);
                    VedleggsGrunnlag vedleggsgrunnlag = new VedleggsGrunnlag(soknad, brukervedlegg);

                    muligeVedlegg.put(key, vedleggsgrunnlag);
                }
                muligeVedlegg.get(key).medGrunnlag(vedleggStruktur, faktaSomTriggerVedlegg);
            }
        }
        return new ArrayList<>(muligeVedlegg.values());
    }

    private boolean vedleggsgrunnlagFinnesIkke(Map<String, VedleggsGrunnlag> muligeVedlegg, String key) {
        return !muligeVedlegg.containsKey(key);
    }

    private String createVedleggKey(VedleggForFaktumStruktur vedleggStruktur) {
        return vedleggStruktur.getSkjemaNummer() + vedleggStruktur.getSkjemanummerTillegg();
    }

    private String createVedleggKey(VedleggForFaktumStruktur vedleggStruktur, Faktum faktum) {
        return vedleggStruktur.getSkjemaNummer() + vedleggStruktur.getSkjemanummerTillegg() + faktum.getFaktumId();
    }

    public FaktumStruktur finnStrukturForKey(final String key) {
        List<FaktumStruktur> strukturListe = on(fakta).filter(new Predicate<FaktumStruktur>() {
            @Override
            public boolean evaluate(FaktumStruktur faktumStruktur) {
                return faktumStruktur.getId().equals(key);
            }
        }).collect();
        return strukturListe.isEmpty()? null: strukturListe.get(0);
    }
}
