package fi.livi.digitraffic.common.util;

public abstract class MqttUtil {

    protected MqttUtil() {
        throw new AssertionError("The " + getClass().getSimpleName() + " class methods should be accessed statically");
    }

    /**
     * Formats topic with {@code String.format(...)} i.e.
     * <pre>{@code getTopicForMessage("topic/%s/%d", "foo" , 10) => "topic/foo/10" }</pre>
     * @param topicStringFormat String format string to use for topic.
     * @param topicParams String format parameters.
     * @return Formatted topic string.
     */
    public static String getTopicForMessage(final String topicStringFormat, final Object...topicParams) {
        return String.format(topicStringFormat, topicParams);
    }
}
