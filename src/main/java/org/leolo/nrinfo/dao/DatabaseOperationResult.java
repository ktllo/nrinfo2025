package org.leolo.nrinfo.dao;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class DatabaseOperationResult {
    private boolean success;
    private int inserted;
    private int updated;
    private int deleted;

    public DatabaseOperationResult() {
        success = true;
    }

    public void addInserted() {
        inserted++;
    }

    public void addUpdated() {
        updated++;
    }

    public void addDeleted() {
        deleted++;
    }

    public DatabaseOperationResult(boolean success, int inserted, int updated, int deleted) {
        this.success = success;
        this.inserted = inserted;
        this.updated = updated;
        this.deleted = deleted;
    }

    public DatabaseOperationResult add(DatabaseOperationResult o) {
        return new DatabaseOperationResult(
                success && o.success,
                inserted+o.inserted,
                updated+o.updated,
                deleted+o.deleted
        );
    }
}
