# README

**Repository:** `management-node`  
**Description:** `The Management Node Module is a Spring Boot application that provides APIs to be accessed by Consumer and Producer Federators. It implements a secure communication architecture using Mutual TLS (MTLS) connectivity between Federator instances and itself, as well as establishing zero trust connectivity with Keycloak for authentication and authorization.`  
**Repository Status:** `Private – NDTP InnerSource`  

---

## Overview

This repository is part of the **National Digital Twin Programme (NDTP)**. It supports the development of secure, modular, and standards-based components for internal use across NDTP projects.

> **This repository is private and governed by the NDTP InnerSource Licence – Version 1.0.**  
> It is intended solely for collaboration among NDTP teams and authorised suppliers.  
> It is **not open source** and must not be disclosed, redistributed, or published externally.

--- 

## Prerequisites
- Java 21
- Maven 3.9+
- Docker and Docker Compose
- OpenSSL (for certificate generation)

## Quick Start

### Setting up Keycloak with Docker Compose

The application uses Keycloak for authentication and authorization. Follow these steps to set up Keycloak using Docker Compose:

1. Navigate to the docker directory:
   ```bash
   cd docker
   ```

2. Make sure you have the required certificates in the `docker` directory:
   - `keystore.jks` - Java keystore containing the server certificate
   - `truststore.jks` - Java truststore containing trusted certificates
   - `localhost.p12` - PKCS12 keystore for client authentication
   - `localhost.crt` - Certificate file
   - `localhost.key` - Private key file

   If you need to generate these files for development, see the [Certificate Setup](#certificate-setup) section.

3. Start Keycloak and PostgreSQL using Docker Compose:
   ```bash
   docker compose -f keycloak/docker-compose.yml up -d
   ```

4. Verify that Keycloak is running:
   ```bash
   curl -k https://localhost:8443/health
   ```

5. Access the Keycloak admin console at https://localhost:8443/admin with the following credentials:
   - Username: `admin`
   - Password: `password`

### Configuration

For Docker Compose to run successfully, you need to create a `.env` file in the `docker/keycloak` directory with the following settings:

```
POSTGRES_DB=keycloak_db
POSTGRES_USER=keycloak_db_user
POSTGRES_PASSWORD=keycloak_db_user_password
KEYCLOAK_ADMIN=admin
KEYCLOAK_ADMIN_PASSWORD=password
KC_HOSTNAME_STRICT_BACKCHANNEL=false
SERVER_SSL_KEY_STORE_PASSWORD=changeit
SERVER_SSL_TRUST_STORE_PASSWORD=changeit
KC_HTTPS_KEY_STORE_PASSWORD=changeit
KC_HTTPS_TRUST_STORE_PASSWORD=changeit
KC_SPI_TRUSTSTORE_FILE_PASSWORD=changeit
KC_HOSTNAME=keycloak
KC_HOSTNAME_PORT=8080
KC_HTTP_ENABLED=false
KC_HOSTNAME_STRICT_HTTPS=false
KC_HEALTH_ENABLED=true
KC_DB=postgres
KC_HTTPS_CLIENT_AUTH=required
KC_HTTPS_ENABLED=true
KC_HTTPS_PORT=8443
KC_LOG_LEVEL=INFO
```

This file contains essential environment variables for both PostgreSQL and Keycloak configuration. You can modify these values as needed for your environment, but make sure to create this file before running Docker Compose.

## Certificate Setup

The Management Node Module implements a zero-trust security architecture using Mutual TLS (MTLS) for secure communication between all components. This section explains why certificates are needed, how to generate them, and where they are used in the system.

> **Note:** For detailed instructions on configuring MTLS for both Keycloak and the Management Node, see the [MTLS Configuration Guide](docs/MTLS_CONFIGURATION.md).

### Why Certificates Are Needed

1. **Zero-Trust Security Model**: The system follows a zero-trust approach where all communications must be authenticated and encrypted, regardless of whether they occur inside or outside the network perimeter.

2. **Mutual TLS (MTLS)**: Unlike standard TLS where only the server authenticates itself to the client, MTLS requires both parties to authenticate each other using X.509 certificates.

3. **Service-to-Service Authentication**: Certificates provide a secure way for services to verify each other's identity without relying on passwords or API keys.

### Certificate Types and Their Purpose

The system requires several certificate files:

1. **Private Key (`localhost.key`)**: 
   - The private key used to sign and decrypt data
   - Must be kept secure and never shared
   - Used by both Keycloak and the Management Node

2. **Certificate (`localhost.crt`)**: 
   - The public certificate containing the public key
   - Shared with other services to verify the identity
   - Used in both server and client authentication

3. **PKCS12 Keystore (`localhost.p12`)**: 
   - A container format that stores the private key and certificate
   - Used primarily for client authentication
   - Imported by Keycloak for client certificate validation

4. **Java Keystore (`keystore.jks`)**: 
   - Java-specific format for storing the server's private key and certificate
   - Used by both Keycloak and the Management Node for their TLS endpoints

5. **Java Truststore (`truststore.jks`)**: 
   - Contains certificates that the server trusts
   - Used to validate client certificates during MTLS

### Step-by-Step Certificate Generation

For development purposes, follow these steps to generate certificates for mTLS. All passwords used are `changeit`. When generating these certficates, for the `Country Name`, you can use the value of 'UK'. All remaining certificate fields can be left to their default values.

1. **Generate a Root CA certificate**:
   ```bash
   openssl req -x509 -sha256 -days 3650 -newkey rsa:4096 -keyout rootCA.key -out rootCA.crt
   ```
   This creates a Root Certificate Authority (CA) that will be used to sign other certificates. The certificate is valid for 10 years (3650 days).

2. **Generate a host certificate**:
   ```bash
   openssl req -new -newkey rsa:4096 -keyout localhost.key -out localhost.csr -nodes
   ```
   This creates a private key and certificate signing request (CSR) for the host.

3. **Sign the host certificate with the Root CA**:
   ```bash
   openssl x509 -req -CA rootCA.crt -CAkey rootCA.key -in localhost.csr -out localhost.crt -days 365 -CAcreateserial -extfile localhost.ext
   ```
   This signs the host CSR with the Root CA, creating a certificate valid for 365 days.
   
   The content of the `localhost.ext` file should be:
   ```
   authorityKeyIdentifier=keyid,issuer
   basicConstraints=CA:FALSE
   subjectAltName = @alt_names
   [alt_names]
   DNS.1 = localhost
   DNS.2 = keycloak
   ```
   This configuration specifies that the certificate is valid for both `localhost` and `keycloak` hostnames.

4. **Create a PKCS12 keystore for the server**:
   ```bash
   openssl pkcs12 -export -out localhost.p12 -name "localhost" -inkey localhost.key -in localhost.crt
   ```
   This bundles the host certificate and private key into a PKCS12 format.

5. **Create a PEM file for Linux keystore**:
   ```bash
   openssl pkcs12 -in localhost.p12 -clcerts -nokeys -out localhost.pem
   ```
   This extracts the certificate (without the private key) in PEM format.

6. **Add the Root CA to the Trust Store**:
   ```bash
   keytool -importcert -file rootCA.crt -alias clientca -keystore localhost.p12 -storetype PKCS12 -storepass changeit
   ```
   This adds the Root CA to the trust store so that clients signed by this CA will be trusted.

7. **Generate a client certificate**:
   ```bash
   openssl req -new -newkey rsa:4096 -nodes -keyout client.key -out client.csr
   ```
   This creates a private key and CSR for the client.

8. **Sign the client certificate with the Root CA**:
   ```bash
   openssl x509 -req -CA rootCA.crt -CAkey rootCA.key -in client.csr -out client.crt -days 365 -CAcreateserial
   ```
   This signs the client CSR with the Root CA, creating a certificate valid for 365 days.

9. **Create a PKCS12 keystore for the client**:
   ```bash
   openssl pkcs12 -export -out client.p12 -name "client" -inkey client.key -in client.crt
   ```
   This bundles the client certificate and private key into a PKCS12 format for use in browsers or client applications.

10. **Create a Java keystore using keytool**:
    ```bash
    keytool -importkeystore -destkeystore keystore.jks -srckeystore localhost.p12 -srcstoretype PKCS12 -alias "localhost"
    ```
    This converts the PKCS12 keystore to a Java KeyStore (JKS) format used by Java applications.

11. **Create a Java truststore using keytool**:
    ```bash
    keytool -import -trustcacerts -noprompt -alias ca -ext san=dns:localhost,ip:127.0.0.1 -file rootCA.crt -keystore truststore.jks
    ```
    This creates a truststore containing the Root CA certificate, which will be used to validate client certificates.

12. **Import the Root CA into the truststore**:
    ```bash
    keytool -importcert -file rootCA.crt -alias rootCA -keystore truststore.jks -storetype JKS
    ```
    This ensures the Root CA is properly imported into the Java truststore.

### Certificate Placement and Configuration

After generating the certificates, place them in the appropriate locations:

1. **For Keycloak**:
   - Place all certificate files in the `docker` directory
   - The docker-compose.yml maps these files into the Keycloak container:
     ```yaml
     volumes:
       - ./localhost.p12:/keystores/localhost.p12
       - ./localhost.crt:/cert/localhost.crt
       - ./localhost.key:/key/localhost.key
       - ./keystore.jks:/cert/keystore.jks
       - ./truststore.jks:/cert/truststore.jks
     ```
   - Keycloak uses these certificates for:
     - Securing its HTTPS endpoint (port 8443)
     - Validating client certificates for MTLS

2. **For Management Node**:
   - The application.yml references the certificate files:
     ```yaml
     server:
       ssl:
         key-store: /path/to/keystore.jks
         key-store-password: changeit
         trust-store: /path/to/truststore.jks
         trust-store-password: changeit
     ```
   - When running in Docker, the Dockerfile copies these files:
     ```dockerfile
     COPY docker/keystore.jks /app/docker/keystore.jks
     COPY docker/truststore.jks /app/docker/truststore.jks
     ```

3. **For Client Applications**:
   - Client applications connecting to the Management Node need:
     - The client certificate and private key for authentication
     - The server's certificate in their truststore to validate the server

### Certificate Password Management

All certificates use the password "changeit" for development. These passwords are configured in the `.env` file:

```
SERVER_SSL_KEY_STORE_PASSWORD=changeit
SERVER_SSL_TRUST_STORE_PASSWORD=changeit
KC_HTTPS_KEY_STORE_PASSWORD=changeit
KC_HTTPS_TRUST_STORE_PASSWORD=changeit
KC_SPI_TRUSTSTORE_FILE_PASSWORD=changeit
```

For production environments, use strong, unique passwords and secure storage solutions for managing these credentials.

## Keycloak Realm Setup

After starting Keycloak, you need to set up a realm for the Management Node. You can either import the pre-configured realm or create it manually. To access the administrative interface at https://localhost:8443/admin, you will need to first import your client.p12 digital certificate file into your local browser, else the request will be rejected.

### Option 1: Import the Realm Configuration (Recommended)

1. Log in to the Keycloak admin console at https://localhost:8443/admin
2. Click on the dropdown menu in the top-left corner (it may show "master" if you haven't created any realms yet)
3. Click on "Create Realm" or "Add realm" button
4. Click on the "Browse" or "Select file" button
5. Navigate to and select the `docker/keycloak/management-node-realm.json` file from your project directory
6. Click "Create" or "Import"
7. After the import is complete, verify that the `management-node` realm has been created with all the necessary configurations
8. Note the client secret for the `ztf-client` from the Credentials tab (Clients → ztf-client → Credentials) and update it in your application.yml if needed

### Option 2: Manual Configuration

If you prefer to set up the realm manually:

1. Log in to the Keycloak admin console at https://localhost:8443/admin
2. Create a new realm named `management-node`
3. Create a client with the following settings:
   - Client ID: `ztf-client`
   - Client Protocol: `openid-connect`
   - Access Type: `confidential`
   - Valid Redirect URIs: `https://localhost:8090/*`
   - Web Origins: `+`
4. Note the client secret from the Credentials tab and update it in your application.yml if needed

### Testing mTLS connectivity:

Once KeyCloak is running and configured, you can test mTLS connectivity using the below command:

    ```bash
    curl --location 'https://localhost:8443/realms/management-node/protocol/openid-connect/token' \
    --cert client.crt --key client.key \
    --header 'Content-Type: application/x-www-form-urlencoded' \
    --data-urlencode 'client_id=ztf-client' \
    --data-urlencode 'grant_type=client_credentials'
    ```

This tests the mTLS setup by attempting to obtain a token from Keycloak using client certificate authentication.

## Building and Running with Maven

### Building the Application

The Management Node Module uses Maven for dependency management and build automation. To build the application:

1. Ensure you have Maven 3.9+ installed:
   ```bash
   mvn --version
   ```

2. Build the application:
   ```bash
   mvn clean package
   ```
   This command will:
   - Clean the target directory
   - Compile the source code
   - Run the tests
   - Package the application into a JAR file

3. If you want to skip tests during the build:
   ```bash
   mvn clean package -DskipTests
   ```

### Running the Application

After building, you can run the application using one of these methods:

1. Using the Java command:
   ```bash
   java -jar target/management-node-0.0.1.jar
   ```

2. Using the Maven Spring Boot plugin:
   ```bash
   mvn spring-boot:run
   ```

3. Using Docker:
   ```bash
   docker build -t management-node -f docker/Dockerfile .
   docker run -p 8090:8090 management-node
   ```

The application will be available at https://localhost:8090

### Using Profile-Specific Configuration Files

Spring Boot supports profile-specific property files, which are essential for local development environments where you need to configure sensitive information like passwords and URLs without committing them to version control.

#### Why Use Profile-Specific Configuration?

1. **Security**: Keep sensitive information like passwords and API keys out of version control
2. **Environment-Specific Settings**: Configure different settings for development, testing, and production
3. **Local Development**: Each developer can have their own configuration without affecting others

#### Creating a Profile-Specific YAML File

1. Create a file named `application-{profile}.yml` in the `src/main/resources` directory, where `{profile}` is the name of your profile (e.g., `application-local.yml` for a "local" profile)

2. Add your environment-specific configuration to this file. For example:

   ```yaml
   spring:
     security:
       oauth2:
         resourceserver:
           opaquetoken:
             client-secret: your-client-secret-here
             client-id: ztf-client
     datasource:
       password: your-database-password-here
   
   server:
     ssl:
       key-store-password: your-keystore-password-here
       trust-store-password: your-truststore-password-here
       key-store: /path/to/your/local/keystore.jks
       trust-store: /path/to/your/local/truststore.jks
   ```

3. Make sure not to commit this file to version control by adding it to your `.gitignore` file:
   ```
   src/main/resources/application-local.yml
   ```

#### Running the Application with a Specific Profile

To run the application with your profile, use one of these methods:

1. Using the Java command with the `spring.profiles.active` parameter:
   ```bash
   java -jar target/management-node-0.0.1.jar --spring.profiles.active=local
   ```

2. Using the Maven Spring Boot plugin:
   ```bash
   mvn spring-boot:run -Dspring-boot.run.profiles=local
   ```

3. Using environment variables:
   ```bash
   export SPRING_PROFILES_ACTIVE=local
   java -jar target/management-node-0.0.1.jar
   ```

4. When running with Docker, you can pass the profile as an environment variable:
   ```bash
   docker run -p 8090:8090 -e "SPRING_PROFILES_ACTIVE=local" management-node
   ```

The application will load both the default `application.yml` and your profile-specific `application-local.yml`, with the latter overriding any duplicate properties.

## Code Coverage with JaCoCo

The project uses JaCoCo for code coverage analysis. For detailed information about the JaCoCo setup, thresholds, and recommendations, see the [JaCoCo Coverage Documentation](docs/JACOCO_COVERAGE.md).

### Running Code Coverage

To generate code coverage reports:

1. Run the Maven verify goal:
   ```bash
   mvn clean verify
   ```

2. The JaCoCo report will be generated in the `target/site/jacoco` directory.

3. Open `target/site/jacoco/index.html` in a web browser to view the detailed coverage report.

The current configuration aims for 80% code coverage across instructions, branches, lines, methods, and 50% for classes.

## Troubleshooting

### Common Issues

1. **Certificate Issues**:
   - Ensure that the paths to the keystore and truststore files in application.yml are correct
   - Verify that the certificate passwords match those in the .env file

2. **Keycloak Connection Issues**:
   - Check that Keycloak is running and accessible at https://localhost:8443
   - Verify that the client secret in application.yml matches the one in Keycloak

3. **Database Connection Issues**:
   - Ensure PostgreSQL is running and accessible
   - Check the database credentials in the .env file

## Security Considerations

This setup implements a zero-trust security model with:
- MTLS for all service-to-service communication
- JWT-based authentication and authorization via Keycloak
- HTTPS for all endpoints
- Client certificate authentication

For production deployments, consider:
- Using properly signed certificates from a trusted CA
- Implementing network segmentation
- Regularly rotating secrets and certificates
- Setting up monitoring and alerting for security events

## Public Funding Acknowledgment  
This repository has been developed with public funding as part of the National Digital Twin Programme (NDTP), a UK Government initiative. NDTP, alongside its partners, has invested in this work to advance open, secure, and reusable digital twin technologies for any organisation, whether from the public or private sector, irrespective of size.  

## Licensing

This repository, including all source code, documentation, configuration files, and related materials, is licensed under the:

**NDTP InnerSource Licence – Version 1.0**  
See [LICENSE.md](LICENSE.md) for the full licence text.

> ⚠️ This repository is **not open source**.  
> Redistribution, disclosure, or publication of any part of this repository is prohibited without the **explicit, written approval** of the NDTP Management Team.

All intellectual property rights are held by the **Department for Business and Trade (UK)** as the governing entity for the National Digital Twin Programme (NDTP).

## Security and Responsible Disclosure  
We take security seriously. If you believe you have found a security vulnerability in this repository, please follow our responsible disclosure process outlined in `SECURITY.md`.  

## Software Bill of Materials (SBOM)

This project provides a Software Bill of Materials (SBOM) to help users and integrators understand its dependencies.

### Current SBOM
Download the [latest SBOM for this codebase](../../dependency-graph/sbom) to view the current list of components used in this repository.

## Contributing  
We welcome contributions that align with the Programme’s objectives. Please read our `CONTRIBUTING.md` guidelines before submitting pull requests.  

## Acknowledgements  
This repository has benefited from collaboration with various organisations. For a list of acknowledgments, see `ACKNOWLEDGEMENTS.md`.  

## Support and Contact  
For questions or support, check our Issues or contact the NDTP team by emailing ndtp@businessandtrade.gov.uk.

**Maintained by the National Digital Twin Programme (NDTP).**  

© Crown Copyright 2025. This work has been developed by the National Digital Twin Programme and is legally attributed to the Department for Business and Trade (UK) as the governing entity.