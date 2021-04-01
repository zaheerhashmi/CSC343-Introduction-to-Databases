-- Q3. North and South Connections

-- You must not change the next 2 lines or the table definition.
SET SEARCH_PATH TO air_travel, public;
DROP TABLE IF EXISTS q3 CASCADE;

CREATE TABLE q3 (
    outbound VARCHAR(30),
    inbound VARCHAR(30),
    direct INT,
    one_con INT,
    two_con INT,
    earliest timestamp
);

-- Do this for each of the views that define your intermediate steps.  
-- (But give them better names!) The IF EXISTS avoids generating an error 
-- the first time this file is imported.

DROP VIEW IF EXISTS Cities_canada CASCADE;
DROP VIEW IF EXISTS Cities_usa CASCADE;
DROP VIEW IF EXISTS Pair_canus CASCADE;
DROP VIEW IF EXISTS Pair_uscan CASCADE;
DROP VIEW IF EXISTS Canus_dir CASCADE;
DROP VIEW IF EXISTS Canus_dir_final CASCADE;
DROP VIEW IF EXISTS Canus_one_con CASCADE;
DROP VIEW IF EXISTS Canus_one_con_final CASCADE;
DROP VIEW IF EXISTS Canus_two_con CASCADE;
DROP VIEW IF EXISTS Canus_two_con_final CASCADE;
DROP VIEW IF EXISTS Uscan_dir CASCADE;
DROP VIEW IF EXISTS Uscan_dir_final CASCADE;
DROP VIEW IF EXISTS Uscan_one_con CASCADE;
DROP VIEW IF EXISTS Uscan_one_con_final CASCADE;
DROP VIEW IF EXISTS Uscan_two_con CASCADE;
DROP VIEW IF EXISTS Uscan_two_con_final CASCADE;
DROP VIEW IF EXISTS Can2us_flights CASCADE;
DROP VIEW IF EXISTS Us2can_flights CASCADE;

-- Define views for your intermediate steps here:

-- Canadian Cities -- 
CREATE VIEW Cities_canada AS
SELECT city AS cancity,code AS cancode
FROM Airport
WHERE country = 'Canada';

-- American Cities--
CREATE VIEW Cities_usa AS
SELECT city AS uscity,code AS uscode
FROM Airport
WHERE country = 'USA';

--CAN-USA city pairing--
CREATE VIEW Pair_canus AS
SELECT cancity,uscity
FROM Cities_canada,Cities_usa;

--USA-CAN city pairing--
CREATE VIEW Pair_uscan AS
SELECT uscity,cancity
FROM Cities_usa,Cities_canada;

--Direct Routes--

--Canada to US direct flights on 2021-04-30
CREATE VIEW Canus_dir AS
SELECT
cancity, uscity,s_arv
FROM Cities_canada JOIN Flight ON cancode = outbound JOIN
Cities_usa ON uscode = inbound
WHERE date(s_dep) = '2021-04-30' AND date(s_arv) = '2021-04-30';

CREATE VIEW Canus_dir_final AS
SELECT cancity, uscity, count(s_arv) AS direct, min(s_arv) AS earliest_dir
FROM Canus_dir NATURAL RIGHT JOIN Pair_canus
GROUP BY cancity,uscity;

-- US to Canada direct flights on 2021-04-30
CREATE VIEW Uscan_dir AS
SELECT
uscity, cancity,s_arv
FROM Cities_usa JOIN Flight ON uscode = outbound JOIN
Cities_canada ON cancode = inbound
WHERE date(s_dep) = '2021-04-30' AND date(s_arv) = '2021-04-30';

CREATE VIEW Uscan_dir_final AS
SELECT uscity, cancity, count(s_arv) AS direct, min(s_arv) AS earliest_dir
FROM Uscan_dir NATURAL RIGHT JOIN Pair_uscan
GROUP BY uscity,cancity;

-- Canada to US one connection flights on 2021-04-30--
CREATE VIEW Canus_one_con AS
SELECT cancity, uscity, F2.s_arv
FROM Flight F1 JOIN Flight F2 ON F1.inbound = F2.outbound JOIN
Cities_canada ON F1.outbound = Cities_canada.cancode JOIN
Cities_usa ON F2.inbound = Cities_usa.uscode 
WHERE date(F1.s_dep) = '2021-4-30' AND date(F2.s_arv) = '2021-4-30'
AND (F2.s_dep - F1.s_arv) = '00:30:00';

CREATE VIEW Canus_one_con_final AS
SELECT cancity,uscity, count(s_arv) AS one_con, min(s_arv) AS earliest_one_con
FROM Canus_one_con NATURAL RIGHT JOIN Pair_canus
GROUP BY cancity, uscity;


-- US to Canada one connection flights on 2021-04-30--
CREATE VIEW Uscan_one_con AS
SELECT uscity, cancity,F2.s_arv
FROM Flight F1 JOIN Flight F2 ON F1.inbound = F2.outbound JOIN
Cities_usa ON F1.outbound = uscode JOIN 
Cities_canada ON F2.inbound = cancode
WHERE date(F1.s_dep) = '2021-4-30' AND date(F2.s_arv) = '2021-4-30'
AND (F2.s_dep - F1.s_arv)  >= '00:30:00';

CREATE VIEW Uscan_one_con_final AS
SELECT uscity,cancity, count(s_arv) AS one_con, min(s_arv) AS earliest_one_con
FROM Uscan_one_con NATURAL RIGHT JOIN Pair_uscan
GROUP BY cancity, uscity;


-- Canada to US two connection flights on 2021-04-30--
CREATE VIEW Canus_two_con AS
SELECT cancity, uscity,F3.s_arv
FROM
Flight F1 JOIN Flight F2 ON F1.inbound = F2.outbound JOIN
Flight F3 ON F2.inbound = F3.outbound JOIN 
Cities_canada ON F1.outbound = cancode JOIN
Cities_usa ON F3.inbound = uscode
WHERE date(F1.s_dep) = '2021-04-30' AND date(F3.s_arv) = '2021-04-30'
AND (F2.s_dep - F1.s_arv) >= '00:30:00' AND (F3.s_dep - F2.s_arv) >= '00:30:00';

CREATE VIEW Canus_two_con_final AS
SELECT cancity, uscity, count(s_arv) AS two_con, min(s_arv) AS earliest_two_con
FROM Canus_two_con NATURAL RIGHT JOIN Pair_canus
GROUP BY cancity,uscity;
-- US to Canada two connection flights on 2021-04-30--
CREATE VIEW UScan_two_con AS
SELECT uscity, cancity, F3.s_arv
FROM
Flight F1 JOIN Flight F2 ON F1.inbound = F2.outbound JOIN
Flight F3 ON F2.inbound = F3.outbound JOIN 
Cities_usa ON F1.outbound = uscode JOIN
Cities_canada ON F3.inbound = cancode
WHERE date(F1.s_dep) = '2021-04-30' AND date(F3.s_arv) = '2021-04-30'
AND (F2.s_dep - F1.s_arv) >= '00:30:00' AND (F3.s_dep - F2.s_arv) >= '00:30:00';

CREATE VIEW Uscan_two_con_final AS
SELECT uscity, cancity, count(s_arv) AS two_con, min(s_arv) AS earliest_two_con
FROM Uscan_two_con NATURAL RIGHT JOIN Pair_uscan
GROUP BY uscity,cancity;

-- Collecting all types of connection by country into two total relations -- 
--- Canada to US flights ---
CREATE VIEW Can2us_flights AS
SELECT cancity AS outbound, uscity AS inbound, direct,one_con,two_con,
least(earliest_dir,earliest_one_con,earliest_two_con) AS earliest
FROM Canus_dir_final NATURAL FULL JOIN Canus_one_con_final NATURAL FULL JOIN
Canus_two_con_final;

--- US to Canada flights --- 
CREATE VIEW  Us2can_flights AS
SELECT uscity AS outbound,cancity AS inbound, direct,one_con,two_con,
least(earliest_dir,earliest_one_con,earliest_two_con) AS earliest
FROM Uscan_dir_final NATURAL FULL JOIN Uscan_one_con_final NATURAL FULL JOIN
Uscan_two_con_final;

-- Your query that answers the question goes below the "insert into" line:
INSERT INTO q3

-- All flights from US TO CANADA AND VICE VERSA -- 
(SELECT * FROM Can2us_flights) 
UNION
(SELECT * FROM us2can_flights);
