package fari_99.keycloak.required_action;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import org.keycloak.authentication.InitiatedActionSupport;
import org.keycloak.authentication.RequiredActionContext;
import org.keycloak.authentication.RequiredActionProvider;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.UserModel;
import org.keycloak.models.utils.FormMessage;
import org.keycloak.sessions.AuthenticationSessionModel;

import jakarta.ws.rs.core.Response;
import fari_99.keycloak.authenticator.SmsAuthenticator;
import fari_99.keycloak.authenticator.SmsConstants;

public class VerifyMobileNumberRequiredAction extends SmsAuthenticator implements RequiredActionProvider {
    
    public static final String PROVIDER_ID = "verify-phone-number-ra";
	private static final String VERIFY_MOBILE_NUMBER_FORM = "verify-mobile-number.ftl";
	private static final String VERIFY_MOBILE_NUMBER_FIELD = "phoneNumberVerified";
    
    @Override
	public InitiatedActionSupport initiatedActionSupport() {
		return InitiatedActionSupport.SUPPORTED;
	}

    @Override
    public void close() {
    }

    @Override
	public void evaluateTriggers(RequiredActionContext context) {
        UserModel user = context.getUser();
		String isVerifiedData = user.getFirstAttribute(VERIFY_MOBILE_NUMBER_FIELD);
        Boolean isVerifed = Boolean.parseBoolean(isVerifiedData);
		
		if (isVerifiedData == null || !isVerifed) {
			context.getUser().addRequiredAction(PROVIDER_ID);
		 	context.getAuthenticationSession().addRequiredAction(PROVIDER_ID);
		} 
	}

    @Override
	public void requiredActionChallenge(RequiredActionContext context) {
		// show initial form
        UserModel user = context.getUser();
        String mobileNumber = user.getFirstAttribute(MOBILE_NUMBER_FIELD);

        Map<String, String> configData = exotelConfig();
		Map<String, String> response = performAuthentication(configData, mobileNumber);

		String errorMessage = response.get("error_message");
		if (errorMessage != null && errorMessage != "") {
            context.form().setError("smsAuthSmsNotSent", errorMessage)
				.createErrorPage(Response.Status.INTERNAL_SERVER_ERROR);
			return;
		} 
		
		String verificationId = response.get(SmsConstants.EXOTEL_VERIFICATION_ID);
		String ttl = response.get(SmsConstants.CODE_TTL);

		AuthenticationSessionModel authSession = context.getAuthenticationSession();
		authSession.setAuthNote(SmsConstants.EXOTEL_VERIFICATION_ID, verificationId);
		authSession.setAuthNote(SmsConstants.CODE_TTL, ttl);

		context.challenge(createForm(context, null));
		return;
	}

    @Override
    public void processAction(RequiredActionContext context) {
        String enteredCode = context.getHttpRequest().getDecodedFormParameters().getFirst(SmsConstants.CODE);
        
        AuthenticationSessionModel authSession = context.getAuthenticationSession();
		String verificationId = authSession.getAuthNote(SmsConstants.EXOTEL_VERIFICATION_ID);
		String ttl = authSession.getAuthNote(SmsConstants.CODE_TTL);

        Map<String, String> configData = exotelConfig();
		Map<String, String> response = processAction(configData, verificationId, ttl, enteredCode);

		String errorMessage = response.get("error_message");
		if (errorMessage != null && errorMessage != "") {
			context.challenge(context.form().setError("smsAuthSmsNotSent", errorMessage)
                .createErrorPage(Response.Status.INTERNAL_SERVER_ERROR));
            return;
		}

		Boolean isValid = Boolean.parseBoolean(response.get("is_valid"));
		if (isValid) {
			if (Long.parseLong(ttl) < System.currentTimeMillis()) {
				// expired
				context.challenge(context.form().setError("smsAuthCodeExpired")
                .createErrorPage(Response.Status.BAD_REQUEST));
                return;
			} else {
				// valid
                UserModel user = context.getUser();
                
                user.setSingleAttribute(VERIFY_MOBILE_NUMBER_FIELD, "true");
                user.removeRequiredAction(PROVIDER_ID);
                context.getAuthenticationSession().removeRequiredAction(PROVIDER_ID);

				context.success();
			}
		} else {
			// invalid
			context.challenge(createForm(context, form -> form.addError(new FormMessage(VERIFY_MOBILE_NUMBER_FIELD, "smsAuthCodeInvalid"))));
			return;
		}
    }

    private Response createForm(RequiredActionContext context, Consumer<LoginFormsProvider> formConsumer) {
		LoginFormsProvider form = context.form();
		form.setAttribute("username", context.getUser().getUsername());

		String mobileNumber = context.getUser().getFirstAttribute(MOBILE_NUMBER_FIELD);
		form.setAttribute(MOBILE_NUMBER_FIELD, mobileNumber == null ? "" : mobileNumber);
		form.setAttribute("realm", context.getRealm());

		if (formConsumer != null) {
			formConsumer.accept(form);
		}

		return form.createForm(VERIFY_MOBILE_NUMBER_FORM);
	}

	private Map<String, String> exotelConfig() {
		Map<String, String> config = new HashMap<>();

		config.put(SmsConstants.EXOTEL_BASE_URL, "https://exoverify.exotel.com/v2");
		config.put(SmsConstants.EXOTEL_REQUEST_OTP_PATH, "/accounts/%s/verifications/sms");
		config.put(SmsConstants.EXOTEL_VERIFY_OTP_PATH, "/accounts/%s/verifications/sms/%s");
		config.put(SmsConstants.SIMULATION_MODE, "true");

		return config;
	}

}
