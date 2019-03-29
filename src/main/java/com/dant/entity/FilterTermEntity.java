package com.dant.entity;

import db.search.*;
import db.structure.Column;
import db.structure.Table;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.logging.Filter;
import java.util.stream.Collectors;

public class FilterTermEntity {
    public JoinMethod joinMethod;
    public FilterTermEntity[] terms;
    public String column;
    public Operator operator;
    public Object value;

    public void validate (Table table, ArrayList<ResponseError> allErrors) {

        boolean isValid = false;
        if (this.joinMethod != null && (this.terms != null && this.terms.length > 0)) {
            isValid = true;
            Arrays.stream(terms).forEach(t -> t.validate(table, allErrors));
        }

        if (this.column != null && this.operator != null && this.value != null) {
            isValid = true;
            Optional<Column> realColumn = table.getColumnByName(column);
            if (realColumn.isPresent()) {
                if (realColumn.get().getDataType().isOperatorCompatible(operator)){
                    if (value != null) {
                        if (!realColumn.get().getDataType().inputCanBeParsed((String)value)) {
                            allErrors.add(new ResponseError(Location.search, Type.invalidData, "value can't be parsed in selected column : " + column + " " + value));
                        }
                    } else {
                        allErrors.add(new ResponseError(Location.search, Type.missingData, "value is missing"));
                    }
                } else {
                    allErrors.add(new ResponseError(Location.search, Type.invalidData, "operator is not compatible with this data Type" + operator));
                }
            } else {
                allErrors.add(new ResponseError(Location.search, Type.invalidData, "column does not exist : " + column));
            }
        }


        if (!isValid) {
            allErrors.add(new ResponseError(Location.search, Type.invalidData, "filterTerm is neither an array of filterTerms nor a predicate "));
        }
    }

    public FilterTerm convertToFilterTerm (Table table) {
        if (this.joinMethod != null && (this.terms != null && this.terms.length > 0)) {
            return new FilterGroup(joinMethod, Arrays.stream(this.terms).map(t -> t.convertToFilterTerm(table)).toArray(FilterTerm[]::new));
        }

        if (this.column != null && this.operator != null && this.value != null) {
            return new Predicate(table, table.getColumnByName(this.column).get(), this.operator, this.value);
        }
    return null;
    }
}