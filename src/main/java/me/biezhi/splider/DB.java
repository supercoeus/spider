package me.biezhi.splider;

import org.sql2o.Sql2o;

/**
 * Hello world!
 *
 */
public class DB {
	
	private static Sql2o sql2o = new Sql2o("jdbc:mysql://localhost:3306/nojb", "root", "root");
	
	public static Sql2o getSql2o(){
		return sql2o;
	}
}
