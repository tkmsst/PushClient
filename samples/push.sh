#!/bin/sh

APIKEY=AAAALPnr2wM:APA91bEoMDt4GgFZWEx5-RwzYOzNcZkk4pr3RdDQgME4Oip8fA39XcC3hEzvpR3z2Caim1vTqGGr7JNG_ITZIe1jgSHhdi2t2kHllEW5Jy2Z5mG7xv5GHfoqeHO-1AVp9blJcE0AG3Cb
APPLICATION=App-Package-Name

DATE=`date '+%m/%d %R'`
MESSAGE="[$DATE] $1 <$2>"
NUMBER="$2"

cnt=3
while read token; do
	for i in `seq $cnt`; do
		RESULT=`curl -k --header "Authorization: key=$APIKEY" --header Content-Type:"application/json" https://fcm.googleapis.com/fcm/send -d "{\"to\":\"$token\",\"collapse_key\":\"pushclient\",\"priority\":\"high\",\"content_available\":true,\"data\":{\"app\":\"$APPLICATION\",\"msg\":\"$MESSAGE\",\"num\":\"$NUMBER\"}}"`
		if `echo $RESULT | grep -sq "\"success\":1"` || [ $i -eq $cnt ]; then
			break
		fi
		sleep 1
	done
done < `dirname $0`/push.dat
