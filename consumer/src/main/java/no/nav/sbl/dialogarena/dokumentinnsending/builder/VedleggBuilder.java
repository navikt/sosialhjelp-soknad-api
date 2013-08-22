package no.nav.sbl.dialogarena.dokumentinnsending.builder;

import no.nav.sbl.dialogarena.dokumentinnsending.domain.Dokument;
import no.nav.sbl.dialogarena.dokumentinnsending.domain.Dokument.Type;
import no.nav.sbl.dialogarena.dokumentinnsending.domain.DokumentInnhold;
import no.nav.sbl.dialogarena.dokumentinnsending.domain.InnsendingsValg;
import no.nav.tjeneste.domene.brukerdialog.henvendelse.v1.informasjon.WSDokument;

import java.util.Date;

//// TODO: Vurder å inline builderene i domene-klassene. Fører til mindre duplisering
public class VedleggBuilder<T extends VedleggBuilder<T>> {

    protected final Type type;
    protected Long dokumentForventningId;
    protected String behandlingsId;
    protected Date opplastetDato;
    protected String tittel;
    protected String beskrivelse;
    protected String kodeverkId;
    protected InnsendingsValg innsendingsValg;

    @SuppressWarnings("unchecked")
    protected final T self = (T) this;

    public static VedleggBuilder<?> forType(Type type) {
        return new TypeClosedDokumentBuilder(type);
    }

    protected VedleggBuilder(Type type) {
        this.type = type;
    }

    public T brukerBehandlingId(String behandlingsId) {
        this.behandlingsId = behandlingsId;
        return self;
    }

    public T opplastetDatoFra(WSDokument dokument) {
        if (dokument != null) {
            if (dokument.getOpplastetDato() != null) {
                this.opplastetDato = dokument.getOpplastetDato().toGregorianCalendar().getTime();
            }
        }
        return self;
    }

    public T dokumentForventningId(Long dokumentForventningId) {
        this.dokumentForventningId = dokumentForventningId;
        return self;
    }

    public T tittel(String tittel) {
        this.tittel = tittel;
        return self;
    }

    public T beskrivelse(String beskrivelse) {
        this.beskrivelse = beskrivelse;
        return self;
    }

    public T kodeverkId(String kodeverkId) {
        this.kodeverkId = kodeverkId;
        return self;
    }

    public T innsendingsValg(InnsendingsValg valg) {
        this.innsendingsValg = valg;
        return self;
    }

    public Dokument build() {
        Dokument dokument = new Dokument(type);
        dokument.setDokumentForventningsId(dokumentForventningId);
        dokument.setBehandlingsId(behandlingsId);
        dokument.setDokumentInnhold(new DokumentInnhold());
        dokument.setOpplastetDato(opplastetDato);
        dokument.setNavn(tittel);
        dokument.setBeskrivelse(beskrivelse);
        dokument.setKodeverkId(kodeverkId);
        dokument.setInnsendingsvalg(innsendingsValg);
        return dokument;
    }

    private static final class TypeClosedDokumentBuilder extends VedleggBuilder<TypeClosedDokumentBuilder> {
        private TypeClosedDokumentBuilder(Type type) {
            super(type);
        }
    }
}