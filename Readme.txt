[Overview]

任意のアプリをプッシュにより起動する。
Launch any application from a push notification.


[Preparation]

下記のリンクに従い google-services.json をダウンロードする。
Follow the linke below and download a google-services.json file.
https://firebase.google.com/docs/android/setup

ファイルを PushClient ディレクトリにコピーして Android Studio でコンパイルする。
Copy the file into PushClient directly and compile with Android Studio.

プッシュにより起動させるアプリをインストールする。
Install an application to be launched from a push notification.

プッシュ通知を送信するためのサーバーを設置する。
Set up a server　to send push notifications.


[Setting]

Server URL :	Registration token を受信するサーバーの URL
Activity :		プッシュにより起動させるアクティビティ名もしくはパッケージ名
Action :		プッシュ通知をタップした時に起動するアクション名

Server URL :	Server URL receives Registration token
Activity :		Activity name or package name to be launched from the push notification
Action :		Action name to be launched by tapping the notification


[Setting example]

<CSipSiple>
Activity :	com.csipsimple.ui.SipHome
Action :	com.csipsimple.phone.action.CALLLOG

<Zoiper>
Activity :	com.zoiper.android.ui.SplashScreen
Action :	(blank)


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
