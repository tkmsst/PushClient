[Overview]

任意のアプリをプッシュにより起動する。
Launch any application from a push notification.


[Preparation]

下記のリンクに従い google-services.json をダウンロードする。
Follow the link below and download a google-services.json file.
https://firebase.google.com/docs/android/setup

ファイルを PushClient ディレクトリにコピーして Android Studio でコンパイルする。
Copy the file into PushClient directly and compile with Android Studio.

プッシュにより起動させるアプリをインストールする。
Install an application to be launched from a push notification.

プッシュ通知を送信するためのサーバーを設置する。
Set up a server　to send push notifications.


[Setting]

Server URL :	Registration token を受信するサーバーの URL
Server URL :	Server URL receives Registration token

プッシュにより起動させるアプリケーションのパッケージ名はスクリプトファイルに記述
する。
A package name of an application to be launched from a push notification should
be specified in a script file.


[Sample file]

<Script>
push.bat :		Windows 用プッシュ発信バッチファイル
push.sh :		Linux 用プッシュ発信スクリプト
push.dat :		Registration token リスト
reg.php :		Token 登録用 PHP スクリプト

push.bat :		Push batch file for Windows
push.sh :		Push script for Linux
push.dat :		Registration token list
reg.php :		PHP script for token registration

<Asterisk>
extensions.conf :	ダイヤルプラン
queues.conf :		コールキュー

extensions.conf :	Dialplan
queues.conf :		Call queues
