package db.search;

import db.disk.dataHandler.DiskDataPosition;

import java.util.Set;

public interface FilterTerm {
    Set<DiskDataPosition> execute() throws SearchException;
}
