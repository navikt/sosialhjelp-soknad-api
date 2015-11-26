
package no.nav.sbl.dialogarena.service;

import no.nav.sbl.dialogarena.common.kodeverk.Kodeverk;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Vedlegg;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.oppsett.FaktumStruktur;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.oppsett.SoknadStruktur;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.oppsett.VedleggForFaktumStruktur;
import org.apache.commons.collections15.Transformer;

import java.util.ArrayList;
import java.util.List;

import static no.nav.modig.lang.collections.IterUtils.on;


public class OppsummeringsContext {
    public List<OppsummeringsBolk> bolker = new ArrayList<>();
    WebSoknad soknad;
    Kodeverk kodeverk;

    public OppsummeringsContext(WebSoknad soknad, SoknadStruktur soknadStruktur, Kodeverk kodeverk) {
        this.soknad = soknad;
        this.kodeverk = kodeverk;
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
        if (fakta.size() == 0) {
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

    private class OppsummeringsBolk {
        public String navn;
        public List<OppsummeringsFaktum> fakta = new ArrayList<>();

        public OppsummeringsBolk(String panel) {
            this.navn = panel;
        }
    }

    public class OppsummeringsFaktum {
        public final Faktum faktum;
        public final FaktumStruktur struktur;
        public final List<OppsummeringsFaktum> barneFakta;
        public final List<OppsummeringsVedlegg> vedlegg;

        public OppsummeringsFaktum(FaktumStruktur faktumStruktur, Faktum faktum, List<OppsummeringsFaktum> barnFakta, List<OppsummeringsVedlegg> vedlegg) {
            this.struktur = faktumStruktur;
            this.faktum = faktum;
            this.barneFakta = barnFakta;
            this.vedlegg = vedlegg;
        }

        public boolean erSynlig() {
            return struktur.erSynlig(soknad, faktum);
        }

        public String template() {
            if ("checkboxGroup".equals(struktur.getType())) {
                return "skjema/generisk/checkboxGroup";
            } else if ("textbox".equals(struktur.getType())) {
                return "skjema/generisk/textbox";
            } else if ("periode".equals(struktur.getType())) {
                return "skjema/generisk/periode";
            } else if ("date".equals(struktur.getType())) {
                return "skjema/generisk/date";
            } else if ("tilleggsopplysninger".equals(struktur.getType())) {
                return "skjema/generisk/tilleggsopplysninger";
            } else if ("hidden".equals(struktur.getType())) {
                return "skjema/generisk/hidden";
            } else if ("dagpenger-barn".equals(struktur.getType())) {
                return "skjema/generisk/dagpenger-barn";
            } else if ("inputgroup".equals(struktur.getType())) {
                return "skjema/generisk/inputgroup";
            }
            return "skjema/generisk/default";
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
