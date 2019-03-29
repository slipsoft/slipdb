package db.structure;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import db.data.types.IntegerType;
import db.data.types.StringType;

class TableTest {
	protected Table table;
	protected List<Column> columns = new ArrayList<>();
	protected List<Index> indexes = new ArrayList<>();

	@BeforeEach
	void setUp() throws Exception {

		columns.add(new Column("col1", new StringType(10)));
		columns.add(new Column("col2", new IntegerType()));
		Column[] cols = {columns.get(0)};
		Index index = new IndexHash(cols);
		indexes.add(index);
		table = new Table("test", columns, null);
		table.addIndex(index);
	}

	@AfterEach
	void tearDown() throws Exception {
	}

	@Test
	final void testGetName() {
		assertEquals("test", table.getName());
	}

	@Test
	final void testGetColumns() {
		assertEquals(columns, table.getColumns());
	}

	@Test
	final void testGetIndexes() {
		assertEquals(indexes, table.getIndexes());
	}

	@Test
	final void testAddIndex() {
		Column[] cols = {columns.get(1)};
		Index index = new IndexHash(cols);
		table.addIndex(index);
		assertEquals(index, table.getIndexes().get(1));
	}

	@Test
	final void testGetLineSize() {
		assertEquals(14, table.getLineSize());
	}

}
