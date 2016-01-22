package no.nav.sbl.dialogarena.soknadinnsending.consumer;

import no.nav.sbl.dialogarena.soknadinnsending.consumer.arbeid.*;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.fillager.*;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.henvendelse.*;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.person.*;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.personalia.*;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.personinfo.*;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.wsconfig.*;
import org.springframework.cache.annotation.*;
import org.springframework.context.annotation.*;

import static java.lang.System.*;

@Configuration
@EnableCaching
@Import({
        FillagerService.class,
        HenvendelseService.class,
        PersonService.class,
        PersonInfoService.class,
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
            KodeverkWSConfig.class,
            PersonWSConfig.class,
            MaalgruppeWSConfig.class,
            SakOgAktivitetWSConfig.class
    })
    public static class WsServices {
    }

}
