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


package de.independit.scheduler.server.output;

import java.lang.*;
import java.util.*;
import java.sql.*;
import java.io.*;
import de.independit.scheduler.server.*;
import de.independit.scheduler.server.util.*;
import de.independit.scheduler.server.exception.*;

public abstract class SDMSScriptRenderer extends SDMSOutputRenderer
{

	public final static String __version = "@(#) $Id: SDMSScriptRenderer.java,v 2.0.20.1 2013/03/14 10:24:18 ronald Exp $";

	public SDMSScriptRenderer ()
	{
		super();
	}

	protected String maskQuotes(String str)
	{
		StringBuffer sb = new StringBuffer(str);
		StringBuffer result = new StringBuffer(2 * str.length());
		int l = sb.length();

		for(int i = 0; i < l; i++) {
			char c = sb.charAt(i);
			if(c == '\\') {
				result.append("\\\\");
			} else if(c == '\'') {
				result.append("\\'");
			} else if(c == '\r') {
				result.append("\\r");
			} else if(c == '\n') {
				result.append("\\n");
			} else {
				result.append(c);
			}
		}
		return result.toString();
	}

	protected String maskJsonQuotes(String str)
	{
		StringBuffer sb = new StringBuffer(str);
		StringBuffer result = new StringBuffer(2 * str.length());
		int l = sb.length();

		for(int i = 0; i < l; i++) {
			char c = sb.charAt(i);
			if(c == '\\') {
				result.append("\\\\");
			} else if(c == '/') {
				result.append("\\/");
			} else if(c == '\"') {
				result.append("\\\"");
			} else if(c == '\r') {
				result.append("\\r");
			} else if(c == '\n') {
				result.append("\\n");
			} else if(c == '\t') {
				result.append("\\t");
			} else {
				result.append(c);
			}
		}
		return result.toString();
	}
}
