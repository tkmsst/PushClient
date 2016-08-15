#!/bin/sh

APIKEY=Your-API-Key
APPLICATION=Package-Name

DATE=`date '+%m/%d %R'`
MESSAGE="[$DATE] $1"

cnt=3
while read token; do
	for i in `seq $cnt`; do
		RESULT=`curl -k --header "Authorization: key=$APIKEY" --header Content-Type:"application/json" https://fcm.googleapis.com/fcm/send -d "{\"to\":\"$token\",\"collapse_key\":\"pushclient\",\"priority\":\"high\",\"content_available\":true,\"data\":{\"app\":\"$APPLICATION\",\"msg\":\"$MESSAGE\"}}"`
		if `echo $RESULT | grep -sq "\"success\":1"` || [ $i -eq $cnt ]; then
			break
		fi
		sleep 1
	done
done < `dirname $0`/push.dat
