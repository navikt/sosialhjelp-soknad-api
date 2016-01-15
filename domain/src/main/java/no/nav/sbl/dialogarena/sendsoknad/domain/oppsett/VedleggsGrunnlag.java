package no.nav.sbl.dialogarena.sendsoknad.domain.oppsett;

import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.message.NavMessageSource;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class VedleggsGrunnlag {
    public List<Pair<VedleggForFaktumStruktur, List<Faktum>>> grunnlag = new ArrayList<>();
    public WebSoknad soknad;
    public Vedlegg vedlegg;
    public NavMessageSource navMessageSource;

    public VedleggsGrunnlag(WebSoknad soknad, Vedlegg vedlegg, NavMessageSource navMessageSource) {
        this.soknad = soknad;
        this.vedlegg = vedlegg;
        this.navMessageSource = navMessageSource;
    }

    VedleggsGrunnlag medGrunnlag(VedleggForFaktumStruktur vedlegg, List<Faktum> faktum) {
        grunnlag.add(new ImmutablePair<>(vedlegg, faktum));
        return this;
    }

    public VedleggsGrunnlag medGrunnlag(VedleggForFaktumStruktur vedlegg, Faktum... faktum) {
        return medGrunnlag(vedlegg, Arrays.asList(faktum));
    }


    public Vedlegg.Status oppdaterInnsendingsvalg(boolean vedleggErPaakrevd) {
        if (vedleggErPaakrevd && vedlegg.getInnsendingsvalg().equals(Vedlegg.Status.IkkeVedlegg)) {
            vedlegg.oppdatertInnsendtStatus();
        } else if (!vedleggErPaakrevd && !vedlegg.getInnsendingsvalg().equals(Vedlegg.Status.IkkeVedlegg)) {
            vedlegg.setInnsendingsvalg(Vedlegg.Status.IkkeVedlegg);
        }
        return vedlegg.getInnsendingsvalg();
    }

    public boolean erVedleggPaakrevd() {
        for (Pair<VedleggForFaktumStruktur, List<Faktum>> pair : grunnlag) {
            if (matcherEtAvFaktumeneKravTilVedlegg(pair.getRight(), pair.getLeft())) {
                return true;
            }
        }
        return false;
    }

    private boolean matcherEtAvFaktumeneKravTilVedlegg(List<Faktum> fakta, VedleggForFaktumStruktur vedleggForFaktumStruktur) {
        for (Faktum faktum : fakta) {
            if (vedleggForFaktumStruktur.getFaktum().erSynlig(soknad, faktum) && vedleggForFaktumStruktur.trengerVedlegg(faktum) && vedleggForFaktumStruktur.harFilterProperty(faktum)) {
                return true;
            }
        }
        return false;
    }

    private Faktum finnForsteFaktum() {
        for (Pair<VedleggForFaktumStruktur, List<Faktum>> pair : grunnlag) {
            if (!pair.getRight().isEmpty()) {
                return pair.getRight().get(0);
            }
        }
        return null;
    }

    public Vedlegg getVedlegg() {
        return vedlegg;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("grunnlag", grunnlag)
                .append("vedlegg", vedlegg)
                .toString();
    }



    public boolean vedleggHarTittelFraProperty(VedleggForFaktumStruktur vedlegg, Faktum faktum) {
        return vedlegg.getProperty() != null && faktum.getProperties().containsKey(vedlegg.getProperty());
    }
    public void opprettVedleggFraFaktum() {
        vedlegg = grunnlag.get(0).getLeft().genererVedlegg(finnForsteFaktum());
    }

    public boolean vedleggIkkeFinnes() {
        return !vedleggFinnes();
    }

    public boolean vedleggFinnes() {
        return vedlegg != null;
    }

}
