package no.nav.sosialhjelp.soknad.consumer.common.cxf;

/* Originally from common-java-modules (no.nav.sbl.dialogarena.common.cxf) */

import org.apache.cxf.Bus;
import org.apache.cxf.feature.AbstractFeature;
import org.apache.cxf.interceptor.AttachmentInInterceptor;
import org.apache.cxf.interceptor.AttachmentOutInterceptor;
import org.apache.cxf.interceptor.InterceptorProvider;
import org.apache.cxf.interceptor.LoggingInInterceptor;

public class LoggingFeatureUtenBinaryOgUtenSamlTokenLogging extends AbstractFeature {

    private static final int DEFAULT_LIMIT = 64 * 1024;
    private static final LoggingInInterceptor IN = new LoggingInInterceptor(DEFAULT_LIMIT);
    private static final CXFMaskSAMLTokenLoggingOutInterceptor OUT = new CXFMaskSAMLTokenLoggingOutInterceptor(DEFAULT_LIMIT);
    static {
        IN.addAfter(AttachmentInInterceptor.class.getName());
        OUT.addAfter(AttachmentOutInterceptor.class.getName());
    }

    public LoggingFeatureUtenBinaryOgUtenSamlTokenLogging() {
    }

    @Override
    protected void initializeProvider(InterceptorProvider provider, Bus bus) {
        provider.getInInterceptors().add(IN);
        provider.getInFaultInterceptors().add(IN);
        provider.getOutInterceptors().add(OUT);
        provider.getOutFaultInterceptors().add(OUT);
    }
}
