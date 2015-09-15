package no.nav.sbl.dialogarena.soknadinnsending.business.transformer.refusjondagligreise;

import meldingsmodell.no.nav.melding.virksomhet.paaloepteutgifter.v1.paaloepteutgifter.PaaloepteUtgifter;
import meldingsmodell.no.nav.melding.virksomhet.paaloepteutgifter.v1.paaloepteutgifter.Utgiftsperioder;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.AlternativRepresentasjon;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;
import org.apache.commons.collections15.Transformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXB;
import java.io.ByteArrayOutputStream;
import java.util.UUID;

import static no.nav.modig.lang.collections.IterUtils.on;
import static no.nav.sbl.dialogarena.soknadinnsending.business.domain.FaktumPredicates.harPropertyMedValue;

public class RefusjonDagligreiseTilXml implements Transformer<WebSoknad, AlternativRepresentasjon>
{
    private static final Logger LOG = LoggerFactory.getLogger(RefusjonDagligreiseTilXml.class);

    private static final Transformer<Faktum, Utgiftsperioder> FAKTUM_TIL_UTGIFTSPERIODER = new Transformer<Faktum, Utgiftsperioder>() {
        @Override
        public Utgiftsperioder transform(Faktum faktum) {
            Utgiftsperioder utgiftsperioder = new Utgiftsperioder();
            utgiftsperioder.setBetalingsplanId(faktum.getProperties().get("id"));
            return null;
        }
    };

    private static PaaloepteUtgifter refusjonDagligreise(WebSoknad webSoknad) {
        PaaloepteUtgifter skjema = new PaaloepteUtgifter();
        Faktum vedtak = webSoknad.getFaktumMedKey("vedtak");

        skjema.setVedtaksId(vedtak.getProperties().get("id"));
        skjema.getUtgiftsperioder().addAll(on(webSoknad.getFaktaMedKey("vedtak.betalingsplan")).filter(harPropertyMedValue("registrert", "true")).map(FAKTUM_TIL_UTGIFTSPERIODER).collect());

        return null;
    }

    @Override
    public AlternativRepresentasjon transform(WebSoknad webSoknad) {
        PaaloepteUtgifter refusjonDagligreise = refusjonDagligreise(webSoknad);
        ByteArrayOutputStream xml = new ByteArrayOutputStream();
        JAXB.marshal(refusjonDagligreise, xml);
        return new AlternativRepresentasjon()
                .medMimetype("application/xml")
                .medFilnavn("RefusjonDagligreise.xml")
                .medUuid(UUID.randomUUID().toString())
                .medContent(xml.toByteArray());
    }
}
