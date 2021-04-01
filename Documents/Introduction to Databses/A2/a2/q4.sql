-- Q4. Plane Capacity Histogram

-- You must not change the next 2 lines or the table definition.
SET SEARCH_PATH TO air_travel, public;
DROP TABLE IF EXISTS q4 CASCADE;

CREATE TABLE q4 (
	airline CHAR(2),
	tail_number CHAR(5),
	very_low INT,
	low INT,
	fair INT,
	normal INT,
	high INT
);

-- Do this for each of the views that define your intermediate steps.  
-- (But give them better names!) The IF EXISTS avoids generating an error 
-- the first time this file is imported.
DROP VIEW IF EXISTS intermediate_step CASCADE;

DROP VIEW IF EXISTS Planes_departed CASCADE;
DROP VIEW IF EXISTS Plane_capacity CASCADE;
DROP VIEW IF EXISTS Flight_capacity CASCADE;
DROP VIEW IF EXISTS Flight_usage CASCADE;
DROP VIEW IF EXISTS Flight_usage_percentages CASCADE; 
DROP VIEW IF EXISTS Flight_usage_percentages_ CASCADE;
DROP VIEW IF EXISTS Very_low CASCADE;
DROP VIEW IF EXISTS Low CASCADE;
DROP VIEW IF EXISTS Fair CASCADE;
DROP VIEW IF EXISTS Normal CASCADE;
DROP VIEW IF EXISTS High CASCADE;



-- Define views for your intermediate steps here:

CREATE VIEW Planes_departed AS

SELECT Plane.airline AS airline, plane AS tail_number 
FROM Plane JOIN Flight ON plane = tail_number
GROUP BY Plane.airline,plane;

CREATE VIEW Plane_capacity AS 
SELECT tail_number,airline, capacity_economy + capacity_economy + capacity_first AS plane_capacity
FROM Plane;

CREATE VIEW Flight_capacity AS
SELECT Flight.id AS flight_id, Flight.airline AS airline, tail_number , plane_capacity AS flight_capacity 
FROM Plane_capacity FULL JOIN Flight ON Plane_capacity.tail_number = Flight.plane;


CREATE VIEW Flight_usage AS 
SELECT   FC.flight_id AS flight_id, FC.airline AS airline, tail_number, count(pass_id) AS flight_usage
FROM Flight_capacity FC FULL JOIN Booking BK ON FC.flight_id = BK.flight_id
GROUP BY FC.flight_id, FC.airline, tail_number
ORDER BY FC.flight_id;

CREATE VIEW Flight_usage_percentages AS
SELECT flight_id, airline,tail_number,CAST(flight_usage AS FLOAT)/flight_capacity * 100 as percentage
FROM Flight_usage FU NATURAL FULL JOIN flight_capacity FC;


CREATE VIEW Very_low AS
SELECT flight_id,airline,tail_number, count(percentage) AS very_low 
FROM Flight_usage_percentages
WHERE (percentage >= 0) AND (percentage < 20)
GROUP BY flight_id,airline,tail_number,percentage;


CREATE VIEW Low AS
SELECT flight_id,airline,tail_number, count(percentage) AS low 
FROM Flight_usage_percentages
WHERE  (percentage >= 20) AND (percentage < 40)
GROUP BY flight_id,airline,tail_number;

CREATE VIEW Fair AS
SELECT flight_id,airline,tail_number, count(percentage) AS fair 
FROM Flight_usage_percentages
WHERE (percentage >= 40) AND (percentage < 60)
GROUP BY flight_id,airline,tail_number;

CREATE VIEW Normal AS
SELECT flight_id,airline,tail_number, count(percentage) AS normal 
FROM Flight_usage_percentages
WHERE (percentage >= 60) AND (percentage < 80)
GROUP BY flight_id,airline,tail_number;

CREATE VIEW High AS
SELECT flight_id,airline,tail_number, count(percentage) AS high
FROM Flight_usage_percentages
WHERE percentage >= 80
GROUP BY flight_id,airline,tail_number;

CREATE VIEW Combined AS 
SELECT airline,tail_number,count(very_low) AS very_low, count(low) AS low,
count(fair) AS fair, count(Normal) AS normal, count(high) AS high
FROM Very_low NATURAL FULL JOIN LOW
NATURAL FULL JOIN  Fair 
NATURAL FULL JOIN Normal
NATURAL FULL JOIN High
GROUP BY airline,tail_number;




-- Your query that answers the question goes below the "insert into" line:
INSERT INTO q4

SELECT * 
FROM Combined;