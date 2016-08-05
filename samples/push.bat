set APIKEY=Your-API-Key

for /f "usebackq" %%I in ("%~dp0push.dat") do (
	curl -k --header "Authorization: key=%APIKEY%" --header Content-Type:"application/json" https://fcm.googleapis.com/fcm/send -d "{\"to\":\"%%I\",\"collapse_key\":\"pushclient\",\"priority\":\"high\",\"content_available\":true,\"data\":{\"name\":\"%1\",\"num\":\"%2\"}}"
)
