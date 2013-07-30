/*
Copyright (c) 2000-2013 "independIT Integrative Technologies GmbH",
Authors: Ronald Jeninga, Dieter Stubler

schedulix Enterprise Job Scheduling System

independIT Integrative Technologies GmbH [http://www.independit.de]
mailto:contact@independit.de

This file is part of schedulix

schedulix is free software:
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

import java.lang.*;
import java.util.*;
import java.io.*;

import de.independit.scheduler.server.*;
import de.independit.scheduler.server.exception.*;
import de.independit.scheduler.server.util.*;

public class SDMSVersions
{

	public final static String __version = "@(#) $Id: SDMSVersions.java,v 2.8.2.1 2013/03/14 10:25:27 ronald Exp $";

	Vector	versions;

	LinkedList	o_v;

	SDMSTransaction tx;

	SDMSTable       table;

	public Long	id;

	public final static String STAT_VERSION_COUNT = "VERSION_COUNT";
	public final static String STAT_LOW_VERSION   = "LOW_VERSION";

	protected SDMSVersions(SDMSTable p_table, Long oid)
	{
		id = oid;
		versions = new Vector();
		o_v   = null;
		tx    = null;
		table = p_table;
	}

	protected void flush(SystemEnvironment env, boolean isNew)
	throws SDMSException
	{
		if (env.tx.versionId == SDMSTransaction.UNDEFINED) {
			throw new FatalException (new SDMSMessage(env, "03110181544",
			                          "Cannot flush without transaction versionId"));
		}
		if (o_v == null) {
			throw new FatalException (new SDMSMessage(env, "02110261539",
			                          "Cannot flush an unchanged versions"));
		}
		if (o_v.size() == 0) {

			return;
		}
		SDMSObject o = (SDMSObject)(o_v.getFirst());
		if (isNew) {
			if (! o.isCurrent) {

				return;
			}
			if (! o.isDeleted) {
				o.insertDBObject (env);
			}

		} else {
			if (! o.isDeleted) {
				o.updateDBObject(env, (SDMSObject)(versions.lastElement()));
			} else {
				if (! o.memOnly)
					((SDMSObject)(versions.lastElement())).deleteDBObject (env);
			}
		}
	}

	protected synchronized void commitOrRollback(SystemEnvironment env, long versionId,
	                boolean isNew, boolean isCommit)
	throws SDMSException
	{
		int s;
		SDMSPurgeSet purgeSet;

		long purgeLow;
		purgeLow = env.roTxList.first(env);
		if (table.getIsVersioned()) {

			long lowSeVersion = env.seVersionList.first(env);
			if (lowSeVersion < purgeLow)
				purgeLow = lowSeVersion;
			purgeSet = env.vPurgeSet;
		} else {
			purgeSet = env.nvPurgeSet;
		}

		if (o_v == null) {
			throw new FatalException (new SDMSMessage(env, "02110261556",
			                          "Cannot commit or rollback an unchanged versions"));
		}

		if (o_v.size() == 0) {

			tx  = null;
			o_v = null;

			purgeSet.add(env, this);
			return;
		}
		SDMSObject o = (SDMSObject)(o_v.getFirst());
		s = versions.size();
		if (isCommit) {
			if (isNew == false) {
				if (s == 0) {
					throw new FatalException (new SDMSMessage(env, "03110181545",
					                          "Cannot update or delete commit empty versions"));
				}
				((SDMSObject)(versions.lastElement())).validTo = versionId;
			}
			if (! o.isDeleted) {
				o.validFrom = versionId;
				o.validTo   = Long.MAX_VALUE;
				versions.addElement(o);
			}

			s = versions.size();
			if( s > 1 ||
			    (s == 1 && ((SDMSObject)(versions.lastElement())).validTo != Long.MAX_VALUE)
			  ) {
				purgeSet.add(env, this);
			}
		} else {
			if (! o.isDeleted) {
				table.unIndex(env, o);
			}
			if(isNew == false) {
				if (s == 0) {
					throw new FatalException (new SDMSMessage(env, "03111021305",
					                          "Cannot update or delete rollback empty versions"));
				}
				((SDMSObject)(versions.lastElement())).isCurrent = true;
			}

			purgeSet.add(env, this);
		}
		tx  = null;
		o_v = null;
	}

	protected void add(SystemEnvironment env, SDMSObject p_o)
	throws SDMSException
	{
		int s;

		if (!id.equals(p_o.id)) {

			throw new FatalException (new SDMSMessage(env, "03110181546",
			                          "versions id ( $1 ) does not match objects id ( $2 )",
			                          id, p_o.id));
		}
		s = versions.size() - 1;
		while (s >= 0) {
			if ( ((SDMSObject)(versions.elementAt(s))).validTo <= p_o.validFrom) {
				break;
			}
			s--;
		}
		versions.insertElementAt(p_o, s+1);
		p_o.versions = this;
		if (p_o.validTo == Long.MAX_VALUE) {

			p_o.isCurrent = true;
		}

	}

	public synchronized void purge (SystemEnvironment env, long versionId, Iterator i)
	throws SDMSException
	{
		if (i == null) {
			throw new FatalException(
			        new SDMSMessage (env, "02225041315",
			                         "purge with null iterator !"));
		}
		int s = versions.size();

		long startVersion = 1;
		if (s > 0)
			startVersion = ((SDMSObject)(versions.elementAt(0))).validFrom;

		while (s > 0 && ((SDMSObject)(versions.elementAt(0))).validTo < versionId) {
			table.unIndex(env, (SDMSObject)(versions.elementAt(0)));
			versions.remove (0);
			--s;
		}

		if ( s == 0 ||
		     (s == 1 && ((SDMSObject)(versions.elementAt(0))).validTo == Long.MAX_VALUE)
		   )
			i.remove();

		if (s == 0)
			table.hashMap.remove(id);
		else

			if (!table.isVersioned) {
				((SDMSObject)(versions.elementAt(0))).validFrom = startVersion;
			}
	}

	protected SDMSObject get (SystemEnvironment env)
	throws SDMSException
	{
		SDMSObject obj = getRaw (env);

		if(obj == null) {
			raiseNotFoundException(env, 0);
		}
		return obj;
	}

	protected SDMSObject getRaw (SystemEnvironment env)
	throws SDMSException
	{
		SDMSObject o;
		if (tx == env.tx) {
			if (o_v.size() == 0) {

				o = (SDMSObject) getRaw(env, Long.MAX_VALUE);
				if (o == null) return null;
				if (!o.isCurrent) {
					throw new FatalException(
					        new SDMSMessage (env, "02110301833",
					                         "get returned non current version"));
				}
				return o;
			} else {

				o = (SDMSObject)(o_v.getLast());
				if (! o.isDeleted) {
					if (!o.isCurrent) {
						throw new FatalException(
						        new SDMSMessage (env, "02110301834",
						                         "get returned non current version"));
					}
					return o;
				} else {
					return null;
				}
			}
		} else {
			if (env.tx.mode == SDMSTransaction.READONLY) {
				return getRaw(env, env.tx.versionId);
			} else {
				lockShared(env);
				o = (SDMSObject)getRaw(env, Long.MAX_VALUE);
				if (o != null && !o.isCurrent) {
					throw new FatalException(
					        new SDMSMessage (env, "02110301835",
					                         "get returned non current version"));
				}
				return o;
			}
		}
	}

	protected synchronized SDMSObject get (SystemEnvironment env, long versionId)
	throws SDMSException
	{
		SDMSObject obj = getRaw (env, versionId);
		if(obj == null) {
			raiseNotFoundException(env, versionId);
		}
		return obj;
	}

	private void raiseNotFoundException(SystemEnvironment env, long versionId)
	throws SDMSException
	{
		if (versionId == 0)
			if (env.tx.mode == SDMSTransaction.READONLY)
				versionId = env.tx.versionId;
			else
				versionId = Long.MAX_VALUE;
		if(env.traceLevel >= SDMSThread.SEVERITY_DEBUG)
			SDMSThread.doTrace(env.cEnv, "Version " + versionId + " of id " + id + " not found\n" + this, SDMSThread.SEVERITY_DEBUG);
		throw new NotFoundException(new SDMSMessage(env, "03210252200",
		                            "Version $1 of id $2 not found",
		                            new Long(versionId), id));
	}

	private synchronized SDMSObject getRaw (SystemEnvironment env, long versionId)
	throws SDMSException
	{
		int s = versions.size();
		SDMSObject obj;
		obj = null;

		if(!table.isVersioned)
			lockShared(env);

		for (; s > 0; --s) {
			obj = (SDMSObject)(versions.elementAt(s - 1));
			if (obj.validTo >= versionId && obj.validFrom < versionId) {
				return obj;
			}
		}
		return null;
	}

	public void dump()
	{
		SDMSThread.doTrace(null, toString(), SDMSThread.SEVERITY_DEBUG);
	}

	public String toString(int indent)
	{
		try {
			StringBuffer result = new StringBuffer(
			        "-- Start SDMSVersions dump --\n" +
			        "Dump of SDMSVersions object for Object : " + id.toString() + "\n" +
			        "Object is a " + table.tableName() + "\n" +
			        "Tx : " + (tx == null ? "null" : "\n" + tx.toString()) + "\n" +
			        "-- Start Committed Versions --\n");

			Iterator i = versions.iterator();
			SDMSObject o;
			while (i.hasNext()) {
				o = (SDMSObject)(i.next());
				result.append(
				        "\n==========================\n" +
				        "validFrom : " + o.validFrom + "\n" +
				        "validTo : " + o.validTo + "\n" +
				        "subTxId : " + o.subTxId + "\n" +
				        "isDeleted : " + o.isDeleted + "\n" +
				        "memOnly : " + o.memOnly + "\n" +
				        "--------------------------\n" +
				        o.toString(indent));
			}
			result.append("-- End Committed Versions --\n");

			if (o_v != null) {
				result.append("-- Start Uncommitted Versions --\n");
				i = o_v.listIterator(0);
				while (i.hasNext()) {
					o = (SDMSObject)(i.next());
					result.append(o.toString(indent) + "\n" +
					              "----------------------------\n" +
					              "subTxId : " + o.subTxId + "\n" +
					              "isDeleted : " + o.isDeleted + "\n" +
					              "isCurrent : " + o.isDeleted + "\n" +
					              "memOnly : " + o.memOnly + "\n" +
					              "============================\n");
				}
				result.append("-- End Uncommitted Versions --\n");
			}

			result.append("-- End SDMSVersions dump --\n");

			return result.toString();
		} catch (Exception e) {
			return "Dump of Versions Object run into an Exception : " + e.toString();
		}
	}

	public String toString()
	{
		return toString(0);
	}

	private void lock(SystemEnvironment env, int mode)
	throws SDMSException
	{

	}
	protected void lockExclusive(SystemEnvironment env)
	throws SDMSException
	{
		lock(env, SDMSLock.X);
	}
	protected void lockShared(SystemEnvironment env)
	throws SDMSException
	{
		lock(env, SDMSLock.S);
	}
	protected void unLock(SystemEnvironment env)
	{

	}

	public synchronized HashMap stat(SystemEnvironment sysEnv)
	throws SDMSException
	{
		HashMap result = new HashMap();

		long lowVersion = Long.MAX_VALUE;
		long countVersions = 0;

		Iterator i = versions.iterator();
		while(i.hasNext()) {
			countVersions++;
			SDMSObject o = (SDMSObject) i.next();
			if (o.validTo < lowVersion) lowVersion = o.validTo;
		}
		result.put(STAT_VERSION_COUNT, new Long(countVersions));
		result.put(STAT_LOW_VERSION, new Long(lowVersion));

		return result;
	}

}

