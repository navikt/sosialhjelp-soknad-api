package no.nav.sbl.dialogarena.soknadinnsending.consumer;

import no.nav.sbl.dialogarena.soknadinnsending.consumer.arbeid.ArbeidssokerInfoService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.henvendelse.HenvendelseService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.person.EpostService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.person.PersonService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.personalia.PersonaliaFletter;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.personinfo.PersonInfoService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.wsconfig.*;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

import static java.lang.System.setProperty;

@Configuration
@EnableCaching
@Import({
        HenvendelseService.class,
        PersonService.class,
        PersonInfoService.class,
        EpostService.class,
        ArbeidssokerInfoService.class,
        ConsumerConfig.WsServices.class,
        PersonaliaFletter.class
})

public class ConsumerConfig {

    //Må godta så store xml-payloads pga Kodeverk postnr
    static {
        setProperty("org.apache.cxf.staxutils.innerElementCountThreshold", "70000");
    }

    @Configuration
    @Profile("!integration")
    @Import({
            SendSoknadWSConfig.class,
            FilLagerWSConfig.class,
            PersonInfoWSConfig.class,
            ArbeidWSConfig.class,
            OrganisasjonWSConfig.class,
            BrukerProfilWSConfig.class,
            DkifWSConfig.class,
            KodeverkWSConfig.class,
            PersonWSConfig.class,
            MaalgruppeWSConfig.class,
            SakOgAktivitetWSConfig.class,
            HenvendelseInformasjonConfig.class
    })
    public static class WsServices {
    }

}
