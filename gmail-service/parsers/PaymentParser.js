const Payment = require("../domain/Payment")
const APARTMENT_MAP = {
    "Airport Flat, Mai Khao": "2622",
    "Cozy flat near the Airport": "2605",
    "1BR near Phuket Airport": "2606",
    "2BR flat near Phuket Airport": "2807",
};
function messages(payouts) {
    let total = 0;

    const janStart = new Date("2026-01-01");
    const febStart = new Date("2026-02-01");

    for (const payout of payouts) {
        const date = new Date(payout.messageDate);

        if (date >= janStart && date < febStart) {
            const apart = messageToDb(payout);
            console.dir(apart, { depth: null, colors: true });
            // console.log(
            //   date.toISOString().slice(0, 10), // YYYY-MM-DD
            //   payout.totalUsd,
            // );
            total += payout.totalUsd;
        }
    }

    console.log("January 2026 total:", total);
}
class PaymentParser {
    static parsePayment(payout) {
        const totalUSD = payout.totalUsd;
        const totalTHB = this.calcTotalTHB(payout.bookings);
        let prevUSD = 0;
        const errors = [];

        console.log("===== Making payments");
        console.dir(payout, { depth: null, colors: true });

        const payments = payout.bookings.map((booking, index) => {
            try {
                const isLast = index === payout.bookings.length - 1;

                const amountUSD = isLast
                    ? totalUSD - prevUSD
                    : Math.round(totalUSD * (booking.amountTHB / totalTHB) * 100) / 100;

                prevUSD += amountUSD;
                const roomNumber = APARTMENT_MAP[booking.apartment];

                console.log(`Тип roomNumber: ${typeof roomNumber}, значение: ${roomNumber}`);

                if (!roomNumber) {
                    throw new Error(`Квартира "${booking.apartment}" не найдена в APARTMENT_MAP`);
                }

                const payment = new Payment({
                    room_number: roomNumber,
                    amount: amountUSD,
                    payment_date: payout.messageDate,
                    category: "Airbnb Payout"
                });

                console.log(`Платеж создан для комнаты ${roomNumber}`);
                return payment;

            } catch (error) {
                errors.push({
                    booking: booking,
                    index: index,
                    error: error.message,
                    stack: error.stack
                });

                console.error(`Ошибка при создании платежа ${index + 1}:`, error.message);
                return null;
            }
        });

        if (errors.length > 0) {
            console.error("\n===== ОШИБКИ ПРИ СОЗДАНИИ ПЛАТЕЖЕЙ =====");
            errors.forEach((err, i) => {
                console.error(`${i + 1}. ${err.error}`);
                console.error(`   Квартира: "${err.booking?.apartment}"`);
            });
            console.error("===== КОНЕЦ СПИСКА ОШИБОК =====");
        }

        const validPayments = payments.filter(p => p !== null);

        console.log(`===== Made payments (${validPayments.length} из ${payments.length} успешных) =====`);
        console.dir(validPayments, { depth: null, colors: true });

        return validPayments;
    }
    static parsePaymentD(payout) {
        const totalUSD = payout.totalUsd;
        const totalTHB = this.calcTotalTHB(payout.bookings);
        let prevUSD = 0;
        const payments = payout.bookings.map((booking, index) => {
            const isLast = index === payout.bookings.length - 1;

            const amountUSD = isLast
                ? totalUSD - prevUSD
                : Math.round(totalUSD * (booking.amountTHB / totalTHB) * 100) / 100;

            prevUSD += amountUSD;
            const roomNumber = APARTMENT_MAP[booking.apartment];
            console.log(typeof roomNumber);
            const payment = new Payment({
                room_number: roomNumber,
                amount: amountUSD,
                payment_date: payout.messageDate
            });

            console.log("===== One payment")
            console.dir(payment, { depth: null, colors: true });
            return payment;
        });
        console.log("===== Made paymepnts")
        console.dir(payments, { depth: null, colors: true });
        return payments;
    }
    static calcTotalTHB(bookings) {
        return bookings.reduce((sum, b) => sum + b.amountTHB, 0);
    }
}


module.exports = { PaymentParser };
