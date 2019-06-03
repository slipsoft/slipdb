package com.dant.entity;

import db.search.DirectionEnum;
import db.search.Field;
import db.search.Sort;
import db.structure.Table;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;

public class SortEntity {
	public DirectionEnum direction;
	public String field;

	public void validate(Table table) {
		if (this.direction == null) {
			throw new BadRequestException("Direction is invalid in sort");
		}
	}

	public Sort convertToSort(Table table) {
		this.validate(table);
		return new Sort(direction, table.getColumnByName(field).orElseThrow(() -> new NotFoundException("field not found: " + field)));
	}
}
