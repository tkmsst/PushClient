[Overview]

任意のアプリをプッシュにより起動する。


[Prerequisites]

プッシュにより起動させるアプリをインストールする。
登録トークンの保管およびプッシュ通知を送信するためのサーバーを設置する。


[Sample files]

起動させるアプリのパッケージ名はスクリプトファイルに記述する。
サンプルのスクリプトおよび Asterisk 用の設定ファイルは samples ディレクトリを参照。

<Script>
push.bat :		Windows 用プッシュ発信バッチファイル
push.sh :		Linux 用プッシュ発信スクリプト
push.dat :		Registration token リスト
reg.php :		Token 登録用 PHP スクリプト

<Asterisk>
extensions.conf :	ダイヤルプラン
queues.conf :		コールキュー
