package com.example.opsc7312;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class BudgetDao_Impl implements BudgetDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<BudgetEntity> __insertionAdapterOfBudgetEntity;

  private final EntityDeletionOrUpdateAdapter<BudgetEntity> __updateAdapterOfBudgetEntity;

  private final SharedSQLiteStatement __preparedStmtOfDeleteBudgetActionById;

  public BudgetDao_Impl(RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfBudgetEntity = new EntityInsertionAdapter<BudgetEntity>(__db) {
      @Override
      public String createQuery() {
        return "INSERT OR ABORT INTO `budget_actions` (`id`,`userId`,`accountName`,`category`,`amountBudgeted`,`amountSpent`,`actionType`,`isSynced`) VALUES (nullif(?, 0),?,?,?,?,?,?,?)";
      }

      @Override
      public void bind(SupportSQLiteStatement stmt, BudgetEntity value) {
        stmt.bindLong(1, value.getId());
        if (value.getUserId() == null) {
          stmt.bindNull(2);
        } else {
          stmt.bindString(2, value.getUserId());
        }
        if (value.getAccountName() == null) {
          stmt.bindNull(3);
        } else {
          stmt.bindString(3, value.getAccountName());
        }
        if (value.getCategory() == null) {
          stmt.bindNull(4);
        } else {
          stmt.bindString(4, value.getCategory());
        }
        stmt.bindDouble(5, value.getAmountBudgeted());
        stmt.bindDouble(6, value.getAmountSpent());
        if (value.getActionType() == null) {
          stmt.bindNull(7);
        } else {
          stmt.bindString(7, value.getActionType());
        }
        final int _tmp = value.isSynced() ? 1 : 0;
        stmt.bindLong(8, _tmp);
      }
    };
    this.__updateAdapterOfBudgetEntity = new EntityDeletionOrUpdateAdapter<BudgetEntity>(__db) {
      @Override
      public String createQuery() {
        return "UPDATE OR ABORT `budget_actions` SET `id` = ?,`userId` = ?,`accountName` = ?,`category` = ?,`amountBudgeted` = ?,`amountSpent` = ?,`actionType` = ?,`isSynced` = ? WHERE `id` = ?";
      }

      @Override
      public void bind(SupportSQLiteStatement stmt, BudgetEntity value) {
        stmt.bindLong(1, value.getId());
        if (value.getUserId() == null) {
          stmt.bindNull(2);
        } else {
          stmt.bindString(2, value.getUserId());
        }
        if (value.getAccountName() == null) {
          stmt.bindNull(3);
        } else {
          stmt.bindString(3, value.getAccountName());
        }
        if (value.getCategory() == null) {
          stmt.bindNull(4);
        } else {
          stmt.bindString(4, value.getCategory());
        }
        stmt.bindDouble(5, value.getAmountBudgeted());
        stmt.bindDouble(6, value.getAmountSpent());
        if (value.getActionType() == null) {
          stmt.bindNull(7);
        } else {
          stmt.bindString(7, value.getActionType());
        }
        final int _tmp = value.isSynced() ? 1 : 0;
        stmt.bindLong(8, _tmp);
        stmt.bindLong(9, value.getId());
      }
    };
    this.__preparedStmtOfDeleteBudgetActionById = new SharedSQLiteStatement(__db) {
      @Override
      public String createQuery() {
        final String _query = "DELETE FROM budget_actions WHERE id = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertBudgetAction(final BudgetEntity budgetEntity,
      final Continuation<? super Unit> continuation) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfBudgetEntity.insert(budgetEntity);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, continuation);
  }

  @Override
  public Object updateBudgetAction(final BudgetEntity budgetEntity,
      final Continuation<? super Unit> continuation) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfBudgetEntity.handle(budgetEntity);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, continuation);
  }

  @Override
  public void update(final BudgetEntity budgetEntity) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __updateAdapterOfBudgetEntity.handle(budgetEntity);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public Object deleteBudgetActionById(final int id,
      final Continuation<? super Unit> continuation) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteBudgetActionById.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, id);
        __db.beginTransaction();
        try {
          _stmt.executeUpdateDelete();
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
          __preparedStmtOfDeleteBudgetActionById.release(_stmt);
        }
      }
    }, continuation);
  }

  @Override
  public Object getPendingActions(final Continuation<? super List<BudgetEntity>> continuation) {
    final String _sql = "SELECT * FROM budget_actions WHERE isSynced = 0";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<BudgetEntity>>() {
      @Override
      public List<BudgetEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "userId");
          final int _cursorIndexOfAccountName = CursorUtil.getColumnIndexOrThrow(_cursor, "accountName");
          final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
          final int _cursorIndexOfAmountBudgeted = CursorUtil.getColumnIndexOrThrow(_cursor, "amountBudgeted");
          final int _cursorIndexOfAmountSpent = CursorUtil.getColumnIndexOrThrow(_cursor, "amountSpent");
          final int _cursorIndexOfActionType = CursorUtil.getColumnIndexOrThrow(_cursor, "actionType");
          final int _cursorIndexOfIsSynced = CursorUtil.getColumnIndexOrThrow(_cursor, "isSynced");
          final List<BudgetEntity> _result = new ArrayList<BudgetEntity>(_cursor.getCount());
          while(_cursor.moveToNext()) {
            final BudgetEntity _item;
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final String _tmpUserId;
            if (_cursor.isNull(_cursorIndexOfUserId)) {
              _tmpUserId = null;
            } else {
              _tmpUserId = _cursor.getString(_cursorIndexOfUserId);
            }
            final String _tmpAccountName;
            if (_cursor.isNull(_cursorIndexOfAccountName)) {
              _tmpAccountName = null;
            } else {
              _tmpAccountName = _cursor.getString(_cursorIndexOfAccountName);
            }
            final String _tmpCategory;
            if (_cursor.isNull(_cursorIndexOfCategory)) {
              _tmpCategory = null;
            } else {
              _tmpCategory = _cursor.getString(_cursorIndexOfCategory);
            }
            final double _tmpAmountBudgeted;
            _tmpAmountBudgeted = _cursor.getDouble(_cursorIndexOfAmountBudgeted);
            final double _tmpAmountSpent;
            _tmpAmountSpent = _cursor.getDouble(_cursorIndexOfAmountSpent);
            final String _tmpActionType;
            if (_cursor.isNull(_cursorIndexOfActionType)) {
              _tmpActionType = null;
            } else {
              _tmpActionType = _cursor.getString(_cursorIndexOfActionType);
            }
            final boolean _tmpIsSynced;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsSynced);
            _tmpIsSynced = _tmp != 0;
            _item = new BudgetEntity(_tmpId,_tmpUserId,_tmpAccountName,_tmpCategory,_tmpAmountBudgeted,_tmpAmountSpent,_tmpActionType,_tmpIsSynced);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, continuation);
  }

  @Override
  public List<BudgetEntity> getUnsyncedDeleteActions() {
    final String _sql = "SELECT * FROM budget_actions WHERE actionType = 'delete' AND isSynced = 0";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "userId");
      final int _cursorIndexOfAccountName = CursorUtil.getColumnIndexOrThrow(_cursor, "accountName");
      final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
      final int _cursorIndexOfAmountBudgeted = CursorUtil.getColumnIndexOrThrow(_cursor, "amountBudgeted");
      final int _cursorIndexOfAmountSpent = CursorUtil.getColumnIndexOrThrow(_cursor, "amountSpent");
      final int _cursorIndexOfActionType = CursorUtil.getColumnIndexOrThrow(_cursor, "actionType");
      final int _cursorIndexOfIsSynced = CursorUtil.getColumnIndexOrThrow(_cursor, "isSynced");
      final List<BudgetEntity> _result = new ArrayList<BudgetEntity>(_cursor.getCount());
      while(_cursor.moveToNext()) {
        final BudgetEntity _item;
        final int _tmpId;
        _tmpId = _cursor.getInt(_cursorIndexOfId);
        final String _tmpUserId;
        if (_cursor.isNull(_cursorIndexOfUserId)) {
          _tmpUserId = null;
        } else {
          _tmpUserId = _cursor.getString(_cursorIndexOfUserId);
        }
        final String _tmpAccountName;
        if (_cursor.isNull(_cursorIndexOfAccountName)) {
          _tmpAccountName = null;
        } else {
          _tmpAccountName = _cursor.getString(_cursorIndexOfAccountName);
        }
        final String _tmpCategory;
        if (_cursor.isNull(_cursorIndexOfCategory)) {
          _tmpCategory = null;
        } else {
          _tmpCategory = _cursor.getString(_cursorIndexOfCategory);
        }
        final double _tmpAmountBudgeted;
        _tmpAmountBudgeted = _cursor.getDouble(_cursorIndexOfAmountBudgeted);
        final double _tmpAmountSpent;
        _tmpAmountSpent = _cursor.getDouble(_cursorIndexOfAmountSpent);
        final String _tmpActionType;
        if (_cursor.isNull(_cursorIndexOfActionType)) {
          _tmpActionType = null;
        } else {
          _tmpActionType = _cursor.getString(_cursorIndexOfActionType);
        }
        final boolean _tmpIsSynced;
        final int _tmp;
        _tmp = _cursor.getInt(_cursorIndexOfIsSynced);
        _tmpIsSynced = _tmp != 0;
        _item = new BudgetEntity(_tmpId,_tmpUserId,_tmpAccountName,_tmpCategory,_tmpAmountBudgeted,_tmpAmountSpent,_tmpActionType,_tmpIsSynced);
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
