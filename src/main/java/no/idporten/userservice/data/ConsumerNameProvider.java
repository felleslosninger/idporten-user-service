package no.idporten.userservice.data;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ConsumerNameProvider {

    private static String name = null;

    public static String getConsumerName() {
        if (name != null) {
            return name;
        }

        String podName = System.getenv("POD_NAME");
        String podUid = System.getenv("POD_UID");

        if (podName == null) {
            podName = "local";
        }
        if (podUid == null) {
            podUid = UUID.randomUUID().toString();
        }
        name = String.format("consumer-%s-%s", podName, podUid);

        return name;
    }

}
