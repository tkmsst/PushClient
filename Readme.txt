[概要]

任意のアプリをプッシュにより起動する。


[事前準備]

プッシュにより起動させるアプリをインストールする。
登録トークンの保管およびプッシュ通知を送信するためのサーバーを設置する。


[サンプルファイル]

起動させるアプリのパッケージ名はスクリプトファイルに記述する。
サンプルのスクリプトおよび Asterisk 用の設定ファイルは samples ディレクトリを参照。

<Script>
oauth.php :		Access token 取得 PHP スクリプト
oauth.sh :		Access token 取得シェルスクリプト
push.bat :		Windows 用プッシュ発信バッチファイル
push.sh :		Linux 用プッシュ発信シェルスクリプト
push.dat :		Registration token リスト
reg.php :		Token 登録 PHP スクリプト

<Asterisk>
extensions.conf :	ダイヤルプラン
queues.conf :		コールキュー


[注意事項]
アプリの完全な動作のために、デバイスで下記の設定を許可する必要があります。

・通知
・システム設定の変更
・他のアプリの上に重ねて表示
・電池の最適化をオフ
