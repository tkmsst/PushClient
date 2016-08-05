#!/bin/sh

APIKEY=Your-API-Key

while read line; do
	if [ -n "$tokens" ]; then
		tokens=${tokens},\"${line}\"
	else
		tokens=\"${line}\"
	fi
done < `dirname $0`/push.dat

cnt=3
for i in `seq $cnt`; do
	RESULT=`curl -k --header "Authorization: key=$APIKEY" --header Content-Type:"application/json" https://fcm.googleapis.com/fcm/send -d "{\"registration_ids\":[$tokens],\"collapse_key\":\"pushclient\",\"priority\":\"high\",\"content_available\":true,\"data\":{\"name\":\"$1\",\"num\":\"$2\"}}"`
	if `echo $RESULT | grep -sq "\"failure\":0"`; then
		break
	fi
	if [ $i -lt $cnt ]; then
		sleep 1
	fi
done
