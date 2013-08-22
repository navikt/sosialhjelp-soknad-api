package no.nav.sbl.dialogarena.dokumentinnsending.builder;

import no.nav.sbl.dialogarena.dokumentinnsending.domain.BrukerBehandlingType;
import no.nav.sbl.dialogarena.dokumentinnsending.domain.DokumentSoknad;

import java.util.ArrayList;
import java.util.List;


public class SoknadBuilder {

    private String ident;
    private String soknadsId;
    private BrukerBehandlingType type;
    private List<SkjemaBuilder> skjemaer = new ArrayList<>();
    private List<SkjemaBuilder> navVedlegg = new ArrayList<>();
    private List<VedleggBuilder<?>> eksterntVedlegg = new ArrayList<>();

    public static SoknadBuilder with() {
        return new SoknadBuilder();
    }

    public SoknadBuilder ident(String ident) {
        this.ident = ident;
        return this;
    }

    public SoknadBuilder soknadsId(String soknadsId) {
        this.soknadsId = soknadsId;
        return this;
    }

    public SoknadBuilder skjema(SkjemaBuilder skjema) {
        this.skjemaer.add(skjema);
        return this;
    }

    public SoknadBuilder navVedlegg(SkjemaBuilder nyttNAVVedlegg) {
        this.navVedlegg.add(nyttNAVVedlegg);
        return this;
    }

    public SoknadBuilder eksterntVedlegg(VedleggBuilder<?> nyttVedlegg) {
        this.eksterntVedlegg.add(nyttVedlegg);
        return this;
    }

    public SoknadBuilder type(BrukerBehandlingType type) {
        this.type = type;
        return this;
    }

    public DokumentSoknad build() {
        DokumentSoknad soknad = new DokumentSoknad(ident, soknadsId);
        soknad.brukerBehandlingType = type;

        for (SkjemaBuilder skjema : skjemaer) {
            soknad.hovedskjema = skjema.build();
        }
        for (SkjemaBuilder etVedlegg : navVedlegg) {
            soknad.leggTilVedlegg(etVedlegg.build());
        }
        for (VedleggBuilder<?> etVedlegg : eksterntVedlegg) {
            soknad.leggTilVedlegg(etVedlegg.build());
        }

        return soknad;
    }
}