package fari_99.keycloak.authenticator;

import com.google.auto.service.AutoService;

import org.keycloak.Config;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Niko Köbler, https://www.n-k.de, @dasniko
 */
@AutoService(AuthenticatorFactory.class)
public class SmsAuthenticatorFactory implements AuthenticatorFactory {

	public static final String PROVIDER_ID = "sms-authenticator";

	private static final SmsAuthenticator SINGLETON = new SmsAuthenticator();

	@Override
	public String getId() {
		return PROVIDER_ID;
	}

	@Override
	public String getDisplayType() {
		return "SMS Authentication";
	}

	@Override
	public String getHelpText() {
		return "Validates an OTP sent via SMS to the users mobile phone.";
	}

	@Override
	public String getReferenceCategory() {
		return "otp";
	}

	@Override
	public boolean isConfigurable() {
		return true;
	}

	@Override
	public boolean isUserSetupAllowed() {
		return true;
	}

	@Override
	public AuthenticationExecutionModel.Requirement[] getRequirementChoices() {
		return REQUIREMENT_CHOICES;
	}

	@Override
	public List<ProviderConfigProperty> getConfigProperties() {
		return CONFIG_PROPERTIES;
	}

	private static final List<ProviderConfigProperty> CONFIG_PROPERTIES = new ArrayList<>();

    static {
        ProviderConfigProperty property;
        property = new ProviderConfigProperty();
        property.setName(SmsConstants.EXOTEL_BASE_URL);
        property.setLabel("Base Url Exotel");
		property.setHelpText("Base URL accessing Exotel API");
        property.setType(ProviderConfigProperty.STRING_TYPE);
		property.setDefaultValue("https://exoverify.exotel.com/v2");
        CONFIG_PROPERTIES.add(property);

        property = new ProviderConfigProperty();
        property.setName(SmsConstants.EXOTEL_REQUEST_OTP_PATH);
        property.setLabel("Request OTP Url Exotel Path");
		property.setHelpText("Path url exotel for request OTP");
        property.setType(ProviderConfigProperty.STRING_TYPE);
		property.setDefaultValue("/accounts/%s/verifications/sms");
        CONFIG_PROPERTIES.add(property);

		property = new ProviderConfigProperty();
        property.setName(SmsConstants.EXOTEL_VERIFY_OTP_PATH);
        property.setLabel("Verify OTP Url Exotel Path");
		property.setHelpText("Path url exotel for verify OTP");
        property.setType(ProviderConfigProperty.STRING_TYPE);
		property.setDefaultValue("/accounts/%s/verifications/sms/%s");
        CONFIG_PROPERTIES.add(property);

		property = new ProviderConfigProperty();
        property.setName(SmsConstants.SIMULATION_MODE);
        property.setLabel("Simulation mode");
		property.setHelpText("In simulation mode, the SMS won't be sent, but printed to the server logs");
        property.setType(ProviderConfigProperty.BOOLEAN_TYPE);
		property.setDefaultValue(true);
        CONFIG_PROPERTIES.add(property);
    }

	@Override
	public Authenticator create(KeycloakSession session) {
		return SINGLETON;
	}

	@Override
	public void init(Config.Scope config) {
	}

	@Override
	public void postInit(KeycloakSessionFactory factory) {
	}

	@Override
	public void close() {
	}

}
