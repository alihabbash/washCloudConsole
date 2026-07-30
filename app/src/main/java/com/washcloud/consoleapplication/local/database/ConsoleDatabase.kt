package com.washcloud.consoleapplication.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.washcloud.consoleapplication.local.database.dao.BoxDao
import com.washcloud.consoleapplication.local.database.dao.TransactionDao
import com.washcloud.consoleapplication.local.database.dto.BoxDto
import com.washcloud.consoleapplication.local.database.dto.TransactionDto
import com.washcloud.consoleapplication.local.database.dto.CustomerUserDto
import com.washcloud.consoleapplication.local.database.dao.CustomerUserDao
import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase



@Database(

    entities = [TransactionDto::class, BoxDto::class, CustomerUserDto::class],
    version = DatabaseConstants.DATABASE_VERSION,

)
@TypeConverters(Converters::class)
abstract class ConsoleDatabase : RoomDatabase(){
    abstract fun getTransactionDao(): TransactionDao

    abstract fun getBoxDao(): BoxDao
    
    abstract fun getCustomerUserDao(): CustomerUserDao

    companion object {
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add customer_id to transactions and boxes
                db.execSQL("ALTER TABLE transactions ADD COLUMN customer_id TEXT DEFAULT NULL")
                db.execSQL("ALTER TABLE boxes ADD COLUMN customer_id TEXT DEFAULT NULL")

                // Create customer_users table
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `customer_users` (" +
                            "`user_id` TEXT NOT NULL, " +
                            "`phone_number` TEXT, " +
                            "`otp_password` TEXT, " +
                            "`last_update` INTEGER NOT NULL, " +
                            "`needs_sync` INTEGER NOT NULL, " +
                            "PRIMARY KEY(`user_id`))"
                )
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Drop and recreate customer_users table with the new schema
                db.execSQL("DROP TABLE IF EXISTS `customer_users`")
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `customer_users` (" +
                            "`user_id` INTEGER NOT NULL, " +
                            "`customer_name` TEXT, " +
                            "`phone_number` TEXT, " +
                            "`console_password` TEXT, " +
                            "`console_last_update` TEXT NOT NULL, " +
                            "PRIMARY KEY(`user_id`))"
                )

                // For boxes and transactions, the customer_id was previously TEXT.
                // SQLite allows storing INTEGER in TEXT, but Room expects INTEGER.
                // Since this feature is completely new, we can drop the column and recreate it,
                // but SQLite ALTER TABLE does not support DROP COLUMN until version 3.35.0.
                // Given this is a local development version step, a simple table recreation or 
                // schema matching for INTEGER is needed.
                // However, since Android SQLite might be old, we recreate the tables.
                
                // Transactions Table Recreation
                db.execSQL("CREATE TABLE IF NOT EXISTS `transactions_new` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `order_serial` TEXT NOT NULL, `order_id` INTEGER NOT NULL, `box_id` INTEGER NOT NULL, `trans_date` INTEGER NOT NULL, `branch_id` INTEGER NOT NULL, `trans_type` TEXT NOT NULL, `box_Size` TEXT NOT NULL, `customer_id` INTEGER)")
                
                var hasTransactionsCustomerId = false
                db.query("PRAGMA table_info(transactions)").use { cursor ->
                    while(cursor.moveToNext()) {
                        if (cursor.getString(1) == "customer_id") {
                            hasTransactionsCustomerId = true
                            break
                        }
                    }
                }
                
                if (hasTransactionsCustomerId) {
                    db.execSQL("INSERT INTO transactions_new (id, order_serial, order_id, box_id, trans_date, branch_id, trans_type, box_Size, customer_id) SELECT id, order_serial, order_id, box_id, trans_date, branch_id, trans_type, box_Size, CAST(customer_id AS INTEGER) FROM transactions")
                } else {
                    db.execSQL("INSERT INTO transactions_new (id, order_serial, order_id, box_id, trans_date, branch_id, trans_type, box_Size, customer_id) SELECT id, order_serial, order_id, box_id, trans_date, branch_id, trans_type, box_Size, NULL FROM transactions")
                }
                db.execSQL("DROP TABLE transactions")
                db.execSQL("ALTER TABLE transactions_new RENAME TO transactions")

                // Boxes Table Recreation
                db.execSQL("CREATE TABLE IF NOT EXISTS `boxes_new` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `order_serial` TEXT NOT NULL, `order_id` INTEGER NOT NULL, `box_id` INTEGER NOT NULL, `box_number` INTEGER NOT NULL, `trans_date` INTEGER NOT NULL, `branch_id` INTEGER NOT NULL, `trans_type` TEXT NOT NULL, `box_Size` TEXT NOT NULL, `box_type` TEXT NOT NULL, `box_state` TEXT NOT NULL, `station_id` INTEGER NOT NULL, `port_id` TEXT NOT NULL, `customer_id` INTEGER)")
                
                var hasBoxesCustomerId = false
                var hasBoxesBoxNumber = false
                db.query("PRAGMA table_info(boxes)").use { cursor ->
                    while(cursor.moveToNext()) {
                        val colName = cursor.getString(1)
                        if (colName == "customer_id") hasBoxesCustomerId = true
                        if (colName == "box_number") hasBoxesBoxNumber = true
                    }
                }
                
                val boxNumberSelect = if (hasBoxesBoxNumber) "box_number" else "0"
                val customerIdSelectBoxes = if (hasBoxesCustomerId) "CAST(customer_id AS INTEGER)" else "NULL"
                
                db.execSQL("INSERT INTO boxes_new (id, order_serial, order_id, box_id, box_number, trans_date, branch_id, trans_type, box_Size, box_type, box_state, station_id, port_id, customer_id) SELECT id, order_serial, order_id, box_id, $boxNumberSelect, trans_date, branch_id, trans_type, box_Size, box_type, box_state, station_id, port_id, $customerIdSelectBoxes FROM boxes")
                
                db.execSQL("DROP TABLE boxes")
                db.execSQL("ALTER TABLE boxes_new RENAME TO boxes")
            }
        }
    }
}