@echo off

setlocal EnableDelayedExpansion

set PROJECT=public-pushclient
set TTL=10s

set TOKEN_FILE=access_token.dat
set TITLE=%~1
set MESSAGE=[%date:~5% %time:~,5%] %~2
set APP=%~3

set /p ACCESS=<"%TOKEN_FILE%"

for /f "usebackq" %%I in ("%~dp0push.dat") do (
	set DATA={\"message\":{\"token\":\"%%I\",\"data\":{\"title\":\"%TITLE%\",\"msg\":\"%MESSAGE%\",\"app\":\"%APP%\"},\"android\":{\"priority\":\"high\",\"ttl\":\"%TTL%\",\"direct_boot_ok\":true}}}
	curl -X POST -H "Authorization: Bearer %ACCESS%" -H "Content-Type: application/json" -d "!DATA!" https://fcm.googleapis.com/v1/projects/%PROJECT%/messages:send
)

endlocal
