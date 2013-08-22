package no.nav.sbl.dialogarena.dokumentinnsending.cleanup;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class UploadCleanerContextListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        //File fileStoreFolder = (File)servletContextEvent.getServletContext().getAttribute("javax.servlet.context.tempdir");

        //FileUtils.deleteDirectory();
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {

    }
}
