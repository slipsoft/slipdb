package db.data;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import db.search.Operator;
import db.search.Predicate;
import db.structure.Column;
import db.structure.Index;
import db.structure.IndexHash;
import db.structure.Table;

class PredicateTest {
	Column column;
	Predicate predicate;
	Table table;
	ArrayList<Column> columns = new ArrayList<Column>();

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
	}

	@BeforeEach
	void setUp() throws Exception {
		column = new Column("testcolumn", new StringType(12));
		predicate = new Predicate(column, Operator.equals, "test");
		columns.add(column);
		table = new Table("testtable", columns);
	}

	@AfterEach
	void tearDown() throws Exception {
	}

	@Test
	void testFindBestIndex() {
		Executable exec = new Executable() {
			@Override
			public void execute() throws Throwable {
				predicate.findBestIndex(table);
			}
		};
		assertThrows(Exception.class, exec);
		Index index = new IndexHash(new Column[] {column});
		table.addIndex(index);
		assertDoesNotThrow(exec);
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

	@Test
	void testGetIndex() {
		assertEquals(null, predicate.getIndex());
	}

}
