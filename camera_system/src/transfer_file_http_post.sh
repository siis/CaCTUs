#!/bin/sh
#usage: ./transfer_file_http_post filename_path filename hostname

if [ "$#" -ne 3 ]; then
  echo "The script requires 3 arguments"
  exit 1
fi

filename_path=$1
filename=$2
hostname=$3
#curl request to upload encrypted frame
curl -H "correct-filename: $filename" --data-binary "@$filename_path" "$hostname"
#remove frame that has just been uploaded
rm $filename_path