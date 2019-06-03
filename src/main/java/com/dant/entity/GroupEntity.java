package com.dant.entity;

import com.google.gson.Gson;
import db.search.Field;
import db.search.Group;
import db.structure.Column;
import db.structure.Table;

import javax.ws.rs.NotFoundException;
import java.util.ArrayList;
import java.util.List;

public class GroupEntity {
	public List<String> fields = new ArrayList<>();

	public Group convertToGroup(Table table) {
		ArrayList<Column> columns = new ArrayList<>();
		for (String field : fields) {
			columns.add(table.getColumnByName(field).orElseThrow(() -> new NotFoundException("field not found: " + field)));
		}
		return new Group(columns);
	}

	public String toString() {
        return new Gson().toJson(this);
    }
}
