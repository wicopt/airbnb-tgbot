require('dotenv').config(); // Добавьте в самый верх
const express = require('express');
const app = express();
const port = process.env.PORT || 3000;
const MessageService = require("./services/MessageService");
require("./config/scheduler");



app.listen(port, async () => {
  const messageService = new MessageService();
  messageService.processMessages('my-group');
  console.log(`Сервер запущен`);
});
