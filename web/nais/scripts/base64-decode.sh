#!/usr/bin/env sh

if test -d /var/run/secrets/nais.io/vault;
then
    for FILE in /var/run/secrets/nais.io/vault/*.b64
    do
        echo "base64-decoding $FILE"
        OUT=`echo $(dirname $FILE)/$(basename $FILE .b64)`
        base64 -d $FILE > $OUT
    done
fi