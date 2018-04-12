package no.nav.sbl.dialogarena.soknadinnsending.consumer.wsconfig;

import no.nav.sbl.dialogarena.types.Pingable;
import no.nav.sbl.dialogarena.types.Pingable.Ping.PingMetadata;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

import static no.nav.sbl.dialogarena.types.Pingable.Ping.feilet;
import static no.nav.sbl.dialogarena.types.Pingable.Ping.lyktes;

@Configuration
public class EmailConfig {

    @Value("${dokumentinnsending.smtpServer.host}")
    private String host;

    @Value("${dokumentinnsending.smtpServer.port}")
    private int port;

    private static final int SMTP_TIMEOUT = 3000;

    @Bean
    public Pingable pingable() {
        return new Pingable() {
            @Override
            @Cacheable("emailPingCache")
            public Ping ping () {
                SocketAddress socketAddress = new InetSocketAddress(host, port);
                Socket socket = new Socket();
                Ping pingResultat;
                PingMetadata metadata = new PingMetadata(host, "SMTP-server for mail-utsending", false);

                try {
                    socket.connect(socketAddress, SMTP_TIMEOUT);
                    pingResultat = lyktes(metadata);
                } catch (IOException e) {
                    pingResultat = feilet(metadata, e);
                } finally {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        pingResultat = feilet(metadata, e);
                    }
                }
                return pingResultat;
            }
        };


    }
}
