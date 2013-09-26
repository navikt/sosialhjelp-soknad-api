package no.nav.sbl.dialogarena.person.consumer;

import no.nav.sbl.dialogarena.person.GjeldendeAdressetype;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLPostadressetyper;

import static no.nav.modig.lang.collections.TransformerUtils.asEnum;
import static no.nav.modig.lang.option.Optional.optional;
import static no.nav.sbl.dialogarena.person.consumer.transform.Transform.kodeverdi;


/**
 * Kodeverk for gjeldende adresse
 */
enum GjeldendeAdresseKodeverk {

    BOSTEDSADRESSE                   (GjeldendeAdressetype.FOLKEREGISTRERT),
    POSTADRESSE                      (GjeldendeAdressetype.FOLKEREGISTRERT),
    MIDLERTIDIG_POSTADRESSE_NORGE    (GjeldendeAdressetype.MIDLERTIDIG_NORGE),
    MIDLERTIDIG_POSTADRESSE_UTLAND   (GjeldendeAdressetype.MIDLERTIDIG_UTLAND),
    UKJENT_ADRESSE                   (GjeldendeAdressetype.UKJENT);

    final GjeldendeAdressetype mapping;

    final no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLPostadressetyper forLestjeneste =
            new no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLPostadressetyper().withValue(this.name());

    final no.nav.tjeneste.virksomhet.behandlebrukerprofil.v1.informasjon.XMLPostadressetyper forSkrivtjeneste =
            new no.nav.tjeneste.virksomhet.behandlebrukerprofil.v1.informasjon.XMLPostadressetyper().withValue(this.name());

    GjeldendeAdresseKodeverk(GjeldendeAdressetype mapping) {
        this.mapping = mapping;
    }

    static GjeldendeAdressetype somGjeldendeAdressetype(XMLPostadressetyper postadressetype) {
        return optional(postadressetype).map(kodeverdi()).map(asEnum(GjeldendeAdresseKodeverk.class)).getOrElse(UKJENT_ADRESSE).mapping;
    }

}
