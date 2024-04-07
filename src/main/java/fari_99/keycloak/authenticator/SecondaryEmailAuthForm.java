package fari_99.keycloak.authenticator;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.Authenticator;
import org.keycloak.common.util.SecretGenerator;
import org.keycloak.email.freemarker.FreeMarkerEmailTemplateProvider;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.utils.FormMessage;
import org.keycloak.services.validation.Validation;
import org.keycloak.sessions.AuthenticationSessionModel;

import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import lombok.experimental.UtilityClass;
import fari_99.keycloak.required_action.SecondaryEmailRequiredAction;
import fari_99.keycloak.required_action.VerifySecondaryEmailRequiredAction;

public class SecondaryEmailAuthForm implements Authenticator {

    protected static final String SECONDARY_EMAIL_FIELD = "secondaryEmail";
	private static final String SECONDARY_EMAIL_VERIFIED_FIELD = "secondaryEmailVerified";
    private static final String SECONDARY_EMAIL_OTP_FORM = "login-secondary-email-otp.ftl";
    private static final String SECONDARY_EMAIL_OTP_EMAIL = "email-login-secondary-email-otp.ftl";
    private static final Long TTL = (long) 5; // in minutes
	private static final Integer OTP_LENGTH = 8;
	private static final Integer OTP_COUNTER = 3;

    @UtilityClass
	public class VerifySecondaryEmailConstant {
		public String CODE = "code";
		public String TTL = "ttl";
		public String COUNT = "count";
	}

    @Override
    public void close() {}

    @Override
    public void authenticate(AuthenticationFlowContext context) {
        KeycloakSession keycloakSession = context.getSession();
		UserModel user = context.getUser();
		AuthenticationSessionModel authSession = context.getAuthenticationSession();
		
		String code = SecretGenerator.getInstance().randomString(OTP_LENGTH, SecretGenerator.DIGITS);
		String ttl = Long.toString(System.currentTimeMillis() + (TTL * 1000L * 60L));

		// Send email
		FreeMarkerEmailTemplateProvider emailTemplateProvider = new FreeMarkerEmailTemplateProvider(keycloakSession);
		emailTemplateProvider.setRealm(context.getRealm());
		emailTemplateProvider.setUser(user);
		emailTemplateProvider.setAuthenticationSession(authSession);

		List<Object> subjectAttribute = Collections.emptyList();
		Map<String, Object> bodyAttribute = new HashMap<>();
		bodyAttribute.put("otpCode", code);

		try {
			String subjectKey = "subjectVerifySecondaryEmail";
			String emailTemplate = SECONDARY_EMAIL_OTP_EMAIL;
			emailTemplateProvider.send(subjectKey, subjectAttribute, emailTemplate, bodyAttribute);
		} catch(Exception e) {
			e.printStackTrace();

			context.challenge(context.form().setError("verifySecondaryEmailNotSent", e.getMessage())
				.createErrorPage(Response.Status.INTERNAL_SERVER_ERROR));
			return;
		}

		// Set auth note
		authSession.setAuthNote(VerifySecondaryEmailConstant.CODE, code);
		authSession.setAuthNote(VerifySecondaryEmailConstant.TTL, ttl);
		authSession.setAuthNote(VerifySecondaryEmailConstant.COUNT, "0");

		// show initial form
		context.challenge(createForm(context, null));
    }

    @Override
    public void action(AuthenticationFlowContext context) {
// submitted form
		MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();
		String enteredCode = formData.getFirst(VerifySecondaryEmailConstant.CODE);
		
		if (Validation.isBlank(enteredCode) || enteredCode.length() != OTP_LENGTH) {
			context.challenge(createForm(context, form -> form.addError(new FormMessage(SECONDARY_EMAIL_VERIFIED_FIELD, "Invalid input"))));
			return;
		}

		AuthenticationSessionModel authSession = context.getAuthenticationSession();
		String otpCode = authSession.getAuthNote(VerifySecondaryEmailConstant.CODE);
		String ttl = authSession.getAuthNote(VerifySecondaryEmailConstant.TTL);
		String count = authSession.getAuthNote(VerifySecondaryEmailConstant.COUNT);

		Integer counter = Integer.parseInt(count);
		counter++;

		if (counter > OTP_COUNTER) {
			// too many try
			context.challenge(context.form().setError("verifySecondaryEmailCounterTooMany")
				.createErrorPage(Response.Status.TOO_MANY_REQUESTS));
			return;
		}

		// set new counter
		authSession.setAuthNote(VerifySecondaryEmailConstant.COUNT, Integer.toString(counter));

		Boolean isValid = false;
		if (enteredCode.equals(otpCode)) {
			isValid = true;
		}

		if (isValid) {
			if (Long.parseLong(ttl) < System.currentTimeMillis()) {
				// expired
				context.challenge(context.form().setError("verifySecondaryEmailExpired")
                	.createErrorPage(Response.Status.BAD_REQUEST));
                return;
			} else {
				// valid
                UserModel user = context.getUser();
                
                user.setSingleAttribute(SECONDARY_EMAIL_VERIFIED_FIELD, "true");
                user.removeRequiredAction(VerifySecondaryEmailRequiredAction.PROVIDER_ID);
                context.getAuthenticationSession().removeRequiredAction(VerifySecondaryEmailRequiredAction.PROVIDER_ID);

				context.success();
			}
		} else {
			// invalid
			context.challenge(createForm(context, form -> form.addError(new FormMessage(SECONDARY_EMAIL_VERIFIED_FIELD, "verifySecondaryEmailInvalid"))));
			return;
		}
    }

    @Override
    public boolean requiresUser() {
        return true;
    }

    @Override
    public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
        String secondaryEmail = user.getFirstAttribute(SECONDARY_EMAIL_FIELD);
        String secondaryEmailVerifiedData = user.getFirstAttribute(SECONDARY_EMAIL_VERIFIED_FIELD);
        Boolean secondaryEmailVerified = Boolean.parseBoolean(secondaryEmailVerifiedData);

		return secondaryEmail != null && secondaryEmailVerified;
    }

    @Override
    public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {
        String secondaryEmail = user.getFirstAttribute(SECONDARY_EMAIL_FIELD);
        String secondaryEmailVerifiedData = user.getFirstAttribute(SECONDARY_EMAIL_VERIFIED_FIELD);
        Boolean secondaryEmailVerified = Boolean.parseBoolean(secondaryEmailVerifiedData);

		if (secondaryEmail == null) {
            user.addRequiredAction(SecondaryEmailRequiredAction.PROVIDER_ID);
            return;
        }

        if (!secondaryEmailVerified) {
            user.addRequiredAction(VerifySecondaryEmailRequiredAction.PROVIDER_ID);
            return;
        }
    }

    private Response createForm(AuthenticationFlowContext context, Consumer<LoginFormsProvider> formConsumer) {
		LoginFormsProvider form = context.form();
		form.setAttribute("username", context.getUser().getUsername());

		String secondaryEmail = context.getUser().getFirstAttribute(SECONDARY_EMAIL_VERIFIED_FIELD);
		form.setAttribute(SECONDARY_EMAIL_VERIFIED_FIELD, secondaryEmail == null ? "" : secondaryEmail);

		if (formConsumer != null) {
			formConsumer.accept(form);
		}

		return form.createForm(SECONDARY_EMAIL_OTP_FORM);
	}
    
}
