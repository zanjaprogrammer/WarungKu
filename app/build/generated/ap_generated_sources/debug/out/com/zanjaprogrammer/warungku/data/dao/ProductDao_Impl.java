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

  private final EntityDeletionOrUpdateAdapter<Product> __updateAdapterOfProduct;

  public ProductDao_Impl(RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfProduct = new EntityInsertionAdapter<Product>(__db) {
      @Override
      public String createQuery() {
        return "INSERT OR ABORT INTO `products` (`id`,`name`,`sellPrice`,`buyPrice`,`currentStock`,`minStock`,`salesCount`) VALUES (nullif(?, 0),?,?,?,?,?,?)";
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
      }
    };
    this.__updateAdapterOfProduct = new EntityDeletionOrUpdateAdapter<Product>(__db) {
      @Override
      public String createQuery() {
        return "UPDATE OR ABORT `products` SET `id` = ?,`name` = ?,`sellPrice` = ?,`buyPrice` = ?,`currentStock` = ?,`minStock` = ?,`salesCount` = ? WHERE `id` = ?";
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
        stmt.bindLong(8, value.id);
      }
    };
  }

  @Override
  public void insert(final Product product) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __insertionAdapterOfProduct.insert(product);
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
      } else {
        _result = null;
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
