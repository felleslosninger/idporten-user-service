package no.idporten.userservice.data;

import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity(name = "user")
@Table(name = "user")
public class UserEntity {

    @Id
    @GeneratedValue
    @Column(name = "uuid")
    @Type(type = "uuid-char")
    private UUID uuid;

    @Column(name = "person_identifier")
    private String personIdentifier;

    @Column(name = "user_created_ms")
    private long userCreatedAtEpochMs;

    @Column(name = "user_last_updated_ms")
    private long userLastUpdatedAtEpochMs;

    @Column(name = "active")
    private boolean active;

    @Column(name = "closed_code")
    private String closedCode;

    @Column(name = "closed_code_updated_ms")
    private long closedCodeUpdatedAtEpochMs;

    @Column(name = "help_desk_case_references")
    private String helpDeskCaseReferences;

    @OneToOne
    @JoinColumn(name = "previous_user")
    @Type(type = "uuid-char")
    private UserEntity previousUser;

    @OneToOne(mappedBy = "previousUser")
    private UserEntity nextUser;

    @OneToMany(mappedBy = "user", orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<LoginEntity> logins = new java.util.ArrayList<>();

    public UserEntity() {
    }

    public UserEntity(UUID uuid, String personIdentifier, long userCreatedAtEpochMs, long userLastUpdatedAtEpochMs, Boolean active, String closedCode, long closedCodeUpdatedAtEpochMs, String helpDeskCaseReferences, List<LoginEntity> logins) {
        this.uuid = uuid;
        this.personIdentifier = personIdentifier;
        this.userCreatedAtEpochMs = userCreatedAtEpochMs;
        this.userLastUpdatedAtEpochMs = userLastUpdatedAtEpochMs;
        this.active = active;
        this.closedCode = closedCode;
        this.closedCodeUpdatedAtEpochMs = closedCodeUpdatedAtEpochMs;
        this.helpDeskCaseReferences = helpDeskCaseReferences;
        setLogins(logins);
    }

    public UserEntity(UUID uuid, String personIdentifier, long userCreatedAtEpochMs, long userLastUpdatedAtEpochMs, Boolean active, String closedCode, long closedCodeUpdatedAtEpochMs, String helpDeskCaseReferences, List<LoginEntity> logins, UserEntity previousUser, UserEntity nextUser) {
        this.uuid = uuid;
        this.personIdentifier = personIdentifier;
        this.userCreatedAtEpochMs = userCreatedAtEpochMs;
        this.userLastUpdatedAtEpochMs = userLastUpdatedAtEpochMs;
        this.active = active;
        this.closedCode = closedCode;
        this.closedCodeUpdatedAtEpochMs = closedCodeUpdatedAtEpochMs;
        this.helpDeskCaseReferences = helpDeskCaseReferences;
        setLogins(logins);
        setPreviousUser(previousUser);
        setNextUser(nextUser);
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getPersonIdentifier() {
        return personIdentifier;
    }

    public void setPersonIdentifier(String personIdentifier) {
        this.personIdentifier = personIdentifier;
    }

    public long getUserCreatedAtEpochMs() {
        return userCreatedAtEpochMs;
    }

    public long getUserLastUpdatedAtEpochMs() {
        return userLastUpdatedAtEpochMs;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getClosedCode() {
        return closedCode;
    }

    public void setClosedCode(String closeCode) {
        this.closedCode = closeCode;
    }

    public long getClosedCodeUpdatedAtEpochMs() {
        return closedCodeUpdatedAtEpochMs;
    }

    public void setClosedCodeUpdatedAtEpochMs(long closeCodeUpdatedAtEpochMs) {
        this.closedCodeUpdatedAtEpochMs = closeCodeUpdatedAtEpochMs;
    }

    public String getHelpDeskCaseReferences() {
        return helpDeskCaseReferences;
    }

    public void setHelpDeskCaseReferences(String helpDeskCaseReferences) {
        this.helpDeskCaseReferences = helpDeskCaseReferences;
    }

    public List<LoginEntity> getLogins() {
        return this.logins;
    }

    public void setLogins(List<LoginEntity> logins) {
        this.logins.clear();
        if (logins != null) {
            this.logins.addAll(logins);
        }
    }

    public void addLogin(LoginEntity eid) {
        this.logins.add(eid);
    }

    public UserEntity getPreviousUser() {
        return previousUser;
    }

    public void setPreviousUser(UserEntity previousUser) {
        this.previousUser = previousUser;
    }

    public UserEntity getNextUser() {
        return nextUser;
    }

    public void setNextUser(UserEntity nextUser) {
        this.nextUser = nextUser;
    }

    @PrePersist
    public void onPrePersist() {
        userCreatedAtEpochMs = Instant.now().toEpochMilli();
        userLastUpdatedAtEpochMs = getUserCreatedAtEpochMs();
    }

    @PreUpdate
    public void onPreUpdate() {
        userLastUpdatedAtEpochMs = Instant.now().toEpochMilli();
    }

    public static UserEntityBuilder builder() {
        return new UserEntityBuilder();
    }

    public static class UserEntityBuilder {
        private UUID uuid;
        private String personIdentifier;
        private String closedCode;
        private long closedCodeUpdatedAtEpochMs;
        private String helpDeskCaseReferences;
        private boolean active;
        private UserEntity previousUser;
        private UserEntity nextUser;
        private List<LoginEntity> logins;

        public UserEntityBuilder uuid(UUID uuid) {
            this.uuid = uuid;
            return this;
        }


        public UserEntityBuilder personIdentifier(String personIdentifier) {
            this.personIdentifier = personIdentifier;
            return this;
        }

        public UserEntityBuilder closedCode(String closedCode) {
            this.closedCode = closedCode;
            return this;
        }

        public UserEntityBuilder closedCodeUpdatedAtEpochMs(long closedCodeUpdatedAtEpochMs) {
            this.closedCodeUpdatedAtEpochMs = closedCodeUpdatedAtEpochMs;
            return this;
        }

        public UserEntityBuilder helpDeskCaseReferences(String helpDeskCaseReferences) {
            this.helpDeskCaseReferences = helpDeskCaseReferences;
            return this;
        }

        public UserEntityBuilder active(boolean active) {
            this.active = active;
            return this;
        }

        public UserEntityBuilder previousUser(UserEntity previousUser){
            this.previousUser = previousUser;
            return this;
        }
        public UserEntityBuilder nextUser(UserEntity nextUser){
            this.nextUser = nextUser;
            return this;
        }

        public UserEntityBuilder logins(List<LoginEntity> logins){
            this.logins = logins;
            return this;
        }

        public UserEntity build() {
            return new UserEntity(uuid, personIdentifier, 0l, 0l, active, closedCode, closedCodeUpdatedAtEpochMs, helpDeskCaseReferences, logins, previousUser, nextUser);
        }
    }
}
