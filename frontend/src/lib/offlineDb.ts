import { openDB, DBSchema, IDBPDatabase } from 'idb';
import { apiRequest } from './api';

interface AttendanceDB extends DBSchema {
  rosters: {
    key: string; // sectionId
    value: {
      sectionId: string;
      sectionName: string;
      gradeName: string;
      students: Array<{ id: string; studentNumber: string; fullName: string }>;
      cachedAt: string;
    };
  };
  offlineRecords: {
    key: string; // idempotencyKey
    value: {
      idempotencyKey: string;
      sessionId: string;
      studentId: string;
      studentNumber: string;
      studentName: string;
      status: 'PRESENT' | 'LATE' | 'ABSENT';
      deviceRecordedAt: string;
      syncStatus: 'RECORDED_OFFLINE' | 'SYNC_PENDING' | 'SYNCHRONIZED' | 'CONFLICT';
    };
  };
}

let dbPromise: Promise<IDBPDatabase<AttendanceDB>> | null = null;

function getDb() {
  if (!dbPromise) {
    dbPromise = openDB<AttendanceDB>('school-attendance-db', 1, {
      upgrade(db) {
        if (!db.objectStoreNames.contains('rosters')) {
          db.createObjectStore('rosters', { keyPath: 'sectionId' });
        }
        if (!db.objectStoreNames.contains('offlineRecords')) {
          db.createObjectStore('offlineRecords', { keyPath: 'idempotencyKey' });
        }
      },
    });
  }
  return dbPromise;
}

export async function saveRosterLocally(sectionId: string, sectionName: string, gradeName: string, students: any[]) {
  const db = await getDb();
  await db.put('rosters', {
    sectionId,
    sectionName,
    gradeName,
    students,
    cachedAt: new Date().toISOString(),
  });
}

export async function getLocalRoster(sectionId: string) {
  const db = await getDb();
  return db.get('rosters', sectionId);
}

export async function recordAttendanceOffline(
  sessionId: string,
  studentId: string,
  studentNumber: string,
  studentName: string,
  status: 'PRESENT' | 'LATE' | 'ABSENT'
) {
  const db = await getDb();
  const idempotencyKey = crypto.randomUUID();
  const record = {
    idempotencyKey,
    sessionId,
    studentId,
    studentNumber,
    studentName,
    status,
    deviceRecordedAt: new Date().toISOString(),
    syncStatus: 'RECORDED_OFFLINE' as const,
  };
  await db.put('offlineRecords', record);
  return record;
}

export async function getPendingOfflineRecords() {
  const db = await getDb();
  const records = await db.getAll('offlineRecords');
  return records.filter((r) => r.syncStatus === 'RECORDED_OFFLINE' || r.syncStatus === 'SYNC_PENDING');
}

export async function syncOfflineRecords() {
  const pending = await getPendingOfflineRecords();
  if (pending.length === 0) return { total: 0, synced: 0 };

  const db = await getDb();
  const batchId = crypto.randomUUID();

  const payload = {
    sourceDeviceId: localStorage.getItem('deviceId') || 'BROWSER-' + navigator.userAgent.substring(0, 30),
    batchIdentifier: 'BATCH-' + batchId,
    items: pending.map((r) => ({
      sessionId: r.sessionId,
      studentId: r.studentId,
      status: r.status,
      deviceRecordedAt: r.deviceRecordedAt,
      idempotencyKey: r.idempotencyKey,
    })),
  };

  try {
    const result: any = await apiRequest('/offline-sync/batches', {
      method: 'POST',
      body: JSON.stringify(payload),
    });

    for (const r of pending) {
      r.syncStatus = 'SYNCHRONIZED';
      await db.put('offlineRecords', r);
    }

    return { total: pending.length, synced: result.synchronizedItems || pending.length };
  } catch (err) {
    console.error('Offline batch sync failed:', err);
    throw err;
  }
}
