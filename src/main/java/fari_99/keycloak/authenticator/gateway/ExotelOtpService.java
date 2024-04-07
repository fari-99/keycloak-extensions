package fari_99.keycloak.authenticator.gateway;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;

import fari_99.keycloak.authenticator.SmsConstants;
import fari_99.keycloak.authenticator.exotel.ExotelRequestOtpResponse;
import fari_99.keycloak.authenticator.exotel.ExotelVerifyOtpResponse;
import okhttp3.Credentials;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ExotelOtpService implements SmsService {
    private final String accountSid;
    private final String applicationId;
    private final String secretKey;
    private final String baseUrl;
    private final String requestOtpPath;
    private final String verifyOtpPath;

    ExotelOtpService(Map<String, String> config) throws Exception {
		accountSid = System.getenv("EXOTEL_ACCOUNT_SID");
        applicationId = System.getenv("EXOTEL_APPLICATION_ID");
        secretKey = System.getenv("EXOTEL_APP_SECRET_KEY");

        if (accountSid == null || accountSid == "" || applicationId == null || applicationId == "" || secretKey == null || secretKey == "") {
            throw new Exception("account sid, application id, or secret key Exotel is not set properly on environment for request otp");
        }

        baseUrl = config.get(SmsConstants.EXOTEL_BASE_URL);
        requestOtpPath = config.get(SmsConstants.EXOTEL_REQUEST_OTP_PATH);
        verifyOtpPath = config.get(SmsConstants.EXOTEL_VERIFY_OTP_PATH);
	}

    public static String toJson(Map<String, Object> data) {
        Gson gson = new Gson();
        return gson.toJson(data);
    }
 
    @Override
	public ExotelRequestOtpResponse exotelOtpRequest(String phoneNumber) {
        OkHttpClient client = new OkHttpClient();
        
        Map<String, Object> data = new HashMap<>();
        data.put("phone_number", phoneNumber);
        data.put("application_id", applicationId);
        data.put("app_secret_key", secretKey);

        String json = toJson(data);
        RequestBody body = RequestBody.create(json, MediaType.get("application/json; charset=utf-8"));

        String credentials = Credentials.basic(applicationId, secretKey);
        String requestOtpUrl = baseUrl + requestOtpPath;

        Request request = new Request.Builder()
				.url(String.format(requestOtpUrl, accountSid))
                .addHeader("Authorization", credentials)
                .addHeader("Content-Type", "application/json")
                .post(body)
				.build();

        try {
			Response response = client.newCall(request).execute();
			Gson connect = new Gson();
			String res = null;
			try {
				res = response.body().string();
                ExotelRequestOtpResponse exotelResponse = connect.fromJson(res, ExotelRequestOtpResponse.class);

                return exotelResponse;
			} catch (IOException error) {
				error.printStackTrace();
			}

		} catch (Exception error) {
			error.printStackTrace();
		}

        return null;
	}

    @Override
	public ExotelVerifyOtpResponse exotelVerifyOtp(String verificationId, String OTP) {
        OkHttpClient client = new OkHttpClient();
        
        Map<String, Object> data = new HashMap<>();
        data.put("OTP", OTP);

        String json = toJson(data);
        RequestBody body = RequestBody.create(json, MediaType.get("application/json; charset=utf-8"));

        String credentials = Credentials.basic(applicationId, secretKey);
        String verifyOtpUrl = baseUrl + verifyOtpPath;

        Request request = new Request.Builder()
				.url(String.format(verifyOtpUrl, accountSid, verificationId))
                .addHeader("Authorization", credentials)
                .addHeader("Content-Type", "application/json")
                .post(body)
				.build();

        try {
			Response response = client.newCall(request).execute();
			Gson connect = new Gson();
			String res = null;
			try {
				res = response.body().string();
                ExotelVerifyOtpResponse exotelResponse = connect.fromJson(res, ExotelVerifyOtpResponse.class);
                
                return exotelResponse;
			} catch (IOException error) {
				error.printStackTrace();
			}

		} catch (Exception error) {
			error.printStackTrace();
		}

        return null;
	}
}
