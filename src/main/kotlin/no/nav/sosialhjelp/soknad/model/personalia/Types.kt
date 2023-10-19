package no.nav.sosialhjelp.soknad.model.personalia

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

internal val mapper = jacksonObjectMapper()

enum class AdresseValg {
    FOLKEREGISTRERT, OPPHOLD, SOKNAD
}

interface AdresseTypeMapper {
    fun mapJsonToAdresse(adresseJson: String): AdresseObject
}

enum class AdresseType: AdresseTypeMapper {

        GATEADRESSE {
            override fun mapJsonToAdresse(adresseJson: String): AdresseObject {
                return mapper.readValue(adresseJson, GateAdresseObject::class.java)
            }
        },
        MATRIKKELADRESSE {
            override fun mapJsonToAdresse(adresseJson: String): AdresseObject {
                return mapper.readValue(adresseJson, MatrikkelAdresseObject::class.java)
            }
        },
        POSTBOKSADRESSE {
            override fun mapJsonToAdresse(adresseJson: String): AdresseObject {
                return mapper.readValue(adresseJson, PostboksAdresseObject::class.java)

            }
        },
        USTRUKTURERT {
            override fun mapJsonToAdresse(adresseJson: String): AdresseObject {
                return mapper.readValue(adresseJson, UstrukturertAdresseObject::class.java)
            }
        };
}