//message-parser
const { google } = require("googleapis");
const oAuth2Client = require("./gmail-auth");

async function getPayoutMessages(  limit = 400) {

  try {
    const gmail = google.gmail({ version: "v1", auth: oAuth2Client });

    const listRes = await gmail.users.messages.list({
      userId: "me",
      maxResults: limit,
      q: 'subject:"We sent a payout of"',
    });

    const messages = listRes.data.messages || [];
    const payouts = [];

    for (const message of messages) {
      const msg = await gmail.users.messages.get({
        userId: "me",
        id: message.id,
        format: "full",
      });
      let messageDate = new Date(Number(msg.data.internalDate));
      messageDate = messageDate.toISOString().slice(0, 10); // UTC-дата
      const payload = msg.data.payload;
      const headers = payload.headers || [];

      // ---------- total USD ----------
      const subject = headers.find(h => h.name === "Subject")?.value || "";
      const usdMatch = subject.match(/\$([\d.]+)\sUSD/);
      const totalUsd = usdMatch ? Number(usdMatch[1]) : null;

      // ---------- text ----------
      const textPart = findTextPlain(payload);
      if (!textPart?.body?.data) continue;

      const text = Buffer.from(textPart.body.data, "base64").toString("utf-8");

      // ---------- type ----------
      const type = detectMessageType(text);

      // ---------- bookings ----------
      let bookings = [];

      switch (type) {
        case "STANDARD":
          bookings = parseStandardBookings(text);
          break;

        case "RESOLUTION":
          bookings = parseResolutionBookings(text);
          break;

        default:
          console.warn("Unknown payout format:", message.id);
      }
      
      payouts.push({
        totalUsd,
        bookings,
        messageDate
      });
    }

    return payouts;
  } catch (error) {
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
