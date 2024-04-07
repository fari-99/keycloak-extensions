# build extensions
FROM maven:3.8.5-openjdk-17 AS builder_extensions

# Copy and build extension source
COPY ./ /workspace/extensions
RUN cd /workspace/extensions && mvn clean package -DskipTests

# Base image for production
FROM quay.io/keycloak/keycloak:23.0.7

COPY --chown=keycloak:keycloak --from=builder_extensions /workspace/extensions/target/fari_99.custom_extensions.jar /opt/keycloak/providers/fari_99.custom_extensions.jar

CMD [ "start-dev" ]