package no.nav.sbl.dialogarena.dokumentinnsending.builder;

import no.nav.sbl.dialogarena.dokumentinnsending.domain.Dokument.Type;
import no.nav.sbl.dialogarena.dokumentinnsending.domain.DokumentInnhold;
import no.nav.sbl.dialogarena.dokumentinnsending.domain.Skjema;

public final class SkjemaBuilder extends VedleggBuilder<SkjemaBuilder> {

    private String skjemaId;
    private String link;
    private String gosysId;
    private String skjemanummer;

    public static SkjemaBuilder forType(Type type) {
        return new SkjemaBuilder(type);
    }

    public SkjemaBuilder skjemanummer(String skjemanummer) {
        this.skjemanummer = skjemanummer;
        return this;
    }

    public SkjemaBuilder link(String link) {
        this.link = link;
        return this;
    }

    public SkjemaBuilder gosysId(String gosysId) {
        this.gosysId = gosysId;
        return this;
    }

    public SkjemaBuilder skjemaId(String skjemaId) {
        this.skjemaId = skjemaId;
        return this;
    }

    @Override
    public Skjema build() {
        Skjema skjema = new Skjema(type, skjemaId);
        skjema.setDokumentForventningsId(dokumentForventningId);
        skjema.setBehandlingsId(behandlingsId);
        skjema.setDokumentInnhold(new DokumentInnhold());
        skjema.setOpplastetDato(opplastetDato);
        skjema.setNavn(tittel);
        skjema.setBeskrivelse(beskrivelse);
        skjema.setKodeverkId(kodeverkId);
        skjema.setLink(link);
        skjema.setSkjemanummer(skjemanummer);
        skjema.setGosysId(gosysId);
        skjema.setInnsendingsvalg(innsendingsValg);
        return skjema;
    }

    private SkjemaBuilder(Type type) {
        super(type);
    }
}