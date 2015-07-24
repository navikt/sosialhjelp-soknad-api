package no.nav.sbl.dialogarena.soknadinnsending.business.domain.oppsett;

import no.nav.sbl.dialogarena.soknadinnsending.business.db.vedlegg.VedleggRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Vedlegg;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class VedleggsGrunnlag {
    public List<Pair<SoknadVedlegg, List<Faktum>>> grunnlag = new ArrayList<>();
    private WebSoknad soknad;
    private Vedlegg vedlegg;

    public VedleggsGrunnlag(WebSoknad soknad, Vedlegg vedlegg) {
        this.soknad = soknad;
        this.vedlegg = vedlegg;
    }

    VedleggsGrunnlag medGrunnlag(SoknadVedlegg vedlegg, List<Faktum> faktum) {
        grunnlag.add(new ImmutablePair<>(vedlegg, faktum));
        return this;
    }

    VedleggsGrunnlag medGrunnlag(SoknadVedlegg vedlegg, Faktum... faktum) {
        return medGrunnlag(vedlegg, Arrays.asList(faktum));
    }


    public boolean oppdaterInnsendingsvalg(boolean erPaakrevd) {

        if (vedlegg == null && !erPaakrevd) {
            return false;
        } else if (vedlegg == null) {
            vedlegg = grunnlag.get(0).getLeft().genererVedlegg(finnForsteFaktum());
        }

        Vedlegg.Status orginalt = vedlegg.getInnsendingsvalg();
        if (erPaakrevd && vedlegg.getInnsendingsvalg().equals(Vedlegg.Status.IkkeVedlegg)) {
            vedlegg.oppdatertInnsendtStatus();
        } else if (!erPaakrevd && !vedlegg.getInnsendingsvalg().equals(Vedlegg.Status.IkkeVedlegg)) {
            vedlegg.setInnsendingsvalg(Vedlegg.Status.IkkeVedlegg);
        }
        return !vedlegg.getInnsendingsvalg().equals(orginalt) || vedlegg.getVedleggId() == null;
    }


    public boolean kreverVedleggsEndring() {
        Faktum forsteFaktum = finnForsteFaktum();
        if (forsteFaktum != null) {
            for (Pair<SoknadVedlegg, List<Faktum>> pair : grunnlag) {
                if (matcherEtAvFaktumeneKravTilVedlegg(pair.getRight(), pair.getLeft())) return true;
            }
        }
        return false;
    }

    private boolean matcherEtAvFaktumeneKravTilVedlegg(List<Faktum> fakta, SoknadVedlegg soknadVedlegg) {
        for (Faktum faktum : fakta) {
            if (soknadVedlegg.getFaktum().erSynlig(soknad) && soknadVedlegg.trengerVedlegg(faktum)) {
                return true;
            }
        }
        return false;
    }

    private Faktum finnForsteFaktum() {
        for (Pair<SoknadVedlegg, List<Faktum>> pair : grunnlag) {
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

    public void oppdaterInnsendingsvalg(VedleggRepository vedleggRepository) {
        Boolean kreverDbOppdatering = oppdaterInnsendingsvalg(kreverVedleggsEndring());
        if (kreverDbOppdatering) {
            vedleggRepository.opprettEllerLagreVedleggUtenEndingAvData(vedlegg);
        }
    }
}
