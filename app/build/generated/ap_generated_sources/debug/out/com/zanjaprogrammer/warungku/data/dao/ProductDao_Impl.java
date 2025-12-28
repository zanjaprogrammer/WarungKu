package com.zanjaprogrammer.warungku.data.dao;

import android.database.Cursor;
import androidx.lifecycle.LiveData;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.zanjaprogrammer.warungku.data.entity.Product;
import java.lang.Class;
import java.lang.Double;
import java.lang.Exception;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class ProductDao_Impl implements ProductDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<Product> __insertionAdapterOfProduct;

  private final EntityDeletionOrUpdateAdapter<Product> __deletionAdapterOfProduct;

  private final EntityDeletionOrUpdateAdapter<Product> __updateAdapterOfProduct;

  public ProductDao_Impl(RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfProduct = new EntityInsertionAdapter<Product>(__db) {
      @Override
      public String createQuery() {
        return "INSERT OR ABORT INTO `products` (`id`,`name`,`sellPrice`,`buyPrice`,`currentStock`,`minStock`,`salesCount`,`isFavorite`,`lastSoldTimestamp`,`barcode`,`synced`,`lastSyncedAt`,`cloudId`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      public void bind(SupportSQLiteStatement stmt, Product value) {
        stmt.bindLong(1, value.id);
        if (value.name == null) {
          stmt.bindNull(2);
        } else {
          stmt.bindString(2, value.name);
        }
        stmt.bindDouble(3, value.sellPrice);
        if (value.buyPrice == null) {
          stmt.bindNull(4);
        } else {
          stmt.bindDouble(4, value.buyPrice);
        }
        stmt.bindLong(5, value.currentStock);
        stmt.bindLong(6, value.minStock);
        stmt.bindLong(7, value.salesCount);
        final int _tmp = value.isFavorite ? 1 : 0;
        stmt.bindLong(8, _tmp);
        stmt.bindLong(9, value.lastSoldTimestamp);
        if (value.barcode == null) {
          stmt.bindNull(10);
        } else {
          stmt.bindString(10, value.barcode);
        }
        final int _tmp_1 = value.synced ? 1 : 0;
        stmt.bindLong(11, _tmp_1);
        stmt.bindLong(12, value.lastSyncedAt);
        if (value.cloudId == null) {
          stmt.bindNull(13);
        } else {
          stmt.bindString(13, value.cloudId);
        }
      }
    };
    this.__deletionAdapterOfProduct = new EntityDeletionOrUpdateAdapter<Product>(__db) {
      @Override
      public String createQuery() {
        return "DELETE FROM `products` WHERE `id` = ?";
      }

      @Override
      public void bind(SupportSQLiteStatement stmt, Product value) {
        stmt.bindLong(1, value.id);
      }
    };
    this.__updateAdapterOfProduct = new EntityDeletionOrUpdateAdapter<Product>(__db) {
      @Override
      public String createQuery() {
        return "UPDATE OR ABORT `products` SET `id` = ?,`name` = ?,`sellPrice` = ?,`buyPrice` = ?,`currentStock` = ?,`minStock` = ?,`salesCount` = ?,`isFavorite` = ?,`lastSoldTimestamp` = ?,`barcode` = ?,`synced` = ?,`lastSyncedAt` = ?,`cloudId` = ? WHERE `id` = ?";
      }

      @Override
      public void bind(SupportSQLiteStatement stmt, Product value) {
        stmt.bindLong(1, value.id);
        if (value.name == null) {
          stmt.bindNull(2);
        } else {
          stmt.bindString(2, value.name);
        }
        stmt.bindDouble(3, value.sellPrice);
        if (value.buyPrice == null) {
          stmt.bindNull(4);
        } else {
          stmt.bindDouble(4, value.buyPrice);
        }
        stmt.bindLong(5, value.currentStock);
        stmt.bindLong(6, value.minStock);
        stmt.bindLong(7, value.salesCount);
        final int _tmp = value.isFavorite ? 1 : 0;
        stmt.bindLong(8, _tmp);
        stmt.bindLong(9, value.lastSoldTimestamp);
        if (value.barcode == null) {
          stmt.bindNull(10);
        } else {
          stmt.bindString(10, value.barcode);
        }
        final int _tmp_1 = value.synced ? 1 : 0;
        stmt.bindLong(11, _tmp_1);
        stmt.bindLong(12, value.lastSyncedAt);
        if (value.cloudId == null) {
          stmt.bindNull(13);
        } else {
          stmt.bindString(13, value.cloudId);
        }
        stmt.bindLong(14, value.id);
      }
    };
  }

  @Override
  public long insert(final Product product) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      long _result = __insertionAdapterOfProduct.insertAndReturnId(product);
      __db.setTransactionSuccessful();
      return _result;
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void delete(final Product product) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __deletionAdapterOfProduct.handle(product);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void update(final Product product) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __updateAdapterOfProduct.handle(product);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public LiveData<List<Product>> getAllProducts() {
    final String _sql = "SELECT * FROM products ORDER BY name ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return __db.getInvalidationTracker().createLiveData(new String[]{"products"}, false, new Callable<List<Product>>() {
      @Override
      public List<Product> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfSellPrice = CursorUtil.getColumnIndexOrThrow(_cursor, "sellPrice");
          final int _cursorIndexOfBuyPrice = CursorUtil.getColumnIndexOrThrow(_cursor, "buyPrice");
          final int _cursorIndexOfCurrentStock = CursorUtil.getColumnIndexOrThrow(_cursor, "currentStock");
          final int _cursorIndexOfMinStock = CursorUtil.getColumnIndexOrThrow(_cursor, "minStock");
          final int _cursorIndexOfSalesCount = CursorUtil.getColumnIndexOrThrow(_cursor, "salesCount");
          final int _cursorIndexOfIsFavorite = CursorUtil.getColumnIndexOrThrow(_cursor, "isFavorite");
          final int _cursorIndexOfLastSoldTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "lastSoldTimestamp");
          final int _cursorIndexOfBarcode = CursorUtil.getColumnIndexOrThrow(_cursor, "barcode");
          final int _cursorIndexOfSynced = CursorUtil.getColumnIndexOrThrow(_cursor, "synced");
          final int _cursorIndexOfLastSyncedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "lastSyncedAt");
          final int _cursorIndexOfCloudId = CursorUtil.getColumnIndexOrThrow(_cursor, "cloudId");
          final List<Product> _result = new ArrayList<Product>(_cursor.getCount());
          while(_cursor.moveToNext()) {
            final Product _item;
            final String _tmpName;
            if (_cursor.isNull(_cursorIndexOfName)) {
              _tmpName = null;
            } else {
              _tmpName = _cursor.getString(_cursorIndexOfName);
            }
            final double _tmpSellPrice;
            _tmpSellPrice = _cursor.getDouble(_cursorIndexOfSellPrice);
            final Double _tmpBuyPrice;
            if (_cursor.isNull(_cursorIndexOfBuyPrice)) {
              _tmpBuyPrice = null;
            } else {
              _tmpBuyPrice = _cursor.getDouble(_cursorIndexOfBuyPrice);
            }
            final int _tmpCurrentStock;
            _tmpCurrentStock = _cursor.getInt(_cursorIndexOfCurrentStock);
            final int _tmpMinStock;
            _tmpMinStock = _cursor.getInt(_cursorIndexOfMinStock);
            _item = new Product(_tmpName,_tmpSellPrice,_tmpBuyPrice,_tmpCurrentStock,_tmpMinStock);
            _item.id = _cursor.getInt(_cursorIndexOfId);
            _item.salesCount = _cursor.getInt(_cursorIndexOfSalesCount);
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsFavorite);
            _item.isFavorite = _tmp != 0;
            _item.lastSoldTimestamp = _cursor.getLong(_cursorIndexOfLastSoldTimestamp);
            if (_cursor.isNull(_cursorIndexOfBarcode)) {
              _item.barcode = null;
            } else {
              _item.barcode = _cursor.getString(_cursorIndexOfBarcode);
            }
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfSynced);
            _item.synced = _tmp_1 != 0;
            _item.lastSyncedAt = _cursor.getLong(_cursorIndexOfLastSyncedAt);
            if (_cursor.isNull(_cursorIndexOfCloudId)) {
              _item.cloudId = null;
            } else {
              _item.cloudId = _cursor.getString(_cursorIndexOfCloudId);
            }
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public LiveData<List<Product>> getShoppingList() {
    final String _sql = "SELECT * FROM products WHERE currentStock <= minStock ORDER BY salesCount DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return __db.getInvalidationTracker().createLiveData(new String[]{"products"}, false, new Callable<List<Product>>() {
      @Override
      public List<Product> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfSellPrice = CursorUtil.getColumnIndexOrThrow(_cursor, "sellPrice");
          final int _cursorIndexOfBuyPrice = CursorUtil.getColumnIndexOrThrow(_cursor, "buyPrice");
          final int _cursorIndexOfCurrentStock = CursorUtil.getColumnIndexOrThrow(_cursor, "currentStock");
          final int _cursorIndexOfMinStock = CursorUtil.getColumnIndexOrThrow(_cursor, "minStock");
          final int _cursorIndexOfSalesCount = CursorUtil.getColumnIndexOrThrow(_cursor, "salesCount");
          final int _cursorIndexOfIsFavorite = CursorUtil.getColumnIndexOrThrow(_cursor, "isFavorite");
          final int _cursorIndexOfLastSoldTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "lastSoldTimestamp");
          final int _cursorIndexOfBarcode = CursorUtil.getColumnIndexOrThrow(_cursor, "barcode");
          final int _cursorIndexOfSynced = CursorUtil.getColumnIndexOrThrow(_cursor, "synced");
          final int _cursorIndexOfLastSyncedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "lastSyncedAt");
          final int _cursorIndexOfCloudId = CursorUtil.getColumnIndexOrThrow(_cursor, "cloudId");
          final List<Product> _result = new ArrayList<Product>(_cursor.getCount());
          while(_cursor.moveToNext()) {
            final Product _item;
            final String _tmpName;
            if (_cursor.isNull(_cursorIndexOfName)) {
              _tmpName = null;
            } else {
              _tmpName = _cursor.getString(_cursorIndexOfName);
            }
            final double _tmpSellPrice;
            _tmpSellPrice = _cursor.getDouble(_cursorIndexOfSellPrice);
            final Double _tmpBuyPrice;
            if (_cursor.isNull(_cursorIndexOfBuyPrice)) {
              _tmpBuyPrice = null;
            } else {
              _tmpBuyPrice = _cursor.getDouble(_cursorIndexOfBuyPrice);
            }
            final int _tmpCurrentStock;
            _tmpCurrentStock = _cursor.getInt(_cursorIndexOfCurrentStock);
            final int _tmpMinStock;
            _tmpMinStock = _cursor.getInt(_cursorIndexOfMinStock);
            _item = new Product(_tmpName,_tmpSellPrice,_tmpBuyPrice,_tmpCurrentStock,_tmpMinStock);
            _item.id = _cursor.getInt(_cursorIndexOfId);
            _item.salesCount = _cursor.getInt(_cursorIndexOfSalesCount);
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsFavorite);
            _item.isFavorite = _tmp != 0;
            _item.lastSoldTimestamp = _cursor.getLong(_cursorIndexOfLastSoldTimestamp);
            if (_cursor.isNull(_cursorIndexOfBarcode)) {
              _item.barcode = null;
            } else {
              _item.barcode = _cursor.getString(_cursorIndexOfBarcode);
            }
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfSynced);
            _item.synced = _tmp_1 != 0;
            _item.lastSyncedAt = _cursor.getLong(_cursorIndexOfLastSyncedAt);
            if (_cursor.isNull(_cursorIndexOfCloudId)) {
              _item.cloudId = null;
            } else {
              _item.cloudId = _cursor.getString(_cursorIndexOfCloudId);
            }
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Product getProductById(final int id) {
    final String _sql = "SELECT * FROM products WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
      final int _cursorIndexOfSellPrice = CursorUtil.getColumnIndexOrThrow(_cursor, "sellPrice");
      final int _cursorIndexOfBuyPrice = CursorUtil.getColumnIndexOrThrow(_cursor, "buyPrice");
      final int _cursorIndexOfCurrentStock = CursorUtil.getColumnIndexOrThrow(_cursor, "currentStock");
      final int _cursorIndexOfMinStock = CursorUtil.getColumnIndexOrThrow(_cursor, "minStock");
      final int _cursorIndexOfSalesCount = CursorUtil.getColumnIndexOrThrow(_cursor, "salesCount");
      final int _cursorIndexOfIsFavorite = CursorUtil.getColumnIndexOrThrow(_cursor, "isFavorite");
      final int _cursorIndexOfLastSoldTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "lastSoldTimestamp");
      final int _cursorIndexOfBarcode = CursorUtil.getColumnIndexOrThrow(_cursor, "barcode");
      final int _cursorIndexOfSynced = CursorUtil.getColumnIndexOrThrow(_cursor, "synced");
      final int _cursorIndexOfLastSyncedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "lastSyncedAt");
      final int _cursorIndexOfCloudId = CursorUtil.getColumnIndexOrThrow(_cursor, "cloudId");
      final Product _result;
      if(_cursor.moveToFirst()) {
        final String _tmpName;
        if (_cursor.isNull(_cursorIndexOfName)) {
          _tmpName = null;
        } else {
          _tmpName = _cursor.getString(_cursorIndexOfName);
        }
        final double _tmpSellPrice;
        _tmpSellPrice = _cursor.getDouble(_cursorIndexOfSellPrice);
        final Double _tmpBuyPrice;
        if (_cursor.isNull(_cursorIndexOfBuyPrice)) {
          _tmpBuyPrice = null;
        } else {
          _tmpBuyPrice = _cursor.getDouble(_cursorIndexOfBuyPrice);
        }
        final int _tmpCurrentStock;
        _tmpCurrentStock = _cursor.getInt(_cursorIndexOfCurrentStock);
        final int _tmpMinStock;
        _tmpMinStock = _cursor.getInt(_cursorIndexOfMinStock);
        _result = new Product(_tmpName,_tmpSellPrice,_tmpBuyPrice,_tmpCurrentStock,_tmpMinStock);
        _result.id = _cursor.getInt(_cursorIndexOfId);
        _result.salesCount = _cursor.getInt(_cursorIndexOfSalesCount);
        final int _tmp;
        _tmp = _cursor.getInt(_cursorIndexOfIsFavorite);
        _result.isFavorite = _tmp != 0;
        _result.lastSoldTimestamp = _cursor.getLong(_cursorIndexOfLastSoldTimestamp);
        if (_cursor.isNull(_cursorIndexOfBarcode)) {
          _result.barcode = null;
        } else {
          _result.barcode = _cursor.getString(_cursorIndexOfBarcode);
        }
        final int _tmp_1;
        _tmp_1 = _cursor.getInt(_cursorIndexOfSynced);
        _result.synced = _tmp_1 != 0;
        _result.lastSyncedAt = _cursor.getLong(_cursorIndexOfLastSyncedAt);
        if (_cursor.isNull(_cursorIndexOfCloudId)) {
          _result.cloudId = null;
        } else {
          _result.cloudId = _cursor.getString(_cursorIndexOfCloudId);
        }
      } else {
        _result = null;
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public Product getProductByBarcode(final String barcode) {
    final String _sql = "SELECT * FROM products WHERE barcode = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (barcode == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, barcode);
    }
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
      final int _cursorIndexOfSellPrice = CursorUtil.getColumnIndexOrThrow(_cursor, "sellPrice");
      final int _cursorIndexOfBuyPrice = CursorUtil.getColumnIndexOrThrow(_cursor, "buyPrice");
      final int _cursorIndexOfCurrentStock = CursorUtil.getColumnIndexOrThrow(_cursor, "currentStock");
      final int _cursorIndexOfMinStock = CursorUtil.getColumnIndexOrThrow(_cursor, "minStock");
      final int _cursorIndexOfSalesCount = CursorUtil.getColumnIndexOrThrow(_cursor, "salesCount");
      final int _cursorIndexOfIsFavorite = CursorUtil.getColumnIndexOrThrow(_cursor, "isFavorite");
      final int _cursorIndexOfLastSoldTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "lastSoldTimestamp");
      final int _cursorIndexOfBarcode = CursorUtil.getColumnIndexOrThrow(_cursor, "barcode");
      final int _cursorIndexOfSynced = CursorUtil.getColumnIndexOrThrow(_cursor, "synced");
      final int _cursorIndexOfLastSyncedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "lastSyncedAt");
      final int _cursorIndexOfCloudId = CursorUtil.getColumnIndexOrThrow(_cursor, "cloudId");
      final Product _result;
      if(_cursor.moveToFirst()) {
        final String _tmpName;
        if (_cursor.isNull(_cursorIndexOfName)) {
          _tmpName = null;
        } else {
          _tmpName = _cursor.getString(_cursorIndexOfName);
        }
        final double _tmpSellPrice;
        _tmpSellPrice = _cursor.getDouble(_cursorIndexOfSellPrice);
        final Double _tmpBuyPrice;
        if (_cursor.isNull(_cursorIndexOfBuyPrice)) {
          _tmpBuyPrice = null;
        } else {
          _tmpBuyPrice = _cursor.getDouble(_cursorIndexOfBuyPrice);
        }
        final int _tmpCurrentStock;
        _tmpCurrentStock = _cursor.getInt(_cursorIndexOfCurrentStock);
        final int _tmpMinStock;
        _tmpMinStock = _cursor.getInt(_cursorIndexOfMinStock);
        _result = new Product(_tmpName,_tmpSellPrice,_tmpBuyPrice,_tmpCurrentStock,_tmpMinStock);
        _result.id = _cursor.getInt(_cursorIndexOfId);
        _result.salesCount = _cursor.getInt(_cursorIndexOfSalesCount);
        final int _tmp;
        _tmp = _cursor.getInt(_cursorIndexOfIsFavorite);
        _result.isFavorite = _tmp != 0;
        _result.lastSoldTimestamp = _cursor.getLong(_cursorIndexOfLastSoldTimestamp);
        if (_cursor.isNull(_cursorIndexOfBarcode)) {
          _result.barcode = null;
        } else {
          _result.barcode = _cursor.getString(_cursorIndexOfBarcode);
        }
        final int _tmp_1;
        _tmp_1 = _cursor.getInt(_cursorIndexOfSynced);
        _result.synced = _tmp_1 != 0;
        _result.lastSyncedAt = _cursor.getLong(_cursorIndexOfLastSyncedAt);
        if (_cursor.isNull(_cursorIndexOfCloudId)) {
          _result.cloudId = null;
        } else {
          _result.cloudId = _cursor.getString(_cursorIndexOfCloudId);
        }
      } else {
        _result = null;
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public LiveData<List<Product>> getTopSellingProducts(final int limit) {
    final String _sql = "SELECT * FROM products WHERE salesCount > 0 ORDER BY salesCount DESC LIMIT ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, limit);
    return __db.getInvalidationTracker().createLiveData(new String[]{"products"}, false, new Callable<List<Product>>() {
      @Override
      public List<Product> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfSellPrice = CursorUtil.getColumnIndexOrThrow(_cursor, "sellPrice");
          final int _cursorIndexOfBuyPrice = CursorUtil.getColumnIndexOrThrow(_cursor, "buyPrice");
          final int _cursorIndexOfCurrentStock = CursorUtil.getColumnIndexOrThrow(_cursor, "currentStock");
          final int _cursorIndexOfMinStock = CursorUtil.getColumnIndexOrThrow(_cursor, "minStock");
          final int _cursorIndexOfSalesCount = CursorUtil.getColumnIndexOrThrow(_cursor, "salesCount");
          final int _cursorIndexOfIsFavorite = CursorUtil.getColumnIndexOrThrow(_cursor, "isFavorite");
          final int _cursorIndexOfLastSoldTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "lastSoldTimestamp");
          final int _cursorIndexOfBarcode = CursorUtil.getColumnIndexOrThrow(_cursor, "barcode");
          final int _cursorIndexOfSynced = CursorUtil.getColumnIndexOrThrow(_cursor, "synced");
          final int _cursorIndexOfLastSyncedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "lastSyncedAt");
          final int _cursorIndexOfCloudId = CursorUtil.getColumnIndexOrThrow(_cursor, "cloudId");
          final List<Product> _result = new ArrayList<Product>(_cursor.getCount());
          while(_cursor.moveToNext()) {
            final Product _item;
            final String _tmpName;
            if (_cursor.isNull(_cursorIndexOfName)) {
              _tmpName = null;
            } else {
              _tmpName = _cursor.getString(_cursorIndexOfName);
            }
            final double _tmpSellPrice;
            _tmpSellPrice = _cursor.getDouble(_cursorIndexOfSellPrice);
            final Double _tmpBuyPrice;
            if (_cursor.isNull(_cursorIndexOfBuyPrice)) {
              _tmpBuyPrice = null;
            } else {
              _tmpBuyPrice = _cursor.getDouble(_cursorIndexOfBuyPrice);
            }
            final int _tmpCurrentStock;
            _tmpCurrentStock = _cursor.getInt(_cursorIndexOfCurrentStock);
            final int _tmpMinStock;
            _tmpMinStock = _cursor.getInt(_cursorIndexOfMinStock);
            _item = new Product(_tmpName,_tmpSellPrice,_tmpBuyPrice,_tmpCurrentStock,_tmpMinStock);
            _item.id = _cursor.getInt(_cursorIndexOfId);
            _item.salesCount = _cursor.getInt(_cursorIndexOfSalesCount);
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsFavorite);
            _item.isFavorite = _tmp != 0;
            _item.lastSoldTimestamp = _cursor.getLong(_cursorIndexOfLastSoldTimestamp);
            if (_cursor.isNull(_cursorIndexOfBarcode)) {
              _item.barcode = null;
            } else {
              _item.barcode = _cursor.getString(_cursorIndexOfBarcode);
            }
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfSynced);
            _item.synced = _tmp_1 != 0;
            _item.lastSyncedAt = _cursor.getLong(_cursorIndexOfLastSyncedAt);
            if (_cursor.isNull(_cursorIndexOfCloudId)) {
              _item.cloudId = null;
            } else {
              _item.cloudId = _cursor.getString(_cursorIndexOfCloudId);
            }
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public LiveData<List<Product>> getUnsoldProducts() {
    final String _sql = "SELECT * FROM products WHERE salesCount = 0 OR salesCount IS NULL ORDER BY name ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return __db.getInvalidationTracker().createLiveData(new String[]{"products"}, false, new Callable<List<Product>>() {
      @Override
      public List<Product> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfSellPrice = CursorUtil.getColumnIndexOrThrow(_cursor, "sellPrice");
          final int _cursorIndexOfBuyPrice = CursorUtil.getColumnIndexOrThrow(_cursor, "buyPrice");
          final int _cursorIndexOfCurrentStock = CursorUtil.getColumnIndexOrThrow(_cursor, "currentStock");
          final int _cursorIndexOfMinStock = CursorUtil.getColumnIndexOrThrow(_cursor, "minStock");
          final int _cursorIndexOfSalesCount = CursorUtil.getColumnIndexOrThrow(_cursor, "salesCount");
          final int _cursorIndexOfIsFavorite = CursorUtil.getColumnIndexOrThrow(_cursor, "isFavorite");
          final int _cursorIndexOfLastSoldTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "lastSoldTimestamp");
          final int _cursorIndexOfBarcode = CursorUtil.getColumnIndexOrThrow(_cursor, "barcode");
          final int _cursorIndexOfSynced = CursorUtil.getColumnIndexOrThrow(_cursor, "synced");
          final int _cursorIndexOfLastSyncedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "lastSyncedAt");
          final int _cursorIndexOfCloudId = CursorUtil.getColumnIndexOrThrow(_cursor, "cloudId");
          final List<Product> _result = new ArrayList<Product>(_cursor.getCount());
          while(_cursor.moveToNext()) {
            final Product _item;
            final String _tmpName;
            if (_cursor.isNull(_cursorIndexOfName)) {
              _tmpName = null;
            } else {
              _tmpName = _cursor.getString(_cursorIndexOfName);
            }
            final double _tmpSellPrice;
            _tmpSellPrice = _cursor.getDouble(_cursorIndexOfSellPrice);
            final Double _tmpBuyPrice;
            if (_cursor.isNull(_cursorIndexOfBuyPrice)) {
              _tmpBuyPrice = null;
            } else {
              _tmpBuyPrice = _cursor.getDouble(_cursorIndexOfBuyPrice);
            }
            final int _tmpCurrentStock;
            _tmpCurrentStock = _cursor.getInt(_cursorIndexOfCurrentStock);
            final int _tmpMinStock;
            _tmpMinStock = _cursor.getInt(_cursorIndexOfMinStock);
            _item = new Product(_tmpName,_tmpSellPrice,_tmpBuyPrice,_tmpCurrentStock,_tmpMinStock);
            _item.id = _cursor.getInt(_cursorIndexOfId);
            _item.salesCount = _cursor.getInt(_cursorIndexOfSalesCount);
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsFavorite);
            _item.isFavorite = _tmp != 0;
            _item.lastSoldTimestamp = _cursor.getLong(_cursorIndexOfLastSoldTimestamp);
            if (_cursor.isNull(_cursorIndexOfBarcode)) {
              _item.barcode = null;
            } else {
              _item.barcode = _cursor.getString(_cursorIndexOfBarcode);
            }
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfSynced);
            _item.synced = _tmp_1 != 0;
            _item.lastSyncedAt = _cursor.getLong(_cursorIndexOfLastSyncedAt);
            if (_cursor.isNull(_cursorIndexOfCloudId)) {
              _item.cloudId = null;
            } else {
              _item.cloudId = _cursor.getString(_cursorIndexOfCloudId);
            }
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public List<Product> getUnsyncedProducts() {
    final String _sql = "SELECT * FROM products WHERE synced = 0";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
      final int _cursorIndexOfSellPrice = CursorUtil.getColumnIndexOrThrow(_cursor, "sellPrice");
      final int _cursorIndexOfBuyPrice = CursorUtil.getColumnIndexOrThrow(_cursor, "buyPrice");
      final int _cursorIndexOfCurrentStock = CursorUtil.getColumnIndexOrThrow(_cursor, "currentStock");
      final int _cursorIndexOfMinStock = CursorUtil.getColumnIndexOrThrow(_cursor, "minStock");
      final int _cursorIndexOfSalesCount = CursorUtil.getColumnIndexOrThrow(_cursor, "salesCount");
      final int _cursorIndexOfIsFavorite = CursorUtil.getColumnIndexOrThrow(_cursor, "isFavorite");
      final int _cursorIndexOfLastSoldTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "lastSoldTimestamp");
      final int _cursorIndexOfBarcode = CursorUtil.getColumnIndexOrThrow(_cursor, "barcode");
      final int _cursorIndexOfSynced = CursorUtil.getColumnIndexOrThrow(_cursor, "synced");
      final int _cursorIndexOfLastSyncedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "lastSyncedAt");
      final int _cursorIndexOfCloudId = CursorUtil.getColumnIndexOrThrow(_cursor, "cloudId");
      final List<Product> _result = new ArrayList<Product>(_cursor.getCount());
      while(_cursor.moveToNext()) {
        final Product _item;
        final String _tmpName;
        if (_cursor.isNull(_cursorIndexOfName)) {
          _tmpName = null;
        } else {
          _tmpName = _cursor.getString(_cursorIndexOfName);
        }
        final double _tmpSellPrice;
        _tmpSellPrice = _cursor.getDouble(_cursorIndexOfSellPrice);
        final Double _tmpBuyPrice;
        if (_cursor.isNull(_cursorIndexOfBuyPrice)) {
          _tmpBuyPrice = null;
        } else {
          _tmpBuyPrice = _cursor.getDouble(_cursorIndexOfBuyPrice);
        }
        final int _tmpCurrentStock;
        _tmpCurrentStock = _cursor.getInt(_cursorIndexOfCurrentStock);
        final int _tmpMinStock;
        _tmpMinStock = _cursor.getInt(_cursorIndexOfMinStock);
        _item = new Product(_tmpName,_tmpSellPrice,_tmpBuyPrice,_tmpCurrentStock,_tmpMinStock);
        _item.id = _cursor.getInt(_cursorIndexOfId);
        _item.salesCount = _cursor.getInt(_cursorIndexOfSalesCount);
        final int _tmp;
        _tmp = _cursor.getInt(_cursorIndexOfIsFavorite);
        _item.isFavorite = _tmp != 0;
        _item.lastSoldTimestamp = _cursor.getLong(_cursorIndexOfLastSoldTimestamp);
        if (_cursor.isNull(_cursorIndexOfBarcode)) {
          _item.barcode = null;
        } else {
          _item.barcode = _cursor.getString(_cursorIndexOfBarcode);
        }
        final int _tmp_1;
        _tmp_1 = _cursor.getInt(_cursorIndexOfSynced);
        _item.synced = _tmp_1 != 0;
        _item.lastSyncedAt = _cursor.getLong(_cursorIndexOfLastSyncedAt);
        if (_cursor.isNull(_cursorIndexOfCloudId)) {
          _item.cloudId = null;
        } else {
          _item.cloudId = _cursor.getString(_cursorIndexOfCloudId);
        }
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
