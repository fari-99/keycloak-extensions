package fari_99.keycloak.authenticator;

import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.Authenticator;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

import jakarta.ws.rs.core.Response;

public class SecondaryEmailForm implements Authenticator {
    
    private static final String SECONDARY_EMAIL_FIELD = "secondaryEmail";
    private static final String SECONDARY_EMAIL_FORM = "login-secondary-email-form.ftl";

    @Override
    public void close() {
        
    }

    @Override
    public void authenticate(AuthenticationFlowContext context) {
        context.challenge(context.form().setAttribute("realm", context.getRealm()).createForm(SECONDARY_EMAIL_FORM));
    }

    @Override
    public void action(AuthenticationFlowContext context) {
        // Retrieve the username or email submitted by the user
        String email = context.getHttpRequest().getDecodedFormParameters().getFirst(SECONDARY_EMAIL_FIELD);

        // Find user by secondary email
        UserModel user = context.getSession().users().searchForUserByUserAttributeStream(context.getRealm(), SECONDARY_EMAIL_FIELD, email).findFirst().orElse(null);
        
        if (user != null) {
            context.setUser(user);
            context.success();
        } else {
            context.failureChallenge(AuthenticationFlowError.CLIENT_NOT_FOUND,
					context.form().setError("secondaryEmailFormNotFound", "secondary email not found")
						.createErrorPage(Response.Status.NOT_FOUND));
				return;
        }
    }

    @Override
    public boolean requiresUser() {
        return false;
    }

    @Override
    public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
        return false;
    }

    @Override
    public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {
    }
    
}
