// services/PaymentService.js
const PaymentRepository = require("../repositories/PaymentRepository");
const Payment = require("../domain/Payment");
const { PaymentParser } = require("../parsers/PaymentParser"); // ваш парсер

class PaymentService {
    // 1. Создать один платеж из готовых данных
    static async createPayment(paymentData, groupId) {
        try {
            // Создаем доменный объект
            const payment = new Payment(paymentData);

            // Сохраняем в БД, передавая groupId

            console.dir("до бд" + payment, { depth: null, colors: true });
            const dbData = payment.toDatabase(groupId);
            const saved = await PaymentRepository.create(dbData);
            console.dir(saved, { depth: null, colors: true });

            return saved;
        } catch (error) {
            return {
                success: false,
                error: error.message,
                message: "Failed to create payment"
            };
        }
    }

    // 2. Создать платежи из распарсенного payout (одно письмо)
    static async createPaymentsFromPayout(payout, groupId) {
        try {
            // Парсим payout в массив Payment объектов
            const payments = PaymentParser.parsePayment(payout);

            if (payments.length === 0) {
                return {
                    success: false,
                    message: "No valid payments in payout",
                    savedCount: 0
                };
            }

            // Сохраняем каждый платеж
            const savedPayments = [];
            const errors = [];

            for (const payment of payments) {
                try {
                    const dbData = payment.toDatabase(groupId);
                    const saved = await PaymentRepository.create(dbData);
                    if (!saved.alreadyExists) {
                        savedPayments.push(saved);
                    }
                } catch (error) {
                    errors.push({
                        roomId: payment.roomId,
                        amount: payment.amount,
                        error: error.message
                    });
                }
            }

            return {
                success: savedPayments.length > 0,
                savedCount: savedPayments.length,
                totalCount: payments.length,
                savedPayments: savedPayments,
                errors: errors,
                message: `Saved ${savedPayments.length} of ${payments.length} payments`
            };
        } catch (error) {
            return {
                success: false,
                error: error.message,
                message: "Failed to process payout"
            };
        }
    }

    static async createPaymentsFromPayouts(payouts, groupId, dateFilter = null) {
        const allPayments = PaymentParser.parseAllPayments(payouts, dateFilter);

        if (allPayments.length === 0) {
            return {
                success: false,
                message: "No payments to save",
                savedCount: 0
            };
        }

        const savedPayments = [];
        const errors = [];

        for (const payment of allPayments) {
            try {
                const dbData = payment.toDatabase(groupId);
                const saved = await PaymentRepository.create(dbData);
                if (!saved.alreadyExists) {
                    savedPayments.push(saved);
                }
            } catch (error) {
                errors.push({
                    roomId: payment.roomId,
                    amount: payment.amount,
                    date: payment.paymentDate,
                    error: error.message
                });
            }
        }

        return {
            success: savedPayments.length > 0,
            savedCount: savedPayments.length,
            totalCount: allPayments.length,
            savedPayments: savedPayments,
            errors: errors,
            message: `Saved ${savedPayments.length} of ${allPayments.length} payments`
        };
    }
}

module.exports = PaymentService;