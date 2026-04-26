// get-token.js
require('dotenv').config();
const { google } = require('googleapis');
const readline = require('readline');

// Ваши данные из Google Cloud Console
const CLIENT_ID = process.env.CLIENT_ID;
const CLIENT_SECRET = process.env.CLIENT_SECRET;
const REDIRECT_URI = 'http://localhost:3000/auth/callback';

const oAuth2Client = new google.auth.OAuth2(
  CLIENT_ID,
  CLIENT_SECRET,
  REDIRECT_URI
);

const SCOPES = ['https://www.googleapis.com/auth/gmail.readonly'];

async function getAccessToken() {
  const authUrl = oAuth2Client.generateAuthUrl({
    access_type: 'offline',
    scope: SCOPES,
    prompt: 'consent' // Важно! Без этого не получим refresh_token
  });

  console.log('===========================================');
  console.log('1. Откройте эту ссылку в браузере:');
  console.log(authUrl);
  console.log('===========================================');
  
  const rl = readline.createInterface({
    input: process.stdin,
    output: process.stdout,
  });

  rl.question('2. Введите код из адресной строки после авторизации: ', (code) => {
    rl.close();
    
    oAuth2Client.getToken(code, (err, token) => {
      if (err) {
        console.error('Ошибка:', err.message);
        return;
      }
      console.log('===========================================');
      console.log('3. УСПЕХ! Ваши токены:');
      console.log('===========================================');
      console.log('REFRESH_TOKEN (сохраните его!):');
      console.log(token.refresh_token);
      console.log('===========================================');
      console.log('Полный ответ:');
      console.log(JSON.stringify(token, null, 2));
    });
  });
}

getAccessToken();