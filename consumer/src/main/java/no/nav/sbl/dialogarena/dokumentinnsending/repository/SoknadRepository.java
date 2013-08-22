package no.nav.sbl.dialogarena.dokumentinnsending.repository;

import no.nav.sbl.dialogarena.common.kodeverk.Kodeverk;
import no.nav.sbl.dialogarena.dokumentinnsending.builder.SkjemaBuilder;
import no.nav.sbl.dialogarena.dokumentinnsending.builder.SoknadBuilder;
import no.nav.sbl.dialogarena.dokumentinnsending.builder.VedleggBuilder;
import no.nav.sbl.dialogarena.dokumentinnsending.domain.Dokument.Type;
import no.nav.sbl.dialogarena.dokumentinnsending.domain.InnsendingsValg;
import no.nav.sbl.dialogarena.dokumentinnsending.domain.DokumentSoknad;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static no.nav.sbl.dialogarena.common.kodeverk.Kodeverk.Nokkel.BESKRIVELSE;
import static no.nav.sbl.dialogarena.common.kodeverk.Kodeverk.Nokkel.GOSYS_ID;
import static no.nav.sbl.dialogarena.common.kodeverk.Kodeverk.Nokkel.TITTEL;
import static no.nav.sbl.dialogarena.common.kodeverk.Kodeverk.Nokkel.URL;
import static no.nav.sbl.dialogarena.dokumentinnsending.domain.Dokument.Type.HOVEDSKJEMA;

public class SoknadRepository implements Serializable {

    private Map<String, DokumentSoknad> soknader = new LinkedHashMap<>();

    @Inject
    private Kodeverk kodeverk;

    public SoknadRepository() {
    }

    public void lagSoknader() {
        SoknadBuilder soknad1 = SoknadBuilder.with()
                .soknadsId(UUID.randomUUID().toString())
                .skjema(SkjemaBuilder.forType(HOVEDSKJEMA)
                        .tittel(kodeverk.getKode("NAV 04-01.03", TITTEL))
                        .beskrivelse(kodeverk.getKode("NAV 04-01.03", BESKRIVELSE))
                        .link(kodeverk.getKode("NAV 04-01.03", URL))
                        .skjemanummer("NAV 04-01.03")
                        .kodeverkId("NAV 04-01.03")
                        .innsendingsValg(InnsendingsValg.IKKE_VALGT)
                        .gosysId(kodeverk.getKode("NAV 04-01.03", GOSYS_ID)))
                .navVedlegg(SkjemaBuilder.forType(Type.NAV_VEDLEGG)
                        .tittel(kodeverk.getTittel("S6"))
                        .beskrivelse(kodeverk.getKode("S6", BESKRIVELSE))
                        .link(kodeverk.getKode("S6", URL))
                        .skjemanummer("S6")
                        .kodeverkId("S6")
                        .innsendingsValg(InnsendingsValg.IKKE_VALGT)
                        .gosysId(kodeverk.getKode("S6", GOSYS_ID)))
                .eksterntVedlegg(VedleggBuilder.forType(Type.EKSTERNT_VEDLEGG)
                        .tittel(kodeverk.getKode("S7", TITTEL))
                        .beskrivelse(kodeverk.getKode("S7", BESKRIVELSE))
                        .kodeverkId("S7")
                        .innsendingsValg(InnsendingsValg.IKKE_VALGT))
                .eksterntVedlegg(VedleggBuilder.forType(Type.EKSTERNT_VEDLEGG)
                        .tittel(kodeverk.getTittel("T6"))
                        .beskrivelse(kodeverk.getKode("T6", BESKRIVELSE))
                        .kodeverkId("T6")
                        .innsendingsValg(InnsendingsValg.IKKE_VALGT));

        SoknadBuilder soknad2 = SoknadBuilder.with()
                .soknadsId(UUID.randomUUID().toString())
                .skjema(SkjemaBuilder.forType(Type.HOVEDSKJEMA)
                        .tittel(kodeverk.getTittel("NAV 15-00.01"))
                        .beskrivelse(kodeverk.getKode("NAV 15-00.01", BESKRIVELSE))
                        .link(kodeverk.getKode("NAV 15-00.01", URL))
                        .skjemanummer("NAV 15-00.01")
                        .kodeverkId("NAV 15-00.01")
                        .innsendingsValg(InnsendingsValg.IKKE_VALGT)
                        .gosysId(kodeverk.getKode("NAV 15-00.01", GOSYS_ID)))
                .eksterntVedlegg(VedleggBuilder.forType(Type.EKSTERNT_VEDLEGG).tittel(kodeverk.getTittel("NAV 00-01.00")).kodeverkId("NAV 00-01.00").innsendingsValg(InnsendingsValg.IKKE_VALGT))
                .eksterntVedlegg(VedleggBuilder.forType(Type.EKSTERNT_VEDLEGG).tittel(kodeverk.getTittel("P4")).kodeverkId("P4").innsendingsValg(InnsendingsValg.IKKE_VALGT))
                .eksterntVedlegg(VedleggBuilder.forType(Type.EKSTERNT_VEDLEGG).tittel(kodeverk.getTittel("P3")).kodeverkId("P3").innsendingsValg(InnsendingsValg.IKKE_VALGT))
                .eksterntVedlegg(VedleggBuilder.forType(Type.EKSTERNT_VEDLEGG).tittel(kodeverk.getTittel("Q3")).kodeverkId("Q3").innsendingsValg(InnsendingsValg.IKKE_VALGT))
                .eksterntVedlegg(VedleggBuilder.forType(Type.EKSTERNT_VEDLEGG).tittel(kodeverk.getTittel("P2")).kodeverkId("P2").innsendingsValg(InnsendingsValg.IKKE_VALGT))
                .eksterntVedlegg(VedleggBuilder.forType(Type.EKSTERNT_VEDLEGG).tittel(kodeverk.getTittel("P1")).kodeverkId("P1").innsendingsValg(InnsendingsValg.IKKE_VALGT))
                .eksterntVedlegg(VedleggBuilder.forType(Type.EKSTERNT_VEDLEGG).tittel(kodeverk.getTittel("T8")).kodeverkId("T8").innsendingsValg(InnsendingsValg.IKKE_VALGT))
                .eksterntVedlegg(VedleggBuilder.forType(Type.EKSTERNT_VEDLEGG).tittel(kodeverk.getTittel("T7")).kodeverkId("T7").innsendingsValg(InnsendingsValg.IKKE_VALGT))
                .eksterntVedlegg(VedleggBuilder.forType(Type.EKSTERNT_VEDLEGG).tittel(kodeverk.getTittel("Q8")).kodeverkId("Q8").innsendingsValg(InnsendingsValg.IKKE_VALGT))
                .eksterntVedlegg(VedleggBuilder.forType(Type.EKSTERNT_VEDLEGG).tittel(kodeverk.getTittel("Q9")).kodeverkId("Q9").innsendingsValg(InnsendingsValg.IKKE_VALGT))
                .eksterntVedlegg(VedleggBuilder.forType(Type.EKSTERNT_VEDLEGG).tittel(kodeverk.getTittel("U2")).kodeverkId("U2").innsendingsValg(InnsendingsValg.IKKE_VALGT))
                .eksterntVedlegg(VedleggBuilder.forType(Type.EKSTERNT_VEDLEGG).tittel(kodeverk.getTittel("S2")).kodeverkId("S2").innsendingsValg(InnsendingsValg.IKKE_VALGT))
                .eksterntVedlegg(VedleggBuilder.forType(Type.EKSTERNT_VEDLEGG).tittel(kodeverk.getTittel("A1")).kodeverkId("A1").innsendingsValg(InnsendingsValg.IKKE_VALGT));

        SoknadBuilder soknad3 = SoknadBuilder.with()
                .soknadsId(UUID.randomUUID().toString())
                .skjema(SkjemaBuilder.forType(Type.HOVEDSKJEMA)
                        .tittel(kodeverk.getTittel("S8"))
                        .beskrivelse(kodeverk.getKode("S8", BESKRIVELSE))
                        .link(kodeverk.getKode("S8", URL))
                        .skjemanummer("S8")
                        .kodeverkId("S8")
                        .innsendingsValg(InnsendingsValg.IKKE_VALGT)
                        .gosysId(""))
                .navVedlegg(SkjemaBuilder.forType(Type.NAV_VEDLEGG)
                        .tittel(kodeverk.getTittel("NAV 34-00.15"))
                        .beskrivelse(kodeverk.getKode("NAV 34-00.15", BESKRIVELSE))
                        .link(kodeverk.getKode("NAV 34-00.15", URL))
                        .skjemanummer("NAV 34-00.15")
                        .kodeverkId("NAV 34-00.15")
                        .innsendingsValg(InnsendingsValg.IKKE_VALGT)
                        .gosysId(kodeverk.getKode("NAV 34-00.15", GOSYS_ID)))
                .eksterntVedlegg(VedleggBuilder.forType(Type.EKSTERNT_VEDLEGG).tittel(kodeverk.getTittel("W4")).kodeverkId("W4").innsendingsValg(InnsendingsValg.IKKE_VALGT))
                .eksterntVedlegg(VedleggBuilder.forType(Type.EKSTERNT_VEDLEGG).tittel(kodeverk.getTittel("P1")).kodeverkId("P1").innsendingsValg(InnsendingsValg.IKKE_VALGT))
                .eksterntVedlegg(VedleggBuilder.forType(Type.EKSTERNT_VEDLEGG).tittel(kodeverk.getTittel("R3")).kodeverkId("R3").innsendingsValg(InnsendingsValg.IKKE_VALGT))
                .eksterntVedlegg(VedleggBuilder.forType(Type.EKSTERNT_VEDLEGG).tittel(kodeverk.getTittel("R4")).kodeverkId("R4").innsendingsValg(InnsendingsValg.IKKE_VALGT))
                .eksterntVedlegg(VedleggBuilder.forType(Type.EKSTERNT_VEDLEGG).tittel(kodeverk.getTittel("O1")).kodeverkId("O1").innsendingsValg(InnsendingsValg.IKKE_VALGT))
                .eksterntVedlegg(VedleggBuilder.forType(Type.EKSTERNT_VEDLEGG).tittel(kodeverk.getTittel("P8")).kodeverkId("P8").innsendingsValg(InnsendingsValg.IKKE_VALGT));

        leggTilSoknad(soknad1.build());
        leggTilSoknad(soknad2.build());
        leggTilSoknad(soknad3.build());


    }

    public DokumentSoknad hentSoknadMedHovedskjema(String skjemanavn) {
        for (DokumentSoknad soknad : soknader.values()) {
            if (soknad.hovedskjema.getNavn().equals(skjemanavn)) {
                return soknad;
            }
        }
        return null;
    }

    public DokumentSoknad hentSoknadFraSoknadsId(String soknadsId) {
        return soknader.get(soknadsId);
    }

    public String lagreSoknad(DokumentSoknad soknad) {

        String soknadsId = UUID.randomUUID().toString();
        soknad.soknadsId = soknadsId;
        soknader.put(soknadsId, soknad);
        return soknadsId;
    }

    public void knyttSoknadTilBruker(String soknadsId, String ident) {
        DokumentSoknad soknad = hentSoknadFraSoknadsId(soknadsId);
        soknad.ident = ident;
    }

    public List<DokumentSoknad> getSoknadListe() {
        List<DokumentSoknad> soknadListe = new ArrayList<>();
        for (DokumentSoknad soknad : soknader.values()) {
            soknadListe.add(soknad);
        }
        return soknadListe;
    }

    private void leggTilSoknad(DokumentSoknad soknad) {
        soknader.put(soknad.soknadsId, soknad);
    }

}
