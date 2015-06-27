#! /bin/bash
echo
echo "**************************************************************************"
echo "********************** Starting Best Actor Analysis **********************"
echo "**************************************************************************"
echo

# Memorizzo anche il tempo impiegato dallo script salvandolo su un file apposito
#./copyOnhdfs.sh
mkdir Result/BestActors
mkdir Result/Times
mkdir Result/Times/BestActors
{ time pig BestActors.pig ; } 2> Result/Times/BestActors/tmp.txt 
cat Result/Times/BestActors/tmp.txt | tail -3 > Result/Times/PigBestActorsTime.txt
rm -rf Result/Times/BestActors
echo "Done."