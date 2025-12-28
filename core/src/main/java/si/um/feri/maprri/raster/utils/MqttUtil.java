package si.um.feri.maprri.raster.utils;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;

public class MqttUtil {
    public MqttClient client;
    private String broker = EnvUtil.get("MQTT_BROKER");
    private String username = EnvUtil.get("MQTT_USERNAME");
    private String password = EnvUtil.get("MQTT_PASSWORD");

    public MqttUtil() {
        try {
            client = new MqttClient(broker, MqttClient.generateClientId());

            MqttConnectOptions options = new MqttConnectOptions();
            options.setUserName(username);
            options.setPassword(password.toCharArray());

            client.connect(options);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
