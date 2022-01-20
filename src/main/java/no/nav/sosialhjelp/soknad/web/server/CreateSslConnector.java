//package no.nav.sosialhjelp.soknad.web.server;
//
//import org.eclipse.jetty.server.HttpConfiguration;
//import org.eclipse.jetty.server.HttpConnectionFactory;
//import org.eclipse.jetty.server.SecureRequestCustomizer;
//import org.eclipse.jetty.server.Server;
//import org.eclipse.jetty.server.ServerConnector;
//
//class CreateSslConnector {
//
//    private final Server jetty;
//    private final HttpConfiguration baseConfiguration;
//
//    CreateSslConnector(Server jetty, HttpConfiguration baseConfiguration) {
//        this.jetty = jetty;
//        this.baseConfiguration = baseConfiguration;
//    }
//
//    public ServerConnector transform(Integer sslPort) {
//        HttpConfiguration httpsConfiguration = new HttpConfiguration(baseConfiguration);
//        httpsConfiguration.setSecureScheme("https");
//        httpsConfiguration.setSecurePort(sslPort);
//        httpsConfiguration.addCustomizer(new SecureRequestCustomizer());
//
//        ServerConnector sslConnector = new ServerConnector(jetty,
//                new HttpConnectionFactory(httpsConfiguration));
//        sslConnector.setPort(sslPort);
//        return sslConnector;
//    }
//
//}