package fi.livi.digitraffic.common.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MqttUtilTest {

    private static final String WEATHER_TOPIC = "weather-v2/%d/%d";
    private static final String WEATHER_STATUS_TOPIC = "weather-v2/status";
    // maintenance-v2/trackings/{domain}
    public static final String MAINTENANCE_TRACKING_V2_TOPIC = "maintenance-v2/routes/%s";

    @Test
    public void getTopicForMessage() {
        Assertions.assertEquals("weather-v2/1/2", MqttUtil.getTopicForMessage(WEATHER_TOPIC, 1, 2));
        Assertions.assertEquals(WEATHER_STATUS_TOPIC, MqttUtil.getTopicForMessage(WEATHER_STATUS_TOPIC));
        Assertions.assertEquals("maintenance-v2/routes/state-roads", MqttUtil.getTopicForMessage(MAINTENANCE_TRACKING_V2_TOPIC, "state-roads"));
        Assertions.assertEquals("topic/foo/10", MqttUtil.getTopicForMessage("topic/%s/%d", "foo" , 10));
    }
}
