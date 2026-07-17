# Database Setup — TiDB Cloud (Shared)

CookFlow uses a shared cloud database hosted on **TiDB Cloud Serverless** (MySQL 8.0 compatible, free tier).  
This means everyone on the team connects to the same database — no local MySQL needed.

---

## Connection values

Copy these into `backend/.env`, replacing the default `DB_*` block:

```env
DB_CONNECTION=mysql
DB_HOST=gateway01.eu-central-1.prod.aws.tidbcloud.com
DB_PORT=4000
DB_DATABASE=cookflow
DB_USERNAME=CDzXWGVuk2iVSYB.root
DB_PASSWORD=P31TxbGsH5Js90WE
MYSQL_ATTR_SSL_CA=/absolute/path/to/CookFlow/backend/database/certif/certification.pem
```

> **MYSQL_ATTR_SSL_CA** must be an **absolute path** on your machine.  
> The cert file is already in the repo at `backend/database/certif/certification.pem`.  
> Example on Linux/Mac: `/home/youruser/Documents/CookFlow/backend/database/certif/certification.pem`  
> Example on Windows: `C:\Users\youruser\Documents\CookFlow\backend\database\certif\certification.pem`

---

## First-time setup

You do **not** need to run `migrate:fresh` unless you're resetting the DB intentionally.  
The database is already migrated and seeded.

To verify your connection works:

```bash
cd backend
php artisan db:show
```

You should see `MySQL 8.0.11-TiDB-v8.5.3-serverless` and `Tables: 13`.

---

## Test credentials

| Role | Email | Password |
|------|-------|----------|
| Admin | admin@cookflow.com | admin |
| Waiter | camarero@cookflow.com | camarero |

---

## Resetting the database

Only do this if you need a clean slate — it affects everyone on the team:

```bash
php artisan migrate:fresh --seed
```

---

## Notes

- TiDB Cloud requires TLS — the `MYSQL_ATTR_SSL_CA` line is mandatory, connections will fail without it.
- The free tier gives 5 GB storage and 50 M row reads/month — more than enough for a TFG.
- The CA cert (`certification.pem`) is a public root certificate, safe to commit.
- **Never commit `.env`** — it is already in `.gitignore`.
