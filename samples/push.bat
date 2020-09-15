@echo off

set APPLICATION=App-Package-Name
set TITLE=Push Client

set MESSAGE=[%date:~5% %time:~,5%] %~1 "<%~2>"
set APIKEY=AAAALPnr2wM:APA91bEoMDt4GgFZWEx5-RwzYOzNcZkk4pr3RdDQgME4Oip8fA39XcC3hEzvpR3z2Caim1vTqGGr7JNG_ITZIe1jgSHhdi2t2kHllEW5Jy2Z5mG7xv5GHfoqeHO-1AVp9blJcE0AG3Cb

setlocal EnableDelayedExpansion

for /f "usebackq" %%I in ("%~dp0push.dat") do (
	set IDS=!IDS!,\"%%I\"
)
set IDS=%IDS:~1%

curl -k --header "Authorization: key=%APIKEY%" --header Content-Type:"application/json" https://fcm.googleapis.com/fcm/send -d "{\"registration_ids\":[%IDS%],\"collapse_key\":\"pushclient\",\"priority\":\"high\",\"direct_book_ok\":true,\"data\":{\"app\":\"%APPLICATION%\",\"title\":\"%TITLE%\",\"msg\":\"%MESSAGE%\"}}"

endlocal
