#!/usr/bin/env sh

if test -d /var/run/secrets/nais.io/vault;
then
    for FILE in /var/run/secrets/nais.io/vault/*.b64
    do
        OUT=`echo $(dirname $FILE)/$(basename $FILE .b64)`
        echo "Base64-decode $FILE to $OUT"
        base64 -d $FILE > $OUT
    done
fi