package db.search;

import java.util.ArrayList;
import java.util.List;

public class View {

	FilterTerm filter;
	List<Field>  fields  = new ArrayList<>();
	List<Sort>   sorts   = new ArrayList<>();
	Group groupBy;
}
