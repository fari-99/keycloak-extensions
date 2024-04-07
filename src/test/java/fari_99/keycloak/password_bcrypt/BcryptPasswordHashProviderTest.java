package fari_99.keycloak.password_bcrypt;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.keycloak.models.credential.PasswordCredentialModel;

public class BcryptPasswordHashProviderTest {
    private static final String password = "password";

    @Test
	public void encodeHelloWorld() {
		final var provider = new BcryptPasswordHashProvider(BcryptPasswordHashProviderFactory.ID);
		var passwordHash = "$2y$10$wfqzlWWzEBWcQ3Kw4tcIauc5jLE6b9i6RB0Mz12RDnM2.VCFIZuBK";
        PasswordCredentialModel passwordCred = PasswordCredentialModel.createFromValues(
            BcryptPasswordHashProviderFactory.ID, 
            null, 
            0, 
            passwordHash);
		
        assertTrue(provider.verify(password, passwordCred));
	}
	
}
