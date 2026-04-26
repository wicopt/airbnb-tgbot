const { PaymentsService } = require("./message-service");

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

function messageToDb(payout) {
  const totalUSD = payout.totalUsd;
  const totalTHB = calcTotalTHB(payout.bookings);

  let prevUSD = 0;
console.dir(payout, { depth: null, colors: true });
  const payments = payout.bookings.map((booking, index) => {
    const isLast = index === payout.bookings.length - 1;

    const amountUSD = isLast
      ? totalUSD - prevUSD
      : Math.round(totalUSD * (booking.amountTHB / totalTHB) * 100) / 100;

    prevUSD += amountUSD;

    return {
      payment_date: payout.messageDate, // Date без времени
      room_number: APARTMENT_MAP[booking.apartment],
      amount: amountUSD,
    };
  });
  //console.dir(payments, { depth: null, colors: true });
  //const paymentsService = new PaymentsService();
  //return paymentsService.savePayments(payments);
  return payments;
}
function calcTotalTHB(bookings) {
  return bookings.reduce((sum, b) => sum + b.amountTHB, 0);
}

module.exports = { messages };
