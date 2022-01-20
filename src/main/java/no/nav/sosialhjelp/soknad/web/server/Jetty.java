//package no.nav.sosialhjelp.soknad.web.server;
//
//import io.prometheus.client.exporter.MetricsServlet;
//import org.eclipse.jetty.plus.webapp.EnvConfiguration;
//import org.eclipse.jetty.plus.webapp.PlusConfiguration;
//import org.eclipse.jetty.server.Connector;
//import org.eclipse.jetty.server.HttpConfiguration;
//import org.eclipse.jetty.server.HttpConnectionFactory;
//import org.eclipse.jetty.server.Server;
//import org.eclipse.jetty.server.ServerConnector;
//import org.eclipse.jetty.servlet.ServletContextHandler;
//import org.eclipse.jetty.servlet.ServletHolder;
//import org.eclipse.jetty.util.resource.Resource;
//import org.eclipse.jetty.webapp.FragmentConfiguration;
//import org.eclipse.jetty.webapp.JettyWebXmlConfiguration;
//import org.eclipse.jetty.webapp.MetaInfConfiguration;
//import org.eclipse.jetty.webapp.WebAppContext;
//import org.eclipse.jetty.webapp.WebInfConfiguration;
//import org.eclipse.jetty.webapp.WebXmlConfiguration;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import javax.naming.NamingException;
//import javax.sql.DataSource;
//import java.io.File;
//import java.io.IOException;
//import java.net.InetAddress;
//import java.net.MalformedURLException;
//import java.net.URL;
//import java.net.URLClassLoader;
//import java.net.UnknownHostException;
//import java.util.Collections;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.Optional;
//
//import static org.apache.commons.io.FilenameUtils.getBaseName;
//import static org.apache.commons.lang3.StringUtils.isBlank;
//import static org.apache.commons.lang3.StringUtils.isNotBlank;
//
//
///**
// * Brukes til å starte opp en embedded Jetty-server, både synkront og asynkront.
// */
//public final class Jetty {
//
//    private static final Logger LOG = LoggerFactory.getLogger(Jetty.class);
//
//    public static JettyBuilder usingWar(File file) {
//        return new JettyBuilder().war(file);
//    }
//
//    /**
//     * Builder for å konfigurere opp en Jetty-instans.
//     */
//    public static class JettyBuilder {
//        private File war;
//        private String contextPath;
//        private int port = 35000;
//        private Optional<Integer> sslPort = Optional.empty();
//        private WebAppContext context;
//        private File overridewebXmlFile;
//        private Map<String, DataSource> dataSources = new HashMap<>();
//
//
//        public final JettyBuilder war(File warPath) {
//            this.war = warPath;
//            return this;
//        }
//
//        public final JettyBuilder at(String ctxPath) {
//            this.contextPath = ctxPath;
//            return this;
//        }
//
//        public final JettyBuilder port(int jettyPort) {
//            this.port = jettyPort;
//            return this;
//        }
//
//        public final JettyBuilder sslPort(int sslPort) {
//            this.sslPort = Optional.of(sslPort);
//            return this;
//        }
//
//        public final JettyBuilder overrideWebXml(File overrideWebXmlFile) {
//            this.overridewebXmlFile = overrideWebXmlFile;
//            return this;
//        }
//
//        public final JettyBuilder addDatasource(DataSource dataSource, String jndiName) {
//            dataSources.put(jndiName, dataSource);
//            return this;
//        }
//
//
//        public final Jetty buildJetty() {
//            try {
//                if (context == null) {
//                    context = new WebAppContext();
//                }
//
//                if (war == null) {
//                    useWebapp(context);
//                    return new Jetty(null, this);
//                } else {
//                    String warPath = getWarPath();
//                    if (isBlank(contextPath)) {
//                        contextPath = getBaseName(warPath);
//                    }
//                    return new Jetty(warPath, this);
//                }
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//        }
//
//        private static void useWebapp(WebAppContext webAppContext) {
//            if (isDevEnviroment()) {
//                if (new File("src/main/webapp").exists()) {
//                    webAppContext.setResourceBase("src/main/webapp");
//                } else {
//                    webAppContext.setResourceBase("src/main/webapp");
//                }
//            } else {
//                webAppContext.setWar("/app");
//            }
//        }
//
//        private static boolean isDevEnviroment() {
//            return new File("pom.xml").exists();
//        }
//
//        private String getWarPath() throws IOException {
//            if (war != null) {
//                return war.getCanonicalPath();
//            } else {
//                return context.getWar();
//            }
//        }
//
//    }
//
//
//    private final int port;
//    private final Optional<Integer> sslPort;
//    private final File overrideWebXmlFile;
//    private final String warPath;
//    private final String contextPath;
//    public final Server server;
//    public final WebAppContext context;
//    private final Map<String, DataSource> dataSources;
//
//
//    public final Runnable stop = new Runnable() {
//        @Override
//        public void run() {
//            try {
//                server.stop();
//                server.join();
//                LOG.info("JETTY STOPPED");
//            } catch (Exception e) {
//                throw new RuntimeException(e.getMessage(), e);
//            }
//        }
//    };
//
//    private static final String[] CONFIGURATION_CLASSES = {
//            WebInfConfiguration.class.getName(),
//            WebXmlConfiguration.class.getName(),
//            MetaInfConfiguration.class.getName(),
//            FragmentConfiguration.class.getName(),
//            JettyWebXmlConfiguration.class.getName(),
//            EnvConfiguration.class.getName(),
//            PlusConfiguration.class.getName()
//    };
//
//    private Jetty(String warPath, JettyBuilder builder) {
//        this.warPath = warPath;
//        this.overrideWebXmlFile = builder.overridewebXmlFile;
//        this.dataSources = builder.dataSources;
//        this.port = builder.port;
//        this.sslPort = builder.sslPort;
//        this.contextPath = (builder.contextPath.startsWith("/") ? "" : "/") + builder.contextPath;
//        this.context = setupWebapp(builder.context);
//        this.server = setupJetty(new Server());
//    }
//
//    private WebAppContext setupWebapp(final WebAppContext webAppContext) {
//        if (isNotBlank(contextPath)) {
//            webAppContext.setContextPath(contextPath);
//        }
//        if (isNotBlank(warPath)) {
//            webAppContext.setWar(warPath);
//        }
//        if (overrideWebXmlFile != null) {
//            webAppContext.setOverrideDescriptor(overrideWebXmlFile.getAbsolutePath());
//        }
//
//        webAppContext.setConfigurationClasses(CONFIGURATION_CLASSES);
//        Map<String, String> initParams = webAppContext.getInitParams();
//        initParams.put("useFileMappedBuffer", "false");
//        initParams.put("org.eclipse.jetty.servlet.SessionIdPathParameterName", "none"); // Forhindre url rewriting av sessionid
//
//        if (!dataSources.isEmpty()) {
//            for (Map.Entry<String, DataSource> entrySet : dataSources.entrySet()) {
//                try {
//                    new org.eclipse.jetty.plus.jndi.Resource(webAppContext, entrySet.getKey(), entrySet.getValue());
//                } catch (NamingException e) {
//                    throw new RuntimeException("Kunne ikke legge til datasource " + e, e);
//                }
//            }
//        }
//        // When we embed jetty in this way, some classes might be loaded by the default WebAppClassLoader and some by the system class loader.
//        // These classes will be incompatible with each other. Also, Jetty does not consult the classloader of the webapp when resolving resources
//        // such as the swagger-ui. We mitigate both these problems by installing an empty classloader that will always defer to the system classloader
//        webAppContext.setClassLoader(URLClassLoader.newInstance(new URL[0]));
//
//        return webAppContext;
//    }
//
//
//    private Server setupJetty(final Server jetty) {
//
//        Resource.setDefaultUseCaches(false);
//
//        HttpConfiguration configuration = new HttpConfiguration();
//        configuration.setOutputBufferSize(32768);
//        configuration.setRequestHeaderSize(16384);
//
//        ServerConnector httpConnector = new ServerConnector(jetty, new HttpConnectionFactory(configuration));
//        httpConnector.setPort(port);
//
//        if (sslPort.isPresent()) {
//            jetty.setConnectors(new Connector[]{httpConnector, new CreateSslConnector(jetty, configuration).transform(sslPort.get())});
//        } else {
//            jetty.setConnectors(new Connector[]{httpConnector});
//        }
//
//        registerMetricsServlet(context);
//
//        context.setServer(jetty);
//        jetty.setHandler(context);
//        return jetty;
//    }
//
//    public Jetty start() {
//        return startAnd(() -> {
//
//        });
//    }
//
//    public Jetty startAnd(Runnable doWhenStarted) {
//        try {
//            server.start();
//            LOG.info(getStatusString());
//            doWhenStarted.run();
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//        return this;
//    }
//
//    private String getStatusString() {
//        final StringBuilder statusBuilder = new StringBuilder(
//                    "\n STARTED JETTY"
//                    + "\n * WAR: " + warPath
//                    + "\n * Context path: " + contextPath
//                    + "\n * Http port: " + port
//                    + "\n * Localhost url: " + "http://localhost:" + port + contextPath
//                    );
//        sslPort.ifPresent(integer -> statusBuilder.append("\n * Https port: ").append(integer));
//        for (URL url : getBaseUrls()) {
//            statusBuilder.append("\n * Url: ").append(url);
//        }
//        return statusBuilder.toString();
//    }
//
//    public Iterable<URL> getBaseUrls() {
//        return sslPort
//                .map(integer -> Collections.singletonList(toUrl("https", contextPath, integer)))
//                .orElseGet(() -> Collections.singletonList(toUrl("http", contextPath, port)));
//    }
//
//    private void registerMetricsServlet(final ServletContextHandler context) {
//        final ServletHolder metricsServlet = new ServletHolder(new MetricsServlet());
//        context.addServlet(metricsServlet, "/internal/metrics/*");
//    }
//
//    private URL toUrl(String scheme, String path, Integer port) {
//        try {
//            return new URL(scheme + "://" + InetAddress.getLocalHost().getCanonicalHostName() + ":" + port + path);
//        } catch (MalformedURLException | UnknownHostException e) {
//            throw new RuntimeException(e);
//        }
//    }
//}
