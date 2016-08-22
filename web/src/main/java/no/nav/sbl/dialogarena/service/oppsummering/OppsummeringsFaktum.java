package no.nav.sbl.dialogarena.service.oppsummering;

import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.oppsett.FaktumStruktur;
import no.nav.sbl.dialogarena.sendsoknad.domain.oppsett.PropertyStruktur;
import no.nav.sbl.dialogarena.sendsoknad.domain.oppsett.VedleggForFaktumStruktur;
import org.apache.commons.collections15.Transformer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.Collections.emptyList;
import static no.nav.modig.lang.collections.IterUtils.on;

public class OppsummeringsFaktum implements OppsummeringsBase{
    private final WebSoknad soknad;
    public final Faktum faktum;
    public final FaktumStruktur struktur;
    public final List<OppsummeringsFaktum> barneFakta;
    public final List<OppsummeringsProperty> barneProperties;
    public final List<OppsummeringsVedlegg> vedlegg;

    public OppsummeringsFaktum(WebSoknad soknad, FaktumStruktur faktumStruktur, Faktum faktum, List<OppsummeringsFaktum> barnFakta, List<OppsummeringsVedlegg> vedlegg) {
        this.soknad = soknad;
        this.struktur = faktumStruktur;
        this.faktum = faktum;
        this.barneFakta = barnFakta;
        this.vedlegg = vedlegg;
        if(struktur.getProperties() != null) {
            barneProperties = on(struktur.getProperties()).map(new Transformer<PropertyStruktur, OppsummeringsProperty>() {
                @Override
                public OppsummeringsProperty transform(PropertyStruktur propertyStruktur) {
                    return new OppsummeringsProperty(propertyStruktur);
                }
            }).collect();
        }else {
            barneProperties = emptyList();
        }
    }

    public boolean erSynlig() {
        return struktur.erSynlig(soknad, faktum);
    }

    public String property(String configKey) {
        if(struktur.hasConfig(configKey)){
            return OppsummeringsFaktum.this.faktum.getProperties().get(struktur.getConfiguration().get(configKey));
        }
        return OppsummeringsFaktum.this.faktum.getProperties().get(configKey);
    }

    public String template() {
        return OppsummeringsTyper.resolve(struktur.getType());
    }



    public String key() {
        return struktur.getId().toLowerCase();
    }

    public String value() {
        return faktum.getValue() != null? faktum.getValue().toLowerCase(): "";
    }

    public String originalValue() {
        return faktum.getValue();
    }




    private class OppsummeringsProperty  implements OppsummeringsBase{
        private final PropertyStruktur struktur;
        public final List<OppsummeringsVedlegg> vedlegg = new ArrayList<>();

        public OppsummeringsProperty(PropertyStruktur struktur) {
            this.struktur = struktur;
        }

        @Override
        public String key() {
            return OppsummeringsFaktum.this.key().toLowerCase() + "." + struktur.getId().toLowerCase();
        }

        @Override
        public String value() {
            String value = originalValue();
            return value != null ? value.toLowerCase(): "";
        }

        @Override
        public String originalValue() {
            return OppsummeringsFaktum.this.faktum.getProperties().get(struktur.getId());
        }

        @Override
        public boolean erSynlig() {
            return struktur.erSynlig(faktum);
        }

        @Override
        public String property(String configKey) {
            if(struktur.hasConfig(configKey)){
                return OppsummeringsFaktum.this.faktum.getProperties().get(struktur.getConfiguration().get(configKey));
            }
            return OppsummeringsFaktum.this.faktum.getProperties().get(configKey);
        }

        public List<OppsummeringsFaktum> getBarneProperties(){
            return Collections.emptyList();
        }
        public List<OppsummeringsFaktum> getBarneFakta(){
            return Collections.emptyList();
        }
        public String template() {
            return OppsummeringsTyper.resolve(struktur.getType());
        }
    }
    public static class OppsummeringsVedlegg {
        public Vedlegg vedlegg;
        public VedleggForFaktumStruktur struktur;
        private Faktum faktum;

        public OppsummeringsVedlegg(Faktum faktum, Vedlegg vedlegg, VedleggForFaktumStruktur vedleggStruktur) {
            this.vedlegg = vedlegg;
            this.struktur = vedleggStruktur;
            this.faktum = faktum;
        }
        public boolean erSynlig(){
            return struktur.trengerVedlegg(faktum);
        }
        public String navn(){
            return struktur.getSkjemaNummer();
        }

    }
}
