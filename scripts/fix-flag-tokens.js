/**
 * Script para actualizar las URLs de flag en Firebase Realtime Database
 * con el download token correcto de Firebase Storage.
 *
 * Nodos 111-163 tienen URLs sin token → 403 Forbidden.
 * Este script obtiene el token real de cada archivo en Storage
 * y actualiza el campo flag en la DB.
 */

const admin = require("firebase-admin");
const { v4: uuidv4 } = require("uuid");
const path = require("path");

const serviceAccount = require(
  path.resolve(__dirname, "../quiz-world-game-firebase-adminsdk-9w9r6-58bf9a076e.json")
);

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount),
  databaseURL: "https://quiz-world-game.firebaseio.com",
  storageBucket: "quiz-world-game.appspot.com",
});

const db = admin.database();
const bucket = admin.storage().bucket();

const START_NODE = 111;
const END_NODE = 163;

async function getStoragePathFromUrl(flagUrl) {
  // URL format: https://firebasestorage.googleapis.com/v0/b/BUCKET/o/ENCODED_PATH?alt=media
  const match = flagUrl.match(/\/o\/(.+?)\?/);
  if (!match) return null;
  return decodeURIComponent(match[1]);
}

async function getOrCreateDownloadToken(storagePath) {
  const file = bucket.file(storagePath);
  const [metadata] = await file.getMetadata();

  // Check if token already exists
  const existingToken =
    metadata.metadata && metadata.metadata.firebaseStorageDownloadTokens;
  if (existingToken) return existingToken;

  // No token exists — generate one and set it in metadata
  const newToken = uuidv4();
  await file.setMetadata({
    metadata: { firebaseStorageDownloadTokens: newToken },
  });
  return newToken;
}

function buildDownloadUrl(storagePath, token) {
  const encoded = encodeURIComponent(storagePath).replace(/%2F/g, "%2F");
  return `https://firebasestorage.googleapis.com/v0/b/quiz-world-game.appspot.com/o/${encoded}?alt=media&token=${token}`;
}

async function main() {
  let updated = 0;
  let failed = 0;

  for (let id = START_NODE; id <= END_NODE; id++) {
    try {
      const snapshot = await db.ref(`newQuiz/pride/${id}/flag`).once("value");
      const currentUrl = snapshot.val();

      if (!currentUrl) {
        console.log(`Node ${id}: NO flag field — SKIPPED`);
        failed++;
        continue;
      }

      // Already has token?
      if (currentUrl.includes("token=")) {
        console.log(`Node ${id}: Already has token — SKIPPED`);
        continue;
      }

      const storagePath = await getStoragePathFromUrl(currentUrl);
      if (!storagePath) {
        console.log(`Node ${id}: Could not parse storage path — SKIPPED`);
        failed++;
        continue;
      }

      const token = await getOrCreateDownloadToken(storagePath);

      const newUrl = buildDownloadUrl(storagePath, token);
      await db.ref(`newQuiz/pride/${id}/flag`).set(newUrl);
      console.log(`Node ${id}: UPDATED ✓`);
      updated++;
    } catch (err) {
      console.error(`Node ${id}: ERROR — ${err.message}`);
      failed++;
    }
  }

  console.log(`\nDone. Updated: ${updated}, Failed: ${failed}`);
  process.exit(0);
}

main();
