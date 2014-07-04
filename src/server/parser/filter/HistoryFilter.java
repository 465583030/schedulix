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


package de.independit.scheduler.server.parser.filter;

import java.io.*;
import java.util.*;
import java.lang.*;

import de.independit.scheduler.server.*;
import de.independit.scheduler.server.repository.*;
import de.independit.scheduler.server.parser.*;
import de.independit.scheduler.server.exception.*;

public class HistoryFilter extends Filter
{

	public final static String __version = "@(#) $Id: HistoryFilter.java,v 2.1.6.1 2013/03/14 10:25:14 ronald Exp $";

	long numMillisFrom = 0;
	long numMillisTo = 0;

	public HistoryFilter(SystemEnvironment sysEnv, WithHash interval1, WithHash interval2)
	{
		super();
		numMillisFrom = convertInterval(interval1);
		numMillisTo = convertInterval(interval2);
		if (numMillisFrom > numMillisTo) {
			long tmp = numMillisTo;
			numMillisTo = numMillisFrom;
			numMillisFrom = tmp;
		}
	}

	public HistoryFilter(SystemEnvironment sysEnv, WithHash interval)
	{
		super();
		numMillisFrom = convertInterval(interval);
		numMillisTo = -1;
	}

	private long convertInterval(WithHash interval)
	{
		long numMillis;

		Integer mult = (Integer) interval.get("MULT");
		Integer base = (Integer) interval.get("INTERVAL");
		if(mult == null || base == null) {
			return 0L;
		}
		numMillis = System.currentTimeMillis();
		switch(base.intValue()) {
			case SDMSInterval.MINUTE:
				numMillis -= mult.longValue() * 60 * 1000;
				break;
			case SDMSInterval.HOUR:
				numMillis -= mult.longValue() * 60 * 60 * 1000;
				break;
			case SDMSInterval.DAY:
				numMillis -= mult.longValue() * 24 * 60 * 60 * 1000;
				break;
			case SDMSInterval.WEEK:
				numMillis -= mult.longValue() * 7 * 24 * 60 * 60 * 1000;
				break;
			case SDMSInterval.MONTH:
				numMillis -= mult.longValue() * 30 * 24 * 60 * 60 * 1000;
				break;
			case SDMSInterval.YEAR:
				numMillis -= mult.longValue() * 365 * 24 * 60 * 60 * 1000;
				break;
		}

		return numMillis;
	}

	public boolean valid(SystemEnvironment sysEnv, SDMSProxy p)
		throws SDMSException
	{
		try {
			if (!(p instanceof SDMSSubmittedEntity))
				if (numMillisTo == -1)
					return true;
				else
					return false;
			SDMSSubmittedEntity sme = (SDMSSubmittedEntity) p;
			Long fts = sme.getFinalTs(sysEnv);
			if(fts == null || fts.longValue() >= numMillisFrom) {
				if (numMillisTo == -1 || (fts != null && fts <= numMillisTo))
					return true;
			}
		} catch (Exception e) { }
		return false;
	}
}
