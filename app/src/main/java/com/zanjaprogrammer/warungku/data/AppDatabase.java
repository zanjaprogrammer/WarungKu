package com.zanjaprogrammer.warungku.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.zanjaprogrammer.warungku.data.dao.CashFlowDao;
import com.zanjaprogrammer.warungku.data.dao.ProductDao;
import com.zanjaprogrammer.warungku.data.entity.CashFlow;
import com.zanjaprogrammer.warungku.data.entity.Product;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = { Product.class, CashFlow.class }, version = 3, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract ProductDao productDao();

    public abstract CashFlowDao cashFlowDao();

    private static volatile AppDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    // Migration from version 1 to 2: Add isFavorite and lastSoldTimestamp columns
    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // Add new columns with default values
            database.execSQL("ALTER TABLE products ADD COLUMN isFavorite INTEGER NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE products ADD COLUMN lastSoldTimestamp INTEGER NOT NULL DEFAULT 0");
        }
    };

    // Migration from version 2 to 3: Add barcode column
    static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // Add barcode column (nullable TEXT)
            database.execSQL("ALTER TABLE products ADD COLUMN barcode TEXT");
        }
    };

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "warungku_db")
                            .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                            .fallbackToDestructiveMigration() // For development: drop and recreate if migration fails
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
