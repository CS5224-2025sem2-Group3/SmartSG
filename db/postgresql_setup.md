# PostgreSQL Setup Guide

# 1. Install PostgreSQL

## Mac (Homebrew)

Install PostgreSQL:

```bash
brew install postgresql
```

Start PostgreSQL service:

```bash
brew services start postgresql
```

Verify installation:

```bash
psql --version
```

---

## Ubuntu / Linux

Install PostgreSQL:

```bash
sudo apt update
sudo apt install postgresql postgresql-contrib
```

Start PostgreSQL:

```bash
sudo service postgresql start
```

Verify installation:

```bash
psql --version
```

---

## Windows

1. Download PostgreSQL installer
   https://www.postgresql.org/download/windows/

2. Run the installer and follow the setup steps.

Important settings during installation:

* Port: `5432`
* Default user: `postgres`
* Set a password and remember it

After installation, open **SQL Shell (psql)** from the Start Menu.

---

# 2. Connect to PostgreSQL

Open terminal and run:

```bash
psql postgres
```

or

```bash
psql -U postgres
```

---

# 3. Create Project Database

Inside the `psql` shell run:

```sql
CREATE DATABASE smartsg;
```

List databases:

```sql
\l
```

Connect to the project database:

```sql
\c smartsg
```

---

# 4. Run Schema File

Execute the schema file to create all tables.

```bash
psql -U postgres -d smartsg -f schema.sql
```

# 5. Verify Tables

Enter PostgreSQL shell again:

```bash
psql -d smartsg
```

List tables:

```sql
\dt
```

Describe a table:

```sql
\d "User"
```

Example output:

```
Column        | Type
--------------|-------
user_id       | integer
name          | varchar
email         | varchar
```

---

# 6. Insert Mock Data

Example test insert:

```sql
INSERT INTO "User"(name, email, password_hash, school)
VALUES ('Alice', 'alice@email.com', 'hashed_password', 'NUS');
```

Check data:

```sql
SELECT * FROM "User";
```

---

# Summary

Setup process:

1. Install PostgreSQL
2. Start PostgreSQL service
3. Create database `smartsg`
4. Run `schema.sql`
5. Insert mock data
