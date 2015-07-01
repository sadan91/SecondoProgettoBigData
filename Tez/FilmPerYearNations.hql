---------------------------------------
---------- FilmPerYearNations ---------
---------------------------------------

-------------------------------------------------------------------------------
-------------------------------------------------------------------------------
-------------------------------------------------------------------------------

----------------------- Caricamento delle tabelle -----------------------------

-------------------------------------------------------------------------------
-------------------------------------------------------------------------------
-------------------------------------------------------------------------------
SET hive.execution.engine=tez;
--
-- Estraggo le nazioni di produzione dei film --
--

CREATE TABLE IF NOT EXISTS countries (title STRING, nation STRING)
    ROW FORMAT DELIMITED FIELDS TERMINATED BY '\t' LINES TERMINATED BY '\n';

LOAD DATA INPATH '/input/countriesENDVALUE.list' OVERWRITE INTO TABLE countries;

--
-- Estraggo i film --
--

CREATE TABLE IF NOT EXISTS movies (title STRING, year STRING)
    ROW FORMAT DELIMITED FIELDS TERMINATED BY '\t' LINES TERMINATED BY '\n';

LOAD DATA INPATH '/input/moviesENDVALUE.list' OVERWRITE INTO TABLE movies;

-------------------------------------------------------------------------------
-------------------------------------------------------------------------------
-------------------------------------------------------------------------------

-------------------------- Fase di analisi sui dati ---------------------------

-------------------------------------------------------------------------------
-------------------------------------------------------------------------------
-------------------------------------------------------------------------------

--
-- Numero di film prodotti per ciascun anno in ogni nazione --
--
INSERT OVERWRITE LOCAL DIRECTORY 'Result/FilmPerYearNations'
SELECT nation, year, COUNT(*) as numFilms
FROM movies, countries
WHERE movies.title=countries.title
GROUP BY nation, year;

-------------------------------------------------------------------------------
-------------------------------------------------------------------------------
-------------------------------------------------------------------------------

------------------------- Eliminazione delle tabelle --------------------------

-------------------------------------------------------------------------------
-------------------------------------------------------------------------------
-------------------------------------------------------------------------------

DROP TABLE countries;
DROP TABLE movies;