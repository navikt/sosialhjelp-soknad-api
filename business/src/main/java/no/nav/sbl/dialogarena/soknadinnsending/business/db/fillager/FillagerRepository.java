package no.nav.sbl.dialogarena.soknadinnsending.business.db.fillager;

import java.util.List;

public interface FillagerRepository {

    void lagreFil(Fil fil);

    Fil hentFil(String uuid);

    void slettFil(String uuid);

    List<Fil> hentFiler(String behandlingsId);

    void slettAlle(String behandlingsId);


    class Fil {
        public final String behandlingsId;
        public final String uuid;
        public final byte[] data;
        public final String eier;
        public final String sha512;

        public Fil(String behandlingsId, String uuid, byte[] data, String eier, String sha512) {
            this.behandlingsId = behandlingsId;
            this.uuid = uuid;
            this.data = data;
            this.eier = eier;
            this.sha512 = sha512;
        }
    }

}
