# Grow Up Backend

## 概要
このプロジェクトは Spring Boot を使用したバックエンド API サーバーです。

## 必要環境
- Java 17 以上
- Gradle
- PostgreSQL (推奨)

## セットアップ手順
1. リポジトリをクローン
   ```sh
git clone <このリポジトリのURL>
```
2. データベースを作成
   - PostgreSQL で新しいデータベースを作成します。
   - 例: `growup_db`
3. `src/main/resources/application.properties` を編集し、DB接続情報を設定します。
   ```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/growup_db
spring.datasource.username=your_db_user
spring.datasource.password=your_db_password
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```
4. Gradle で依存関係をインストール
   ```sh
./gradlew build
```
5. アプリケーションを起動
   ```sh
./gradlew bootRun
```

## データベース設定例 (PostgreSQL)
```sh
# DB作成
createdb growup_db
# ユーザー作成
createuser your_db_user --pwprompt
# 権限付与
psql -c "GRANT ALL PRIVILEGES ON DATABASE growup_db TO your_db_user;"
```

## その他
- 設定ファイルは `src/main/resources/application.properties` にあります。
- 詳細はコメントや各クラスの Javadoc を参照してください。

## ライセンス
MIT License
