# MTLS Configuration Guide

This guide provides detailed instructions on how to configure Mutual TLS (MTLS) for both the Keycloak authentication server and the Management Node Spring Boot application.

## What is MTLS and Why It's Needed

Mutual TLS (MTLS) is a security protocol that requires both the client and server to authenticate each other using X.509 certificates. Unlike standard TLS where only the server authenticates itself to the client, MTLS ensures bidirectional authentication, providing a higher level of security.

In the context of the Management Node Module:
- MTLS establishes a zero-trust security model where all communications must be authenticated and encrypted
- It provides service-to-service authentication without relying on passwords or API keys
- It prevents unauthorized access to sensitive APIs and data

## Certificate Files Overview

Before configuring MTLS, ensure you have the following certificate files:

| File Type | Purpose | Used By |
|-----------|---------|---------|
| `keystore.jks` | Java keystore containing the server certificate and private key | Keycloak & Management Node |
| `truststore.jks` | Java truststore containing trusted client certificates | Keycloak & Management Node |
| `localhost.p12` | PKCS12 keystore for client authentication | Keycloak |
| `localhost.crt` | Certificate file | Keycloak |
| `localhost.key` | Private key file | Keycloak |

For instructions on generating these files, refer to the [Certificate Setup](#certificate-setup) section in the main README.

## Configuring MTLS for Keycloak

Keycloak's MTLS configuration is defined in the `docker/docker-compose.yml` file. The following environment variables control MTLS behavior:

```yaml
KC_HTTPS_CLIENT_AUTH: ${KC_HTTPS_CLIENT_AUTH}  # Set to 'required' to enforce MTLS
KC_HTTPS_ENABLED: ${KC_HTTPS_ENABLED}          # Must be 'true' for MTLS
KC_HTTPS_PORT: ${KC_HTTPS_PORT}                # Default is 8443
KC_HTTPS_KEY_STORE_FILE: /cert/keystore.jks    # Server certificate
KC_HTTPS_KEY_STORE_PASSWORD: ${KC_HTTPS_KEY_STORE_PASSWORD}
KC_HTTPS_CERTIFICATE_FILE: /cert/localhost.crt
KC_HTTPS_CERTIFICATE_KEY_FILE: /key/localhost.key
KC_HTTPS_TRUST_STORE_FILE: /keystores/localhost.p12
KC_HTTPS_TRUST_STORE_PASSWORD: ${KC_HTTPS_TRUST_STORE_PASSWORD}
KC_SPI_TRUSTSTORE_FILE_FILE: /cert/truststore.jks
KC_SPI_TRUSTSTORE_FILE_PASSWORD: ${KC_SPI_TRUSTSTORE_FILE_PASSWORD}
```

### Steps to Configure Keycloak MTLS:

1. Place your certificate files in the `docker` directory:
   - `keystore.jks`
   - `truststore.jks`
   - `localhost.p12`
   - `localhost.crt`
   - `localhost.key`

2. Create or update the `.env` file in the `docker` directory with the following MTLS-related variables:
   ```
   KC_HTTPS_CLIENT_AUTH=required
   KC_HTTPS_ENABLED=true
   KC_HTTPS_PORT=8443
   KC_HTTPS_KEY_STORE_PASSWORD=changeit
   KC_HTTPS_TRUST_STORE_PASSWORD=changeit
   KC_SPI_TRUSTSTORE_FILE_PASSWORD=changeit
   ```

3. The `docker-compose.yml` file maps these certificate files into the Keycloak container:
   ```yaml
   volumes:
     - ./localhost.p12:/keystores/localhost.p12
     - ./localhost.crt:/cert/localhost.crt
     - ./localhost.key:/key/localhost.key
     - ./keystore.jks:/cert/keystore.jks
     - ./truststore.jks:/cert/truststore.jks
   ```

## Configuring MTLS for the Management Node

The Management Node's MTLS configuration is defined in the `src/main/resources/application.yml` file under the `server.ssl` section:

```yaml
server:
  port: 8090
  ssl:
    key-alias: localhost
    key-store: /path/to/keystore.jks
    key-store-type: JKS
    key-store-password: changeit
    trust-store: /path/to/truststore.jks
    trust-store-password: changeit
    trust-store-type: JKS
    client-auth: need  # This enables MTLS
```

### Steps to Configure Management Node MTLS:

1. Update the `application.yml` file with the correct paths to your certificate files:
   ```yaml
   server:
     port: 8090
     ssl:
       key-alias: localhost
       key-store: /path/to/keystore.jks  # Update this path
       key-store-type: JKS
       key-store-password: changeit
       trust-store: /path/to/truststore.jks  # Update this path
       trust-store-password: changeit
       trust-store-type: JKS
       client-auth: need  # Add this line to enable MTLS
   ```

2. For Docker deployment, update the Dockerfile to copy the certificate files:
   ```dockerfile
   COPY ../docker/keystore.jks /app/docker/keystore.jks
   COPY ../docker/truststore.jks /app/docker/truststore.jks
   ```

3. When running the application, ensure the certificate files are accessible at the paths specified in the configuration.

## Testing MTLS Configuration

To verify that MTLS is properly configured:

1. For Keycloak:
   ```bash
   # This should fail without a client certificate
   curl -k https://localhost:8443/health
   
   # This should succeed with a client certificate
   curl -k --cert client.crt --key client.key https://localhost:8443/health
   ```

2. For Management Node:
   ```bash
   # This should fail without a client certificate
   curl -k https://localhost:8090/actuator/health
   
   # This should succeed with a client certificate
   curl -k --cert client.crt --key client.key https://localhost:8090/actuator/health
   ```

## Troubleshooting MTLS Issues

Common MTLS configuration issues:

1. **Certificate Path Issues**:
   - Ensure the paths to certificate files are correct and accessible
   - For Docker deployments, verify that volumes are properly mounted

2. **Certificate Password Issues**:
   - Verify that the passwords in configuration files match the actual certificate passwords

3. **Certificate Trust Issues**:
   - Ensure the client's certificate is trusted by the server's truststore
   - Ensure the server's certificate is trusted by the client's truststore

4. **Certificate Expiration**:
   - Check that certificates are not expired

For more detailed troubleshooting, check the logs:
- Keycloak logs: `docker logs keycloak`
- Management Node logs: Check the application logs for SSL-related errors