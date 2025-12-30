**Repository:** `management-node`  
**Description:** `The Management Node Module is a Spring Boot application that provides APIs to be accessed by Consumer and Producer Federators. It implements a secure communication architecture using Mutual TLS (MTLS) connectivity between Federator instances and itself, as well as establishing zero trust connectivity with Keycloak for authentication and authorization.`  
**SPDX-License-Identifier:** `Apache-2.0 AND OGL-UK-3.0 `


# mTLS with KeyCloak

## Create X.509 certificates


All passwords: _changeit_

## RootCA

    openssl req -x509 -sha256 -days 3650 -newkey rsa:4096 -keyout rootCA.key -out rootCA.crt

## Host certificate

    openssl req -new -newkey rsa:4096 -keyout localhost.key -out localhost.csr -nodes

Sign host csr with rootCA (see below for file `localhost.ext`):

    openssl x509 -req -CA rootCA.crt -CAkey rootCA.key -in localhost.csr -out localhost.crt -days 365 -CAcreateserial -extfile localhost.ext

### Create pkcs12 file for server
Import local key and crt in keystore to create the "certificate" to be used in keyCloak Server Config:

    openssl pkcs12 -export -out localhost.p12 -name "localhost" -inkey localhost.key -in localhost.crt

PEM file creation to be used in linux keystore

    openssl pkcs12 -in localhost.p12 -clcerts -nokeys -out localhost.pem

adding CA Root to Trust Store

    keytool -importcert -file rootCA.crt -alias clientca -keystore localhost.p12 -storetype PKCS12 -storepass changeit

---

## Client (user) certificate

    openssl req -new -newkey rsa:4096 -nodes -keyout client.key -out client.csr

Sign client csr with rootCA:

    openssl x509 -req -CA rootCA.crt -CAkey rootCA.key -in client.csr -out client.crt -days 365 -CAcreateserial

Import client key and crt in keystore to create the "certificate" to be used in the browser:

    openssl pkcs12 -export -out client.p12 -name "client" -inkey client.key -in client.crt




### Create a keystore using keytool

    keytool -importkeystore -destkeystore keystore.jks -srckeystore localhost.p12 -srcstoretype PKCS12 -alias "localhost" 

---


### Create a truststore using keytool

    keytool -import -trustcacerts -noprompt -alias ca -ext san=dns:localhost,ip:127.0.0.1 -file rootCA.crt -keystore truststore.jks
    
    ##Or in pkcs12
    openssl pkcs12 -export -in rootCA.crt -inkey rootCA.key -out truststore.p12 -name "server certificate" -chain -CAfile rootCA.crt -caname "self signed ca certificate" -passin pass:$PW -passout pass:$PW

### import the Root CA into TrustStore
        
    keytool -importcert -file rootCA.crt -alias rootCA -keystore truststore.jks -storetype JKS 
---


## To Test MTLS:

    curl --location 'https://localhost:8443/realms/management-node/protocol/openid-connect/token' \
--cert client.crt --key client.key \
--header 'Content-Type: application/x-www-form-urlencoded' \
--data-urlencode 'client_id=management-node' \
--data-urlencode 'grant_type=client_credentials'

---

## Docker Setup

### Prerequisites
- Docker and Docker Compose installed on your system
- Maven installed for building the application

### Building the Application
Before running the Docker containers, build the Spring Boot application:

```bash
cd /path/to/managementNode
mvn clean package
```

### Running with Docker Compose
1. Set up environment variables in a `.env` file in the docker directory:

```
# Database configuration
POSTGRES_DB=keycloak_db
POSTGRES_USER=keycloak_db_user
POSTGRES_PASSWORD=keycloak_db_user_password

# Keycloak admin credentials
KEYCLOAK_ADMIN=admin
KEYCLOAK_ADMIN_PASSWORD=password

# SSL configuration
SERVER_SSL_KEY_STORE_PASSWORD=changeit
SERVER_SSL_TRUST_STORE_PASSWORD=changeit
KC_HTTPS_KEY_STORE_PASSWORD=changeit
KC_HTTPS_TRUST_STORE_PASSWORD=changeit
KC_SPI_TRUSTSTORE_FILE_PASSWORD=changeit

# Keycloak configuration
KC_HOSTNAME=localhost
KC_HOSTNAME_PORT=8080
KC_HOSTNAME_STRICT_BACKCHANNEL=false
KC_HTTP_ENABLED=false
KC_HOSTNAME_STRICT_HTTPS=false
KC_HEALTH_ENABLED=true
KC_DB=postgres
KC_HTTPS_CLIENT_AUTH=required
KC_HTTPS_ENABLED=true
KC_HTTPS_PORT=8443
KC_LOG_LEVEL=INFO
```

2. Start all services using Docker Compose:

```bash
cd docker
docker-compose up -d
```

This will start:
- PostgreSQL database
- Keycloak authentication server
- Management Node application

3. Access the application at https://localhost:8090

### Stopping the Services

```bash
cd docker
docker-compose down
```

To remove volumes as well:

```bash
docker-compose down -v
```