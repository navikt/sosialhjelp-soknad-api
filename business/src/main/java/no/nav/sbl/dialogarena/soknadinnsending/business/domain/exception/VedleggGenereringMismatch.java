package no.nav.sbl.dialogarena.soknadinnsending.business.domain.exception;

import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Vedlegg;
import no.nav.sbl.dialogarena.soknadinnsending.business.util.VedleggsgenereringUtil;
import org.apache.commons.collections15.Predicate;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

import static no.nav.modig.lang.collections.IterUtils.on;

@XmlRootElement
public class VedleggGenereringMismatch extends Throwable {

    private final Mismatch mismatch;

    public VedleggGenereringMismatch(final List<Vedlegg> orginaleVedlegg, final List<Vedlegg> nyVedleggslogikk) {
        super("Mismatch mellom ny og gammel generering av vedlegg");

        final List<Vedlegg> vedleggSomMangler = on(orginaleVedlegg).filter(new Predicate<Vedlegg>() {
            @Override
            public boolean evaluate(Vedlegg vedlegg) {
                return !VedleggsgenereringUtil.likeVedlegg(nyVedleggslogikk, vedlegg);
            }
        }).collect();

        List<Vedlegg> vedleggGenerertEkstra = on(nyVedleggslogikk).filter(new Predicate<Vedlegg>() {
            @Override
            public boolean evaluate(Vedlegg vedlegg) {
                return !VedleggsgenereringUtil.likeVedlegg(orginaleVedlegg,vedlegg);
            }
        }).collect();

        this.mismatch = new Mismatch(this.getMessage(), orginaleVedlegg, nyVedleggslogikk, vedleggSomMangler, vedleggGenerertEkstra);
    }

    public Mismatch getMismatch() {
        return mismatch;
    }

    public class Mismatch {

        private final String message;
        private final List<Vedlegg> orginaleVedlegg;
        private final List<Vedlegg> nyVedleggslogikk;
        private final List<Vedlegg> vedleggSomMangler;
        private final List<Vedlegg> vedleggGenerertEkstra;

        public Mismatch(String message, List<Vedlegg> orginaleVedlegg, List<Vedlegg> nyVedleggslogikk, List<Vedlegg> vedleggSomMangler, List<Vedlegg> vedleggGenerertEkstra) {
            this.message = message;
            this.orginaleVedlegg = orginaleVedlegg;
            this.nyVedleggslogikk = nyVedleggslogikk;
            this.vedleggSomMangler = vedleggSomMangler;
            this.vedleggGenerertEkstra = vedleggGenerertEkstra;
        }

        public String getMessage() {
            return message;
        }

        public List<Vedlegg> getOrginaleVedlegg() {
            return orginaleVedlegg;
        }

        public List<Vedlegg> getNyVedleggslogikk() {
            return nyVedleggslogikk;
        }

        public List<Vedlegg> getVedleggSomMangler() {
            return vedleggSomMangler;
        }

        public List<Vedlegg> getVedleggGenerertEkstra() {
            return vedleggGenerertEkstra;
        }
    }

}
