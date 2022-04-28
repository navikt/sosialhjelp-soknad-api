package no.nav.sosialhjelp.soknad.innsending.svarut

import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.DERNull
import org.bouncycastle.asn1.DEROctetString
import org.bouncycastle.asn1.nist.NISTObjectIdentifiers
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers
import org.bouncycastle.asn1.pkcs.RSAESOAEPparams
import org.bouncycastle.asn1.x509.AlgorithmIdentifier
import org.bouncycastle.cms.CMSAlgorithm
import org.bouncycastle.cms.CMSEnvelopedDataGenerator
import org.bouncycastle.cms.CMSException
import org.bouncycastle.cms.CMSProcessableByteArray
import org.bouncycastle.cms.jcajce.JceCMSContentEncryptorBuilder
import org.bouncycastle.cms.jcajce.JceKeyTransRecipientInfoGenerator
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.io.IOException
import java.security.Security
import java.security.cert.CertificateEncodingException
import java.security.cert.CertificateException
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import javax.annotation.PostConstruct

@Component
class DokumentKrypterer(
    @Value("\${fiks.nokkelfil}") private val fiksNokkelfil: String?,
) {
    private var encryptionScheme: AlgorithmIdentifier? = null
    private var cms: ASN1ObjectIdentifier? = null
    private var certificate: X509Certificate? = null

    @PostConstruct
    fun settOppKryptering() {
        Security.addProvider(BouncyCastleProvider())
        encryptionScheme = lagAlgoritmeKonfigurasjon()
        cms = CMSAlgorithm.AES256_CBC
        certificate = lagFiksCertificate()
    }

    fun krypterData(data: ByteArray?): ByteArray {
        return try {
            val recipient = JceKeyTransRecipientInfoGenerator(certificate, encryptionScheme).setProvider("BC")
            val enveloped = CMSEnvelopedDataGenerator()
            enveloped.addRecipientInfoGenerator(recipient)
            val encryptor = JceCMSContentEncryptorBuilder(cms).build()
            val cmsData = enveloped.generate(CMSProcessableByteArray(data), encryptor)
            cmsData.encoded
        } catch (e: CertificateEncodingException) {
            logger.error("Noe feilet under kryptering", e)
            throw RuntimeException(e)
        } catch (e: IOException) {
            logger.error("Noe feilet under kryptering", e)
            throw RuntimeException(e)
        } catch (e: CMSException) {
            logger.error("Kunne ikke kryptere. Om den klager på 'illegal key size' er det fordi java cryptography extension mangler", e)
            throw RuntimeException(e)
        }
    }

    private fun lagAlgoritmeKonfigurasjon(): AlgorithmIdentifier {
        val hash = AlgorithmIdentifier(NISTObjectIdentifiers.id_sha256, DERNull.INSTANCE)
        val mask = AlgorithmIdentifier(PKCSObjectIdentifiers.id_mgf1, hash)
        val pSource = AlgorithmIdentifier(PKCSObjectIdentifiers.id_pSpecified, DEROctetString(ByteArray(0)))
        val params = RSAESOAEPparams(hash, mask, pSource)
        return AlgorithmIdentifier(PKCSObjectIdentifiers.id_RSAES_OAEP, params)
    }

    private fun lagFiksCertificate(): X509Certificate {
        checkNotNull(fiksNokkelfil) { "Propertien 'fiks.nokkelfil' mangler" }
        return try {
            this.javaClass.getResourceAsStream("/svarutpublickey/$fiksNokkelfil").use { publickey ->
                CertificateFactory.getInstance("X509").generateCertificate(publickey) as X509Certificate
            }
        } catch (e: CertificateException) {
            logger.error("Kunne ikke opprette certificate for Fiks: {}", fiksNokkelfil, e)
            throw RuntimeException(e)
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(DokumentKrypterer::class.java)
    }
}
