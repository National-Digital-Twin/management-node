/*
 * SPDX-License-Identifier: Apache-2.0
 * © Crown Copyright 2026. This work has been developed by the National Digital Twin Programme and is legally
 * attributed to the Department for Business and Trade (UK) as the governing entity.
 */

package uk.gov.dbt.ndtp.ia.node.management.utils.cryptography;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class KeyStoreBuilderTest {

    private static KeyPair leafKeyPair;
    private static KeyPair caKeyPair;
    private static X509Certificate testCert;
    private static X509Certificate testCaCert;

    private static final String PASSWORD = "testpassword";

    @BeforeAll
    static void generateTestCerts() throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        caKeyPair = kpg.generateKeyPair();
        leafKeyPair = kpg.generateKeyPair();

        testCaCert = generateSelfSignedCert(caKeyPair, "CN=test-ca");
        testCert = generateSignedCert(leafKeyPair, "CN=test-leaf", caKeyPair, "CN=test-ca");
    }

    private static X509Certificate generateSelfSignedCert(KeyPair keyPair, String dn) throws Exception {
        Instant now = Instant.now();
        X500Name name = new X500Name(dn);
        BigInteger serial = BigInteger.valueOf(now.toEpochMilli());
        Date notBefore = Date.from(now);
        Date notAfter = Date.from(now.plus(1, ChronoUnit.HOURS));

        JcaX509v3CertificateBuilder builder =
                new JcaX509v3CertificateBuilder(name, serial, notBefore, notAfter, name, keyPair.getPublic());
        ContentSigner signer = new JcaContentSignerBuilder("SHA256withRSA").build(keyPair.getPrivate());
        X509CertificateHolder holder = builder.build(signer);
        return new JcaX509CertificateConverter().getCertificate(holder);
    }

    private static X509Certificate generateSignedCert(
            KeyPair subjectKeyPair, String subjectDn, KeyPair issuerKeyPair, String issuerDn) throws Exception {
        Instant now = Instant.now();
        BigInteger serial = BigInteger.valueOf(now.toEpochMilli() + 1);
        Date notBefore = Date.from(now);
        Date notAfter = Date.from(now.plus(1, ChronoUnit.HOURS));

        JcaX509v3CertificateBuilder builder = new JcaX509v3CertificateBuilder(
                new X500Name(issuerDn),
                serial,
                notBefore,
                notAfter,
                new X500Name(subjectDn),
                subjectKeyPair.getPublic());
        ContentSigner signer = new JcaContentSignerBuilder("SHA256withRSA").build(issuerKeyPair.getPrivate());
        X509CertificateHolder holder = builder.build(signer);
        return new JcaX509CertificateConverter().getCertificate(holder);
    }

    @Test
    void createKeyStore_validInputs_returnsLoadableP12() throws Exception {
        byte[] ksBytes =
                KeyStoreBuilder.createKeyStore(leafKeyPair.getPrivate(), testCert, List.of(), PASSWORD, "leaf");

        KeyStore ks = KeyStore.getInstance("PKCS12");
        ks.load(new ByteArrayInputStream(ksBytes), PASSWORD.toCharArray());

        assertThat(ks.containsAlias("leaf")).isTrue();
        assertThat(ks.isKeyEntry("leaf")).isTrue();
    }

    @Test
    void createKeyStore_withCaChain_includesFullChain() throws Exception {
        byte[] ksBytes =
                KeyStoreBuilder.createKeyStore(leafKeyPair.getPrivate(), testCert, List.of(testCaCert), PASSWORD, "my");

        KeyStore ks = KeyStore.getInstance("PKCS12");
        ks.load(new ByteArrayInputStream(ksBytes), PASSWORD.toCharArray());

        Certificate[] chain = ks.getCertificateChain("my");
        assertThat(chain).hasSize(2);
        assertThat(chain[0]).isEqualTo(testCert);
        assertThat(chain[1]).isEqualTo(testCaCert);
    }

    @Test
    void createTrustStore_validCerts_returnsLoadableP12() throws Exception {
        byte[] tsBytes = KeyStoreBuilder.createTrustStore(List.of(testCaCert, testCert), PASSWORD);

        KeyStore ks = KeyStore.getInstance("PKCS12");
        ks.load(new ByteArrayInputStream(tsBytes), PASSWORD.toCharArray());

        assertThat(ks.containsAlias("ca-0")).isTrue();
        assertThat(ks.containsAlias("ca-1")).isTrue();
        assertThat(ks.isCertificateEntry("ca-0")).isTrue();
        assertThat(ks.getCertificate("ca-0")).isEqualTo(testCaCert);
    }

    @Test
    void createTrustStore_emptyList_returnsEmptyStore() throws Exception {
        byte[] tsBytes = KeyStoreBuilder.createTrustStore(List.of(), PASSWORD);

        KeyStore ks = KeyStore.getInstance("PKCS12");
        ks.load(new ByteArrayInputStream(tsBytes), PASSWORD.toCharArray());

        assertThat(ks.size()).isZero();
    }
}
