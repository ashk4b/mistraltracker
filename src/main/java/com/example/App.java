import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class OrangeLiveObjectsReceiver {

    // CONNECTION SETTINGS
    private static final String BROKER_URL = "ssl://liveobjects.orange-business.com:8883"; // Secure connection
    private static final String API_KEY = "YOUR_ORANGE_API_KEY_HERE"; // ** REPLACE THIS **

    // AUTHENTICATION
    // "json+bridge" tells Orange we are an App receiving data, not a Device sending it.
    private static final String USERNAME = "json+bridge";

    // TOPIC SUBSCRIPTION
    // This specific topic listens to NEW data from ALL LoRa devices.
    // format: router/~event/v1/data/new/urn:lo:nsid:lora:#
    private static final String TOPIC = "router/~event/v1/data/new/urn:lo:nsid:lora:#";

    public static void main(String[] args) {
        String clientId = "JavaApp_" + System.currentTimeMillis(); // Unique ID

        try {
            // 1. Create the Client
            MqttClient client = new MqttClient(BROKER_URL, clientId, new MemoryPersistence());

            // 2. Set Connection Options
            MqttConnectOptions options = new MqttConnectOptions();
            options.setUserName(USERNAME);
            options.setPassword(API_KEY.toCharArray());
            options.setCleanSession(true);
            options.setKeepAliveInterval(60);

            // 3. Define what happens when a message arrives
            client.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    System.out.println("Connection lost! " + cause.getMessage());
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) {
                    String payload = new String(message.getPayload());
                    System.out.println("\n--- NEW WEATHER DATA RECEIVED ---");
                    System.out.println("Topic: " + topic);
                    System.out.println("Message: " + payload);

                    // TODO: Add your JSON parsing logic here (e.g. using Jackson or Gson)
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    // Not used for receiving
                }
            });

            // 4. Connect
            System.out.println("Connecting to Orange Live Objects...");
            client.connect(options);
            System.out.println("Connected!");

            // 5. Subscribe
            System.out.println("Subscribing to topic: " + TOPIC);
            client.subscribe(TOPIC);
            System.out.println("Waiting for messages... (Press Ctrl+C to stop)");

        } catch (MqttException e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}