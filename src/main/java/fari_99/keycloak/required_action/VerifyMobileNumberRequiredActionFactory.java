package fari_99.keycloak.required_action;

import org.keycloak.Config.Scope;
import org.keycloak.authentication.RequiredActionFactory;
import org.keycloak.authentication.RequiredActionProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

import com.google.auto.service.AutoService;

@AutoService(RequiredActionFactory.class)
public class VerifyMobileNumberRequiredActionFactory implements RequiredActionFactory {

    @Override
    public RequiredActionProvider create(KeycloakSession session) {
        return new VerifyMobileNumberRequiredAction();
    }

    @Override
    public void init(Scope config) {
        
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
        
    }

    @Override
    public void close() {
        
    }

    @Override
    public String getId() {
        return VerifyMobileNumberRequiredAction.PROVIDER_ID;
    }

    @Override
    public String getDisplayText() {
        return "Verify mobile number";
    }
    
}
