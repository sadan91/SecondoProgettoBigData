#! /bin/bash
echo "Copying zip archive"
scp  -i ~/BigMetaKey.pem ../TarScript.tar.gz hadoop@ec2-52-27-32-241.us-west-2.compute.amazonaws.com:~/TarScript.tar.gz
echo "Copying untar script"
scp  -i ~/BigMetaKey.pem untar.sh hadoop@ec2-52-27-32-241.us-west-2.compute.amazonaws.com:~/untar.sh