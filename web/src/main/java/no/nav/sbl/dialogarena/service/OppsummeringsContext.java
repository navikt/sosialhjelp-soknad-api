
package no.nav.sbl.dialogarena.service;

import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.oppsett.FaktumStruktur;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.oppsett.SoknadStruktur;
import org.apache.commons.collections15.Transformer;

import java.util.ArrayList;
import java.util.List;

import static no.nav.modig.lang.collections.IterUtils.on;


public class OppsummeringsContext{
    public List<OppsummeringsBolk>  bolker = new ArrayList<>();
    WebSoknad soknad;
    public OppsummeringsContext(WebSoknad soknad, SoknadStruktur soknadStruktur) {
        this.soknad = soknad;
        for (FaktumStruktur faktumStruktur : soknadStruktur.getFakta()) {
            if (faktumStruktur.getDependOn() == null) {
                OppsummeringsBolk bolk = hentOgOpprettBolkOmIkkeFinnes(faktumStruktur.getPanel());
                bolk.fakta.addAll(hentOppsummeringForFaktum(faktumStruktur, null, soknadStruktur, soknad));
            }
        }
    }

    private List<OppsummeringsFaktum> hentOppsummeringForFaktum(final FaktumStruktur faktumStruktur, Faktum parent, final SoknadStruktur soknadStruktur, final WebSoknad soknad) {
        List<Faktum> fakta = parent != null?
                soknad.getFaktaMedKeyOgParentFaktum(faktumStruktur.getId(), parent.getFaktumId()):
                soknad.getFaktaMedKey(faktumStruktur.getId());
        if(fakta.size() == 0){
            return new ArrayList<>();
        }

        return on(fakta).map(new Transformer<Faktum, OppsummeringsFaktum>() {
            @Override
            public OppsummeringsFaktum transform(Faktum faktum) {
                return new OppsummeringsFaktum(faktumStruktur, faktum, finnBarnOppsummering(faktumStruktur, null, soknadStruktur, soknad));
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
    public  class OppsummeringsFaktum {
        public Faktum faktum;
        public FaktumStruktur struktur;
        public List<OppsummeringsFaktum> barneFakta = new ArrayList<>();

        public OppsummeringsFaktum(FaktumStruktur faktumStruktur, Faktum faktum, List<OppsummeringsFaktum> barnFakta) {
            this.struktur= faktumStruktur;
            this.faktum = faktum;
            this.barneFakta = barnFakta;
        }
        public boolean erSynlig(){
            return struktur.erSynlig(soknad, faktum);
        }
        public String template(){
            if("checkboxGroup".equals(struktur.getType())){
                return "skjema/generisk/checkboxGroup";
            }
            else if("textbox".equals(struktur.getType())){
                return "skjema/generisk/textbox";
            }
            else if("periode".equals(struktur.getType())){
                return "skjema/generisk/periode";
            }
            else if("date".equals(struktur.getType())){
                return "skjema/generisk/date";
            }
            else if("inputgroup".equals(struktur.getType())){
                return "skjema/generisk/inputgroup";
            }
            return "skjema/generisk/default";
        }
    }

    private OppsummeringsBolk hentOgOpprettBolkOmIkkeFinnes(String panel) {
        String nullsafePanel = panel != null ? panel : "";
        for (OppsummeringsBolk oppsummeringsBolk : bolker) {
            if(oppsummeringsBolk.navn.equals(nullsafePanel)){
                return oppsummeringsBolk;
            }
        }
        OppsummeringsBolk bolk = new OppsummeringsBolk(nullsafePanel);
        bolker.add(bolk);
        return bolk;
    }
}
