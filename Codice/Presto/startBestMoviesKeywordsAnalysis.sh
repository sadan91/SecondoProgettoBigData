#! /bin/bash
echo
echo "**************************************************************************"
echo "************** Starting Best BestMoviesKeywords Analysis *****************"
echo "**************************************************************************"
echo

# Memorizzo anche il tempo impiegato dallo script salvandolo su un file apposito
./copyOnhdfs.sh
mkdir Result
mkdir Result/BestMoviesKeywords
mkdir Result/Times
mkdir Result/Times/BestMoviesKeywords
hive -e 'show tables'|xargs -I '{}' hive -e 'drop table {}'
{ time sh -c 'hive -f BestMoviesKeywordsLoad.hql ; ./presto --server localhost:8080 --catalog hive --schema default --execute "SELECT keyword, COUNT(*) as numFilms FROM ratings CROSS JOIN keywords WHERE ratings.title=keywords.film GROUP BY keyword ORDER BY numFilms DESC LIMIT 100;" --output-format CSV > Result/BestMoviesKeywords/BestMoviesKeywords.csv' ; } 2> Result/Times/BestMoviesKeywords/tmp.txt 
cat Result/Times/BestMoviesKeywords/tmp.txt | tail -3 > Result/Times/PrestoBestMoviesKeywordsTime.txt
rm -rf Result/Times/BestMoviesKeywords/
echo "Done."
