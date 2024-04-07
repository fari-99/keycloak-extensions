package fari_99.keycloak.authenticator.gateway;

import fari_99.keycloak.authenticator.SmsConstants;
import java.util.Map;

/**
 * @author Niko Köbler, https://www.n-k.de, @dasniko
 */
public class SmsServiceFactory {

	public static SmsService getExotelService(Map<String, String> config) throws Exception {
		if (Boolean.parseBoolean(config.getOrDefault(SmsConstants.SIMULATION_MODE, "false"))) {
			return new ExotelSimulationService();
		} else {
			return new ExotelOtpService(config);
		}
	}
}
