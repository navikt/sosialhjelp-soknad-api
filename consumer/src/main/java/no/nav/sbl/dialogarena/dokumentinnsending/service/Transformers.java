package no.nav.sbl.dialogarena.dokumentinnsending.service;

import no.nav.modig.core.exception.ApplicationException;
import no.nav.sbl.dialogarena.common.kodeverk.Kodeverk;
import no.nav.sbl.dialogarena.dokumentinnsending.domain.Dokument;
import no.nav.sbl.dialogarena.dokumentinnsending.domain.Dokument.Type;
import no.nav.sbl.dialogarena.dokumentinnsending.domain.DokumentInnhold;
import no.nav.sbl.dialogarena.dokumentinnsending.domain.Skjema;
import no.nav.sbl.dialogarena.dokumentinnsending.kodeverk.KodeverkClient;
import no.nav.sbl.dialogarena.soknad.kodeverk.KodeverkSkjema;
import no.nav.tjeneste.domene.brukerdialog.henvendelse.v1.informasjon.WSBrukerBehandlingOppsummering;
import no.nav.tjeneste.domene.brukerdialog.henvendelse.v1.informasjon.WSDokument;
import no.nav.tjeneste.domene.brukerdialog.henvendelse.v1.informasjon.WSDokumentForventning;
import no.nav.tjeneste.domene.brukerdialog.henvendelse.v1.informasjon.WSInnsendingsValg;
import org.apache.commons.collections15.Transformer;

import javax.activation.DataHandler;
import javax.mail.util.ByteArrayDataSource;
import java.io.IOException;
import java.io.InputStream;

import static no.nav.sbl.dialogarena.dokumentinnsending.domain.InnsendingsValg.valueOf;
import static org.apache.commons.io.IOUtils.toByteArray;
import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;
import static org.apache.commons.lang3.StringUtils.startsWith;

/**
 * Klasse som inneholder uliker typer av transformere mellom WS-objekter og domene-objekter og vice versa
 */
public final class Transformers {

    public static final Transformer<WSBrukerBehandlingOppsummering, String> BEHANDLINGSID = new Transformer<WSBrukerBehandlingOppsummering, String>() {
        @Override
        public String transform(WSBrukerBehandlingOppsummering brukerBehandling) {
            return brukerBehandling.getBehandlingsId();
        }
    };

    public static final Transformer<String, WSDokumentForventning> TIL_DOKUMENTFORVENTNING = new Transformer<String, WSDokumentForventning>() {
        @Override
        public WSDokumentForventning transform(String id) {
            WSDokumentForventning forventning = new WSDokumentForventning();
            forventning.setKodeverkId(id);
            forventning.setInnsendingsValg(WSInnsendingsValg.IKKE_VALGT);
            forventning.setHovedskjema(false);
            return forventning;
        }
    };

    public static Transformer<WSDokumentForventning, Dokument> tilDokument(final KodeverkClient kodeverk, final String behandlingsId) {
        return new Transformer<WSDokumentForventning, Dokument>() {
            @Override
            public Dokument transform(WSDokumentForventning forventning) {
                Dokument dokument;
                KodeverkSkjema skjema;

                if (forventning.isHovedskjema()) {
                    skjema = kodeverk.hentKodeverkSkjemaForSkjemanummer(forventning.getKodeverkId());
                } else {
                    skjema = kodeverk.hentKodeverkSkjemaForVedleggsid(forventning.getKodeverkId());
                }
                dokument = byggOppDokument(forventning, skjema, behandlingsId);
                return dokument;
            }
        };
    }

    //TODO: fikse det slik at NAVvedlegg ikke hentes ut på den måten, men ved hjelp av tosifret kodeid.
    static Dokument byggOppDokument(WSDokumentForventning forventning, KodeverkSkjema skjema, String behandlingsIden) {
        Dokument dokument;
        if (forventning.isHovedskjema()) {
            dokument = tilSkjema(Type.HOVEDSKJEMA).transform(skjema);
        } else if (equalsIgnoreCase(forventning.getKodeverkId(), Kodeverk.ANNET)) {
            dokument = new Dokument(Type.EKSTRA_VEDLEGG);
            dokument.setNavn(skjema.getTittel() + ": " + forventning.getFriTekst());
        } else if (startsWith(forventning.getKodeverkId(), "NAV")) {
            dokument = tilSkjema(Type.NAV_VEDLEGG).transform(skjema);
        } else {
            dokument = new Dokument(Type.EKSTERNT_VEDLEGG);
            dokument.setNavn(skjema.getTittel());
        }
        dokument.setKodeverk(skjema);
        dokument.setKodeverkId(forventning.getKodeverkId());
        dokument.setBehandlingsId(behandlingsIden);

        dokument.setLink(skjema.getUrl());
        dokument.setDokumentForventningsId(forventning.getId());
        dokument.setInnsendingsvalg(valueOf(forventning.getInnsendingsValg().name()));
        dokument.setDokumentId(forventning.getDokumentId());
        return dokument;
    }

    private static Transformer<KodeverkSkjema, Skjema> tilSkjema(final Skjema.Type type) {
        return new Transformer<KodeverkSkjema, Skjema>() {
            @Override
            public Skjema transform(KodeverkSkjema kodeverkSkjema) {
                Skjema hovedskjema = new Skjema(type, "");
                hovedskjema.setNavn(kodeverkSkjema.getTittel());
                hovedskjema.setLink(kodeverkSkjema.getUrl());
                return hovedskjema;
            }
        };
    }

    public static final Transformer<DokumentInnhold, WSDokument> DOKUMENT_TIL_WS_DOKUMENT = new Transformer<DokumentInnhold, WSDokument>() {
        @Override
        public WSDokument transform(DokumentInnhold dokument) {
            WSDokument dok = new WSDokument();
            dok.setFilnavn(dokument.getNavn());
            dok.setOpplastetDato(dokument.getOpplastetDato());
            dok.setInnhold(new DataHandler(new ByteArrayDataSource(dokument.hentInnholdSomBytes(), "application/octet-stream")));
            return dok;
        }
    };

    public static final Transformer<WSDokument, DokumentInnhold> WS_DOKUMENT_TIL_DOKUMENT_INNHOLD = new Transformer<WSDokument, DokumentInnhold>() {
        @Override
        public DokumentInnhold transform(WSDokument dokument) {
            DokumentInnhold dok = new DokumentInnhold();
            dok.setId(dokument.getId());
            dok.setNavn(dokument.getFilnavn());
            dok.setOpplastetDato(dokument.getOpplastetDato());
            try (InputStream dokumentInputStream = dokument.getInnhold().getInputStream()) {
                dok.setInnhold(toByteArray(dokumentInputStream));
            } catch (IOException e) {
                throw new ApplicationException("Kunne ikke hente attachment fra wsDokument.", e);
            }
            return dok;
        }
    };
}