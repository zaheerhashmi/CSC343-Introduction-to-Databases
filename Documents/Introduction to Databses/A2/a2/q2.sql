-- Q2. Refunds!

-- You must not change the next 2 lines or the table definition.
SET SEARCH_PATH TO air_travel, public;
DROP TABLE IF EXISTS q2 CASCADE;

CREATE TABLE q2 (
    airline CHAR(2),
    name VARCHAR(50),
    year CHAR(4),
    seat_class seat_class,
    refund REAL
);

-- Do this for each of the views that define your intermediate steps.  
-- (But give them better names!) The IF EXISTS avoids generating an error 
-- the first time this file is imported.
DROP VIEW IF EXISTS flights_completed CASCADE;
DROP VIEW IF EXISTS takeoff_countries CASCADE;
DROP VIEW IF EXISTS landing_countries CASCADE;
DROP VIEW IF EXISTS domestic_flights CASCADE;
DROP VIEW IF EXISTS international_flights CASCADE;
DROP VIEW IF EXISTS five_domestic_delays CASCADE;
DROP VIEW IF EXISTS ten_domestic_delays CASCADE;
DROP VIEW IF EXISTS eight_international_delays CASCADE;
DROP VIEW IF EXISTS twelve_international_delays CASCADE;
DROP VIEW IF EXISTS five_domestic_delays_refunds CASCADE;
DROP VIEW IF EXISTS ten_domestic_delays_refunds CASCADE;
DROP VIEW IF EXISTS eight_international_delays_refunds CASCADE;
DROP VIEW IF EXISTS twelve_international_delays_refunds CASCADE;
DROP VIEW IF EXISTS total_refunds CASCADE;
-- Define views for your intermediate steps here:
CREATE VIEW flights_completed AS
SELECT flight_id
FROM arrival;

CREATE VIEW takeoff_countries AS
SELECT id, country AS takeoff
FROM airport JOIN flight
ON airport.code = flight.outbound
JOIN flights_completed
ON flight.id = flights_completed.flight_id;

CREATE VIEW landing_countries AS
SELECT id, country AS landing
FROM flight JOIN airport
ON flight.inbound = airport.code
JOIN flights_completed
ON flight.id = flights_completed.flight_id;

CREATE VIEW domestic_flights AS
SELECT takeoff_countries.id
FROM takeoff_countries NATURAL JOIN landing_countries
WHERE takeoff_countries.takeoff = landing_countries.landing;

CREATE VIEW international_flights AS
SELECT takeoff_countries.id
FROM takeoff_countries NATURAL JOIN landing_countries
WHERE takeoff_countries.takeoff != landing_countries.landing;

CREATE VIEW five_domestic_delays AS
SELECT flight.id, arrival.datetime AS arrival_time
FROM domestic_flights JOIN departure
ON departure.flight_id = domestic_flights.id
JOIN arrival ON arrival.flight_id = domestic_flights.id
NATURAL JOIN flight
WHERE (departure.datetime - s_dep) >= '5:00:00'
AND (departure.datetime - s_dep) < '10:00:00'
AND arrival.datetime - s_arv > (departure.datetime - s_dep) * 0.5;

CREATE VIEW ten_domestic_delays AS
SELECT flight.id, arrival.datetime AS arrival_time
FROM domestic_flights JOIN departure
ON departure.flight_id = domestic_flights.id
JOIN arrival ON arrival.flight_id = domestic_flights.id
NATURAL JOIN flight
WHERE (departure.datetime - s_dep) >= '10:00:00'
AND arrival.datetime - s_arv > (departure.datetime - s_dep) * 0.5;

CREATE VIEW eight_international_delays AS
SELECT flight.id, arrival.datetime AS arrival_time
FROM international_flights JOIN departure
ON departure.flight_id = international_flights.id
JOIN arrival ON arrival.flight_id = international_flights.id
NATURAL JOIN flight
WHERE (departure.datetime - s_dep) >= '8:00:00'
AND (departure.datetime - s_dep) < '12:00:00'
AND arrival.datetime - s_arv > (departure.datetime - s_dep) * 0.5;

CREATE VIEW twelve_international_delays AS
SELECT flight.id, arrival.datetime AS arrival_time
FROM international_flights JOIN departure
ON departure.flight_id = international_flights.id
JOIN arrival ON arrival.flight_id = international_flights.id
NATURAL JOIN flight
WHERE (departure.datetime - s_dep) >= '12:00:00'
AND arrival.datetime - s_arv > (departure.datetime - s_dep) * 0.5;

CREATE VIEW five_domestic_delays_refunds AS
SELECT booking.flight_id, extract(year FROM arrival_time) AS year,
seat_class, sum(price) * 0.35 AS refunds
FROM five_domestic_delays JOIN booking
ON five_domestic_delays.id = booking.flight_id
GROUP BY booking.flight_id, year, seat_class;

CREATE VIEW ten_domestic_delays_refunds AS
SELECT booking.flight_id, extract(year FROM arrival_time) AS year,
seat_class, sum(price) * 0.5 AS refunds
FROM ten_domestic_delays JOIN booking
ON ten_domestic_delays.id = booking.flight_id
GROUP BY booking.flight_id, year, seat_class;

CREATE VIEW eight_international_delays_refunds AS
SELECT booking.flight_id, extract(year FROM arrival_time) AS year,
seat_class, sum(price) * 0.35 AS refunds
FROM eight_international_delays JOIN booking
ON eight_international_delays.id = booking.flight_id
GROUP BY booking.flight_id, year, seat_class;

CREATE VIEW twelve_international_delays_refunds AS
SELECT booking.flight_id, extract(year FROM arrival_time) AS year,
seat_class, sum(price) * 0.5 AS refunds
FROM twelve_international_delays JOIN booking
ON twelve_international_delays.id = booking.flight_id
GROUP BY booking.flight_id, year, seat_class;

CREATE VIEW total_refunds AS
(SELECT * FROM five_domestic_delays_refunds)
UNION
(SELECT * FROM ten_domestic_delays_refunds)
UNION
(SELECT * FROM eight_international_delays_refunds)
UNION
(SELECT * FROM twelve_international_delays_refunds);

-- Your query that answers the question goes below the "insert into" line:
INSERT INTO q2

SELECT airline, name, year, seat_class, sum(refunds) AS refund
FROM flight JOIN total_refunds ON flight.id = total_refunds.flight_id
JOIN airline ON flight.airline = airline.code
GROUP BY airline, name, year, seat_class, year;
