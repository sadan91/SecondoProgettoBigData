---------------------------------------
---------- BestMoviesKeywords ---------
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
-- Carico la tabella dei ratings --
--

CREATE TABLE IF NOT EXISTS ratings (distribution STRING, votes STRING, rank STRING, title STRING)
    ROW FORMAT DELIMITED FIELDS TERMINATED BY '\t' LINES TERMINATED BY '\n';
    
LOAD DATA INPATH '/input/top250movies.list' OVERWRITE INTO TABLE ratings;

--
-- Carico la tabella delle keywords
--

CREATE TABLE IF NOT EXISTS keywords (film STRING, keyword STRING)
    ROW FORMAT DELIMITED FIELDS TERMINATED BY '\t' LINES TERMINATED BY '\n';

LOAD DATA INPATH '/input/keywordsENDVALUE.list' OVERWRITE INTO TABLE keywords;

-------------------------------------------------------------------------------
-------------------------------------------------------------------------------
-------------------------------------------------------------------------------

-------------------------- Fase di analisi sui dati ---------------------------

-------------------------------------------------------------------------------
-------------------------------------------------------------------------------
-------------------------------------------------------------------------------

--
-- Le prime 100 Keywords per i migliori film di sempre --
--

INSERT OVERWRITE LOCAL DIRECTORY 'Result/BestMoviesKeywords'
SELECT keyword, COUNT(*) as numFilms
FROM ratings,keywords
WHERE ratings.title=keywords.film
GROUP BY keyword
ORDER BY numFilms DESC
LIMIT 100;

-------------------------------------------------------------------------------
-------------------------------------------------------------------------------
-------------------------------------------------------------------------------

------------------------- Eliminazione delle tabelle --------------------------

-------------------------------------------------------------------------------
-------------------------------------------------------------------------------
-------------------------------------------------------------------------------

DROP TABLE ratings;
DROP TABLE keywords;