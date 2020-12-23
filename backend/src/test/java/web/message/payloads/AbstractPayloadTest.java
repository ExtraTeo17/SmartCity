package web.message.payloads;


import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AbstractPayloadTest {

    @Test
    void toString_fourFields_correctResult() {
        var payloadImpl = new AbstractPayload() {
            private final int val1 = 1;
            private final float val2 = 10.022f;
            private final double val3 = 0.044;
            private final String val4 = "My_string";
        };

        String result = payloadImpl.toString();

        String expected = "(val1: " + payloadImpl.val1 +
                ", val2: " + payloadImpl.val2 +
                ", val3: " + payloadImpl.val3 +
                ", val4: " + payloadImpl.val4 +
                ")";
        assertEquals(expected, result);
    }

    @Test
    void toString_noFields_emptyResult() {
        var payloadImpl = new AbstractPayload() {};

        String result = payloadImpl.toString();

        String expected = "()";
        assertEquals(expected, result);
    }
}