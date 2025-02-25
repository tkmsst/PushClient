#!/bin/sh

PROJECT=public-pushclient
TTL=10s

TOKEN_FILE='/tmp/access_token.dat'
TITLE="$1"
MESSAGE="[`date '+%m/%d %R'`] $2"
APP="$3"

ACCESS=`cat "${TOKEN_FILE}"`
if [ -z ${ACCESS} ]; then
	exit 1
fi

while read TOKEN; do
	if [ -n ${TOKEN} ]; then
		DATA='{"message":{"token":"'${TOKEN}'","data":{"title":"'${TITLE}'","msg":"'${MESSAGE}'","app":"'${APP}'"},"android":{"priority":"high","ttl":"'${TTL}'","direct_boot_ok":true}}}'
		curl -X POST -H "Authorization: Bearer ${ACCESS}" -H "Content-Type: application/json" -d "${DATA}" https://fcm.googleapis.com/v1/projects/${PROJECT}/messages:send
	fi
done < `dirname $0`/push.dat
