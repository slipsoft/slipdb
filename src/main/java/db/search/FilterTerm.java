package db.search;

import db.data.types.DataPositionList;

public interface FilterTerm {
    DataPositionList execute() throws SearchException;
}
