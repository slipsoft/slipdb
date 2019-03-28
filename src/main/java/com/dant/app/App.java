package com.dant.app;

import com.dant.exception.RuntimeExceptionMapper;
import com.dant.filter.GsonProvider;

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
		return sets;
	}

	public static void initialize() {
		// -> déséraliser la DB ici
		Database.getInstance().getConfigFromFile();
		try {
			SerialStructure.loadStructure();
		} catch (ClassNotFoundException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// -> Je ne serialise que la liste des tables
		// -> Faire SerialStructure.writeStructure(); pour sauvegarder !
	}
}
