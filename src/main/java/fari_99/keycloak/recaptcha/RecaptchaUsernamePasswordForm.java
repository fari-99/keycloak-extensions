package fari_99.keycloak.recaptcha;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.jboss.logging.Logger;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.authenticators.browser.UsernamePasswordForm;
import org.keycloak.connections.httpclient.HttpClientProvider;
import org.keycloak.events.Details;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.AuthenticatorConfigModel;
import org.keycloak.models.utils.FormMessage;
import org.keycloak.services.ServicesLogger;
import org.keycloak.services.messages.Messages;
import org.keycloak.services.validation.Validation;
import org.keycloak.util.JsonSerialization;

import lombok.extern.slf4j.Slf4j;

import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@SuppressWarnings("unused")
@Slf4j
public class RecaptchaUsernamePasswordForm extends UsernamePasswordForm {
	public static final String G_RECAPTCHA_RESPONSE = "g-recaptcha-response";
	public static final String SITE_KEY = "site.key";
	public static final String SITE_SECRET = "secret";
	public static final String USE_RECAPTCHA_NET = "useRecaptchaNet";
	public static final String RECAPTCHA_PAGE = "login-recaptcha.ftl";
	private static final Logger logger = Logger.getLogger(RecaptchaUsernamePasswordForm.class);

	private String siteKey;

	@Override
	protected Response createLoginForm(LoginFormsProvider form ) {
		form.setAttribute("recaptchaRequired", true);
		form.setAttribute("recaptchaSiteKey", siteKey);
		return super.createLoginForm( form );
	}

	@Override
	public void authenticate(AuthenticationFlowContext context) {
		context.getEvent().detail(Details.AUTH_METHOD, "auth_method");
		if (logger.isInfoEnabled()) {
			logger.info(
					"validateRecaptcha(AuthenticationFlowContext, boolean, String, String) - Before the validation");
		}

		AuthenticatorConfigModel captchaConfig = context.getAuthenticatorConfig();
		String userLanguageTag = context.getSession().getContext().resolveLocale(context.getUser()).toLanguageTag();

		if (captchaConfig == null || captchaConfig.getConfig() == null
				|| captchaConfig.getConfig().get(SITE_KEY) == null
				|| captchaConfig.getConfig().get(SITE_SECRET) == null) {
			context.failureChallenge(AuthenticationFlowError.IDENTITY_PROVIDER_NOT_FOUND, 
				context.form().setError(Messages.RECAPTCHA_NOT_CONFIGURED, "Captcha not configured").
				createErrorPage(Response.Status.INTERNAL_SERVER_ERROR));
			return;
		}

		siteKey = captchaConfig.getConfig().get(SITE_KEY);
		LoginFormsProvider form = context.form();
		form.setAttribute("recaptchaRequired", true);
		form.setAttribute("recaptchaSiteKey", siteKey);
		form.addScript("https://www." + getRecaptchaDomain(captchaConfig) + "/recaptcha/api.js?hl=" + userLanguageTag);
		context.challenge(
			form.createForm(RECAPTCHA_PAGE)
		);
		return;
	}

	@Override
	public void action(AuthenticationFlowContext context) {
		if (logger.isDebugEnabled()) {
			logger.debug("action(AuthenticationFlowContext) - start");
		}
		
		MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();
		List<FormMessage> errors = new ArrayList<>();
		boolean success = false;
		context.getEvent().detail(Details.AUTH_METHOD, "auth_method");

		AuthenticatorConfigModel captchaConfig = context.getAuthenticatorConfig();
		String userLanguageTag = context.getSession().getContext().resolveLocale(context.getUser()).toLanguageTag();

		LoginFormsProvider form = context.form();
		form.setAttribute("recaptchaRequired", true);
		form.setAttribute("recaptchaSiteKey", siteKey);
		form.addScript("https://www." + getRecaptchaDomain(captchaConfig) + "/recaptcha/api.js?hl=" + userLanguageTag);

		String captcha = formData.getFirst(G_RECAPTCHA_RESPONSE);
		if (!Validation.isBlank(captcha)) {
			String secret = captchaConfig.getConfig().get(SITE_SECRET);
			success = validateRecaptcha(context, success, captcha, secret);
		}

		if (!success) {
			errors.add(new FormMessage(null, Messages.RECAPTCHA_FAILED));
			formData.remove(G_RECAPTCHA_RESPONSE);
			context.failureChallenge(AuthenticationFlowError.INVALID_CREDENTIALS, 
				form.setError("Invalid captcha, please try again.").createForm(RECAPTCHA_PAGE));
			return;
		}

		if (logger.isDebugEnabled()) {
			logger.debug("action(AuthenticationFlowContext) - end");
		}

		super.action(context);
	}

	private String getRecaptchaDomain(AuthenticatorConfigModel config) {
		Boolean useRecaptcha = Optional.ofNullable(config)
				.map(configModel -> configModel.getConfig())
				.map(cfg -> Boolean.valueOf(cfg.get(USE_RECAPTCHA_NET)))
				.orElse(false);
		if (useRecaptcha) {
			return "recaptcha.net";
		}

		return "google.com";
	}

	protected boolean validateRecaptcha(AuthenticationFlowContext context, boolean success, String captcha, String secret) {
		HttpClient httpClient = context.getSession().getProvider(HttpClientProvider.class).getHttpClient();
		HttpPost post = new HttpPost("https://www." + getRecaptchaDomain(context.getAuthenticatorConfig()) + "/recaptcha/api/siteverify");
		List<NameValuePair> formparams = new LinkedList<>();
		formparams.add(new BasicNameValuePair("secret", secret));
		formparams.add(new BasicNameValuePair("response", captcha));
		formparams.add(new BasicNameValuePair("remoteip", context.getConnection().getRemoteAddr()));
		try {
			UrlEncodedFormEntity form = new UrlEncodedFormEntity(formparams, "UTF-8");
			post.setEntity(form);
			HttpResponse response = httpClient.execute(post);
			InputStream content = response.getEntity().getContent();
			try {
				@SuppressWarnings("rawtypes")
				Map json = JsonSerialization.readValue(content, Map.class);
				Object val = json.get("success");
				success = Boolean.TRUE.equals(val);
			} finally {
				content.close();
			}
		} catch (Exception e) {
			ServicesLogger.LOGGER.recaptchaFailed(e);
		}
		return success;
	}

}
