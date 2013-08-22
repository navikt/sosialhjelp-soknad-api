package no.nav.sbl.dialogarena.dokumentinnsending.domain;

import no.nav.sbl.dialogarena.soknad.kodeverk.KodeverkSkjema;

import org.apache.commons.collections15.Predicate;
import org.apache.commons.collections15.Transformer;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serializable;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import static no.nav.modig.lang.collections.PredicateUtils.equalTo;
import static no.nav.modig.lang.collections.PredicateUtils.not;
import static no.nav.modig.lang.collections.PredicateUtils.where;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

public class Dokument implements Serializable, Comparable<Dokument> {


    public enum Type {HOVEDSKJEMA, NAV_VEDLEGG, EKSTERNT_VEDLEGG, EKSTRA_VEDLEGG, IKKE_SPESIFISERT}

    private final Type type;

    private String beskrivelse;
    private String link;
    private String behandlingsId;
    private Long dokumentForventningsId;
    private InnsendingsValg valg;
    private Long dokumentId;
    private String navn;
    private String kodeverkId;
    private DokumentInnhold dokumentInnhold;

    private KodeverkSkjema kodeverk;

    private Map<Integer, UUID> forhandsvisningIdMap = new LinkedHashMap<>();

    public Dokument(Type type) {
        this.type = defaultIfNull(type, Type.IKKE_SPESIFISERT);
        this.valg = InnsendingsValg.IKKE_VALGT;
    }

    public void setDokumentInnhold(DokumentInnhold dokumentInnhold) {
        this.dokumentInnhold = dokumentInnhold;
    }

    public void setNavn(String navn) {
        this.navn = navn;
    }

    public DokumentInnhold getDokumentInnhold() {
        return dokumentInnhold;
    }

    public Long getDokumentForventningsId() {
        return dokumentForventningsId;
    }

    public void setDokumentId(Long dokumentId) {
        this.dokumentId = dokumentId;
    }

    public Long getDokumentId() {
        return dokumentId;
    }

    public void setInnsendingsvalg(InnsendingsValg innsendingsvalg) {
        valg = innsendingsvalg;
    }

    public void setDokumentForventningsId(Long dokumentForventningsId) {
        this.dokumentForventningsId = dokumentForventningsId;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public void setBeskrivelse(String beskrivelse) {
        this.beskrivelse = beskrivelse;
    }

    public String getLink() {
        return link;
    }

    public String getBeskrivelse() {
        return beskrivelse;
    }

    public void setBehandlingsId(String behandlingsId) {
        this.behandlingsId = behandlingsId;
    }

    public String getBehandlingsId() {
        return behandlingsId;
    }

    public InnsendingsValg getValg() {
        return valg;
    }

    public String getNavn() {
        return navn;
    }

    public Type getType() {
        return type;
    }

    public void setKodeverkId(String kodeverkId) {
        this.kodeverkId = kodeverkId;
    }

    public String getKodeverkId() {
        return kodeverkId;
    }

    public void setKodeverk(KodeverkSkjema kodeverk) {
        this.kodeverk = kodeverk;
    }

    public KodeverkSkjema getKodeverk() {
        return kodeverk;
    }

    public Date getOpplastetDato() {
        return dokumentInnhold.getOpplastetDato() != null ? dokumentInnhold.getOpplastetDato().toDate() : null;
    }

    public void setOpplastetDato(Date opplastetDato) {
        dokumentInnhold.setOpplastetDato(opplastetDato);
    }

    public void settOgTransformerInnhold(Iterable<byte[]> dokument) {
        dokumentInnhold.settOgTransformerInnhold(dokument);
    }

    public boolean erOpplastet() {
        return dokumentId != null;
    }

    public boolean harInnhold() {
        return dokumentId != null;
    }

    public void slettInnhold() {
        dokumentInnhold = null;
        forhandsvisningIdMap.clear();
    }

    public String lagFilNavn() {
        return navn.replaceAll(" ", "-") + ".pdf";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Dokument rhs = (Dokument) o;

        return new EqualsBuilder()
                .append(kodeverkId, rhs.kodeverkId)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(kodeverkId)
                .toHashCode();
    }

    @Override
    public int compareTo(Dokument dokument) {
        return navn.compareTo(dokument.navn);
    }

    public boolean er(Dokument dokument) {
        return this.dokumentForventningsId.equals(dokument.dokumentForventningsId);
    }

    public boolean er(Type type) {
        return this.type == type;
    }

    public boolean erIkke(Type type) {
        return !er(type);
    }

    public boolean er(InnsendingsValg valgSomSjekkes) {
        return valg != null && valg.equals(valgSomSjekkes);
    }

    public boolean erIkke(InnsendingsValg valgSomSjekkes) {
        return !er(valgSomSjekkes);
    }

    public boolean erTittelOver30Tegn() {
        return navn != null && navn.length() > 30;
    }

    public boolean erTittelOver18Tegn() {
        return navn != null && navn.length() > 18;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Dokument{");
        sb.append("type=").append(type);
        sb.append(", dokumentForventningsId=").append(dokumentForventningsId);
        sb.append(", dokumentId=").append(dokumentId);
        sb.append(", behandlingsId='").append(behandlingsId).append('\'');
        sb.append(", navn='").append(navn).append('\'');
        sb.append(", beskrivelse='").append(beskrivelse).append('\'');
        sb.append(", kodeverkId='").append(kodeverkId).append('\'');
        sb.append(", link='").append(link).append('\'');
        sb.append(", valg=").append(valg);
        sb.append(", forhandsvisningIdMap=").append(forhandsvisningIdMap);
        sb.append(", dokumentInnhold=").append(dokumentInnhold);
        sb.append('}');
        return sb.toString();
    }

    public static Predicate<Dokument> ikkeAvType(final Type type) {
        return not(where(tilType(), equalTo(type)));
    }

    public static Predicate<Dokument> avType(final Type type) {
        return where(tilType(), equalTo(type));
    }

    public static Predicate<Dokument> harKodeverkId(final String id) {
        return where(tilKodeverk(), equalTo(id));
    }

    public static Predicate<Dokument> harValg(final InnsendingsValg valg) {
        return where(tilInnsendingsValg(), equalTo(valg));
    }

    public static Predicate<Dokument> harIkkeValg(final InnsendingsValg valg) {
        return not(harValg(valg));
    }

    public static Predicate<Dokument> harDokumentInnhold() {
        return where(tilHarInnhold(), equalTo(true));
    }

    public static Predicate<Dokument> harIkkeDokumentInnhold() {
        return where(tilHarInnhold(), equalTo(false));
    }

    public static Transformer<Dokument, Type> tilType() {
        return new TypeTransformer();
    }

    @SuppressWarnings("UnusedDeclaration")
    public static Transformer<Dokument, String> tilNavn() {
        return new NavnTransformer();
    }

    public static Transformer<Dokument, String> tilKodeverk() {
        return new KodeverkTransformer();
    }

    public static Transformer<Dokument, InnsendingsValg> tilInnsendingsValg() {
        return new InnsendingsValgTransformer();
    }

    public static Transformer<Dokument, Boolean> tilHarInnhold() {
        return new HarInnholdTransformer();
    }

    public static final Transformer<Dokument, Type> TYPE = new TypeTransformer();
    public static final Transformer<Dokument, String> NAVN = new NavnTransformer();
    @SuppressWarnings("UnusedDeclaration")
    public static final Transformer<Dokument, String> KODEVERK_ID = new KodeverkTransformer();

    public static final Transformer<Dokument, InnsendingsValg> INNSENDINGSVALG = new InnsendingsValgTransformer();

    private static final class TypeTransformer implements Transformer<Dokument, Type>, Serializable {
        @Override
        public Type transform(Dokument dokument) {
            return dokument.type;
        }
    }

    private static final class NavnTransformer implements Transformer<Dokument, String>, Serializable {
        @Override
        public String transform(Dokument dokument) {
            return dokument.navn;
        }
    }

    private static final class KodeverkTransformer implements Transformer<Dokument, String>, Serializable {

        @Override
        public String transform(Dokument dokument) {
            return dokument.kodeverkId;
        }
    }

    private static final class InnsendingsValgTransformer implements Transformer<Dokument, InnsendingsValg>, Serializable {
        @Override
        public InnsendingsValg transform(Dokument dokument) {
            return dokument.valg;
        }
    }

    private static final class HarInnholdTransformer implements Transformer<Dokument, Boolean>, Serializable {
        @Override
        public Boolean transform(Dokument dokument) {
            return dokument.harInnhold();
        }
    }
}