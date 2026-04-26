require("dotenv").config();
const { google } = require("googleapis");

const {
  CLIENT_ID,
  CLIENT_SECRET,
  REFRESH_TOKEN,
  REDIRECT_URI = "http://localhost:3000/auth/callback",
} = process.env;

console.log("CLIENT_ID:", process.env.CLIENT_ID);

if (!CLIENT_ID || !CLIENT_SECRET) {
  console.error(
    "❌ Ошибка: Не указаны CLIENT_ID или CLIENT_SECRET в .env файле",
  );
  process.exit(1);
}

const oAuth2Client = new google.auth.OAuth2(
  CLIENT_ID,
  CLIENT_SECRET,
  REDIRECT_URI,
);

// Если есть refresh token - используем его
if (REFRESH_TOKEN) {
  oAuth2Client.setCredentials({
    refresh_token: REFRESH_TOKEN,
  });
  console.log("✅ Refresh token загружен из .env");
} else {
  console.log("⚠️  Refresh token не найден. Нужна авторизация.");
}
module.exports = oAuth2Client;
