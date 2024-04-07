# Keycloak Extensions

this repo is a collection of keycloak extensions that i collected or developed after researching on the internet. 
the collection as follow:

## Authenticator Flow
- SMS 2FA (Using [Exotel](https://exotel.com/id-en/))
- Captcha on Username Password Form

## Password Hash
- Bcrypt (hard coded default rounding is 10)

## Required Action
- Mobile Number 
- Secondary Email 
- Verify Mobile Number
- Verify Secondary Email

# Prerequisite 
- keycloak version 23.0.7
- on `Realm settings`, please enabled `User Profile Enabled` and add this attribute to your `User Profile` tab
    - phoneNumbers
    - secondaryEmail

> please note, `declarative-user-profile` is needed to open `User Profile Enabled` in this version (preview). 
> on version keycloak version >= 24.0.0, this feature is permanently added

# VSCode Editor
install extensions
- Extension Pack for Java
    > this will install essential extension for java development
- FreeMarker
    > this extension for FrerMarker (.ftl)

# How to Generate .jar file
- if you already download another java version (newer), you don't need to update

- if you already download another java version (< v17), then you need to install newer version, and run this command
    ```
        update-java-alternatives --list
        sudo update-java-alternatives --set <path to new java version>
        java -version
    ```

- if you didn't already download java
    1. on your wsl/mac/ubuntu, download java jdk 17
        ```
            sudo apt-get update
            sudo apt-get install -y openjdk-17-jdk openjdk-17-jre
        ```
    2. install maven
        ```
            sudo apt-get install maven
        ```
    3. run `mvn clean install`

# How to add .jar extensions to your keycloak
> Please Note, if you want individuals custom extensions, please copy paste the extensions you want and generate the jar file yourself.
> the .jar file that created here all to install all of the custom extensions.
> after that you need to rebuild the image using `docker-compose bulid --no-cache keycloak`

1. go to your docker-compose.yml
2. on keycloak service, add this line on your keycloak service
    ```
    # example
    volumes:
      - ./target/<your-extension>.jar:/opt/keycloak/providers/extensions.jar
    ```
3. run `docker-compose up -d keycloak` to rebuild your keycloak service

# Thanks
- Niko Köbler (https://github.com/dasniko) for his many example of keycloak extensions