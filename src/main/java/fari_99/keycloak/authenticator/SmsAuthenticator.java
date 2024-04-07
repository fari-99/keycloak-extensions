package fari_99.keycloak.authenticator;

import fari_99.keycloak.authenticator.exotel.ExotelRequestOtpResponse;
import fari_99.keycloak.authenticator.exotel.ExotelVerifyOtpResponse;
import fari_99.keycloak.authenticator.gateway.SmsServiceFactory;
import jakarta.ws.rs.core.Response;

import java.util.HashMap;
import java.util.Map;

import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.Authenticator;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.AuthenticatorConfigModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.sessions.AuthenticationSessionModel;

/**
 * @author Niko Köbler, https://www.n-k.de, @dasniko
 */
public class SmsAuthenticator implements Authenticator {

	protected static final String MOBILE_NUMBER_FIELD = "phoneNumbers";
	private static final String MOBILE_NUMBER_VERIFIED_FIELD = "phoneNumberVerified";
	private static final String TPL_CODE = "login-sms.ftl";

	@Override
	public void authenticate(AuthenticationFlowContext context) {
		AuthenticatorConfigModel config = context.getAuthenticatorConfig();
		UserModel user = context.getUser();

		String mobileNumber = user.getFirstAttribute(MOBILE_NUMBER_FIELD);
		// mobileNumber of course has to be further validated on proper format, country code, ...

		Map<String, String> configData = config.getConfig();
		Map<String, String> response = performAuthentication(configData, mobileNumber);

		String errorMessage = response.get("error_message");
		if (errorMessage != null && errorMessage != "") {
			context.failureChallenge(AuthenticationFlowError.INTERNAL_ERROR,
				context.form().setError("smsAuthSmsNotSent", errorMessage)
				.createErrorPage(Response.Status.INTERNAL_SERVER_ERROR));
				return;
		} 
		
		String verificationId = response.get(SmsConstants.EXOTEL_VERIFICATION_ID);
		String ttl = response.get(SmsConstants.CODE_TTL);

		AuthenticationSessionModel authSession = context.getAuthenticationSession();
		authSession.setAuthNote(SmsConstants.EXOTEL_VERIFICATION_ID, verificationId);
		authSession.setAuthNote(SmsConstants.CODE_TTL, ttl);

		context.challenge(context.form().setAttribute("realm", context.getRealm()).createForm(TPL_CODE));
		return;
	}

	protected Map<String, String> performAuthentication(Map<String, String> config, String mobileNumber) {
		Map<String, String> responseAuth = new HashMap<>();

		try {
			ExotelRequestOtpResponse exotelResponse = SmsServiceFactory.getExotelService(config).exotelOtpRequest(mobileNumber);
			if (exotelResponse == null) {
				responseAuth.put("error_message", "Response from exotel is empty");
				return responseAuth;
			}

			if (exotelResponse.getResponse().getCode() != 200) {
				responseAuth.put("error_message", exotelResponse.getResponse().getErrorData().getDescription());
				return responseAuth;
			}

			String verificationId = exotelResponse.getResponse().getData().getVerificationID();
			Long ttl = exotelResponse.getResponse().getData().getExpirationInSeconds();

			responseAuth.put(SmsConstants.EXOTEL_VERIFICATION_ID, verificationId);
			responseAuth.put(SmsConstants.CODE_TTL, Long.toString(System.currentTimeMillis() + (ttl * 1000L)));

			return responseAuth;
		} catch (Exception e) {
			responseAuth.put("error_message", e.getMessage());
			return responseAuth;
		}
	}

	@Override
	public void action(AuthenticationFlowContext context) {
		AuthenticatorConfigModel config = context.getAuthenticatorConfig();
		String enteredCode = context.getHttpRequest().getDecodedFormParameters().getFirst(SmsConstants.CODE);

		AuthenticationSessionModel authSession = context.getAuthenticationSession();
		String verificationId = authSession.getAuthNote(SmsConstants.EXOTEL_VERIFICATION_ID);
		String ttl = authSession.getAuthNote(SmsConstants.CODE_TTL);

		Map<String, String> configData = config.getConfig();
		Map<String, String> response = processAction(configData, verificationId, ttl, enteredCode);

		String errorMessage = response.get("error_message");
		if (errorMessage != null && errorMessage != "") {
			context.failureChallenge(AuthenticationFlowError.INTERNAL_ERROR,
				context.form().setError("smsAuthSmsNotSent", errorMessage)
				.createErrorPage(Response.Status.INTERNAL_SERVER_ERROR));
				return;
		}

		Boolean isValid = Boolean.parseBoolean(response.get("is_valid"));
		if (isValid) {
			if (Long.parseLong(ttl) < System.currentTimeMillis()) {
				// expired
				context.failureChallenge(AuthenticationFlowError.EXPIRED_CODE,
					context.form().setError("smsAuthCodeExpired")
						.createErrorPage(Response.Status.BAD_REQUEST));
			} else {
				// valid
				context.success();
			}
		} else {
			// invalid
			AuthenticationExecutionModel execution = context.getExecution();
			if (execution.isRequired()) {
				context.failureChallenge(AuthenticationFlowError.INVALID_CREDENTIALS,
					context.form().setAttribute("realm", context.getRealm())
						.setError("smsAuthCodeInvalid").createForm(TPL_CODE));
			} else if (execution.isConditional() || execution.isAlternative()) {
				context.attempted();
			}
		}
	}

	protected Map<String, String> processAction(Map<String, String> config, String verificationId, String ttl, String enteredCode) {
		Map<String, String> responseAuth = new HashMap<>();
		responseAuth.put("is_valid", "false");
		
		if (verificationId == null || ttl == null) {
			responseAuth.put("error_message", "Verification ID or TTL is empty");
			return responseAuth;
		}

		try {
			ExotelVerifyOtpResponse exotelResponse = SmsServiceFactory.getExotelService(config).exotelVerifyOtp(verificationId, enteredCode);
			if (exotelResponse == null) {
				responseAuth.put("error_message", Response.Status.INTERNAL_SERVER_ERROR.getReasonPhrase());
				return responseAuth;
			}

			if (exotelResponse.getResponse().getCode() == 200) {
				responseAuth.put("is_valid", "true");
			}
			
			return responseAuth;
		}  catch (Exception e) {
			responseAuth.put("error_message", e.getMessage());
			return responseAuth;
		}
	}

	@Override
	public boolean requiresUser() {
		return true;
	}

	@Override
	public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
		return user.getFirstAttribute(MOBILE_NUMBER_FIELD) != null;
	}

	@Override
	public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {
		String mobilePhone = user.getFirstAttribute(MOBILE_NUMBER_FIELD);
		String isVerifiedData = user.getFirstAttribute(MOBILE_NUMBER_VERIFIED_FIELD);
		Boolean isVerified = Boolean.parseBoolean(isVerifiedData);

		if (mobilePhone == null || mobilePhone == "") {
			user.addRequiredAction("phone-number-ra");
			return;
		}

		if (isVerifiedData == null || !isVerified) {
			user.addRequiredAction("verify-phone-number-ra");
			return;
		}
	}

	@Override
	public void close() {
	}

}
