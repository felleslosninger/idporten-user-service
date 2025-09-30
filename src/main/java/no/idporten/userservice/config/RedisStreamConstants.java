package no.idporten.userservice.config;

public class RedisStreamConstants {

    // Streamgroups
    public final static String EID_GROUP = "EID-GROUP";

    // Streamnames
    public final static String UPDATE_EID_STREAM = "update-eid";

    // Consumernames
    public final static String EID_UPDATER = "update-eid";
    public final static String EID_RETRY_UPDATER = "-retry-update-eid";

}
