---------------------------------------
------------ BestProducers ------------
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
-- Carico la tabella dei produttori --
--

CREATE TABLE IF NOT EXISTS producersRAW (producer STRING, rowFilm STRING)
    ROW FORMAT DELIMITED FIELDS TERMINATED BY '\t' LINES TERMINATED BY '\n';

LOAD DATA INPATH '/input/producersENDVALUE.list' OVERWRITE INTO TABLE producersRAW;

CREATE TABLE IF NOT EXISTS producers (producer STRING, filmArray ARRAY<STRING>);

INSERT INTO TABLE producers
SELECT producer, filmArray
FROM (SELECT producer, split(rowFilm,'<ENDVALUE>') as filmArray FROM producersRAW) as producers2;

DROP TABLE producersRAW;

--
-- Carico la tabella dei ratings --
--

CREATE TABLE IF NOT EXISTS ratings (distribution STRING, votes STRING, rank STRING, title STRING)
    ROW FORMAT DELIMITED FIELDS TERMINATED BY '\t' LINES TERMINATED BY '\n';
    
LOAD DATA INPATH '/input/top250movies.list' OVERWRITE INTO TABLE ratings;

-------------------------------------------------------------------------------
-------------------------------------------------------------------------------
-------------------------------------------------------------------------------

-------------------------- Fase di analisi sui dati ---------------------------

-------------------------------------------------------------------------------
-------------------------------------------------------------------------------
-------------------------------------------------------------------------------

--
-- Estraggo i film di successo per ogni produttore --
--

add jar ArrayContainsSubstringUDF.jar;
CREATE TEMPORARY FUNCTION array_contains_substring AS 'it.uniroma3.bigDataProject.ArrayContainsSubstringUDF';

INSERT OVERWRITE LOCAL DIRECTORY 'Result/BestProducers'
SELECT producer, COUNT(*) as numFilms
FROM ratings, producers
WHERE array_contains_substring(title,filmArray)
GROUP BY producer
ORDER BY numFilms DESC;

-------------------------------------------------------------------------------
-------------------------------------------------------------------------------
-------------------------------------------------------------------------------

------------------------- Eliminazione delle tabelle --------------------------

-------------------------------------------------------------------------------
-------------------------------------------------------------------------------
-------------------------------------------------------------------------------

DROP TABLE producers;
DROP TABLE ratings;