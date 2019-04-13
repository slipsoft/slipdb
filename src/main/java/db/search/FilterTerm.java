package db.search;

import db.disk.dataHandler.DiskPositionSet;

public interface FilterTerm {
    DiskPositionSet execute() throws SearchException;
}
