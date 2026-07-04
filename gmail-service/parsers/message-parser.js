// parsers/message-parser.js
const { google } = require("googleapis");
const { getGoogleAuthClient } = require("../config/gmail-auth");

async function getPayoutMessages(groupId, limit = 3) {
  console.log("getPayoutMessages: начало", { groupId, limit });

  try {
    console.log("getPayoutMessages: получаем Google auth клиент");
    const auth = await getGoogleAuthClient(groupId);
    
    console.log("getPayoutMessages: создаем Gmail клиент");
    const gmail = google.gmail({ version: "v1", auth: auth });

    console.log("getPayoutMessages: запрашиваем список сообщений");
    const listRes = await gmail.users.messages.list({
      userId: "me",
      maxResults: limit,
      q: 'subject:"We sent a payout of"',
    });

    const messages = listRes.data.messages || [];
    console.log("getPayoutMessages: найдено сообщений", messages.length);

    const payouts = [];

    for (let i = 0; i < messages.length; i++) {
      const message = messages[i];
      console.log(`getPayoutMessages: обрабатываем сообщение ${i + 1}/${messages.length}, id: ${message.id}`);

      const msg = await gmail.users.messages.get({
        userId: "me",
        id: message.id,
        format: "full",
      });
      
      let messageDate = new Date(Number(msg.data.internalDate));
      messageDate = messageDate.toISOString().slice(0, 10);
      console.log("getPayoutMessages: дата сообщения", messageDate);
      
      const payload = msg.data.payload;
      const headers = payload.headers || [];

      // total USD
      const subject = headers.find(h => h.name === "Subject")?.value || "";
      const usdMatch = subject.match(/\$([\d.]+)\sUSD/);
      const totalUsd = usdMatch ? Number(usdMatch[1]) : null;
      console.log("getPayoutMessages: total USD", totalUsd);

      // text
      console.log("getPayoutMessages: извлекаем текст письма");
      const textPart = findTextPlain(payload);
      if (!textPart?.body?.data) {
        console.log("getPayoutMessages: текст не найден, пропускаем");
        continue;
      }

      const text = Buffer.from(textPart.body.data, "base64").toString("utf-8");
      console.log("getPayoutMessages: текст получен, длина", text.length);

      // type
      const type = detectMessageType(text);
      console.log("getPayoutMessages: тип сообщения", type);

      // bookings
      let bookings = [];

      switch (type) {
        case "STANDARD":
          console.log("getPayoutMessages: парсим STANDARD формат");
          bookings = parseStandardBookings(text);
          break;
        case "RESOLUTION":
          console.log("getPayoutMessages: парсим RESOLUTION формат");
          bookings = parseResolutionBookings(text);
          break;
        default:
          console.warn("Unknown payout format:", message.id);
      }

      console.log("getPayoutMessages: найдено бронирований", bookings.length);
      payouts.push({
        totalUsd,
        bookings,
        messageDate
      });
    }

    console.log("getPayoutMessages: завершено, всего выплат", payouts.length);
    return payouts;
  } catch (error) {
    console.log("getPayoutMessages: ошибка", error.message);
    console.error("Ошибка при получении выплат:", error.message);
    throw error;
  }
}

/* ================= helpers ================= */

function detectMessageType(text) {
  if (text.includes("Resolution Payout")) return "RESOLUTION";
  if (text.includes("Home •")) return "STANDARD";
  return "UNKNOWN";
}

function parseStandardBookings(text) {
  const regex =
    /([^\n]+?)\s+฿([\d,.]+)\sTHB\s*\nHome\s•\s([^\n]+)\s*\n([^\n]+)\s*\(\d+\)\s*\n([A-Z0-9]{8,})/g;

  const bookings = [];
  let match;

  while ((match = regex.exec(text)) !== null) {
    bookings.push({
      guest: match[1].trim(),
      amountTHB: Number(match[2].replace(/,/g, "")),
      apartment: match[4].trim(),
      reservationCode: match[5],
    });
  }

  return bookings;
}

function parseResolutionBookings(text) {
  const regex =
    /([^\n]+?)\s+฿([\d,.]+)\sTHB[\s\S]*?Resolution Payout[\s\S]*?([^\n]+?)\s*\(\d+\)[\s\S]*?([A-Z0-9]{8,})?/g;

  const bookings = [];
  let match;

  while ((match = regex.exec(text)) !== null) {
    bookings.push({
      guest: match[1].trim(),
      amountTHB: Number(match[2].replace(/,/g, "")),
      apartment: match[3].trim(),
      reservationCode: match[4] || null,
    });
  }

  return bookings;
}

function findTextPlain(payload) {
  if (payload.mimeType === "text/plain") return payload;

  if (payload.parts) {
    for (const part of payload.parts) {
      const found = findTextPlain(part);
      if (found) return found;
    }
  }

  return null;
}

module.exports = { getPayoutMessages };
