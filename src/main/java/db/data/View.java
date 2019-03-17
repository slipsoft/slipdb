package db.data;

import java.util.ArrayList;
import java.util.List;

public class View {

	List<Filter> filtersList = new ArrayList<>();
	List<Field>  fieldsList  = new ArrayList<>();
	List<Sort>   sortsList   = new ArrayList<>();
	Group groupBy;
}
