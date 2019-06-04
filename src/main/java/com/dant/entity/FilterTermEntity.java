package com.dant.entity;

import com.dant.utils.Log;
import db.search.*;
import db.structure.Column;
import db.structure.Table;

import javax.ws.rs.BadRequestException;
import java.util.Arrays;
import java.util.Optional;

public class FilterTermEntity {
    public JoinMethod joinMethod;
    public FilterTermEntity[] terms;
    public String column;
    public Operator operator;
    public Object value;

    public FilterTerm convertToFilterTerm (Table table) {
        if (this.joinMethod != null && (this.terms != null && this.terms.length > 0)) {
            return new FilterGroup(joinMethod, Arrays.stream(this.terms).map(t -> t.convertToFilterTerm(table)).toArray(FilterTerm[]::new));
        }

        if (this.column != null && this.operator != null && this.value != null) {
            Optional<Column> optColumn = table.getColumnByName(column);
            Column realColumn = optColumn.orElseThrow(() -> new BadRequestException("column does not exist : " + column));
            if (!realColumn.getDataType().isOperatorCompatible(operator)) {
                throw new BadRequestException("operator is not compatible with this data Type" + operator);
            }
            Object realValue;
            try {
                realValue = realColumn.getDataType().getAssociatedClassType().cast(value);
            } catch (ClassCastException e) {
                //throw new BadRequestException("value can't be parsed in selected column : " + column + " " + value);
                Log.warning("value can't be parsed in selected column : " + column + " " + value);
            }
            return new Predicate(table, table.getColumnByName(this.column).get(), this.operator, this.value);
        }

        throw new BadRequestException("filterTerm is neither an array of filterTerms nor a predicate ");
    }
}