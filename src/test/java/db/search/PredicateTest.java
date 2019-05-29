package db.search;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;

import com.dant.utils.Log;
import db.data.types.StringType;
import db.indexHash.IndexHash;
import db.structure.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

class PredicateTest {
	private Column column;
	private Predicate predicate;
	private Table table;
	private ArrayList<Column> columns = new ArrayList<>();

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
		Log.start("predicateTest", 3);
	}

	@BeforeEach
	void setUp() throws Exception {
		column = new Column("testcolumn", new StringType(12));
		columns.add(column);
		table = new Table("testtable", columns);
		predicate = new Predicate(table, column, Operator.equals, "test");
	}

	@Test
	void testGetIndex() throws Exception {
		Executable exec = () -> predicate.getIndex();
		assertThrows(StructureException.class, exec);
		Index index = table.createIndex("testcolumn", IndexHash.class);
		assertDoesNotThrow(predicate::getIndex);
		assertEquals(index, predicate.getIndex());
	}

	@Test
	void testGetColumn() {
		assertEquals(column, predicate.getColumn());
	}

	@Test
	void testGetOperator() {
		assertEquals(Operator.equals, predicate.getOperator());
	}

}
