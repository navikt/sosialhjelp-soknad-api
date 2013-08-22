package no.nav.sbl.dialogarena.dokumentinnsending.pages.opplasting;

import no.nav.sbl.dialogarena.dokumentinnsending.domain.DokumentInnhold;
import org.apache.commons.collections15.Transformer;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.joda.time.DateTime;

import java.io.Serializable;
import java.util.List;

import static no.nav.modig.lang.collections.IterUtils.on;

/**
 * Klasse som holder en opplasting mens den lever i minnet.
 */
public class OpplastetFil implements Serializable {
    /**
     * Kan ikke bruke iterUtils.on fordi den kjører kun en dekorator. Vi må fjerne referansen til FileUpload slik at temp-filer blir slettet.
     */
    public final String name;
    public final byte[] innhold;
    public final long storrelse;
    public final byte[] md5;

    public OpplastetFil(String name, byte[] innhold, long storrelse, byte[] md5) {
        this.name = name;
        this.innhold = innhold.clone();
        this.storrelse = storrelse;
        this.md5 = md5.clone();
    }

    private static final Transformer<OpplastetFil, byte[]> OPPLASTER_TIL_BYTES = new Transformer<OpplastetFil, byte[]>() {
        @Override
        public byte[] transform(OpplastetFil opplastetFil) {
            return opplastetFil.innhold;
        }
    };
    public static final Transformer<FileUpload, OpplastetFil> OPPLASTET_FIL_TRANSFORMER = new Transformer<FileUpload, OpplastetFil>() {
        @Override
        public OpplastetFil transform(FileUpload fileUpload) {
            return new OpplastetFil(fileUpload.getClientFileName(), fileUpload.getBytes(), fileUpload.getSize(), fileUpload.getMD5());
        }
    };
    public static final Transformer<List<OpplastetFil>, DokumentInnhold> OPPLASTING_TIL_DOKUMENT = new Transformer<List<OpplastetFil>, DokumentInnhold>() {
        @Override
        public DokumentInnhold transform(List<OpplastetFil> uploads) {
            List<byte[]> bytes = on(uploads).map(OPPLASTER_TIL_BYTES).collect();
            DokumentInnhold dokumentInnhold = new DokumentInnhold();
            dokumentInnhold.settOgTransformerInnhold(bytes);
            dokumentInnhold.setOpplastetDato(new DateTime());
            return dokumentInnhold;
        }
    };
}
