//package no.nav.sosialhjelp.soknad.consumer.fiks;
//
//import org.bouncycastle.asn1.ASN1ObjectIdentifier;
//import org.bouncycastle.asn1.DERNull;
//import org.bouncycastle.asn1.DEROctetString;
//import org.bouncycastle.asn1.nist.NISTObjectIdentifiers;
//import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
//import org.bouncycastle.asn1.pkcs.RSAESOAEPparams;
//import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
//import org.bouncycastle.cms.CMSAlgorithm;
//import org.bouncycastle.cms.CMSEnvelopedData;
//import org.bouncycastle.cms.CMSEnvelopedDataGenerator;
//import org.bouncycastle.cms.CMSException;
//import org.bouncycastle.cms.CMSProcessableByteArray;
//import org.bouncycastle.cms.jcajce.JceCMSContentEncryptorBuilder;
//import org.bouncycastle.cms.jcajce.JceKeyTransRecipientInfoGenerator;
//import org.bouncycastle.jce.provider.BouncyCastleProvider;
//import org.bouncycastle.operator.OutputEncryptor;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//
//import javax.annotation.PostConstruct;
//import java.io.IOException;
//import java.io.InputStream;
//import java.security.Security;
//import java.security.cert.CertificateEncodingException;
//import java.security.cert.CertificateException;
//import java.security.cert.CertificateFactory;
//import java.security.cert.X509Certificate;
//
//@Service
//public class DokumentKrypterer {
//
//    private static final Logger logger = LoggerFactory.getLogger(DokumentKrypterer.class);
//
//    @Value("${fiks.nokkelfil}")
//    private String fiksNokkelfil;
//
//    private AlgorithmIdentifier encryptionScheme;
//    private ASN1ObjectIdentifier cms;
//    private X509Certificate certificate;
//
//    @PostConstruct
//    public void settOppKryptering() {
//        Security.addProvider(new BouncyCastleProvider());
//        encryptionScheme = lagAlgoritmeKonfigurasjon();
//        cms = CMSAlgorithm.AES256_CBC;
//        certificate = lagFiksCertificate();
//    }
//
//    public byte[] krypterData(byte[] data) {
//        try {
//            JceKeyTransRecipientInfoGenerator recipient = new JceKeyTransRecipientInfoGenerator(certificate, encryptionScheme).setProvider("BC");
//            CMSEnvelopedDataGenerator enveloped = new CMSEnvelopedDataGenerator();
//            enveloped.addRecipientInfoGenerator(recipient);
//            OutputEncryptor encryptor = new JceCMSContentEncryptorBuilder(cms).build();
//
//            CMSEnvelopedData cmsData = enveloped.generate(new CMSProcessableByteArray(data), encryptor);
//            return cmsData.getEncoded();
//        } catch (CertificateEncodingException | IOException e) {
//            logger.error("Noe feilet under kryptering", e);
//            throw new RuntimeException(e);
//        } catch (CMSException e) {
//            logger.error("Kunne ikke kryptere. Om den klager på 'illegal key size' er det" +
//                    " fordi java cryptography extension mangler", e);
//            throw new RuntimeException(e);
//        }
//    }
//
//    private AlgorithmIdentifier lagAlgoritmeKonfigurasjon() {
//        AlgorithmIdentifier hash = new AlgorithmIdentifier(NISTObjectIdentifiers.id_sha256, DERNull.INSTANCE);
//        AlgorithmIdentifier mask = new AlgorithmIdentifier(PKCSObjectIdentifiers.id_mgf1, hash);
//        AlgorithmIdentifier pSource = new AlgorithmIdentifier(PKCSObjectIdentifiers.id_pSpecified, new DEROctetString(new byte[0]));
//        RSAESOAEPparams params = new RSAESOAEPparams(hash, mask, pSource);
//
//        return new AlgorithmIdentifier(PKCSObjectIdentifiers.id_RSAES_OAEP, params);
//    }
//
//    private X509Certificate lagFiksCertificate() {
//        if (fiksNokkelfil == null) {
//            throw new IllegalStateException("Propertien 'fiks.nokkelfil' mangler");
//        }
//
//        try {
//            InputStream publickey = getClass().getResourceAsStream("/svarutpublickey/" + fiksNokkelfil);
//            return (X509Certificate) CertificateFactory.getInstance("X509")
//                    .generateCertificate(publickey);
//        } catch (CertificateException e) {
//            logger.error("Kunne ikke opprette certificate for Fiks: {}", fiksNokkelfil, e);
//            throw new RuntimeException(e);
//        }
//    }
//}
