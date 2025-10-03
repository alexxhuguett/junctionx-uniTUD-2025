

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