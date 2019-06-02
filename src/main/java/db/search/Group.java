package db.search;

import com.google.gson.Gson;
import db.structure.Table;

import java.util.ArrayList;
import java.util.List;

public class Group {
	protected List<Field> field = new ArrayList<>();

	public void validate(Table table) {
	    for (Field oneField : field) {
	        oneField.validate(table);
        }
	}

	public String toString() {
        return new Gson().toJson(this);
    }
}
