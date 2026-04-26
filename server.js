require('dotenv').config(); // Добавьте в самый верх
const express = require('express');
const app = express();
const port = process.env.PORT || 3000;
const { getPayoutMessages } = require("./gmail/message-parser");
const { messages } = require("./gmail/message-to-db");


app.listen(port, async () => {
  const payouts = await getPayoutMessages(100);
  messages(payouts)
  //console.dir(payouts, { depth: null, colors: true });
  console.log(`Сервер запущен`);
});
