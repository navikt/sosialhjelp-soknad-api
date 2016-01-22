package no.nav.sbl.dialogarena.service.oppsummering;

import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.oppsett.FaktumStruktur;
import no.nav.sbl.dialogarena.sendsoknad.domain.oppsett.SoknadStruktur;
import no.nav.sbl.dialogarena.sendsoknad.domain.oppsett.VedleggForFaktumStruktur;
import org.apache.commons.collections15.Transformer;

import java.util.ArrayList;
import java.util.List;

import static no.nav.modig.lang.collections.IterUtils.*;


public class OppsummeringsContext {
    public final boolean visInfotekst;
    public List<OppsummeringsBolk> bolker = new ArrayList<>();
    public WebSoknad soknad;

    public OppsummeringsContext(WebSoknad soknad, SoknadStruktur soknadStruktur, boolean visInfotekst) {
        this.soknad = soknad;
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
                List<OppsummeringsFaktum.OppsummeringsVedlegg> vedlegg = on(vedleggForFaktumStrukturs).map(new Transformer<VedleggForFaktumStruktur, OppsummeringsFaktum.OppsummeringsVedlegg>() {
                    @Override
                    public OppsummeringsFaktum.OppsummeringsVedlegg transform(VedleggForFaktumStruktur vedleggForFaktumStruktur) {
                        return new OppsummeringsFaktum.OppsummeringsVedlegg(faktum, soknad.finnVedleggSomMatcherForventning(vedleggForFaktumStruktur, faktum.getFaktumId()), vedleggForFaktumStruktur);
                    }
                }).collect();
                return new OppsummeringsFaktum(soknad, faktumStruktur, faktum, finnBarnOppsummering(faktumStruktur, faktum, soknadStruktur, soknad), vedlegg);
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

    public class OppsummeringsBolk {
        public String navn;
        public List<OppsummeringsFaktum> fakta = new ArrayList<>();

        public OppsummeringsBolk(String panel) {
            this.navn = panel;
        }
    }
}