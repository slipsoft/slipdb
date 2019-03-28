package db.search;

import db.data.DataPositionList;

public interface FilterTerm {
    DataPositionList execute() throws SearchException;
}
