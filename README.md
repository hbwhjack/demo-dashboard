# Demo Dashboard

Spring Boot + PostgreSQL の簡易設定ダッシュボード。

## Features
- 設定画面（一般/環境/コードレビュー/コネクター/使用状況/データコントロール）
- 変更内容を PostgreSQL に保存
- REST API: `/api/settings`

## Requirements
- Java 17+
- Maven
- PostgreSQL

## Setup
1) DB を用意
```bash
createdb demo_dashboard
```

2) 環境変数を設定
```bash
export DB_URL=jdbc:postgresql://localhost:5432/demo_dashboard
export DB_USER=demo_dashboard_user
export DB_PASSWORD=demo_dashboard_pass_123
```

3) 起動
```bash
mvn spring-boot:run
```

## URL
- App: http://localhost:3000/
- Health: http://localhost:3000/api/health

## Notes
ローカル用の `application.properties` は Git 管理外です。例は `application.example.properties` を参照してください。
