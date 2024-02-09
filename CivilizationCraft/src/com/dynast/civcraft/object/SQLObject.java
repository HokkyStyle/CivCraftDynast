
package com.dynast.civcraft.object;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.dynast.civcraft.exception.CivException;
import com.dynast.civcraft.exception.InvalidNameException;
import com.dynast.civcraft.exception.InvalidObjectException;

/*
 * Any object that needs to be saved will extend this object so it can be
 * saved in the database.
 */
public abstract class SQLObject extends NamedObject {
	
	private boolean isDeleted = false;

	public abstract void load(ResultSet rs) throws SQLException, InvalidNameException, InvalidObjectException, CivException;
		
	public abstract void save();
	
	public abstract void saveNow() throws SQLException;
	
	public abstract void delete() throws SQLException;

	public boolean isDeleted() {
		return isDeleted;
	}

	public void setDeleted(boolean isDeleted) {
		this.isDeleted = isDeleted;
	}
	
}
