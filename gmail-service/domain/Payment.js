// domain/Payment.js
class Payment {
    #paymentId;
    #groupId;
    #roomNumber;
    #amount;
    #paymentDate;
    #category;

    constructor(data) {
        // Валидация
        if (!data.room_number) throw new Error('Room number is required');
        if (!data.amount || data.amount <= 0) throw new Error('Amount is required');
        if (!data.payment_date) throw new Error('Payment date is required');
        if (!data.category) throw new Error('Category is required');

        this.#paymentId = data.payment_id ?? null;
        this.#groupId = data.group_id ?? null;
        this.#roomNumber = data.room_number;
        this.#amount = parseFloat(data.amount);
        this.#paymentDate = new Date(data.payment_date);
        this.#category = data.category;
    }

    get paymentId() { return this.#paymentId; }
    get groupId() { return this.#groupId; }
    get roomNumber() { return this.#roomNumber; }
    get amount() { return this.#amount; }
    get paymentDate() { return this.#paymentDate; }
    get category() { return this.#category; }

    toDatabase(groupId) {
        return {
            payment_id: this.#paymentId,
            group_id: groupId,
            room_number: this.#roomNumber,
            amount: this.#amount,
            payment_date: this.#paymentDate.toISOString().split('T')[0],
            category: this.#category
        };
    }

    toJSON() {
        return {
            payment_id: this.#paymentId,
            group_id: this.#groupId,
            room_number: this.#roomNumber,
            amount: this.#amount,
            payment_date: this.#paymentDate.toISOString().split('T')[0],
            category: this.#category
        };
    }
}

module.exports = Payment;