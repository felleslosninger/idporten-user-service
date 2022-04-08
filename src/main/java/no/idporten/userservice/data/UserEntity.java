package no.idporten.userservice.data;

import lombok.Builder;

import javax.persistence.*;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity(name = "user")
@Table(name = "user")
@Builder
public class UserEntity {

    @Id
    @GeneratedValue
    private UUID uuid;

    @Column(name = "person_identifier")
    private String personIdentifier;

    @Column(name = "user_created_ms")
    private long userCreatedAtEpochMs;

    @Column(name = "user_last_updated_ms")
    private long userLastUpdatedAtEpochMs;

    @Column(name = "active")
    @Builder.Default
    private boolean active = true;

    @Column(name = "close_code")
    private String closeCode;

    @Column(name = "close_code_updated_ms")
    private long closeCodeUpdatedAtEpochMs;

    @Column(name = "help_desk_case_references")
    private String helpDeskCaseReferences;

    @OneToMany(mappedBy = "user", orphanRemoval = true, cascade = CascadeType.ALL)
    private List<EIDEntity> eIDs = new java.util.ArrayList<>();

    public UserEntity(){}

    public UserEntity(UUID uuid, String personIdentifier, long userCreatedAtEpochMs, long userLastUpdatedAtEpochMs, boolean active, String closeCode, long closeCodeUpdatedAtEpochMs, String helpDeskCaseReferences, List<EIDEntity> eIDs) {
        this.uuid = uuid;
        this.personIdentifier = personIdentifier;
        this.userCreatedAtEpochMs = userCreatedAtEpochMs;
        this.userLastUpdatedAtEpochMs = userLastUpdatedAtEpochMs;
        this.active = active;
        this.closeCode = closeCode;
        this.closeCodeUpdatedAtEpochMs = closeCodeUpdatedAtEpochMs;
        this.helpDeskCaseReferences = helpDeskCaseReferences;
        setEIDs(eIDs);
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

    public String getCloseCode() {
        return closeCode;
    }

    public void setCloseCode(String closeCode) {
        this.closeCode = closeCode;
    }

    public long getCloseCodeUpdatedAtEpochMs() {
        return closeCodeUpdatedAtEpochMs;
    }

    public void setCloseCodeUpdatedAtEpochMs(long closeCodeUpdatedAtEpochMs) {
        this.closeCodeUpdatedAtEpochMs = closeCodeUpdatedAtEpochMs;
    }

    public String getHelpDeskCaseReferences() {
        return helpDeskCaseReferences;
    }

    public void setHelpDeskCaseReferences(String helpDeskCaseReferences) {
        this.helpDeskCaseReferences = helpDeskCaseReferences;
    }

    public List<EIDEntity> getEIDs() {
        return this.eIDs;
    }

    public void setEIDs(List<EIDEntity> eIDs) {
        this.eIDs.clear();
        if (eIDs != null) {
            this.eIDs.addAll(eIDs);
        }
    }

    public void addEid(EIDEntity eid) {
        this.eIDs.add(eid);
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
}
