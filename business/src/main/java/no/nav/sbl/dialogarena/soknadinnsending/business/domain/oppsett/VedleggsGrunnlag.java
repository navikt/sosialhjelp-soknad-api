package no.nav.sbl.dialogarena.soknadinnsending.business.domain.oppsett;

import no.nav.sbl.dialogarena.soknadinnsending.business.db.vedlegg.VedleggRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Vedlegg;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;
import no.nav.sbl.dialogarena.soknadinnsending.business.message.NavMessageSource;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class VedleggsGrunnlag {
    public List<Pair<VedleggForFaktumStruktur, List<Faktum>>> grunnlag = new ArrayList<>();
    private WebSoknad soknad;
    private Vedlegg vedlegg;
    private NavMessageSource navMessageSource;

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

    public void oppdaterVedlegg(VedleggRepository vedleggRepository) {
        boolean vedleggErPaakrevd = erVedleggPaakrevd();

        if (vedleggFinnes() || vedleggErPaakrevd) {

            if (vedleggIkkeFinnes()) {
                opprettVedleggFraFaktum();
            }

            Vedlegg.Status orginalStatus = vedlegg.getInnsendingsvalg();
            Vedlegg.Status status = oppdaterInnsendingsvalg(vedleggErPaakrevd);
            VedleggForFaktumStruktur vedleggForFaktumStruktur = grunnlag.get(0).getLeft();
            Faktum faktum = grunnlag.get(0).getRight().get(0);
            if (vedleggHarTittelFraProperty(vedleggForFaktumStruktur, faktum)) {
                vedlegg.setNavn(faktum.getProperties().get(vedleggForFaktumStruktur.getProperty()));
            } else if (vedleggForFaktumStruktur.harOversetting()) {
                vedlegg.setNavn(navMessageSource.getMessage(vedleggForFaktumStruktur.getOversetting().replace("${key}", faktum.getKey()), new Object[0], new Locale("nb", "NO")));
            }

            if (!status.equals(orginalStatus) || vedlegg.erNyttVedlegg()) {
                vedleggRepository.opprettEllerLagreVedleggVedNyGenereringUtenEndringAvData(vedlegg);
            }
        }
    }

    private boolean vedleggHarTittelFraProperty(VedleggForFaktumStruktur vedlegg, Faktum faktum) {
        return vedlegg.getProperty() != null && faktum.getProperties().containsKey(vedlegg.getProperty());
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
