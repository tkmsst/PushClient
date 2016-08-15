set APIKEY=Your-API-Key
set APPLICATION=Package-Name

set MESSAGE=[%date:~5% %time:~,5%] %1

for /f "usebackq" %%I in ("%~dp0push.dat") do (
	curl -k --header "Authorization: key=%APIKEY%" --header Content-Type:"application/json" https://fcm.googleapis.com/fcm/send -d "{\"to\":\"%%I\",\"collapse_key\":\"pushclient\",\"priority\":\"high\",\"content_available\":true,\"data\":{\"app\":\"%APPLICATION%\",\"msg\":\"%MESSAGE%\"}}"
)
