package db.search;

import com.dant.utils.Log;
import db.data.types.StringType;
import db.structure.Column;
import db.structure.Table;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ViewTest {
	private Column column;
	private Table table;
	private Field field;
	private List<Field> listFields = new ArrayList<>();
	private Predicate predicate;
	private View view;

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
		Log.start("predicateTest", 3);
	}

	@BeforeEach
	void setUp() throws Exception {
		table = new Table("testtable");
		table.addColumn("testcolumn", new StringType(12));
		column = table.getColumns().get(0);
		field = new Field("testcolumn");
		predicate = new Predicate(table, column, Operator.equals, "test");
		listFields.add(field);
		view = new View(table, predicate, listFields, new ArrayList<>(), new Group());
	}

	@Test
	void getListColumns() {
		List<Column> expected = new ArrayList<>();
		for (Field f : listFields) {
			table.getColumnByNameNoCheck(f.getName()).ifPresent(expected::add);
		}
		List<Column> actual = null;
		try {
			actual = new ArrayList<>(Arrays.asList(view.getListColumns()));
		} catch (SearchException e) {
			Log.error(e);
		}
		assertEquals(expected, actual);
	}
}