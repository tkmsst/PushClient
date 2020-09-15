#!/bin/sh

APPLICATION=App-Package-Name
TITLE="Push Client"

DATE=`date '+%m/%d %R'`
MESSAGE="[$DATE] $1 <$2>"
APIKEY=AAAALPnr2wM:APA91bEoMDt4GgFZWEx5-RwzYOzNcZkk4pr3RdDQgME4Oip8fA39XcC3hEzvpR3z2Caim1vTqGGr7JNG_ITZIe1jgSHhdi2t2kHllEW5Jy2Z5mG7xv5GHfoqeHO-1AVp9blJcE0AG3Cb

while read TOKEN; do
	if [ -n "$TOKEN" ]; then
		IDS="$IDS",\"$TOKEN\"
	fi
done < `dirname $0`/push.dat
IDS=`echo "$IDS" | sed 's/^.//'`

curl -k --header "Authorization: key=$APIKEY" --header Content-Type:"application/json" https://fcm.googleapis.com/fcm/send -d "{\"registration_ids\":[$IDS],\"collapse_key\":\"pushclient\",\"priority\":\"high\",\"direct_book_ok\":true,\"data\":{\"app\":\"$APPLICATION\",\"title\":\"$TITLE\",\"msg\":\"$MESSAGE\"}}"
