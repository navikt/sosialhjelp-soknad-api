package no.nav.sbl.dialogarena.soknadinnsending.consumer.wsconfig;

import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.*;
import no.nav.sbl.dialogarena.sendsoknad.mockmodul.tjenester.HenvendelseInformasjonMock;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.ServiceBuilder;
import no.nav.sbl.dialogarena.types.Pingable;
import no.nav.tjeneste.domene.brukerdialog.henvendelse.v2.henvendelse.HenvendelsePortType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static no.nav.sbl.dialogarena.soknadinnsending.consumer.util.InstanceSwitcher.createSwitcher;
import static no.nav.sbl.dialogarena.types.Pingable.Ping.feilet;
import static no.nav.sbl.dialogarena.types.Pingable.Ping.lyktes;

@Configuration
public class HenvendelseInformasjonConfig {

    private static final String TILLATHENVENDELSEMOCK_PROPERTY = "start.henvendelseinformasjon.withmock";

    @Bean
    public HenvendelsePortType henvendelseSoknaderPortType() {
        final HenvendelsePortType prod = factory().withUserSecurity().withMDC().get();
        final HenvendelsePortType mock = HenvendelseInformasjonMock.getHenvendelseSoknaderPortTypeMock();

        return createSwitcher(prod, mock, TILLATHENVENDELSEMOCK_PROPERTY, HenvendelsePortType.class);
    }

    private ServiceBuilder<HenvendelsePortType>.PortTypeBuilder<HenvendelsePortType> factory() {
        return new ServiceBuilder<>(HenvendelsePortType.class)
                .asStandardService()
                .withAddress(System.getProperty("soknad.webservice.henvendelse.informasjonservice.url"))
                .withWsdl("classpath:Henvendelse.wsdl")
                .withExtraClasses(new Class[]{
                        XMLHenvendelse.class,
                        XMLMetadataListe.class,
                        XMLJournalfortInformasjon.class,
                        XMLMetadata.class,
                        XMLHenvendelseType.class,
                        XMLHovedskjema.class,
                        XMLVedlegg.class})
                .build()
                .withHttpsMock();
    }


    @Bean
    public Pingable henvendelseSoknaderPing() {
        final HenvendelsePortType ws = factory().withSystemSecurity().get();
        return new Pingable() {
            @Override
            public Ping ping() {
                try {
                    ws.ping();
                    return lyktes("Henvendelse");
                } catch (Exception e) {
                    return feilet("Henvendelse", e);
                }
            }
        };
    }

}
