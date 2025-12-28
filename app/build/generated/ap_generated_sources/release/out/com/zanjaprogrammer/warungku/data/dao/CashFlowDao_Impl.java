package com.zanjaprogrammer.warungku.data.dao;

import android.database.Cursor;
import androidx.lifecycle.LiveData;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.zanjaprogrammer.warungku.data.entity.CashFlow;
import java.lang.Class;
import java.lang.Double;
import java.lang.Exception;
import java.lang.Integer;
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
public final class CashFlowDao_Impl implements CashFlowDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<CashFlow> __insertionAdapterOfCashFlow;

  public CashFlowDao_Impl(RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfCashFlow = new EntityInsertionAdapter<CashFlow>(__db) {
      @Override
      public String createQuery() {
        return "INSERT OR ABORT INTO `cash_flow` (`id`,`type`,`amount`,`description`,`timestamp`,`productId`,`profit`) VALUES (nullif(?, 0),?,?,?,?,?,?)";
      }

      @Override
      public void bind(SupportSQLiteStatement stmt, CashFlow value) {
        stmt.bindLong(1, value.id);
        if (value.type == null) {
          stmt.bindNull(2);
        } else {
          stmt.bindString(2, value.type);
        }
        stmt.bindDouble(3, value.amount);
        if (value.description == null) {
          stmt.bindNull(4);
        } else {
          stmt.bindString(4, value.description);
        }
        stmt.bindLong(5, value.timestamp);
        if (value.productId == null) {
          stmt.bindNull(6);
        } else {
          stmt.bindLong(6, value.productId);
        }
        if (value.profit == null) {
          stmt.bindNull(7);
        } else {
          stmt.bindDouble(7, value.profit);
        }
      }
    };
  }

  @Override
  public void insert(final CashFlow cashFlow) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __insertionAdapterOfCashFlow.insert(cashFlow);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public LiveData<List<CashFlow>> getAllHistory() {
    final String _sql = "SELECT * FROM cash_flow ORDER BY timestamp DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return __db.getInvalidationTracker().createLiveData(new String[]{"cash_flow"}, false, new Callable<List<CashFlow>>() {
      @Override
      public List<CashFlow> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "amount");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfProductId = CursorUtil.getColumnIndexOrThrow(_cursor, "productId");
          final int _cursorIndexOfProfit = CursorUtil.getColumnIndexOrThrow(_cursor, "profit");
          final List<CashFlow> _result = new ArrayList<CashFlow>(_cursor.getCount());
          while(_cursor.moveToNext()) {
            final CashFlow _item;
            final String _tmpType;
            if (_cursor.isNull(_cursorIndexOfType)) {
              _tmpType = null;
            } else {
              _tmpType = _cursor.getString(_cursorIndexOfType);
            }
            final double _tmpAmount;
            _tmpAmount = _cursor.getDouble(_cursorIndexOfAmount);
            final String _tmpDescription;
            if (_cursor.isNull(_cursorIndexOfDescription)) {
              _tmpDescription = null;
            } else {
              _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            }
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final Integer _tmpProductId;
            if (_cursor.isNull(_cursorIndexOfProductId)) {
              _tmpProductId = null;
            } else {
              _tmpProductId = _cursor.getInt(_cursorIndexOfProductId);
            }
            final Double _tmpProfit;
            if (_cursor.isNull(_cursorIndexOfProfit)) {
              _tmpProfit = null;
            } else {
              _tmpProfit = _cursor.getDouble(_cursorIndexOfProfit);
            }
            _item = new CashFlow(_tmpType,_tmpAmount,_tmpDescription,_tmpTimestamp,_tmpProductId,_tmpProfit);
            _item.id = _cursor.getInt(_cursorIndexOfId);
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
  public LiveData<Double> getTotalIncome() {
    final String _sql = "SELECT SUM(amount) FROM cash_flow WHERE type = 'IN'";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return __db.getInvalidationTracker().createLiveData(new String[]{"cash_flow"}, false, new Callable<Double>() {
      @Override
      public Double call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Double _result;
          if(_cursor.moveToFirst()) {
            final Double _tmp;
            if (_cursor.isNull(0)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getDouble(0);
            }
            _result = _tmp;
          } else {
            _result = null;
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
  public LiveData<Double> getTotalExpense() {
    final String _sql = "SELECT SUM(amount) FROM cash_flow WHERE type = 'OUT'";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return __db.getInvalidationTracker().createLiveData(new String[]{"cash_flow"}, false, new Callable<Double>() {
      @Override
      public Double call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Double _result;
          if(_cursor.moveToFirst()) {
            final Double _tmp;
            if (_cursor.isNull(0)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getDouble(0);
            }
            _result = _tmp;
          } else {
            _result = null;
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
  public LiveData<Double> getTotalProfit() {
    final String _sql = "SELECT SUM(profit) FROM cash_flow WHERE type = 'IN'";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return __db.getInvalidationTracker().createLiveData(new String[]{"cash_flow"}, false, new Callable<Double>() {
      @Override
      public Double call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Double _result;
          if(_cursor.moveToFirst()) {
            final Double _tmp;
            if (_cursor.isNull(0)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getDouble(0);
            }
            _result = _tmp;
          } else {
            _result = null;
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
  public LiveData<Double> getCurrentBalance() {
    final String _sql = "SELECT SUM(CASE WHEN type = 'IN' THEN amount ELSE -amount END) FROM cash_flow";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return __db.getInvalidationTracker().createLiveData(new String[]{"cash_flow"}, false, new Callable<Double>() {
      @Override
      public Double call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Double _result;
          if(_cursor.moveToFirst()) {
            final Double _tmp;
            if (_cursor.isNull(0)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getDouble(0);
            }
            _result = _tmp;
          } else {
            _result = null;
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
  public LiveData<Double> getIncomeInRange(final long start, final long end) {
    final String _sql = "SELECT SUM(amount) FROM cash_flow WHERE type = 'IN' AND timestamp BETWEEN ? AND ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, start);
    _argIndex = 2;
    _statement.bindLong(_argIndex, end);
    return __db.getInvalidationTracker().createLiveData(new String[]{"cash_flow"}, false, new Callable<Double>() {
      @Override
      public Double call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Double _result;
          if(_cursor.moveToFirst()) {
            final Double _tmp;
            if (_cursor.isNull(0)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getDouble(0);
            }
            _result = _tmp;
          } else {
            _result = null;
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
  public LiveData<Double> getExpenseInRange(final long start, final long end) {
    final String _sql = "SELECT SUM(amount) FROM cash_flow WHERE type = 'OUT' AND timestamp BETWEEN ? AND ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, start);
    _argIndex = 2;
    _statement.bindLong(_argIndex, end);
    return __db.getInvalidationTracker().createLiveData(new String[]{"cash_flow"}, false, new Callable<Double>() {
      @Override
      public Double call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Double _result;
          if(_cursor.moveToFirst()) {
            final Double _tmp;
            if (_cursor.isNull(0)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getDouble(0);
            }
            _result = _tmp;
          } else {
            _result = null;
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
  public LiveData<Double> getProfitInRange(final long start, final long end) {
    final String _sql = "SELECT SUM(profit) FROM cash_flow WHERE type = 'IN' AND timestamp BETWEEN ? AND ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, start);
    _argIndex = 2;
    _statement.bindLong(_argIndex, end);
    return __db.getInvalidationTracker().createLiveData(new String[]{"cash_flow"}, false, new Callable<Double>() {
      @Override
      public Double call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Double _result;
          if(_cursor.moveToFirst()) {
            final Double _tmp;
            if (_cursor.isNull(0)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getDouble(0);
            }
            _result = _tmp;
          } else {
            _result = null;
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
  public LiveData<Double> getTotalStockPurchase() {
    final String _sql = "SELECT SUM(amount) FROM cash_flow WHERE type = 'OUT' AND (productId IS NOT NULL OR description LIKE 'Tambah Stok:%')";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return __db.getInvalidationTracker().createLiveData(new String[]{"cash_flow"}, false, new Callable<Double>() {
      @Override
      public Double call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Double _result;
          if(_cursor.moveToFirst()) {
            final Double _tmp;
            if (_cursor.isNull(0)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getDouble(0);
            }
            _result = _tmp;
          } else {
            _result = null;
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
  public LiveData<Double> getTotalStockPurchaseInRange(final long start, final long end) {
    final String _sql = "SELECT SUM(amount) FROM cash_flow WHERE type = 'OUT' AND (productId IS NOT NULL OR description LIKE 'Tambah Stok:%') AND timestamp BETWEEN ? AND ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, start);
    _argIndex = 2;
    _statement.bindLong(_argIndex, end);
    return __db.getInvalidationTracker().createLiveData(new String[]{"cash_flow"}, false, new Callable<Double>() {
      @Override
      public Double call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Double _result;
          if(_cursor.moveToFirst()) {
            final Double _tmp;
            if (_cursor.isNull(0)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getDouble(0);
            }
            _result = _tmp;
          } else {
            _result = null;
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
  public LiveData<List<CashFlow>> getCashFlowInRange(final long start, final long end) {
    final String _sql = "SELECT * FROM cash_flow WHERE timestamp BETWEEN ? AND ? ORDER BY timestamp ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, start);
    _argIndex = 2;
    _statement.bindLong(_argIndex, end);
    return __db.getInvalidationTracker().createLiveData(new String[]{"cash_flow"}, false, new Callable<List<CashFlow>>() {
      @Override
      public List<CashFlow> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "amount");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfProductId = CursorUtil.getColumnIndexOrThrow(_cursor, "productId");
          final int _cursorIndexOfProfit = CursorUtil.getColumnIndexOrThrow(_cursor, "profit");
          final List<CashFlow> _result = new ArrayList<CashFlow>(_cursor.getCount());
          while(_cursor.moveToNext()) {
            final CashFlow _item;
            final String _tmpType;
            if (_cursor.isNull(_cursorIndexOfType)) {
              _tmpType = null;
            } else {
              _tmpType = _cursor.getString(_cursorIndexOfType);
            }
            final double _tmpAmount;
            _tmpAmount = _cursor.getDouble(_cursorIndexOfAmount);
            final String _tmpDescription;
            if (_cursor.isNull(_cursorIndexOfDescription)) {
              _tmpDescription = null;
            } else {
              _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            }
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final Integer _tmpProductId;
            if (_cursor.isNull(_cursorIndexOfProductId)) {
              _tmpProductId = null;
            } else {
              _tmpProductId = _cursor.getInt(_cursorIndexOfProductId);
            }
            final Double _tmpProfit;
            if (_cursor.isNull(_cursorIndexOfProfit)) {
              _tmpProfit = null;
            } else {
              _tmpProfit = _cursor.getDouble(_cursorIndexOfProfit);
            }
            _item = new CashFlow(_tmpType,_tmpAmount,_tmpDescription,_tmpTimestamp,_tmpProductId,_tmpProfit);
            _item.id = _cursor.getInt(_cursorIndexOfId);
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

  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
