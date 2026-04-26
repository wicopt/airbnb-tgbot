const { PrismaClient, Prisma } = require("@prisma/client");
const prisma = new PrismaClient();

class PaymentsService {
  async savePayments(payments) {
    return Promise.all(
      payments.map(p =>
        prisma.payments.upsert({
          where: {
            payment_date_room_number: {
              payment_date: p.payment_date,
              room_number: p.room_number,
            },
          },
          update: {
            amount: new Prisma.Decimal(p.amount),
          },
          create: {
            payment_date: p.payment_date,
            room_number: p.room_number,
            amount: new Prisma.Decimal(p.amount),
          },
        })
      )
    );
  }
}

module.exports = { PaymentsService };
