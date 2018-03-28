package no.nav.sbl.dialogarena.soknadinnsending.consumer.wsconfig;

import no.nav.sbl.dialogarena.types.Pingable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

@Configuration
public class EmailConfig {

    @Value("${dokumentinnsending.smtpServer.host}")
    private String host;

    @Value("${dokumentinnsending.smtpServer.port}")
    private int port = 25;

    private static final int SMTP_TIMEOUT = 3000;

    @Bean
    public Pingable pingable() {
        return new Pingable() {
            @Override
            public Ping ping () {
                SocketAddress socketAddress = new InetSocketAddress(host, port);
                Socket socket = new Socket();

                try {
                    socket.connect(socketAddress, SMTP_TIMEOUT);
                    return Ping.lyktes("Epost-utsending");

                } catch (IOException e) {
                    return Ping.feilet("Epost-utsending", e);
                } finally {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        Ping.feilet("Epost-utsending", e);
                    }
                }
            }
        };


    }
}
