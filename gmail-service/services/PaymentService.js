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

    // 3. Создать платежи из нескольких payout'ов (например, за месяц)
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

    // 4. Получить платеж по ID
    static async getPaymentById(paymentId) {
        try {
            const payment = await PaymentRepository.findById(paymentId);
            if (!payment) {
                return {
                    success: false,
                    message: "Payment not found"
                };
            }
            return {
                success: true,
                data: payment
            };
        } catch (error) {
            return {
                success: false,
                error: error.message,
                message: "Failed to fetch payment"
            };
        }
    }

    // 5. Получить все платежи по комнате
    static async getPaymentsByRoomId(roomId) {
        try {
            const payments = await PaymentRepository.findByRoomId(roomId);
            return {
                success: true,
                data: payments,
                count: payments.length,
                message: "Payments fetched successfully"
            };
        } catch (error) {
            return {
                success: false,
                error: error.message,
                message: "Failed to fetch payments"
            };
        }
    }

    // 6. Получить все платежи по группе (пакету)
    static async getPaymentsByGroupId(groupId) {
        try {
            const payments = await PaymentRepository.findByGroupId(groupId);
            return {
                success: true,
                data: payments,
                count: payments.length,
                message: "Payments fetched successfully"
            };
        } catch (error) {
            return {
                success: false,
                error: error.message,
                message: "Failed to fetch payments"
            };
        }
    }

    // 7. Получить статистику по группе платежей
    static async getGroupStats(groupId) {
        try {
            const payments = await PaymentRepository.findByGroupId(groupId);

            const totalAmount = payments.reduce((sum, p) => sum + p.amount, 0);
            const uniqueRooms = new Set(payments.map(p => p.roomId));

            // Статистика по комнатам
            const roomStats = {};
            for (const payment of payments) {
                if (!roomStats[payment.roomId]) {
                    roomStats[payment.roomId] = {
                        count: 0,
                        total: 0
                    };
                }
                roomStats[payment.roomId].count++;
                roomStats[payment.roomId].total += payment.amount;
            }

            return {
                success: true,
                data: {
                    groupId: groupId,
                    totalPayments: payments.length,
                    totalAmount: totalAmount,
                    uniqueRooms: uniqueRooms.size,
                    roomStats: roomStats
                },
                message: "Stats fetched successfully"
            };
        } catch (error) {
            return {
                success: false,
                error: error.message,
                message: "Failed to fetch stats"
            };
        }
    }

    // 8. Удалить все платежи по группе (например, при пересчете)
    static async deletePaymentsByGroupId(groupId) {
        try {
            const deleted = await PaymentRepository.deleteByGroupId(groupId);
            return {
                success: true,
                deletedCount: deleted,
                message: `Deleted ${deleted} payments for group ${groupId}`
            };
        } catch (error) {
            return {
                success: false,
                error: error.message,
                message: "Failed to delete payments"
            };
        }
    }
}

module.exports = PaymentService;