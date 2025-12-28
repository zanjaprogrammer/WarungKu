package com.zanjaprogrammer.warungku.data;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomOpenHelper;
import androidx.room.RoomOpenHelper.Delegate;
import androidx.room.RoomOpenHelper.ValidationResult;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.room.util.TableInfo.Column;
import androidx.room.util.TableInfo.ForeignKey;
import androidx.room.util.TableInfo.Index;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import androidx.sqlite.db.SupportSQLiteOpenHelper.Callback;
import androidx.sqlite.db.SupportSQLiteOpenHelper.Configuration;
import com.zanjaprogrammer.warungku.data.dao.CashFlowDao;
import com.zanjaprogrammer.warungku.data.dao.CashFlowDao_Impl;
import com.zanjaprogrammer.warungku.data.dao.ProductDao;
import com.zanjaprogrammer.warungku.data.dao.ProductDao_Impl;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class AppDatabase_Impl extends AppDatabase {
  private volatile ProductDao _productDao;

  private volatile CashFlowDao _cashFlowDao;

  @Override
  protected SupportSQLiteOpenHelper createOpenHelper(DatabaseConfiguration configuration) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(configuration, new RoomOpenHelper.Delegate(3) {
      @Override
      public void createAllTables(SupportSQLiteDatabase _db) {
        _db.execSQL("CREATE TABLE IF NOT EXISTS `products` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT, `sellPrice` REAL NOT NULL, `buyPrice` REAL, `currentStock` INTEGER NOT NULL, `minStock` INTEGER NOT NULL, `salesCount` INTEGER NOT NULL, `isFavorite` INTEGER NOT NULL, `lastSoldTimestamp` INTEGER NOT NULL, `barcode` TEXT)");
        _db.execSQL("CREATE TABLE IF NOT EXISTS `cash_flow` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `type` TEXT, `amount` REAL NOT NULL, `description` TEXT, `timestamp` INTEGER NOT NULL, `productId` INTEGER, `profit` REAL)");
        _db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        _db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '1f076fff536c38ad63570567d2aa0578')");
      }

      @Override
      public void dropAllTables(SupportSQLiteDatabase _db) {
        _db.execSQL("DROP TABLE IF EXISTS `products`");
        _db.execSQL("DROP TABLE IF EXISTS `cash_flow`");
        if (mCallbacks != null) {
          for (int _i = 0, _size = mCallbacks.size(); _i < _size; _i++) {
            mCallbacks.get(_i).onDestructiveMigration(_db);
          }
        }
      }

      @Override
      public void onCreate(SupportSQLiteDatabase _db) {
        if (mCallbacks != null) {
          for (int _i = 0, _size = mCallbacks.size(); _i < _size; _i++) {
            mCallbacks.get(_i).onCreate(_db);
          }
        }
      }

      @Override
      public void onOpen(SupportSQLiteDatabase _db) {
        mDatabase = _db;
        internalInitInvalidationTracker(_db);
        if (mCallbacks != null) {
          for (int _i = 0, _size = mCallbacks.size(); _i < _size; _i++) {
            mCallbacks.get(_i).onOpen(_db);
          }
        }
      }

      @Override
      public void onPreMigrate(SupportSQLiteDatabase _db) {
        DBUtil.dropFtsSyncTriggers(_db);
      }

      @Override
      public void onPostMigrate(SupportSQLiteDatabase _db) {
      }

      @Override
      public RoomOpenHelper.ValidationResult onValidateSchema(SupportSQLiteDatabase _db) {
        final HashMap<String, TableInfo.Column> _columnsProducts = new HashMap<String, TableInfo.Column>(10);
        _columnsProducts.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsProducts.put("name", new TableInfo.Column("name", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsProducts.put("sellPrice", new TableInfo.Column("sellPrice", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsProducts.put("buyPrice", new TableInfo.Column("buyPrice", "REAL", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsProducts.put("currentStock", new TableInfo.Column("currentStock", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsProducts.put("minStock", new TableInfo.Column("minStock", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsProducts.put("salesCount", new TableInfo.Column("salesCount", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsProducts.put("isFavorite", new TableInfo.Column("isFavorite", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsProducts.put("lastSoldTimestamp", new TableInfo.Column("lastSoldTimestamp", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsProducts.put("barcode", new TableInfo.Column("barcode", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysProducts = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesProducts = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoProducts = new TableInfo("products", _columnsProducts, _foreignKeysProducts, _indicesProducts);
        final TableInfo _existingProducts = TableInfo.read(_db, "products");
        if (! _infoProducts.equals(_existingProducts)) {
          return new RoomOpenHelper.ValidationResult(false, "products(com.zanjaprogrammer.warungku.data.entity.Product).\n"
                  + " Expected:\n" + _infoProducts + "\n"
                  + " Found:\n" + _existingProducts);
        }
        final HashMap<String, TableInfo.Column> _columnsCashFlow = new HashMap<String, TableInfo.Column>(7);
        _columnsCashFlow.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCashFlow.put("type", new TableInfo.Column("type", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCashFlow.put("amount", new TableInfo.Column("amount", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCashFlow.put("description", new TableInfo.Column("description", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCashFlow.put("timestamp", new TableInfo.Column("timestamp", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCashFlow.put("productId", new TableInfo.Column("productId", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCashFlow.put("profit", new TableInfo.Column("profit", "REAL", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysCashFlow = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesCashFlow = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoCashFlow = new TableInfo("cash_flow", _columnsCashFlow, _foreignKeysCashFlow, _indicesCashFlow);
        final TableInfo _existingCashFlow = TableInfo.read(_db, "cash_flow");
        if (! _infoCashFlow.equals(_existingCashFlow)) {
          return new RoomOpenHelper.ValidationResult(false, "cash_flow(com.zanjaprogrammer.warungku.data.entity.CashFlow).\n"
                  + " Expected:\n" + _infoCashFlow + "\n"
                  + " Found:\n" + _existingCashFlow);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "1f076fff536c38ad63570567d2aa0578", "421c85b093c38b4e8745104a0d2ad2df");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(configuration.context)
        .name(configuration.name)
        .callback(_openCallback)
        .build();
    final SupportSQLiteOpenHelper _helper = configuration.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "products","cash_flow");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    try {
      super.beginTransaction();
      _db.execSQL("DELETE FROM `products`");
      _db.execSQL("DELETE FROM `cash_flow`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(ProductDao.class, ProductDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(CashFlowDao.class, CashFlowDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  public List<Migration> getAutoMigrations(
      @NonNull Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecsMap) {
    return Arrays.asList();
  }

  @Override
  public ProductDao productDao() {
    if (_productDao != null) {
      return _productDao;
    } else {
      synchronized(this) {
        if(_productDao == null) {
          _productDao = new ProductDao_Impl(this);
        }
        return _productDao;
      }
    }
  }

  @Override
  public CashFlowDao cashFlowDao() {
    if (_cashFlowDao != null) {
      return _cashFlowDao;
    } else {
      synchronized(this) {
        if(_cashFlowDao == null) {
          _cashFlowDao = new CashFlowDao_Impl(this);
        }
        return _cashFlowDao;
      }
    }
  }
}
