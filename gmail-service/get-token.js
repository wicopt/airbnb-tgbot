// get-token.js
require('dotenv').config();
const { google } = require('googleapis');
const http = require('http');
const url = require('url');
const { pool } = require('./config/dbConfig');

// Ваши данные из Google Cloud Console
const CLIENT_ID = process.env.CLIENT_ID;
const CLIENT_SECRET = process.env.CLIENT_SECRET;
const REDIRECT_URI = 'http://localhost:3000/auth/callback';
const GROUP_ID = process.env.GROUP_ID || 'my-group'; // Добавьте GROUP_ID в .env или укажите здесь

const oAuth2Client = new google.auth.OAuth2(
  CLIENT_ID,
  CLIENT_SECRET,
  REDIRECT_URI
);

const SCOPES = ['https://www.googleapis.com/auth/gmail.readonly'];

async function saveTokensToDatabase(groupId, refreshToken, clientId, clientSecret) {
  try {
    const query = `
      INSERT INTO auth.groups (group_id, client_id, client_secret, refresh_token)
      VALUES ($1, $2, $3, $4)
      ON CONFLICT (group_id) 
      DO UPDATE SET 
        client_id = EXCLUDED.client_id,
        client_secret = EXCLUDED.client_secret,
        refresh_token = EXCLUDED.refresh_token
      RETURNING *
    `;
    
    const values = [groupId, clientId, clientSecret, refreshToken];
    const result = await pool.query(query, values);
    
    console.log('Токен успешно сохранен в базе данных для группы:', groupId);
    return result.rows[0];
  } catch (error) {
    console.error('Ошибка при сохранении токена в БД:', error.message);
    throw error;
  }
}

async function getAccessToken() {
  const authUrl = oAuth2Client.generateAuthUrl({
    access_type: 'offline',
    scope: SCOPES,
    prompt: 'consent'
  });

  console.log('===========================================');
  console.log('1. Откройте браузер и перейдите по ссылке:');
  console.log(authUrl);
  console.log('===========================================');
  
  // Пытаемся открыть браузер, если не получится - показываем ссылку вручную
  try {
    const { default: open } = await import('open');
    await open(authUrl);
    console.log('Браузер открыт автоматически');
  } catch (error) {
    console.log('Не удалось открыть браузер автоматически');
    console.log('Скопируйте ссылку выше и откройте в браузере');
  }
  
  // Создаем временный сервер для получения кода
  const server = http.createServer(async (req, res) => {
    try {
      const queryParams = url.parse(req.url, true).query;
      
      if (queryParams.code) {
        // Получили код, обмениваем его на токены
        const { tokens } = await oAuth2Client.getToken(queryParams.code);
        
        console.log('===========================================');
        console.log('Токены успешно получены');
        console.log('===========================================');
        
        if (tokens.refresh_token) {
          // Сохраняем токен в базу данных
          await saveTokensToDatabase(
            GROUP_ID,
            tokens.refresh_token,
            CLIENT_ID,
            CLIENT_SECRET
          );
          
          console.log('===========================================');
          console.log('REFRESH_TOKEN сохранен в БД для группы:', GROUP_ID);
          console.log('===========================================');
        } else {
          console.log('ВНИМАНИЕ: Refresh token не получен. Убедитесь, что параметр prompt=consent установлен');
        }
        
        console.log('ACCESS_TOKEN (временный):', tokens.access_token);
        console.log('Истекает через:', new Date(tokens.expiry_date).toLocaleString());
        console.log('===========================================');
        
        // Отправляем ответ в браузер
        res.writeHead(200, { 'Content-Type': 'text/html; charset=utf-8' });
        res.end(`
          <html>
            <body style="font-family: Arial; text-align: center; margin-top: 50px;">
              <h2>Authorization Successful</h2>
              <p>Token has been saved to database. You can close this window.</p>
              <hr>
              <p style="font-size: 12px; color: #666;">Group: ${GROUP_ID}</p>
              <script>setTimeout(() => window.close(), 3000);</script>
            </body>
          </html>
        `);
        
        // Закрываем сервер
        server.close(() => {
          console.log('Authorization server stopped');
          process.exit(0);
        });
      } else {
        res.writeHead(400, { 'Content-Type': 'text/html; charset=utf-8' });
        res.end('<h2>Error: Code not received</h2>');
        server.close();
      }
    } catch (error) {
      console.error('Error getting token:', error.message);
      res.writeHead(500);
      res.end('<h2>Error: Authorization failed</h2>');
      server.close();
      process.exit(1);
    }
  });
  
  // Запускаем сервер на порту 3000
  server.listen(3000, () => {
    console.log('Authorization server running on http://localhost:3000');
    console.log('Waiting for Google callback...');
  });
}

// Проверяем подключение к БД перед запуском
async function init() {
  try {
    await pool.query('SELECT NOW()');
    console.log('Database connection successful');
    await getAccessToken();
  } catch (error) {
    console.error('Database connection failed:', error.message);
    process.exit(1);
  }
}

init();