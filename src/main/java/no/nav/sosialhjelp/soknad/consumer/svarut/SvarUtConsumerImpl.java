package no.nav.sosialhjelp.soknad.consumer.svarut;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.ks.fiks.svarut.klient.model.Dokument;
import no.ks.fiks.svarut.klient.model.Forsendelse;
import no.ks.fiks.svarut.klient.model.ForsendelsesId;
import no.nav.sosialhjelp.soknad.consumer.exceptions.TjenesteUtilgjengeligException;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.MultiPart;
import org.glassfish.jersey.media.multipart.file.StreamDataBodyPart;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import java.io.InputStream;
import java.util.Map;

public class SvarUtConsumerImpl implements SvarUtConsumer {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Client client;
    private final String baseUrl;

    public SvarUtConsumerImpl(String baseurl, Client client) {
        this.baseUrl = baseurl;
        this.client = client;
    }

    @Override
    public ForsendelsesId sendForsendelse(Forsendelse forsendelse, Map<String, InputStream> data) {
        if (forsendelse == null) throw new IllegalArgumentException("Forsendelse kan ikke være null");
        if (data == null) throw new IllegalArgumentException("Data kan ikke være null");
        try {
            WebTarget webTarget = client.target(baseUrl + "/tjenester/api/forsendelse/v1/sendForsendelse");

            MultiPart multiPart = new MultiPart();
            multiPart.setMediaType(MediaType.MULTIPART_FORM_DATA_TYPE);

            multiPart.bodyPart(new FormDataBodyPart("forsendelse", objectMapper.writeValueAsString(forsendelse), MediaType.APPLICATION_JSON_TYPE));
            for (Dokument dokument : forsendelse.getDokumenter()) {
                var bodypart = new StreamDataBodyPart("filer", data.get(dokument.getFilnavn()), dokument.getFilnavn(), MediaType.APPLICATION_OCTET_STREAM_TYPE);
                multiPart.bodyPart(bodypart);
            }
            multiPart.close();

            var response = webTarget
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .post(Entity.entity(multiPart, multiPart.getMediaType()), String.class);

            return objectMapper.readValue(response, ForsendelsesId.class);

        } catch (ClientErrorException e) {
            throw e;
        } catch (Exception e) {
            throw new TjenesteUtilgjengeligException("Noe feilet ved kall til SvarUt (rest)", e);
        }
    }

}
