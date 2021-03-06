#! /bin/bash
echo
echo "**************************************************************************"
echo "******************* Starting Best Countries Analysis *********************"
echo "**************************************************************************"
echo

# Memorizzo anche il tempo impiegato dallo script salvandolo su un file apposito
./copyOnhdfs.sh
mkdir Result
mkdir Result/BestCountries
mkdir Result/Times
mkdir Result/Times/BestCountries
hive -e 'show tables'|xargs -I '{}' hive -e 'drop table {}'
{ time sh -c 'hive -f BestCountriesLoad.hql ; ./presto --server localhost:8080 --catalog hive --schema default --execute "SELECT nation, COUNT(*) as numFilms FROM ratings CROSS JOIN countries WHERE ratings.title=countries.title GROUP BY nation ORDER BY numFilms DESC;" --output-format CSV > Result/BestCountries/BestCountries.csv' ; } 2> Result/Times/BestCountries/tmp.txt 
cat Result/Times/BestCountries/tmp.txt | tail -3 > Result/Times/PrestoBestCountriesTime.txt
rm -rf Result/Times/BestCountries
echo "Done."
