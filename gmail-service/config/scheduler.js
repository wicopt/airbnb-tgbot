const cron = require("node-cron");
const MessageService = require("../services/MessageService");

const messageService = new MessageService();

// каждый день в 08:00
cron.schedule("0 8 * * *", async () => {
    console.log("Запуск планового сбора выплат...");
    const groupIds = process.env.GROUP_IDS?.split(",") || [];
    for (const groupId of groupIds) {
        await messageService.processMessages(groupId);
    }
});

console.log("Scheduler запущен");