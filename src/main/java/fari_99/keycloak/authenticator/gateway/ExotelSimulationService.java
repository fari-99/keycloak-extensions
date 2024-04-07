package fari_99.keycloak.authenticator.gateway;

import com.google.gson.Gson;

import lombok.extern.slf4j.Slf4j;
import fari_99.keycloak.authenticator.exotel.ExotelRequestOtpResponse;
import fari_99.keycloak.authenticator.exotel.ExotelVerifyOtpResponse;

@Slf4j
public class ExotelSimulationService implements SmsService {
    @Override
	public ExotelRequestOtpResponse exotelOtpRequest(String phoneNumber) {
        Gson gson = new Gson();
        String jsonInString = "{\"request_id\":\"881cf11407d54595a6902267d05eff4a\",\"method\":\"POST\",\"http_code\":200,\"response\":{\"code\":200,\"error_data\":null,\"status\":\"success\",\"data\":{\"verification_id\":\"463fac2fa3ece58c6551e296c1b9167c\",\"phone_number\":\"+918637XX2391\",\"application_id\":\"DASDADASASASDASD\",\"account_sid\":\"google\",\"max_attempts\":10,\"expiration_in_seconds\":60,\"url\":\"/v2/accounts/google/sms/verifications/463fac2fa3ece58c6551e296c1b9167c\",\"created_at\":\"2022-07-12T12:13:16Z\",\"updated_at\":\"2022-07-12T12:13:16Z\"}}}";
        ExotelRequestOtpResponse exotelResponse = gson.fromJson(jsonInString, ExotelRequestOtpResponse.class);

        String verifId = exotelResponse.getResponse().getData().getVerificationID();
        log.warn(String.format("***** SIMULATION MODE ***** Send SMS to user with verification Id : %s", verifId));    
        return exotelResponse;   
	}

    @Override
	public ExotelVerifyOtpResponse exotelVerifyOtp(String verificationId, String OTP) {
        Gson gson = new Gson();
        String jsonInString = "{\"request_id\":\"48f97c0f1cca46b09fb0e4255ef8a4eb\",\"method\":\"POST\",\"http_code\":200,\"response\":{\"code\":200,\"error_data\":null,\"status\":\"success\",\"data\":{\"verification_id\":\"463fac2fa3ece58c6551e296c1b9167c\",\"application_id\":\"DASDADASASASDASD\",\"account_sid\":\"DADAS\",\"status\":\"success\",\"created_at\":\"2022-07-12T12:15:55+05:30\",\"updated_at\":\"2022-07-12T12:15:55+05:30\"}}}";
        ExotelVerifyOtpResponse exotelResponse = gson.fromJson(jsonInString, ExotelVerifyOtpResponse.class);

        String verifId = exotelResponse.getResponse().getData().getVerificationID();
        log.warn(String.format("***** SIMULATION MODE ***** Send verification id to exotel with verification Id: %s", verifId));    
        return exotelResponse;
	}
}
