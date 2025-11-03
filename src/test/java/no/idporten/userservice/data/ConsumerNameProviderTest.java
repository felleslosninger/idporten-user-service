package no.idporten.userservice.data;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ConsumerNameProviderTest {

    @Test
    void getConsumerName() {
        String consumerName = ConsumerNameProvider.getConsumerName();
        String secondanother = ConsumerNameProvider.getConsumerName();

        assertEquals(consumerName, secondanother);
    }
}