package fari_99.keycloak.required_action;

import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import org.keycloak.authentication.InitiatedActionSupport;
import org.keycloak.authentication.RequiredActionContext;
import org.keycloak.authentication.RequiredActionProvider;
import org.keycloak.common.util.SecretGenerator;
import org.keycloak.email.freemarker.FreeMarkerEmailTemplateProvider;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.UserModel;
import org.keycloak.models.utils.FormMessage;
import org.keycloak.services.validation.Validation;
import org.keycloak.sessions.AuthenticationSessionModel;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Slf4j
public class VerifySecondaryEmailRequiredAction implements RequiredActionProvider {

	public static final String PROVIDER_ID = "verify-secondary-email-ra";
	private static final String VERIFY_SENDARY_EMAIL_FORM = "verify-secondary-email.ftl";
	private static final String VERIFIED_SECONDARY_EMAIL_FIELD = "secondaryEmailVerified";
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
	public InitiatedActionSupport initiatedActionSupport() {
		return InitiatedActionSupport.SUPPORTED;
	}

	@Override
	public void evaluateTriggers(RequiredActionContext context) {
		UserModel user = context.getUser();
        String isVerifiedAttribute = user.getFirstAttribute(VERIFIED_SECONDARY_EMAIL_FIELD);
		Boolean isVerified = Boolean.parseBoolean(isVerifiedAttribute);

		if (!isVerified) {
			context.getUser().addRequiredAction(PROVIDER_ID);
		 	context.getAuthenticationSession().addRequiredAction(PROVIDER_ID);
		}
	}

	@Override
	public void requiredActionChallenge(RequiredActionContext context) {
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
			String emailTemplate = "email-verify-secondary-email.ftl";
			emailTemplateProvider.send(subjectKey, subjectAttribute, emailTemplate, bodyAttribute);
		} catch(Exception e) {
			log.info(e.getMessage());
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
	public void processAction(RequiredActionContext context) {
		// submitted form
		MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();
		String enteredCode = formData.getFirst(VerifySecondaryEmailConstant.CODE);
		
		if (Validation.isBlank(enteredCode) || enteredCode.length() != OTP_LENGTH) {
			context.challenge(createForm(context, form -> form.addError(new FormMessage(VERIFIED_SECONDARY_EMAIL_FIELD, "Invalid input"))));
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
                
                user.setSingleAttribute(VERIFIED_SECONDARY_EMAIL_FIELD, "true");
                user.removeRequiredAction(PROVIDER_ID);
                context.getAuthenticationSession().removeRequiredAction(PROVIDER_ID);

				context.success();
			}
		} else {
			// invalid
			context.challenge(createForm(context, form -> form.addError(new FormMessage(VERIFIED_SECONDARY_EMAIL_FIELD, "verifySecondaryEmailInvalid"))));
			return;
		}
	}

	@Override
	public void close() {
	}

	private Response createForm(RequiredActionContext context, Consumer<LoginFormsProvider> formConsumer) {
		LoginFormsProvider form = context.form();
		form.setAttribute("username", context.getUser().getUsername());

		String secondaryEmail = context.getUser().getFirstAttribute(VERIFIED_SECONDARY_EMAIL_FIELD);
		form.setAttribute(VERIFIED_SECONDARY_EMAIL_FIELD, secondaryEmail == null ? "" : secondaryEmail);

		if (formConsumer != null) {
			formConsumer.accept(form);
		}

		return form.createForm(VERIFY_SENDARY_EMAIL_FORM);
	}

}
