// services/MessageService.js
const PaymentService = require("./PaymentService");
const { getPayoutMessages } = require("../parsers/message-parser.js");
const PaymentRepository = require("../repositories/PaymentRepository");
const { publishEvent } = require("../config/rabbitmq");

class MessageService {

    async processMessages(groupId, dateFilter = null) {
        console.log("Начинаем processMessages для группы:", groupId);

        try {
            console.log("Получаем выплаты из Gmail...");
            const payouts = await getPayoutMessages(groupId);
            console.log("Получено выплат:", payouts.length);

            console.log("Получаем последнюю дату платежа из БД...");
            const lastDate = await PaymentRepository.getLastPaymentDate(groupId);
            console.log("Последняя дата платежа:", lastDate);

            console.log("Фильтруем новые выплаты...");
            const filteredPayouts = lastDate
                ? payouts.filter(p => new Date(p.messageDate) > new Date(lastDate))
                : payouts;
            console.log("Новых выплат после фильтрации:", filteredPayouts.length);

            let totalSaved = 0;
            let totalPayments = 0;
            let allErrors = [];
            let allSavedPayments = [];   

            for (const payout of filteredPayouts) {
                console.log("Обрабатываем выплату от:", payout.messageDate);
                const result = await PaymentService.createPaymentsFromPayout(payout, groupId);
                console.log("Результат обработки выплаты:", result.success ? "успешно" : "ошибка");

                if (result.success) {
                    totalSaved += result.savedCount;
                    totalPayments += result.totalCount;
                    console.log("Сохранено платежей в этой выплате:", result.savedCount);
                }

                if (result.savedPayments?.length > 0) {
                    allSavedPayments.push(...result.savedPayments);  
                }

                if (result.errors?.length > 0) {
                    allErrors.push(...result.errors);
                    console.log("Ошибок в этой выплате:", result.errors.length);
                }
            }

            console.log("Всего сохранено платежей:", totalSaved);
            console.log("Всего обработано платежей:", totalPayments);

            if (totalSaved > 0 ) {
                console.log("Публикуем событие в RabbitMQ...");
                await publishEvent("payment.processed", {
                    groupId,
                    savedCount: totalSaved,
                    totalCount: totalPayments,
                    savedPayments: allSavedPayments,  
                    errors: allErrors,
                    processedAt: new Date().toISOString()
                });
            }

            return { success: true, savedCount: totalSaved };

        } catch (error) {
            console.log("Ошибка в processMessages:", error.message);
            console.error("Ошибка в MessageService:", error.message);
            return { success: false, error: error.message };
        }
    }
}

module.exports = MessageService;