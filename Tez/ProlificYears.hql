---------------------------------------
------------ ProlificYears ------------
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
-- Adesso seleziono l'anno piu' prolifico --
--

INSERT OVERWRITE LOCAL DIRECTORY 'Result/ProlificYears'
SELECT year, COUNT(*) as numFilms
FROM movies
GROUP BY year
ORDER BY numFilms DESC
LIMIT 1;

-------------------------------------------------------------------------------
-------------------------------------------------------------------------------
-------------------------------------------------------------------------------

------------------------- Eliminazione delle tabelle --------------------------

-------------------------------------------------------------------------------
-------------------------------------------------------------------------------
-------------------------------------------------------------------------------

DROP TABLE movies;