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
-- Copyright (C) 2001,2002 topIT Informationstechnologie GmbH
-- Copyright (C) 2003-2006 independIT Integrative Technologies GmbH

CREATE TABLE RESSOURCE (
    ID                             DECIMAL(20) NOT NULL
    , NR_ID                          decimal(20)     NOT NULL
    , SCOPE_ID                       decimal(20)     WITH NULL
    , MASTER_ID                      decimal(20)     WITH NULL
    , OWNER_ID                       decimal(20)     NOT NULL
    , LINK_ID                        decimal(20)     WITH NULL
    , MANAGER_ID                     decimal(20)     WITH NULL
    , TAG                            varchar(64)     WITH NULL
    , RSD_ID                         decimal(20)     WITH NULL
    , RSD_TIME                       decimal(20)     WITH NULL
    , DEFINED_AMOUNT                 integer         WITH NULL
    , REQUESTABLE_AMOUNT             integer         WITH NULL
    , AMOUNT                         integer         WITH NULL
    , FREE_AMOUNT                    integer         WITH NULL
    , IS_ONLINE                      integer         WITH NULL
    , FACTOR                         float           WITH NULL
    , TRACE_INTERVAL                 integer         WITH NULL
    , TRACE_BASE                     integer         WITH NULL
    , TRACE_BASE_MULTIPLIER          integer         NOT NULL
    , TD0_AVG                        float           NOT NULL
    , TD1_AVG                        float           NOT NULL
    , TD2_AVG                        float           NOT NULL
    , LW_AVG                         float           NOT NULL
    , LAST_EVAL                      decimal(20)     NOT NULL
    , LAST_WRITE                     decimal(20)     NOT NULL
    , CREATOR_U_ID                   decimal(20)     NOT NULL
    , CREATE_TS                      decimal(20)     NOT NULL
    , CHANGER_U_ID                   decimal(20)     NOT NULL
    , CHANGE_TS                      decimal(20)     NOT NULL
);\g
CREATE UNIQUE INDEX PK_RESSOURCE
ON RESSOURCE(ID) WITH STRUCTURE = BTREE;\g
CREATE VIEW SCI_RESSOURCE AS
SELECT
    ID
    , NR_ID                          AS NR_ID
    , SCOPE_ID                       AS SCOPE_ID
    , MASTER_ID                      AS MASTER_ID
    , OWNER_ID                       AS OWNER_ID
    , LINK_ID                        AS LINK_ID
    , MANAGER_ID                     AS MANAGER_ID
    , TAG                            AS TAG
    , RSD_ID                         AS RSD_ID
    , '01-JAN-1970 00:00:00 GMT' + date(char(decimal((RSD_TIME- decimal(RSD_TIME/1125899906842624, 18, 0)*1125899906842624)/1000, 18, 0)) + ' secs') AS RSD_TIME
    , DEFINED_AMOUNT                 AS DEFINED_AMOUNT
    , REQUESTABLE_AMOUNT             AS REQUESTABLE_AMOUNT
    , AMOUNT                         AS AMOUNT
    , FREE_AMOUNT                    AS FREE_AMOUNT
    , CASE IS_ONLINE WHEN 1 THEN 'TRUE' WHEN 0 THEN 'FALSE' END AS IS_ONLINE
    , FACTOR                         AS FACTOR
    , TRACE_INTERVAL                 AS TRACE_INTERVAL
    , TRACE_BASE                     AS TRACE_BASE
    , TRACE_BASE_MULTIPLIER          AS TRACE_BASE_MULTIPLIER
    , TD0_AVG                        AS TD0_AVG
    , TD1_AVG                        AS TD1_AVG
    , TD2_AVG                        AS TD2_AVG
    , LW_AVG                         AS LW_AVG
    , '01-JAN-1970 00:00:00 GMT' + date(char(decimal((LAST_EVAL- decimal(LAST_EVAL/1125899906842624, 18, 0)*1125899906842624)/1000, 18, 0)) + ' secs') AS LAST_EVAL
    , '01-JAN-1970 00:00:00 GMT' + date(char(decimal((LAST_WRITE- decimal(LAST_WRITE/1125899906842624, 18, 0)*1125899906842624)/1000, 18, 0)) + ' secs') AS LAST_WRITE
    , CREATOR_U_ID                   AS CREATOR_U_ID
    , '01-JAN-1970 00:00:00 GMT' + date(char(decimal((CREATE_TS- decimal(CREATE_TS/1125899906842624, 18, 0)*1125899906842624)/1000, 18, 0)) + ' secs') AS CREATE_TS
    , CHANGER_U_ID                   AS CHANGER_U_ID
    , '01-JAN-1970 00:00:00 GMT' + date(char(decimal((CHANGE_TS- decimal(CHANGE_TS/1125899906842624, 18, 0)*1125899906842624)/1000, 18, 0)) + ' secs') AS CHANGE_TS
  FROM RESSOURCE;\g
