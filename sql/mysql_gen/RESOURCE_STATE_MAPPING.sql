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
-- Copyright (C) 2001,2002 topIT Informationstechnologie GmbH
-- Copyright (C) 2003-2006 independIT Integrative Technologies GmbH

CREATE TABLE RESOURCE_STATE_MAPPING (
    ID                             DECIMAL(20) NOT NULL
    , `RSMP_ID`                      decimal(20)     NOT NULL
    , `ESD_ID`                       decimal(20)     NOT NULL
    , `FROM_RSD_ID`                  decimal(20)         NULL
    , `TO_RSD_ID`                    decimal(20)     NOT NULL
    , `CREATOR_U_ID`                 decimal(20)     NOT NULL
    , `CREATE_TS`                    decimal(20)     NOT NULL
    , `CHANGER_U_ID`                 decimal(20)     NOT NULL
    , `CHANGE_TS`                    decimal(20)     NOT NULL
    , VALID_FROM                   DECIMAL(20) NOT NULL
    , VALID_TO                     DECIMAL(20) NOT NULL
) engine = innodb;
CREATE INDEX PK_RESOURCE_STATE_MAPPING
ON RESOURCE_STATE_MAPPING(id);
CREATE VIEW SCI_C_RESOURCE_STATE_MAPPING AS
SELECT
    ID
    , `RSMP_ID`                      AS `RSMP_ID`
    , `ESD_ID`                       AS `ESD_ID`
    , `FROM_RSD_ID`                  AS `FROM_RSD_ID`
    , `TO_RSD_ID`                    AS `TO_RSD_ID`
    , `CREATOR_U_ID`                 AS `CREATOR_U_ID`
    , from_unixtime((`CREATE_TS` & ~1125899906842624)/1000) AS `CREATE_TS`
    , `CHANGER_U_ID`                 AS `CHANGER_U_ID`
    , from_unixtime((`CHANGE_TS` & ~1125899906842624)/1000) AS `CHANGE_TS`
  FROM RESOURCE_STATE_MAPPING
 WHERE VALID_TO = 9223372036854775807;
CREATE VIEW SCI_V_RESOURCE_STATE_MAPPING AS
SELECT
    ID
    , `RSMP_ID`                      AS `RSMP_ID`
    , `ESD_ID`                       AS `ESD_ID`
    , `FROM_RSD_ID`                  AS `FROM_RSD_ID`
    , `TO_RSD_ID`                    AS `TO_RSD_ID`
    , `CREATOR_U_ID`                 AS `CREATOR_U_ID`
    , from_unixtime((`CREATE_TS` & ~1125899906842624)/1000) AS `CREATE_TS`
    , `CHANGER_U_ID`                 AS `CHANGER_U_ID`
    , from_unixtime((`CHANGE_TS` & ~1125899906842624)/1000) AS `CHANGE_TS`
    , VALID_FROM
    , VALID_TO
  FROM RESOURCE_STATE_MAPPING;
