
package no.nav.sbl.dialogarena.service;

import no.nav.sbl.dialogarena.common.kodeverk.Kodeverk;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Vedlegg;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.oppsett.FaktumStruktur;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.oppsett.PropertyStruktur;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.oppsett.SoknadStruktur;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.oppsett.VedleggForFaktumStruktur;
import org.apache.commons.collections15.Transformer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.Collections.emptyList;
import static no.nav.modig.lang.collections.IterUtils.on;


public class OppsummeringsContext {
    public final boolean visInfotekst;
    public List<OppsummeringsBolk> bolker = new ArrayList<>();
    public WebSoknad soknad;
    public Kodeverk kodeverk;

    public OppsummeringsContext(WebSoknad soknad, SoknadStruktur soknadStruktur, Kodeverk kodeverk, boolean visInfotekst) {
        this.soknad = soknad;
        this.kodeverk = kodeverk;
        this.visInfotekst = visInfotekst;
        for (FaktumStruktur faktumStruktur : soknadStruktur.getFakta()) {
            if (faktumStruktur.getDependOn() == null && !"hidden".equals(faktumStruktur.getType())) {
                OppsummeringsBolk bolk = hentOgOpprettBolkOmIkkeFinnes(faktumStruktur.getPanel());
                bolk.fakta.addAll(hentOppsummeringForFaktum(faktumStruktur, null, soknadStruktur, soknad));
            }
        }
    }

    private List<OppsummeringsFaktum> hentOppsummeringForFaktum(final FaktumStruktur faktumStruktur, Faktum parent, final SoknadStruktur soknadStruktur, final WebSoknad soknad) {
        List<Faktum> fakta = parent != null ?
                soknad.getFaktaMedKeyOgParentFaktum(faktumStruktur.getId(), parent.getFaktumId()) :
                soknad.getFaktaMedKey(faktumStruktur.getId());
        if (fakta.isEmpty()) {
            return new ArrayList<>();
        }

        return on(fakta).map(new Transformer<Faktum, OppsummeringsFaktum>() {
            @Override
            public OppsummeringsFaktum transform(final Faktum faktum) {
                List<VedleggForFaktumStruktur> vedleggForFaktumStrukturs = soknadStruktur.vedleggFor(faktum);
                List<OppsummeringsVedlegg> vedlegg = on(vedleggForFaktumStrukturs).map(new Transformer<VedleggForFaktumStruktur, OppsummeringsVedlegg>() {
                    @Override
                    public OppsummeringsVedlegg transform(VedleggForFaktumStruktur vedleggForFaktumStruktur) {
                        return new OppsummeringsVedlegg(faktum, soknad.finnVedleggSomMatcherForventning(vedleggForFaktumStruktur, faktum.getFaktumId()), vedleggForFaktumStruktur);
                    }
                }).collect();
                return new OppsummeringsFaktum(faktumStruktur, faktum, finnBarnOppsummering(faktumStruktur, null, soknadStruktur, soknad), vedlegg);
            }
        }).collect();

    }

    private List<OppsummeringsFaktum> finnBarnOppsummering(FaktumStruktur forelderStruktur, final Faktum parentFaktum, final SoknadStruktur soknadStruktur, final WebSoknad soknad) {
        List<FaktumStruktur> faktumStrukturs = soknadStruktur.finnBarneStrukturer(forelderStruktur.getId());
        List<OppsummeringsFaktum> result = new ArrayList<>();
        for (FaktumStruktur faktumStruktur : faktumStrukturs) {
            result.addAll(hentOppsummeringForFaktum(faktumStruktur, parentFaktum, soknadStruktur, soknad));
        }
        return result;
    }
    private String resolveView(String type) {
        if ("checkboxGroup".equals(type)) {
            return "skjema/generisk/checkboxGroup";
        } else if ("textbox".equals(type)) {
            return "skjema/generisk/textbox";
        } else if ("composite".equals(type)) {
            return "skjema/generisk/composite";
        } else if ("compositelist".equals(type)) {
            return "skjema/generisk/compositelist";
        } else if ("periode".equals(type)) {
            return "skjema/generisk/periode";
        } else if ("date".equals(type)) {
            return "skjema/generisk/date";
        } else if ("tilleggsopplysninger".equals(type)) {
            return "skjema/generisk/tilleggsopplysninger";
        } else if ("hidden".equals(type)) {
            return "skjema/generisk/hidden";
        } else if ("inputgroup".equals(type)) {
            return "skjema/generisk/inputgroup";
        } else if ("infotekst".equals(type)) {
            return "skjema/generisk/infotekst";
        }
        return "skjema/generisk/default";
    }
    private class OppsummeringsBolk {
        public String navn;
        public List<OppsummeringsFaktum> fakta = new ArrayList<>();

        public OppsummeringsBolk(String panel) {
            this.navn = panel;
        }
    }
    public abstract class OppsummeringsBase implements PropertyAware{
        public abstract String key();
        public abstract String value();
        public abstract String originalValue();
        public abstract boolean erSynlig();
        public abstract String property(String configKey);

    }

    public class OppsummeringsFaktum extends OppsummeringsBase {
        public final Faktum faktum;
        public final FaktumStruktur struktur;
        public final List<OppsummeringsFaktum> barneFakta;
        public final List<OppsummeringsProperty> barneProperties;
        public final List<OppsummeringsVedlegg> vedlegg;

        public OppsummeringsFaktum(FaktumStruktur faktumStruktur, Faktum faktum, List<OppsummeringsFaktum> barnFakta, List<OppsummeringsVedlegg> vedlegg) {
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

        @Override
        public String property(String configKey) {
            if(struktur.hasConfig(configKey)){
                return OppsummeringsFaktum.this.faktum.getProperties().get(struktur.getConfiguration().get(configKey));
            }
            return OppsummeringsFaktum.this.faktum.getProperties().get(configKey);
        }

        public String template() {
            return resolveView(struktur.getType());
        }



        @Override
        public String key() {
            return struktur.getId().toLowerCase();
        }

        @Override
        public String value() {
            return faktum.getValue() != null? faktum.getValue().toLowerCase(): "";
        }

        @Override
        public String originalValue() {
            return faktum.getValue();
        }

        public class OppsummeringsProperty extends OppsummeringsBase{
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
                return resolveView(struktur.getType());
            }

        }
    }

    public class OppsummeringsVedlegg {
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
            return kodeverk.getKode(struktur.getSkjemaNummer(), Kodeverk.Nokkel.TITTEL);
        }
    }


    private OppsummeringsBolk hentOgOpprettBolkOmIkkeFinnes(String panel) {
        String nullsafePanel = panel != null ? panel : "";
        for (OppsummeringsBolk oppsummeringsBolk : bolker) {
            if (oppsummeringsBolk.navn.equals(nullsafePanel)) {
                return oppsummeringsBolk;
            }
        }
        OppsummeringsBolk bolk = new OppsummeringsBolk(nullsafePanel);
        bolker.add(bolk);
        return bolk;
    }
}
