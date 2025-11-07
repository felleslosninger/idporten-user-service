package no.idporten.userservice.data;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ConsumerNameProviderTest {

    @Test
    @DisplayName("When getting consumer name multiple times, then the same name is returned")
    void getConsumerName() {
        String consumerName = ConsumerNameProvider.getConsumerName();
        String secondanother = ConsumerNameProvider.getConsumerName();

        assertEquals(consumerName, secondanother);
    }
}