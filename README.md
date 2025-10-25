# Grow Up Backend

## 概要

このプロジェクトは Spring Boot を使用したバックエンド API サーバーです。

## 必要環境

-   Java 17 以上
-   Gradle
-   PostgreSQL

## セットアップ手順

1. リポジトリをクローン

```sh
   git clone https://github.com/tavanquangkk/grow-up-backend-spring.git


```

2. データベースを作成

```sh
   # DB作成
   createdb growup_db
   # ユーザー作成（パスワードを聞かれます）
   createuser your_db_user --pwprompt
   # 権限付与
   psql -c "GRANT ALL PRIVILEGES ON DATABASE growup_db TO your_db_user;"
```

3. JWT Key 生成

```sh

   mkdir -p src/main/resources/keys

   openssl genpkey -algorithm RSA -out src/main/resources/keys/private-key.pem -pkeyopt rsa_keygen_bits:2048

   openssl rsa -pubout -in src/main/resources/keys/private-key.pem -out src/main/resources/keys/public-key.pem

```

4. `src/main/resources/application.properties` を作成し、DB 接続情報を設定します。

-   環境変数で置き換えてください（推奨）。（直接 application.properties に置き換えても可能です）

```properties
   # DB 接続情報
   spring.datasource.driver-class-name=org.postgresql.Driver
   spring.datasource.url=${JDBC_DATABASE_URL}
   spring.datasource.username=${JDBC_DATABASE_USERNAME}
   spring.datasource.password=${JDBC_DATABASE_PASSWORD}
   spring.jpa.hibernate.ddl-auto=update

   # Cloudinary config
   cloudinary.cloud_name=${CLOUDINARY_CLOUD_NAME}
   cloudinary.api_key=${CLOUDINARY_API_KEY}
   cloudinary.api_secret=${CLOUDINARY_API_SECRET}

   # JWT config
   jwt.private-key=${RSA_PRIVATE_KEY} # 環境変数にファイルの中身を設定する、もしくは classpath:keys/private-key.pem
jwt.public-key=${RSA_PUBLIC_KEY}   # 同上
   jwt.issuer=grow-up-app
   jwt.expiration=900000

```

5. アプリケーションを起動

```
   ./gradlew bootRun

```

もし Docker で動かす場合は

```sh
docker-compose up --build
```

## その他

### API ドキュメント

-   SwaggerUI で全 API を確認できます。

```デフォルトの URL（ローカル環境）：

http://localhost:8080/swagger-ui/index.html


Swagger には以下の情報が含まれます：

エンドポイント URL

リクエスト・レスポンス形式

HTTP メソッド（GET, POST, PUT, DELETE ）
```
