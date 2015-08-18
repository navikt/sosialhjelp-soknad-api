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


    public boolean erVedleggPaakrevd() {
        for (Pair<VedleggForFaktumStruktur, List<Faktum>> pair : grunnlag) {
            if (matcherEtAvFaktumeneKravTilVedlegg(pair.getRight(), pair.getLeft())) return true;
        }
        return false;
    }

    private boolean matcherEtAvFaktumeneKravTilVedlegg(List<Faktum> fakta, VedleggForFaktumStruktur vedleggForFaktumStruktur) {
        for (Faktum faktum : fakta) {
            if (vedleggForFaktumStruktur.getFaktum().erSynlig(soknad) && vedleggForFaktumStruktur.trengerVedlegg(faktum) && /**Fremdeles veldig usikker på denne her */vedleggForFaktumStruktur.harFilterProperty(faktum)) {
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

    public void oppdaterInnsendingsvalg(VedleggRepository vedleggRepository) {
        Boolean kreverDbOppdatering = oppdaterInnsendingsvalg(erVedleggPaakrevd());
        if (kreverDbOppdatering) {
            logger.warn("\n ########### VEDLEGGSFEIL - Feil i ny vedleggsgenereringslogikk ################# \n" + "Lagrer vedlegg: \n" + vedlegg);

//            vedleggRepository.opprettEllerLagreVedleggVedNyGenereringUtenEndringAvData(vedlegg);
        }
    }
}
