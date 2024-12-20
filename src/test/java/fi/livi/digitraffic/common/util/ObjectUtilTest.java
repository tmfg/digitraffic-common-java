package fi.livi.digitraffic.common.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ObjectUtilTest {

    private enum TestEnum {
        FIRST,
        SECOND;

        @Override
        public String toString() {
            return super.toString().charAt(0) + super.toString().substring(1).toLowerCase();
        }
    }

    @Test
    public void getTopicForMessage() {
        Assertions.assertNotEquals(TestEnum.FIRST.name(), TestEnum.FIRST.toString());
        Assertions.assertEquals("FIRST", ObjectUtil.getEnumName(TestEnum.FIRST));
    }

    @Test
    public void callIfNotNull() {
        Assertions.assertEquals("YES", ObjectUtil.callIfNotNull("", () -> "YES"));
    }

    @Test
    public void callIfNotNullWithNull() {
        ObjectUtil.callIfNotNull(null, () -> {
            throw new RuntimeException("Should not be called");
        });
    }

    @Test
    public void callAndIgnoreExeption() {
        Assertions.assertEquals("YES", ObjectUtil.callAndIgnoreExeption(() -> "YES"));
    }

    @Test
    public void callAndIgnoreExeptionThrows() {
        Assertions.assertNull(ObjectUtil.callAndIgnoreExeption(() -> {
            throw new RuntimeException("Should not be called");
        }));
    }
}
