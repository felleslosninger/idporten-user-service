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
    private boolean active;

    @Column(name = "closed_code")
    private String closedCode;

    @Column(name = "closed_code_updated_ms")
    private long closedCodeUpdatedAtEpochMs;

    @Column(name = "help_desk_case_references")
    private String helpDeskCaseReferences;

    @OneToMany(mappedBy = "user", orphanRemoval = true, cascade = CascadeType.ALL)
    private List<EIDEntity> eIDs = new java.util.ArrayList<>();

    public UserEntity(){}

    public UserEntity(UUID uuid, String personIdentifier, long userCreatedAtEpochMs, long userLastUpdatedAtEpochMs, Boolean active, String closedCode, long closedCodeUpdatedAtEpochMs, String helpDeskCaseReferences, List<EIDEntity> eIDs) {
        this.uuid = uuid;
        this.personIdentifier = personIdentifier;
        this.userCreatedAtEpochMs = userCreatedAtEpochMs;
        this.userLastUpdatedAtEpochMs = userLastUpdatedAtEpochMs;
        this.active = active;
        this.closedCode = closedCode;
        this.closedCodeUpdatedAtEpochMs = closedCodeUpdatedAtEpochMs;
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
