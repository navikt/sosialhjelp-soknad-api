package no.nav.sbl.dialogarena.soknadinnsending.consumer.modigutils;//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import java.io.IOException;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import org.apache.commons.collections15.Factory;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HasReceivedPing implements Factory<Boolean>, Serializable {
    private static final Logger LOG = LoggerFactory.getLogger(HasReceivedPing.class);
    private boolean receivedPing = false;
    private final int port;

    public HasReceivedPing(int port) {
        this.port = port;
    }

    public Boolean create() {
        return this.receivedPing;
    }

    public Thread newMonitor() {
        Thread monitorThread = new Thread(new HasReceivedPing.Notifier());
        monitorThread.setDaemon(true);
        monitorThread.setName("Monitor-" + HasReceivedPing.class.getSimpleName() + "OnPort" + this.port);
        return monitorThread;
    }

    private class Notifier implements Runnable {
        private Notifier() {
        }

        public void run() {
            Socket socket = null;

            try {
                socket = (new ServerSocket(HasReceivedPing.this.port)).accept();
                HasReceivedPing.LOG.info("Received connection on port {}", HasReceivedPing.this.port);
            } catch (IOException var6) {
                throw new RuntimeException(var6.getMessage(), var6);
            } finally {
                IOUtils.closeQuietly(socket);
            }

            HasReceivedPing.this.receivedPing = true;
        }
    }
}
