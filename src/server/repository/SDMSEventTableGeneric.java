/*
Copyright (c) 2000-2013 "independIT Integrative Technologies GmbH",
Authors: Ronald Jeninga, Dieter Stubler

BICsuite!Open Enterprise Job Scheduling System

independIT Integrative Technologies GmbH [http://www.independit.de]
mailto:contact@independit.de

This file is part of BICsuite!Open

BICsuite!Open is free software:
you can redistribute it and/or modify it under the terms of the
GNU Affero General Public License as published by the
Free Software Foundation, either version 3 of the License,
or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/


package de.independit.scheduler.server.repository;

import java.io.*;
import java.util.*;
import java.lang.*;
import java.sql.*;

import de.independit.scheduler.server.*;
import de.independit.scheduler.server.util.*;
import de.independit.scheduler.server.exception.*;

public class SDMSEventTableGeneric extends SDMSTable
{

	public final static String __version = "SDMSEventTableGeneric $Revision: 2.7 $ / @(#) $Id: generate.py,v 2.42.2.7 2013/04/17 12:40:29 ronald Exp $";

	public final static String tableName = "EVENT";
	public static SDMSEventTable table  = null;

	public static SDMSIndex idx_name;
	public static SDMSIndex idx_ownerId;
	public static SDMSIndex idx_seId;

	public SDMSEventTableGeneric(SystemEnvironment env)
	throws SDMSException
	{
		super(env);
		if (table != null) {

			throw new FatalException(new SDMSMessage(env, "01110182009", "Event"));
		}
		table = (SDMSEventTable) this;
		SDMSEventTableGeneric.table = (SDMSEventTable) this;
		isVersioned = false;
		idx_name = new SDMSIndex(env, SDMSIndex.UNIQUE, isVersioned);
		idx_ownerId = new SDMSIndex(env, SDMSIndex.ORDINARY, isVersioned);
		idx_seId = new SDMSIndex(env, SDMSIndex.ORDINARY, isVersioned);
	}
	public SDMSEvent create(SystemEnvironment env
	                        ,String p_name
	                        ,Long p_ownerId
	                        ,Long p_seId
	                       )
	throws SDMSException
	{
		Long p_creatorUId = env.cEnv.uid();
		Long p_createTs = env.txTime();
		Long p_changerUId = env.cEnv.uid();
		Long p_changeTs = env.txTime();

		if(env.tx.mode == SDMSTransaction.READONLY) {

			throw new FatalException(new SDMSMessage(env, "01110182049", "Event"));
		}
		validate(env
		         , p_name
		         , p_ownerId
		         , p_seId
		         , p_creatorUId
		         , p_createTs
		         , p_changerUId
		         , p_changeTs
		        );

		env.tx.beginSubTransaction(env);
		SDMSEventGeneric o = new SDMSEventGeneric(env
		                , p_name
		                , p_ownerId
		                , p_seId
		                , p_creatorUId
		                , p_createTs
		                , p_changerUId
		                , p_changeTs
		                                         );

		SDMSEvent p;
		try {
			env.tx.addToChangeSet(env, o.versions, true);
			env.tx.addToTouchSet(env, o.versions, true);
			table.put(env, o.id, o.versions);
			env.tx.commitSubTransaction(env);
			p = (SDMSEvent)(o.toProxy());
			p.current = true;
		} catch(SDMSException e) {
			p = (SDMSEvent)(o.toProxy());
			p.current = true;
			env.tx.rollbackSubTransaction(env);
			throw e;
		}

		if(!checkCreatePrivs(env, p))
			throw new AccessViolationException(p.accessViolationMessage(env, "01402270738"));

		return p;
	}

	protected boolean checkCreatePrivs(SystemEnvironment env, SDMSEvent p)
	throws SDMSException
	{
		return true;
	}

	protected void validate(SystemEnvironment env
	                        ,String p_name
	                        ,Long p_ownerId
	                        ,Long p_seId
	                        ,Long p_creatorUId
	                        ,Long p_createTs
	                        ,Long p_changerUId
	                        ,Long p_changeTs
	                       )
	throws SDMSException
	{

	}

	protected SDMSObject rowToObject(SystemEnvironment env, ResultSet r)
	throws SDMSException
	{
		Long id;
		String name;
		Long ownerId;
		Long seId;
		Long creatorUId;
		Long createTs;
		Long changerUId;
		Long changeTs;
		long validFrom;
		long validTo;

		try {
			id     = new Long (r.getLong(1));
			name = r.getString(2);
			ownerId = new Long (r.getLong(3));
			seId = new Long (r.getLong(4));
			if (r.wasNull()) seId = null;
			creatorUId = new Long (r.getLong(5));
			createTs = new Long (r.getLong(6));
			changerUId = new Long (r.getLong(7));
			changeTs = new Long (r.getLong(8));
			validFrom = 0;
			validTo = Long.MAX_VALUE;
		} catch(SQLException sqle) {
			SDMSThread.doTrace(null, "SQL Error : " + sqle.getMessage(), SDMSThread.SEVERITY_ERROR);

			throw new FatalException(new SDMSMessage(env, "01110182045", "Event: $1 $2", new Integer(sqle.getErrorCode()), sqle.getMessage()));
		}
		if(validTo < env.lowestActiveVersion) return null;
		return new SDMSEventGeneric(id,
		                            name,
		                            ownerId,
		                            seId,
		                            creatorUId,
		                            createTs,
		                            changerUId,
		                            changeTs,
		                            validFrom, validTo);
	}

	protected void loadTable(SystemEnvironment env)
	throws SQLException, SDMSException
	{
		int read = 0;
		int loaded = 0;

		final String driverName = env.dbConnection.getMetaData().getDriverName();
		final boolean postgres = driverName.startsWith("PostgreSQL");
		String squote = "";
		String equote = "";
		if (driverName.startsWith("MySQL")) {
			squote = "`";
			equote = "`";
		}
		if (driverName.startsWith("Microsoft")) {
			squote = "[";
			equote = "]";
		}
		Statement stmt = env.dbConnection.createStatement();

		ResultSet rset = stmt.executeQuery("SELECT " +
		                                   "ID" +
		                                   ", " + squote + "NAME" + equote +
		                                   ", " + squote + "OWNER_ID" + equote +
		                                   ", " + squote + "SE_ID" + equote +
		                                   ", " + squote + "CREATOR_U_ID" + equote +
		                                   ", " + squote + "CREATE_TS" + equote +
		                                   ", " + squote + "CHANGER_U_ID" + equote +
		                                   ", " + squote + "CHANGE_TS" + equote +
		                                   " FROM " + tableName() +
		                                   ""						  );
		while(rset.next()) {
			if(loadObject(env, rset)) ++loaded;
			++read;
		}
		stmt.close();
		SDMSThread.doTrace(null, "Read " + read + ", Loaded " + loaded + " rows for " + tableName(), SDMSThread.SEVERITY_INFO);
	}

	protected void index(SystemEnvironment env, SDMSObject o)
	throws SDMSException
	{
		idx_name.put(env, ((SDMSEventGeneric) o).name, o);
		idx_ownerId.put(env, ((SDMSEventGeneric) o).ownerId, o);
		idx_seId.put(env, ((SDMSEventGeneric) o).seId, o);
	}

	protected  void unIndex(SystemEnvironment env, SDMSObject o)
	throws SDMSException
	{
		idx_name.remove(env, ((SDMSEventGeneric) o).name, o);
		idx_ownerId.remove(env, ((SDMSEventGeneric) o).ownerId, o);
		idx_seId.remove(env, ((SDMSEventGeneric) o).seId, o);
	}

	public static SDMSEvent getObject(SystemEnvironment env, Long id)
	throws SDMSException
	{
		return (SDMSEvent) table.get(env, id);
	}

	public static SDMSEvent getObject(SystemEnvironment env, Long id, long version)
	throws SDMSException
	{
		return (SDMSEvent) table.get(env, id, version);
	}

	public static SDMSEvent idx_name_getUnique(SystemEnvironment env, Object key)
	throws SDMSException
	{
		return (SDMSEvent) SDMSEventTableGeneric.idx_name.getUnique(env, key);
	}

	public static SDMSEvent idx_name_getUnique(SystemEnvironment env, Object key, long version)
	throws SDMSException
	{
		return (SDMSEvent) SDMSEventTableGeneric.idx_name.getUnique(env, key, version);
	}

	public String tableName()
	{
		return tableName;
	}
}
