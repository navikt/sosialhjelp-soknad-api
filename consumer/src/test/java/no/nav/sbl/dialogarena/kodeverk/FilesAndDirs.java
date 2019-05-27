package no.nav.sbl.dialogarena.kodeverk;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

public final class FilesAndDirs {

    static {
        try {
            File classesDir = new File(FilesAndDirs.class.getResource("/").toURI());
            PROJECT_BASEDIR = new File(classesDir, "../../").getCanonicalFile();
        } catch (URISyntaxException | IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public static final File PROJECT_BASEDIR;

    public static final File TEST_RESOURCES = new File(PROJECT_BASEDIR, "src/test/resources");

    public static final File RESOURCES = new File(PROJECT_BASEDIR, "src/main/resources");

    public static final File WEBAPP_SOURCE = new File(PROJECT_BASEDIR, "src/main/webapp");

    public static final File BUILD_OUTPUT = new File(PROJECT_BASEDIR, "target");

    public static final File POM_XML = new File(PROJECT_BASEDIR, "pom.xml");

    private FilesAndDirs() {
    }

    static {
        new FilesAndDirs();
    }
}
