#! /bin/bash
echo
echo "**************************************************************************"
echo "************************* Starting hive analysis *************************"
echo "**************************************************************************"
echo

# Memorizzo anche il tempo impiegato dallo script salvandolo su un file apposito
mkdir Result
{ time hive -f hiveAnalysis.hql ; } 2> Result/tmp.txt 
cat Result/tmp.txt | tail -3 > Result/HiveTime.txt
rm Result/tmp.txt
# Copio i risultati su s3
aws s3 cp Result s3://bigmetabucket/IMDb/HIVERESULT/ --recursive
# Creo lo zip
zip -r HIVERESULT.zip Result
aws s3 cp HIVERESULT.zip s3://bigmetabucket/IMDb/