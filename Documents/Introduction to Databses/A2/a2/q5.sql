-- Q5. Flight Hopping

-- You must not change the next 2 lines or the table definition.
SET SEARCH_PATH TO air_travel, public;
DROP TABLE IF EXISTS q5 CASCADE;

CREATE TABLE q5 (
	destination CHAR(3),
	num_flights INT
);

-- Do this for each of the views that define your intermediate steps.  
-- (But give them better names!) The IF EXISTS avoids generating an error 
-- the first time this file is imported.
DROP VIEW IF EXISTS intermediate_step CASCADE;
DROP VIEW IF EXISTS day CASCADE;
DROP VIEW IF EXISTS n CASCADE;

CREATE VIEW day AS
SELECT day::date as day FROM q5_parameters;
-- can get the given date using: (SELECT day from day)

CREATE VIEW n AS
SELECT n FROM q5_parameters;
-- can get the given number of flights using: (SELECT n from n)

-- HINT: You can answer the question by writing one recursive query below, without any more views.
-- Your query that answers the question goes below the "insert into" line:
INSERT INTO q5

WITH RECURSIVE Hopping AS (
(SELECT 1 AS i, flight.inbound AS prev_inbound, flight.s_arv AS arrival_time
FROM flight
WHERE date(s_dep) = (SELECT day FROM day)
AND outbound = 'YYZ')
	
UNION ALL

(SELECT i + 1 AS i, flight.inbound AS prev_inbound,
flight.s_arv AS arrival_time
FROM Hopping INNER JOIN flight
ON flight.outbound = Hopping.prev_inbound
WHERE flight.s_dep >= Hopping.arrival_time
AND (Flight.s_dep - Hopping.arrival_time) <= '24:00:00'
AND (s_dep - Hopping.arrival_time) >= '00:00:00'
AND i < (SELECT n FROM n))
)

SELECT prev_inbound AS destination, i AS num_flights FROM Hopping;
