# Database Backup & Disaster Recovery Guide

## 1. Automated PostgreSQL Backup

Create daily compressed backups using `pg_dump`:
```bash
pg_dump -h localhost -U postgres -d attendance_db -F c -b -v -f "/backups/attendance_backup_$(date +%Y%m%d_%H%M%S).dump"
```

## 2. Restoration Procedure

1. Drop existing database connections or create a clean target database:
   ```sql
   DROP DATABASE IF EXISTS attendance_db;
   CREATE DATABASE attendance_db;
   ```
2. Restore using `pg_restore`:
   ```bash
   pg_restore -h localhost -U postgres -d attendance_db -v "/backups/attendance_backup_YYYYMMDD_HHMMSS.dump"
   ```
3. Restart Spring Boot application. Flyway will validate schema checksums and verify integrity.
