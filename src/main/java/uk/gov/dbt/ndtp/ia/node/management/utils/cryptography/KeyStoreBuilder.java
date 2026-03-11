/*
 * SPDX-License-Identifier: Apache-2.0
 * © Crown Copyright 2026. This work has been developed by the National Digital Twin Programme and is legally
 * attributed to the Department for Business and Trade (UK) as the governing entity.
 */

package uk.gov.dbt.ndtp.ia.node.management.utils.cryptography;

import java.io.ByteArrayOutputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import uk.gov.dbt.ndtp.ia.node.management.exception.PkiException;

/**
 * Utility class for building PKCS#12 keystores and truststores in memory.
 */
public class KeyStoreBuilder {

    private static final String KEYSTORE_TYPE = "PKCS12";

    private KeyStoreBuilder() {}

    /**
     * Creates a PKCS#12 keystore containing a private key and its certificate chain.
     *
     * @param privateKey the private key
     * @param certificate the leaf certificate
     * @param caChain the CA certificate chain (may be null or empty)
     * @param password the keystore password
     * @param alias the alias for the key entry
     * @return the serialized PKCS#12 keystore as a byte array
     * @throws PkiException if keystore creation fails
     */
    public static byte[] createKeyStore(
            PrivateKey privateKey,
            X509Certificate certificate,
            List<X509Certificate> caChain,
            String password,
            String alias) {
        try {
            List<Certificate> chain = new ArrayList<>();
            chain.add(certificate);
            if (caChain != null) {
                chain.addAll(caChain);
            }

            KeyStore ks = KeyStore.getInstance(KEYSTORE_TYPE);
            ks.load(null, null);
            ks.setKeyEntry(alias, privateKey, password.toCharArray(), chain.toArray(new Certificate[0]));

            return serialize(ks, password);
        } catch (Exception e) {
            throw new PkiException("Failed to create keystore", e);
        }
    }

    /**
     * Creates a PKCS#12 truststore containing a list of CA certificates.
     *
     * @param caCerts the CA certificates to include
     * @param password the truststore password
     * @return the serialized PKCS#12 truststore as a byte array
     * @throws PkiException if truststore creation fails
     */
    public static byte[] createTrustStore(List<X509Certificate> caCerts, String password) {
        try {
            KeyStore ks = KeyStore.getInstance(KEYSTORE_TYPE);
            ks.load(null, null);

            if (caCerts != null) {
                for (int i = 0; i < caCerts.size(); i++) {
                    ks.setCertificateEntry("ca-" + i, caCerts.get(i));
                }
            }

            return serialize(ks, password);
        } catch (Exception e) {
            throw new PkiException("Failed to create truststore", e);
        }
    }

    private static byte[] serialize(KeyStore ks, String password) {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            ks.store(bos, password.toCharArray());
            return bos.toByteArray();
        } catch (Exception e) {
            throw new PkiException("Failed to serialize keystore", e);
        }
    }
}
