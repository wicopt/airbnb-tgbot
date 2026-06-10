// repositories/paymentRepository.js
const { pool } = require("../config/dbConfig");
const Payment = require("../domain/Payment");

class PaymentRepository {
    static _toDomain(row) {
        if (!row) return null;
        return new Payment({
            payment_id: row.payment_id,
            group_id: row.group_id,
            room_number: row.room_number,  //
            amount: row.amount,
            payment_date: row.payment_date,
            category: row.category,
        });
    }

    static async getLastPaymentDate(groupId) {
        const result = await pool.query(
            `SELECT MAX(payment_date) as last_date FROM core.payment WHERE group_id = $1 AND category = 'Airbnb Payout'`,
            [groupId]
        );
        return result.rows[0]?.last_date ?? null; // null если таблица пустая
    }
    // Получить платеж по ID
    static async findById(paymentId) {
        const result = await pool.query(
            `SELECT payment_id, group_id, room_number, amount, payment_date, category
             FROM core.payment
             WHERE payment_id = $1`,
            [paymentId]
        );

        if (result.rows.length === 0) {
            return null;
        }

        return this._toDomain(result.rows[0]);
    }

    // Получить все платежи по комнате
    static async findByRoomNumber(roomNumber) {  // ← переименовано
        const result = await pool.query(
            `SELECT payment_id, group_id, room_number, amount, payment_date, category
             FROM core.payment
             WHERE room_number = $1
             ORDER BY payment_date DESC`,
            [roomNumber]
        );

        return result.rows.map(row => this._toDomain(row));
    }

    // Получить все платежи по группе
    static async findByGroupId(groupId) {
        const result = await pool.query(
            `SELECT payment_id, group_id, room_number, amount, payment_date, category
             FROM core.payment
             WHERE group_id = $1
             ORDER BY payment_date DESC`,
            [groupId]
        );

        return result.rows.map(row => this._toDomain(row));
    }

    // Получить платежи по комнате за период
    static async findByRoomNumberAndDateRange(roomNumber, startDate, endDate) {
        const result = await pool.query(
            `SELECT payment_id, group_id, room_number, amount, payment_date, category
             FROM core.payment
             WHERE room_number = $1 
               AND payment_date BETWEEN $2 AND $3
             ORDER BY payment_date DESC`,
            [roomNumber, startDate, endDate]
        );

        return result.rows.map(row => this._toDomain(row));
    }

    // Получить все платежи (с опциональной фильтрацией по категории)
    static async findAll(category = null) {
        let query = `
            SELECT payment_id, group_id, room_number, amount, payment_date, category
            FROM core.payment
        `;
        const params = [];

        if (category) {
            query += ` WHERE category = $1`;
            params.push(category);
        }

        query += ` ORDER BY payment_date DESC`;

        const result = await pool.query(query, params);
        return result.rows.map(row => this._toDomain(row));
    }

    // Создать новый платеж
    static async create(paymentData) {
        const { group_id, room_number, amount, payment_date, category } = paymentData;

        // Проверяем, существует ли уже такой платеж
        const existingPayment = await pool.query(
            `SELECT payment_id, group_id, room_number, amount, payment_date, category
            FROM core.payment
            WHERE group_id = $1 
                AND room_number = $2 
                AND amount = $3 
                AND payment_date = $4`,
            [group_id, room_number, amount, payment_date]
        );

        if (existingPayment.rows.length > 0) {
            console.log(`Платеж уже существует: ...`);
            return { ...this._toDomain(existingPayment.rows[0]), alreadyExists: true };
        }

        // Если не существует - создаем новый
        const result = await pool.query(
            `INSERT INTO core.payment (payment_id, group_id, room_number, amount, payment_date, category)
         VALUES (gen_random_uuid(), $1, $2, $3, $4, $5)
         RETURNING payment_id, group_id, room_number, amount, payment_date, category`,
            [group_id, room_number, amount, payment_date, category]
        );

        return { ...this._toDomain(result.rows[0]), alreadyExists: false };
    }
    // Обновить платеж
    static async update(paymentId, updateData) {
        const { amount, payment_date, category } = updateData;

        const updates = [];
        const values = [];
        let paramCount = 1;

        if (amount !== undefined) {
            updates.push(`amount = $${paramCount++}`);
            values.push(amount);
        }
        if (payment_date !== undefined) {
            updates.push(`payment_date = $${paramCount++}`);
            values.push(payment_date);
        }
        if (category !== undefined) {
            updates.push(`category = $${paramCount++}`);
            values.push(category);
        }

        if (updates.length === 0) {
            throw new Error('No fields to update');
        }

        values.push(paymentId);

        const result = await pool.query(
            `UPDATE core.payment 
             SET ${updates.join(', ')}
             WHERE payment_id = $${paramCount}
             RETURNING payment_id, group_id, room_number, amount, payment_date, category`,
            values
        );

        if (result.rows.length === 0) {
            return null;
        }

        return this._toDomain(result.rows[0]);
    }

    // Обновить сумму платежа
    static async updateAmount(paymentId, amount) {
        const result = await pool.query(
            `UPDATE core.payment 
             SET amount = $1
             WHERE payment_id = $2
             RETURNING payment_id, group_id, room_number, amount, payment_date, category`,
            [amount, paymentId]
        );

        if (result.rows.length === 0) {
            return null;
        }

        return this._toDomain(result.rows[0]);
    }

    // Удалить платеж
    static async delete(paymentId) {
        const result = await pool.query(
            `DELETE FROM core.payment
             WHERE payment_id = $1
             RETURNING payment_id`,
            [paymentId]
        );

        return result.rows.length > 0;
    }

    // Удалить все платежи по комнате
    static async deleteByRoomNumber(roomNumber) {
        const result = await pool.query(
            `DELETE FROM core.payment
             WHERE room_number = $1
             RETURNING payment_id`,
            [roomNumber]
        );

        return result.rows.length;
    }

    // Получить сумму всех платежей по комнате
    static async getTotalAmountByRoomNumber(roomNumber) {
        const result = await pool.query(
            `SELECT COALESCE(SUM(amount), 0) as total_amount
             FROM core.payment
             WHERE room_number = $1`,
            [roomNumber]
        );

        return parseFloat(result.rows[0].total_amount);
    }

    // Получить сумму платежей по категории для комнаты
    static async getTotalByCategory(roomNumber, category) {
        const result = await pool.query(
            `SELECT COALESCE(SUM(amount), 0) as total_amount
             FROM core.payment
             WHERE room_number = $1 AND category = $2`,
            [roomNumber, category]
        );

        return parseFloat(result.rows[0].total_amount);
    }

    // Получить уникальные категории платежей
    static async getUniqueCategories() {
        const result = await pool.query(
            `SELECT DISTINCT category
             FROM core.payment
             ORDER BY category`
        );

        return result.rows.map(row => row.category);
    }
}

module.exports = PaymentRepository;