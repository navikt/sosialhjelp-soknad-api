package no.nav.sbl.dialogarena.soknadinnsending.business.domain.oppsett;

import no.nav.sbl.dialogarena.soknadinnsending.business.db.vedlegg.VedleggRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Vedlegg;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

public class VedleggsGrunnlag {
    private static final Logger logger = getLogger(VedleggsGrunnlag.class);
    public List<Pair<VedleggForFaktumStruktur, List<Faktum>>> grunnlag = new ArrayList<>();
    private WebSoknad soknad;
    private Vedlegg vedlegg;

    public VedleggsGrunnlag(WebSoknad soknad, Vedlegg vedlegg) {
        this.soknad = soknad;
        this.vedlegg = vedlegg;
    }

    VedleggsGrunnlag medGrunnlag(VedleggForFaktumStruktur vedlegg, List<Faktum> faktum) {
        grunnlag.add(new ImmutablePair<>(vedlegg, faktum));
        return this;
    }

    VedleggsGrunnlag medGrunnlag(VedleggForFaktumStruktur vedlegg, Faktum... faktum) {
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
            if (matcherEtAvFaktumeneKravTilVedlegg(pair.getRight(), pair.getLeft())) { return true; }
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

    public void oppdaterVedlegg(VedleggRepository vedleggRepository) {
        boolean vedleggErPaakrevd = erVedleggPaakrevd();

        if(vedleggFinnes() || vedleggErPaakrevd){

            if (vedleggIkkeFinnes()) {
                opprettVedleggFraFaktum();
            }

            Vedlegg.Status orginalStatus = vedlegg.getInnsendingsvalg();
            Vedlegg.Status status = oppdaterInnsendingsvalg(vedleggErPaakrevd);

            if (!status.equals(orginalStatus) || vedlegg.erNyttVedlegg()) {
                vedleggRepository.opprettEllerLagreVedleggVedNyGenereringUtenEndringAvData(vedlegg);
            }
        }
    }

    private void opprettVedleggFraFaktum() {
        vedlegg = grunnlag.get(0).getLeft().genererVedlegg(finnForsteFaktum());
    }

    private boolean vedleggIkkeFinnes() {
        return !vedleggFinnes();
    }

    private boolean vedleggFinnes() {
        return vedlegg != null;
    }

}
