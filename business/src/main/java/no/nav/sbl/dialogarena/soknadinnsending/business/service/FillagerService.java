package no.nav.sbl.dialogarena.soknadinnsending.business.service;


import no.nav.sbl.dialogarena.sendsoknad.domain.exception.AlleredeHandtertException;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.fillager.FillagerRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.fillager.FillagerRepository.Fil;
import org.apache.commons.io.IOUtils;
import org.bouncycastle.jcajce.provider.digest.SHA512;
import org.bouncycastle.util.encoders.Hex;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.io.InputStream;
import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;


@Component
public class FillagerService {

    private static final Logger logger = getLogger(FillagerService.class);

    @Inject
    FillagerRepository fillagerRepository;

    public void lagreFil(String behandlingsId, String uid, String fnr, InputStream fil) {
        logger.info("Skal lagre fil til fillager for behandlingsId {}. UUID: {}", behandlingsId, uid);
        SHA512.Digest digest = new SHA512.Digest();


        try {

            byte[] bytes = IOUtils.toByteArray(fil);
            digest.update(bytes);
            String sha = Hex.toHexString(digest.digest());

            fillagerRepository.lagreFil(new Fil(behandlingsId, uid, bytes, fnr, sha));
        } catch (Exception e) {
            logger.error("Kunne ikke lagre fil med uuid {}", uid, e);
            throw new AlleredeHandtertException();
        }
    }

    public byte[] hentFil(String uuid) {
        Fil fil = fillagerRepository.hentFil(uuid);
        if (fil == null) {
            logger.error("Fant ikke fil med uuid {}", uuid);
            throw new AlleredeHandtertException();
        }
        return fil.data;
    }

    public List<Fil> hentFiler(String brukerBehandlingId) {
        return fillagerRepository.hentFiler(brukerBehandlingId);
        // TODO sikkerhet?
    }

    public void slettAlle(String behandlingsId) {
        fillagerRepository.slettAlle(behandlingsId);
    }

    public void slettFil(String uuid) {
        fillagerRepository.slettFil(uuid);
    }
}
