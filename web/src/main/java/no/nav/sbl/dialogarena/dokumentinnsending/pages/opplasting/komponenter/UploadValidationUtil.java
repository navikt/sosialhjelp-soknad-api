package no.nav.sbl.dialogarena.dokumentinnsending.pages.opplasting.komponenter;

import no.nav.sbl.dialogarena.dokumentinnsending.pages.opplasting.OpplastetFil;
import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.lang.Bytes;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class UploadValidationUtil {

    public static final Bytes MAX_COMBINED_UPLOAD_SIZE = Bytes.megabytes(10);

    private static final String VALID_FILE_EXTENSIONS = "jpg,jpeg,png,pdf";


    public static void validateUploadedFiles(Collection<OpplastetFil> newUploads, Component component, Collection<OpplastetFil> uploadedFiles) {
        component.getFeedbackMessages().clear();
        checkFileContainsContent(newUploads, component);
        checkValidFileTypes(newUploads, component);
        checkCombinedFileSize(newUploads, component, uploadedFiles);
    }

    private static void checkFileContainsContent(Collection<OpplastetFil> newUploads, Component component) {
        if (newUploads == null || newUploads.isEmpty()) {
            String error = "${0}" + "validate.opplasting.ingenFil";
            component.error(error);
        }
    }

    private static void checkCombinedFileSize(Collection<OpplastetFil> newUploads, Component component, Collection<OpplastetFil> uploadedFiles) {
        long combinedSize = 0L;
        OpplastetFil sisteOpplastedeFil = null;

        combinedSize += regnUtTotalStorrelsePaaTidligereOpplastedeFiler(uploadedFiles);

        if (newUploads != null && !newUploads.isEmpty()) {
            for (OpplastetFil sisteOpplastetFil : newUploads) {
                combinedSize = leggTilStorrelsePaaSisteOpplastedeFil(combinedSize, sisteOpplastetFil);
                sisteOpplastedeFil = sisteOpplastetFil;
            }
        }

        if (!hasValidCombinedFileSize(combinedSize)) {
            String feil = uploadedFiles.isEmpty() ? "{0}" + component.getString("validate.opplasting.enkeltFilForStor", new Model<>(new String[]{hentFilnavnPaaSisteOpplastedeFil(sisteOpplastedeFil)}))
                    : "validate.opplasting.filerForStorKombinert";
            component.error(feil);
        }
    }

    private static long leggTilStorrelsePaaSisteOpplastedeFil(long combinedSize, OpplastetFil sisteOpplastetFil) {
        return combinedSize + sisteOpplastetFil.storrelse;
    }

    private static long regnUtTotalStorrelsePaaTidligereOpplastedeFiler(Collection<OpplastetFil> uploadedFiles) {
        long combinedSize = 0L;

        for (OpplastetFil tidligereOpplastedeFiler : uploadedFiles) {
            combinedSize += tidligereOpplastedeFiler.storrelse;
        }

        return combinedSize;
    }

    private static String hentFilnavnPaaSisteOpplastedeFil(OpplastetFil sisteOpplastedeFil) {
        return sisteOpplastedeFil != null ?
                sisteOpplastedeFil.name : "";
    }

    private static void checkValidFileTypes(Collection<OpplastetFil> newUploads, Component component) {
        if (newUploads != null && !newUploads.isEmpty()) {
            for (OpplastetFil upload : newUploads) {
                if (!hasValidFileTypeAndFileExtension(upload)) {
                    String error = component.getString("validate.opplasting.feilFilType", new Model<>(new String[]{upload.name}));
                    component.error(error);
                }
            }
        }
    }

    static boolean hasValidFileTypeAndFileExtension(OpplastetFil upload) {
        return new IsValidFileTypeForUpload().evaluate(upload.innhold) && hasValidFileExtension(upload.name);
    }

    static boolean hasValidCombinedFileSize(long combinedFileSize) {
        return combinedFileSize < MAX_COMBINED_UPLOAD_SIZE.bytes();
    }

    static boolean hasValidFileExtension(String filename) {
        String suffix = StringUtils.substringAfterLast(filename, ".");
        List<String> validFileExtensions = Arrays.asList(VALID_FILE_EXTENSIONS.split(","));
        return validFileExtensions.contains(suffix);
    }
}