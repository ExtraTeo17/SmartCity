package web.message.payloads;

import org.junit.Assert;
import org.junit.Test;

public class AbstractPayloadTest {

    @Test
    public void toString_fourFields_correctResult() {
        var payloadImpl = new AbstractPayload() {
            public final int val1 = 1;
            public final float val2 = 10.022f;
            public final double val3 = 0.044;
            public final String val4 = "My_string";
        };

        String result = payloadImpl.toString();

        String expected = "(val1: " + payloadImpl.val1 +
                ", val2: " + payloadImpl.val2 +
                ", val3: " + payloadImpl.val3 +
                ", val4: " + payloadImpl.val4 +
                ")";
        Assert.assertEquals(expected, result);
    }

    @Test
    public void toString_noFields_emptyResult() {
        var payloadImpl = new AbstractPayload() {};

        String result = payloadImpl.toString();

        String expected = "()";
        Assert.assertEquals(expected, result);
    }
}