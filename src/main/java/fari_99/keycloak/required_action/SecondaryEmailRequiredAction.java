package fari_99.keycloak.required_action;

import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import org.keycloak.authentication.InitiatedActionSupport;
import org.keycloak.authentication.RequiredActionContext;
import org.keycloak.authentication.RequiredActionProvider;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.UserModel;
import org.keycloak.models.utils.FormMessage;
import org.keycloak.services.validation.Validation;

import java.util.function.Consumer;

public class SecondaryEmailRequiredAction implements RequiredActionProvider {

	public static final String PROVIDER_ID = "secondary-email-ra";
	private static final String UPDATE_SECONDARY_EMAIL_FORM = "update-secondary-email.ftl";
	private static final String SECONDARY_EMAIL_FIELD = "secondaryEmail";

	@Override
	public InitiatedActionSupport initiatedActionSupport() {
		return InitiatedActionSupport.SUPPORTED;
	}

	@Override
	public void evaluateTriggers(RequiredActionContext context) {
		UserModel user = context.getUser();
        String secondaryEmail = user.getFirstAttribute(SECONDARY_EMAIL_FIELD);

		if (secondaryEmail == null) {
			context.getUser().addRequiredAction(PROVIDER_ID);
		 	context.getAuthenticationSession().addRequiredAction(PROVIDER_ID);
		}
	}

	@Override
	public void requiredActionChallenge(RequiredActionContext context) {
		// show initial form
		context.challenge(createForm(context, null));
	}

	@Override
	public void processAction(RequiredActionContext context) {
		// submitted form

		UserModel user = context.getUser();

		MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();
		String secondaryEmail = formData.getFirst(SECONDARY_EMAIL_FIELD);

		if (Validation.isBlank(secondaryEmail) || !Validation.isEmailValid(secondaryEmail)) {
			context.challenge(createForm(context, form -> form.addError(new FormMessage(SECONDARY_EMAIL_FIELD, "Invalid input"))));
			return;
		}

		user.setSingleAttribute(SECONDARY_EMAIL_FIELD, secondaryEmail);
		user.removeRequiredAction(PROVIDER_ID);
		context.getAuthenticationSession().removeRequiredAction(PROVIDER_ID);

		context.success();
	}

	@Override
	public void close() {
	}

	private Response createForm(RequiredActionContext context, Consumer<LoginFormsProvider> formConsumer) {
		LoginFormsProvider form = context.form();
		form.setAttribute("username", context.getUser().getUsername());

		String secondaryEmail = context.getUser().getFirstAttribute(SECONDARY_EMAIL_FIELD);
		form.setAttribute(SECONDARY_EMAIL_FIELD, secondaryEmail == null ? "" : secondaryEmail);

		if (formConsumer != null) {
			formConsumer.accept(form);
		}

		return form.createForm(UPDATE_SECONDARY_EMAIL_FORM);
	}

}
