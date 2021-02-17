package no.nav.sbl.dialogarena.soknadinnsending.business.domain;

import no.nav.sosialhjelp.soknad.domain.Vedleggstatus;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class BehandlingsKjede {

    public InnsendtSoknad originalSoknad;
    public List<InnsendtSoknad> ettersendelser = new ArrayList<>();

    public BehandlingsKjede medOriginalSoknad(InnsendtSoknad originalSoknad) {
        this.originalSoknad = originalSoknad;
        return this;
    }

    public BehandlingsKjede medEttersendelser(Collection<InnsendtSoknad> ettersendelser) {
        this.ettersendelser.addAll(ettersendelser);
        return this;
    }

    public static class InnsendtSoknad {

        public String behandlingsId;
        public String innsendtDato;
        public String innsendtTidspunkt;
        public long soknadsalderIMinutter;
        public List<Vedlegg> innsendteVedlegg;
        public List<Vedlegg> ikkeInnsendteVedlegg;
        public String navenhet;
        public String orgnummer;

        public InnsendtSoknad medBehandlingId(String behandlingsId) {
            this.behandlingsId = behandlingsId;
            return this;
        }

        public InnsendtSoknad medInnsendtDato(String innsendtDato) {
            this.innsendtDato = innsendtDato;
            return this;
        }

        public InnsendtSoknad medInnsendtTidspunkt(String innsendtTidspunkt) {
            this.innsendtTidspunkt = innsendtTidspunkt;
            return this;
        }

        public InnsendtSoknad medSoknadsalderIMinutter(long soknadsalderIMinutter) {
            this.soknadsalderIMinutter = soknadsalderIMinutter;
            return this;
        }

        public InnsendtSoknad medInnsendteVedlegg(List<Vedlegg> innsendteVedlegg) {
            this.innsendteVedlegg = innsendteVedlegg;
            return this;
        }

        public InnsendtSoknad medIkkeInnsendteVedlegg(List<Vedlegg> ikkeInnsendteVedlegg) {
            this.ikkeInnsendteVedlegg = ikkeInnsendteVedlegg;
            return this;
        }

        public InnsendtSoknad medNavenhet(String navenhet) {
            this.navenhet = navenhet;
            return this;
        }

        public InnsendtSoknad medOrgnummer(String orgnummer) {
            this.orgnummer = orgnummer;
            return this;
        }

        public static class Vedlegg {
            public String skjemaNummer;
            public String skjemanummerTillegg;
            public Vedleggstatus innsendingsvalg;

            public Vedlegg() {
            }

            public Vedlegg(String skjemaNummer, String skjemanummerTillegg, Vedleggstatus innsendingsvalg) {
                this.skjemaNummer = skjemaNummer;
                this.skjemanummerTillegg = skjemanummerTillegg;
                this.innsendingsvalg = innsendingsvalg;
            }
        }
    }

}
