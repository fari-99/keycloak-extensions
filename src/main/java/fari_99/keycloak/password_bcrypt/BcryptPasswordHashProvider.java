package fari_99.keycloak.password_bcrypt;

import org.keycloak.credential.hash.PasswordHashProvider;
import org.keycloak.models.PasswordPolicy;
import org.keycloak.models.credential.PasswordCredentialModel;
import org.springframework.security.crypto.bcrypt.BCrypt;

public class BcryptPasswordHashProvider implements PasswordHashProvider {
    
	private final String providerId;
	public static final String ALGORITHM = "Bcrypt";

	public BcryptPasswordHashProvider(String providerId) {
		this.providerId = providerId;
	}

	@Override
	public void close() {
	}

	@Override
	public boolean policyCheck(PasswordPolicy policy, PasswordCredentialModel credential) {
		return this.providerId.equals(credential.getPasswordCredentialData().getAlgorithm());
	}

	@Override
	public PasswordCredentialModel encodedCredential(String rawPassword, int log_rounds) {
		String encodedPassword = this.encode(rawPassword, 10); // default log_rounds is 10
		return PasswordCredentialModel.createFromValues(this.providerId, new byte[0], log_rounds, encodedPassword);
	}

	@Override
	public boolean verify(String rawPassword, PasswordCredentialModel credential) {
		String hash = credential.getPasswordSecretData().getValue();
		return BCrypt.checkpw(rawPassword, hash);
	}

	@Override
	public String encode(String rawPassword, int log_rounds) {
		try {
			String BycryptSalt;
            if (log_rounds < 10 || log_rounds > 31) { // default 10 for log_rounds
                BycryptSalt = BCrypt.gensalt();
            } else {
				BycryptSalt = BCrypt.gensalt(log_rounds);
			}

			String hashedPassword = BCrypt.hashpw(rawPassword, BycryptSalt);
            return hashedPassword;
		} catch (Exception e) {
			// fail silently
		}

		return null;
	}
}