const amqplib = require("amqplib");

let channel = null;

async function getChannel() {
    if (channel) return channel;
    const connection = await amqplib.connect(process.env.RABBITMQ_URL);
    channel = await connection.createChannel();
    return channel;
}

async function publishEvent(queue, payload) {
    const ch = await getChannel();
    await ch.assertQueue(queue, { durable: true });
    ch.sendToQueue(
        queue,
        Buffer.from(JSON.stringify(payload)),
        { persistent: true }
    );
}

module.exports = { publishEvent };