package no.nav.sbl.dialogarena.service.oppsummering;

import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.oppsett.FaktumStruktur;
import no.nav.sbl.dialogarena.sendsoknad.domain.oppsett.SoknadStruktur;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;

public class OppsummeringsContext {
    public final boolean utvidetSoknad;
    public List<OppsummeringsBolk> bolker = new ArrayList<>();
    public WebSoknad soknad;

    public OppsummeringsContext(WebSoknad soknad, SoknadStruktur soknadStruktur, boolean utvidetSoknad) {
        this.soknad = soknad;
        this.utvidetSoknad = utvidetSoknad;
        for (FaktumStruktur faktumStruktur : soknadStruktur.getFakta()) {
            if (faktumStruktur.getDependOn() == null
                    && !"hidden".equals(faktumStruktur.getType())
                    && ( utvidetSoknad || !faktumStruktur.getKunUtvidet())) {
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

        Function<Faktum,List<OppsummeringsFaktum.OppsummeringsVedlegg>> vedleggForFaktum = f -> soknadStruktur.vedleggFor(f)
                .stream()
                .map(v -> new OppsummeringsFaktum.OppsummeringsVedlegg(f,soknad.finnVedleggSomMatcherForventning(v,f.getFaktumId()),v))
                .collect(toList());

       return fakta.stream()
                .map(f ->  new OppsummeringsFaktum(soknad,faktumStruktur,f,finnBarnOppsummering(faktumStruktur,f,soknadStruktur,soknad), vedleggForFaktum.apply(f)))
                .collect(toList());
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

    private class OppsummeringsBolk {
        public String navn;
        public List<OppsummeringsFaktum> fakta = new ArrayList<>();

        public OppsummeringsBolk(String panel) {
            this.navn = panel;
        }
    }
}