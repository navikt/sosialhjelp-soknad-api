package no.nav.sbl.dialogarena.soknadinnsending.consumer.wsconfig;

import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLHovedskjema;
import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLMetadata;
import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLMetadataListe;
import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLVedlegg;
import no.nav.metrics.MetricsFactory;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.ServiceBuilder;
import no.nav.sbl.dialogarena.types.Pingable;
import no.nav.sbl.dialogarena.types.Pingable.Ping.PingMetadata;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.SendSoknadPortType;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.meldinger.WSSoknadsdata;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.meldinger.WSStartSoknadRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static no.nav.sbl.dialogarena.types.Pingable.Ping.*;

@Configuration
public class SendSoknadWSConfig {

    @Value("${soknad.webservice.henvendelse.sendsoknadservice.url}")
    private String soknadServiceEndpoint;

    private ServiceBuilder<SendSoknadPortType>.PortTypeBuilder<SendSoknadPortType> factory() {
        return new ServiceBuilder<>(SendSoknadPortType.class)
                .asStandardService()
                .withAddress(soknadServiceEndpoint)
                .withWsdl("classpath:SendSoknad.wsdl")
                .withExtraClasses(new Class[]{XMLMetadataListe.class, WSSoknadsdata.class, WSStartSoknadRequest.class, XMLMetadata.class, XMLVedlegg.class, XMLHovedskjema.class})
                .build()
                .withHttpsMock()
                .withMDC();
    }

    @Bean
    public SendSoknadPortType sendSoknadEndpoint() {
        return MetricsFactory.createTimerProxyForWebService("Sendsoknad", factory().withUserSecurity().get(), SendSoknadPortType.class);
    }

    @Bean
    public SendSoknadPortType sendSoknadSelftestEndpoint() {
        return factory().withSystemSecurity().get();
    }

    @Bean
    Pingable henvendelsePing() {
        return new Pingable() {
            @Override
            public Ping ping() {
                PingMetadata metadata = new PingMetadata(soknadServiceEndpoint,"Henvendelse - SoknadService: oprette og utføre operasjoner på påbegynte søknader", true);
                try {
                    sendSoknadSelftestEndpoint().ping();
                    return lyktes(metadata);
                } catch (Exception e) {
                    return feilet(metadata, e);
                }
            }
        };
    }
}
