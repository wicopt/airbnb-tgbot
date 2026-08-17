const { pool } = require("../config/dbConfig");
const Payment = require("../domain/Payment");

class PaymentRepository {
    static _toDomain(row) {
        if (!row) return null;
        return new Payment({
            payment_id: row.payment_id,
            group_id: row.group_id,
            room_number: row.room_number,
            amount: row.amount,
            payment_date: row.payment_date,
            category: row.category, // всё как было
        });
    }
    static get _baseSelect() {
        return `
            SELECT 
                p.payment_id,
                p.group_id,
                p.room_number,
                p.amount,
                p.payment_date,
                c.category_name AS category  -- выглядит как раньше
            FROM core.payment p
            JOIN core.category c ON c.category_id = p.category_id
        `;
    }

    static async getLastPaymentDate(groupId) {
        const result = await pool.query(
            `SELECT MAX(p.payment_date) as last_date 
             FROM core.payment p
             JOIN core.category c ON c.category_id = p.category_id
             WHERE p.group_id = $1 AND c.category_name = 'Airbnb Payout'`,
            [groupId]
        );
        return result.rows[0]?.last_date ?? null;
    }

    static async findById(paymentId) {
        const result = await pool.query(
            `${this._baseSelect} WHERE p.payment_id = $1`,
            [paymentId]
        );
        return this._toDomain(result.rows[0]);
    }

    static async findByRoomNumber(roomNumber) {
        const result = await pool.query(
            `${this._baseSelect} WHERE p.room_number = $1 ORDER BY p.payment_date DESC`,
            [roomNumber]
        );
        return result.rows.map(row => this._toDomain(row));
    }

    static async findByGroupId(groupId) {
        const result = await pool.query(
            `${this._baseSelect} WHERE p.group_id = $1 ORDER BY p.payment_date DESC`,
            [groupId]
        );
        return result.rows.map(row => this._toDomain(row));
    }

    static async findByRoomNumberAndDateRange(roomNumber, startDate, endDate) {
        const result = await pool.query(
            `${this._baseSelect}
             WHERE p.room_number = $1 AND p.payment_date BETWEEN $2 AND $3
             ORDER BY p.payment_date DESC`,
            [roomNumber, startDate, endDate]
        );
        return result.rows.map(row => this._toDomain(row));
    }

    static async findAll(category = null) {
        let query = this._baseSelect;
        const params = [];

        if (category) {
            query += ` WHERE c.category_name = $1`; // фильтр по имени как раньше
            params.push(category);
        }

        query += ` ORDER BY p.payment_date DESC`;

        const result = await pool.query(query, params);
        return result.rows.map(row => this._toDomain(row));
    }

    static async create(paymentData) {
        const { group_id, room_number, amount, payment_date, category } = paymentData;

        // Находим category_id по имени — единственное место где нужна конвертация
        const categoryResult = await pool.query(
            `SELECT category_id FROM core.category WHERE category_name = $1`,
            [category]
        );

        if (categoryResult.rows.length === 0) {
            throw new Error(`Category not found: ${category}`);
        }

        const category_id = categoryResult.rows[0].category_id;

        const existing = await pool.query(
            `SELECT payment_id FROM core.payment
             WHERE group_id = $1 AND room_number = $2 
               AND amount = $3 AND payment_date = $4 AND category_id = $5`,
            [group_id, room_number, amount, payment_date, category_id]
        );

        if (existing.rows.length > 0) {
            console.log(`Платеж уже существует`);
            const payment = await this.findById(existing.rows[0].payment_id);
            return { ...payment.toJSON(), alreadyExists: true };
        }

        const result = await pool.query(
            `INSERT INTO core.payment (payment_id, group_id, room_number, amount, payment_date, category_id)
             VALUES (gen_random_uuid(), $1, $2, $3, $4, $5)
             RETURNING payment_id`,
            [group_id, room_number, amount, payment_date, category_id]
        );

        const payment = await this.findById(result.rows[0].payment_id);
        return { ...payment.toJSON(), alreadyExists: false };
    }

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
            // Конвертируем имя в id только здесь
            const categoryResult = await pool.query(
                `SELECT category_id FROM core.category WHERE category_name = $1`,
                [category]
            );
            if (categoryResult.rows.length === 0) {
                throw new Error(`Category not found: ${category}`);
            }
            updates.push(`category_id = $${paramCount++}`);
            values.push(categoryResult.rows[0].category_id);
        }

        if (updates.length === 0) {
            throw new Error('No fields to update');
        }

        values.push(paymentId);

        await pool.query(
            `UPDATE core.payment SET ${updates.join(', ')} WHERE payment_id = $${paramCount}`,
            values
        );

        return this.findById(paymentId);
    }

    static async updateAmount(paymentId, amount) {
        await pool.query(
            `UPDATE core.payment SET amount = $1 WHERE payment_id = $2`,
            [amount, paymentId]
        );
        return this.findById(paymentId);
    }

    static async delete(paymentId) {
        const result = await pool.query(
            `DELETE FROM core.payment WHERE payment_id = $1 RETURNING payment_id`,
            [paymentId]
        );
        return result.rows.length > 0;
    }

    static async deleteByRoomNumber(roomNumber) {
        const result = await pool.query(
            `DELETE FROM core.payment WHERE room_number = $1 RETURNING payment_id`,
            [roomNumber]
        );
        return result.rows.length;
    }

    static async getTotalAmountByRoomNumber(roomNumber) {
        const result = await pool.query(
            `SELECT COALESCE(SUM(amount), 0) as total_amount
             FROM core.payment WHERE room_number = $1`,
            [roomNumber]
        );
        return parseFloat(result.rows[0].total_amount);
    }

    static async getTotalByCategory(roomNumber, category) {
        const result = await pool.query(
            `SELECT COALESCE(SUM(p.amount), 0) as total_amount
             FROM core.payment p
             JOIN core.category c ON c.category_id = p.category_id
             WHERE p.room_number = $1 AND c.category_name = $2`,
            [roomNumber, category]
        );
        return parseFloat(result.rows[0].total_amount);
    }

    static async getUniqueCategories() {
        const result = await pool.query(
            `SELECT DISTINCT c.category_name AS category
             FROM core.payment p
             JOIN core.category c ON c.category_id = p.category_id
             ORDER BY category`
        );
        return result.rows.map(row => row.category); // возвращает строки как раньше
    }
}

module.exports = PaymentRepository;