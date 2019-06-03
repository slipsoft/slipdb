package com.dant.app;

import com.dant.exception.*;
import com.dant.filter.GsonProvider;

import com.dant.utils.Log;
import db.serial.SerialStructure;
import db.structure.Database;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by pitton on 2017-02-20.
 */
@ApplicationPath("")
public class App extends Application {
	@Override
	public Set<Object> getSingletons() {
		initialize();
		try {
			Log.start("slipdb", Database.getInstance().config.logLevel);
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}
		Set<Object> sets = new HashSet<>(1);
		sets.add(new DBEndpoint());
		sets.add(new TableEndpoint());
		return sets;
	}

	@Override
	public Set<Class<?>> getClasses() {
		Set<Class<?>> sets = new HashSet<>(1);
		sets.add(GsonProvider.class);
		sets.add(RuntimeExceptionMapper.class);
		sets.add(JsonSyntaxExceptionMapper.class);
		sets.add(SearchExceptionMapper.class);
		sets.add(ClientErrorExceptionMapper.class);
		return sets;
	}

	public static void initialize() {
		// -> déséraliser la DB ici
		
		SerialStructure.loadStructure();
		// -> Je ne serialise que la liste des tables
		// -> Faire SerialStructure.writeStructure(); pour sauvegarder !
	}
}
