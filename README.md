# Grow Up Backend (Spring Boot)

Grow Up アプリケーションの基盤となる REST API サーバーです。Java 21 と Spring Boot 3.5.4 を使用して構築されています。

## 🚀 主な機能
- **認証・認可**: JWT (JSON Web Token) と RSA 暗号を用いたセキュアな認証。
- **管理者機能**: ユーザー管理、勉強会（Workshop）管理、スキル（Skill）管理。
- **画像管理**: Cloudinary API を活用したプロフィール画像のアップロードと保存。
- **データベース**: PostgreSQL を使用した堅牢なデータ永続化。

## 🛠 使用技術
- **Language**: Java 21
- **Framework**: Spring Boot 3.5.4
- **Security**: Spring Security (JWT / RS256)
- **Database**: PostgreSQL / Spring Data JPA
- **Build Tool**: Gradle (Kotlin DSL)
- **External API**: Cloudinary (Image Hosting)

## 📦 デプロイと実行
本プロジェクトは Docker マルチステージビルドを採用しており、軽量な JRE イメージで動作します。

### 環境変数 (.env)
以下の環境変数を設定して実行します：
- `JDBC_DATABASE_URL`: DB 接続パス
- `JDBC_DATABASE_USERNAME / PASSWORD`: DB 認証情報
- `CLOUDINARY_CLOUD_NAME / API_KEY / API_SECRET`: 画像サーバー設定
- `RSA_PRIVATE_KEY_PATH / RSA_PUBLIC_KEY_PATH`: JWT 署名用の PEM ファイルパス

### 開発用実行
```bash
./gradlew bootRun
```

## 📂 ディレクトリ構成
- `config/`: Security, JWT, Cloudinary などの設定クラス。
- `controller/`: 各エンドポイントのハンドリング（admin/auth などのパッケージ分け）。
- `domain/`: JPA エンティティクラス。
- `dto/`: リクエスト/レスポンス用データ転送オブジェクト。
- `service/`: ビジネスロジック。
- `repository/`: データベースアクセル層。