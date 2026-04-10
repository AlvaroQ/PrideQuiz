/**
 * Crea el índice compuesto en Firestore necesario para filtrar rankings por gameMode.
 * Índice: ranking-pride → gameMode ASC + score DESC
 *
 * Requiere: Service Account con permisos de Cloud Datastore Index Admin
 */

const { GoogleAuth } = require("google-auth-library");
const path = require("path");

const SERVICE_ACCOUNT_PATH = path.resolve(
  __dirname,
  "../quiz-world-game-firebase-adminsdk-9w9r6-58bf9a076e.json"
);
const PROJECT_ID = "quiz-world-game";

async function createIndex() {
  const auth = new GoogleAuth({
    keyFile: SERVICE_ACCOUNT_PATH,
    scopes: ["https://www.googleapis.com/auth/cloud-platform"],
  });

  const client = await auth.getClient();

  // Firestore Admin REST API - Create Index
  const url = `https://firestore.googleapis.com/v1/projects/${PROJECT_ID}/databases/(default)/collectionGroups/ranking-pride/indexes`;

  const indexBody = {
    queryScope: "COLLECTION",
    fields: [
      { fieldPath: "gameMode", order: "ASCENDING" },
      { fieldPath: "score", order: "DESCENDING" },
    ],
  };

  try {
    const response = await client.request({
      url,
      method: "POST",
      data: indexBody,
    });

    console.log("Index creation started!");
    console.log("Operation:", response.data.name);
    console.log(
      "\nThe index is building. This usually takes 1-5 minutes."
    );
    console.log(
      "You can check progress at: https://console.firebase.google.com/project/quiz-world-game/firestore/indexes"
    );
  } catch (error) {
    if (error.response?.status === 409) {
      console.log("Index already exists! Nothing to do.");
    } else {
      console.error(
        "Error:",
        error.response?.data?.error?.message || error.message
      );
    }
  }
}

createIndex();
