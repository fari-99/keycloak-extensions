package fari_99.keycloak.authenticator.gateway;

import fari_99.keycloak.authenticator.exotel.ExotelRequestOtpResponse;
import fari_99.keycloak.authenticator.exotel.ExotelVerifyOtpResponse;

/**
 * @author Niko Köbler, https://www.n-k.de, @dasniko
 */
public interface SmsService {
	ExotelRequestOtpResponse exotelOtpRequest(String phoneNumber);
	ExotelVerifyOtpResponse exotelVerifyOtp(String verificationId, String OTP);
}
