// config/gmail-auth.js
require("dotenv").config();
const { google } = require("googleapis");
const { pool } = require("./dbConfig");

async function getGoogleAuthClient(groupId) {
    try {
        // Получаем данные группы из базы данных
        const result = await pool.query(
            'SELECT client_id, client_secret, refresh_token FROM auth.groups WHERE group_id = $1',
            [groupId]
        );

        if (result.rows.length === 0) {
            console.error(`❌ Группа "${groupId}" не найдена в БД`);
            throw new Error(`Group ${groupId} not found`);
        }

        const { client_id, client_secret, refresh_token } = result.rows[0];
        
        const REDIRECT_URI = process.env.REDIRECT_URI || "http://localhost:3000/auth/callback";

        if (!client_id || !client_secret) {
            console.error("❌ Ошибка: Не указаны CLIENT_ID или CLIENT_SECRET в БД");
            throw new Error("Missing client credentials");
        }

        const oAuth2Client = new google.auth.OAuth2(
            client_id,
            client_secret,
            REDIRECT_URI
        );

        // Если есть refresh token - используем его
        if (refresh_token) {
            oAuth2Client.setCredentials({
                refresh_token: refresh_token,
            });
            console.log(`Refresh token загружен для группы "${groupId}" из БД`);
        } else {
            console.log(`Refresh token не найден для группы "${groupId}". Нужна авторизация.`);
        }

        return oAuth2Client; // Возвращаем готовый клиент
    } catch (error) {
        console.error("Ошибка при получении данных аутентификации:", error);
        throw error;
    }
}

module.exports = { getGoogleAuthClient };