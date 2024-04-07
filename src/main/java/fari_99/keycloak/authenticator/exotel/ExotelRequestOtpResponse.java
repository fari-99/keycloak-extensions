package fari_99.keycloak.authenticator.exotel;

import com.google.gson.annotations.SerializedName;

public class ExotelRequestOtpResponse {

    @SerializedName("request_id")
    private String requestID;

    @SerializedName("method")
    private String method;

    @SerializedName("http_code")
    private long httpCode;

    @SerializedName("response")
    private Response response;

    public String getRequestID() { return requestID; }
    public void setRequestID(String value) { this.requestID = value; }

    public String getMethod() { return method; }
    public void setMethod(String value) { this.method = value; }

    public long getHTTPCode() { return httpCode; }
    public void setHTTPCode(long value) { this.httpCode = value; }

    public Response getResponse() { return response; }
    public void setResponse(Response value) { this.response = value; }

    public static class Response {

        @SerializedName("code")
        private long code;

        @SerializedName("error_data")
        private ErrorData errorData;

        @SerializedName("status")
        private String status;

        @SerializedName("data")
        private Data data;
    
        public long getCode() { return code; }
        public void setCode(long value) { this.code = value; }
    
        public ErrorData getErrorData() { return errorData; }
        public void setErrorData(ErrorData value) { this.errorData = value; }
    
        public String getStatus() { return status; }
        public void setStatus(String value) { this.status = value; }
    
        public Data getData() { return data; }
        public void setData(Data value) { this.data = value; }
    }

    public static class Data {

        @SerializedName("verification_id")
        private String verificationID;

        @SerializedName("phone_number")
        private String phoneNumber;

        @SerializedName("application_id")
        private String applicationID;

        @SerializedName("account_sid")
        private String accountSid;

        @SerializedName("max_attempts")
        private long maxAttempts;

        @SerializedName("expiration_in_seconds")
        private long expirationInSeconds;

        @SerializedName("url")
        private String url;

        @SerializedName("created_at")
        private String createdAt;

        @SerializedName("updated_at")
        private String updatedAt;
    
        public String getVerificationID() { return verificationID; }
        public void setVerificationID(String value) { this.verificationID = value; }
    
        public String getPhoneNumber() { return phoneNumber; }
        public void setPhoneNumber(String value) { this.phoneNumber = value; }
    
        public String getApplicationID() { return applicationID; }
        public void setApplicationID(String value) { this.applicationID = value; }
    
        public String getAccountSid() { return accountSid; }
        public void setAccountSid(String value) { this.accountSid = value; }
    
        public long getMaxAttempts() { return maxAttempts; }
        public void setMaxAttempts(long value) { this.maxAttempts = value; }
    
        public long getExpirationInSeconds() { return expirationInSeconds; }
        public void setExpirationInSeconds(long value) { this.expirationInSeconds = value; }
    
        public String getURL() { return url; }
        public void setURL(String value) { this.url = value; }
    
        public String getCreatedAt() { return createdAt; }
        public void setCreatedAt(String value) { this.createdAt = value; }
    
        public String getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(String value) { this.updatedAt = value; }
    }

    public class ErrorData {

        @SerializedName("code")
        private long code;

        @SerializedName("description")
        private String description;

        @SerializedName("message")
        private String message;
    
        public long getCode() { return code; }
        public void setCode(long value) { this.code = value; }
    
        public String getDescription() { return description; }
        public void setDescription(String value) { this.description = value; }
    
        public String getMessage() { return message; }
        public void setMessage(String value) { this.message = value; }
    }
    
}
