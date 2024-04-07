package fari_99.keycloak.authenticator;

import lombok.experimental.UtilityClass;

@UtilityClass
public class SmsConstants {
	public String CODE = "code";
	public String CODE_TTL = "ttl";
	public String SIMULATION_MODE = "simulation";

	public String EXOTEL_BASE_URL = "baseUrl";
	public String EXOTEL_REQUEST_OTP_PATH = "requestOtpPath";
	public String EXOTEL_VERIFY_OTP_PATH = "verifyOtpPath";
	public String EXOTEL_VERIFICATION_ID = "verificationId";
}
