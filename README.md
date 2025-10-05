# How to run the project

Spring boot :
./mvnw clean compile -DskipTests
./mvnw spring-boot:run

Frontend :
npm install
npm run dev

ML :
./dev-up.sh
python3 -m ml.server \
    --excel junctionx-uniTUD-2025/ml/data/uber_hackathon_v2_mock_data.xlsx \
    --model junctionx-uniTUD-2025/ml/artifacts/model.pkl \
    --host 0.0.0.0 --port 8000


# 🛠️ Project Setup – Docker + PostgreSQL + pgAdmin + Spring Boot

This document explains how to set up the local infrastructure (PostgreSQL + pgAdmin) and the Spring Boot backend for the **junctionx-uniTUD-2025** project.  
Every team member must follow these steps after cloning the repository.

---

## 1. 📦 Prerequisites
- Install **Docker Desktop**  
  - [Download here](https://www.docker.com/products/docker-desktop)  
  - Verify installation:  
    ```bash
    docker --version
    docker compose version
    ```
- Install **Java 21!!!**  (Ensure you are using version 21)
  - Verify installation:  
    ```bash
    java -version
    ```
- Install **Maven Wrapper** (comes with Spring Boot project)  
  - Verify:  
    ```bash
    ./mvnw -v
    ```

---

## 2. 🐳 Start PostgreSQL + pgAdmin with Docker
From the project root:

```bash
# Start PostgreSQL + pgAdmin
docker compose -f infra/docker-compose.yml up -d
```
# Check running containers
```
docker ps
You should see:
```

junctionx-unitud-2025-postgres → port 55432:5432

junctionx-unitud-2025-pgadmin → port 5050:80

---

3. 🔒 Database credentials

User: app

Password: app

Database: appdb

These credentials are used both in pgAdmin and in the Spring Boot backend.

---

4. 🖥️ Access pgAdmin

URL: http://localhost:5050

Login:

Email: admin@example.com

Password: admin

Add a new server in pgAdmin

Right-click Servers → Create → Server…

General tab

Name: LocalDB

Connection tab

Host: db

Port: 5432

Database: appdb

Username: app

Password: app

✅ If correct, you will see the database appdb.

---

5. 🌱 Load demo data

Use the scripts/seed.sql file to populate the database with sample data.

Option A: via pgAdmin

Open Query Tool on appdb.

Copy and paste the contents of scripts/seed.sql.

Execute with the ⚡ button.

You should see rows inserted in the demo_users table.

---

6. 📴 Shut down containers

When you no longer need them:

docker compose -f infra/docker-compose.yml down


You should see:

junctionx-unitud-2025-postgres → port 55432:5432

junctionx-unitud-2025-pgadmin → port 5050:80

7. ✅ Quick check script

Run the provided script to validate your setup:

./scripts/check_db.sh


Expected output:

 id |  name   |       email
----+---------+-------------------
  1 | Alice   | alice@example.com
  2 | Bob     | bob@example.com
  3 | Charlie | charlie@example.com
(3 rows)

✅ DB OK


Docker setup:

Install docker desktop

user app, password app, db appdb

Startup postgres and pgadmin with docker  
docker compose -f infra/docker-compose.yml up -d
docker ps


Shut down
docker compose -f infra/docker-compose.yml down


pgAdmin: http://localhost:5050

login: admin@example.com / admin

---
## Running desktop app 

Desktop app can be run by running the following command in the client module:

mvn javafx:run

Make sure to reload maven to load the pom.xml files.

# Hackathon Setup Checklist (Local Env)
1. Java 21

Check Java version:

./scripts/check_java.sh


Expected:

✅ Java 21 is correctly installed and active.

2. Docker & Containers

Verify running containers:

docker ps


Expected:

junctionx-unitud-2025-postgres → port 55432:5432 (healthy)
junctionx-unitud-2025-pgadmin → port 5050:80

3. PostgreSQL & pgAdmin access

Open 👉 http://localhost:5050

Login:

Email: admin@example.com

Password: admin

Add new server → Name: LocalDB

Host: db

Port: 5432

Database: appdb

User: app

Password: app

✅ If it connects, the database is ready.

4. Backend Health

Start the backend:

cd backend
./mvnw spring-boot:run


In another terminal:

./scripts/check_backend.sh


Expected:

✅ Backend OK


Also check manually:

curl http://localhost:8080/api/health


Expected:

{"status":"ok","db":"up"}

5. API Endpoints (User demo)

Create a user:

curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"name":"Alice","email":"alice@example.com"}'


List users:

curl http://localhost:8080/api/users


Expected:

[{"id":1,"name":"Alice","email":"alice@example.com"}]


✅ Verify in pgAdmin → app_user table that the row is visible.

6. Static Frontend

Open 👉 http://localhost:8080/

Add a user through the form.

Check that the table refreshes with the new entry.

Confirm in pgAdmin that the user was inserted.

7. Seed data (optional, for demo)

Run the seed script:

docker exec -i junctionx-unitud-2025-postgres \
  psql -U app -d appdb < scripts/seed.sql


✅ You should see 3 rows (Alice, Bob, Charlie).



# Quick System Checks with Scripts

We provide helper scripts to quickly validate the setup. Make sure you gave them execution permissions first:

chmod +x scripts/check_db.sh scripts/check_backend.sh scripts/full_check.sh scripts/reset_db.sh

1. Check Database

Lists all tables in the appdb database.

./scripts/check_db.sh


Expected output: a list of tables, for example:

 Schema |    Name    | Type  | Owner
--------+------------+-------+-------
 public | app_user   | table | app
(1 row)

2. Check Backend Health

Verifies that the backend is running and connected to PostgreSQL.

./scripts/check_backend.sh


Expected output:

✅ Backend OK

3. Full System Check

Runs all checks together: Docker containers, database, backend health.

./scripts/full_check.sh


Expected output:

🔍 Running full system check...
✅ Postgres container running
✅ pgAdmin container running
✅ Backend connected to DB

🎉 ALL CHECKS PASSED — System is ready!

4. DB reset with seed.sql for clean start


./scripts/reset_db.sh
