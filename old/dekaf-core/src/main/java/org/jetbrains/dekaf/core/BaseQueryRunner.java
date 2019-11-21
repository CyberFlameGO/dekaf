package org.jetbrains.dekaf.core;

import org.intellij.lang.annotations.MagicConstant;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.dekaf.intermediate.IntegralIntermediateCursor;
import org.jetbrains.dekaf.intermediate.IntegralIntermediateSeance;
import org.jetbrains.dekaf.util.Objects;

import java.lang.reflect.Array;
import java.util.Collections;



/**
 * @author Leonid Bushuev from JetBrains
 */
public class BaseQueryRunner<S> implements DBQueryRunner<S>, BaseSeanceRunner {

  @NotNull
  protected final IntegralIntermediateSeance myInterSeance;

  @NotNull
  private final ResultLayout<S> myResultLayout;

  @Nullable
  private IntegralIntermediateCursor<S> myCursor;

  /**
   * Pack size limit. 0 means unlimited.
   */
  private int myPackLimit = 0;

  private boolean myMultiPackMode = false;


  protected BaseQueryRunner(@NotNull final IntegralIntermediateSeance interSeance,
                            @NotNull final ResultLayout<S> resultLayout) {
    myInterSeance = interSeance;
    myResultLayout = resultLayout;
  }

  @NotNull
  @Override
  public synchronized BaseQueryRunner<S> withParams(final Object... params) {
    if (myCursor != null)
      throw new IllegalStateException("Cursor is open. Close (or completely fetch) it before run the query again.");

    if (params != null && params.length > 0) {
      myInterSeance.setInParameters(params);
    }

    return this;
  }

  @NotNull
  @Override
  public synchronized BaseQueryRunner<S> packBy(final int rowsPerPack) {
    endFetching();

    myPackLimit = rowsPerPack;
    myMultiPackMode = true;
    return this;
  }

  @Override
  public synchronized S run() {
    endFetching();

    myInterSeance.setPackLimit(myPackLimit);

    myInterSeance.execute();

    myCursor = myInterSeance.openCursor(0, myResultLayout);

    return fetchPack();
  }

  @Override
  public synchronized S nextPack() {
    if (myCursor != null) {
      return fetchPack();
    }
    else {
      return null;
    }
  }

  private S fetchPack() {
    assert myCursor != null;
    boolean ok = false;
    try {
      S result = myCursor.fetch();
      ok = true;
      return result != null ? result : emptyResult();
    }
    finally {
      if (!ok || !myMultiPackMode || !myCursor.hasRows()) {
        myCursor.close();
        myCursor = null;
      }
    }
  }

  @SuppressWarnings("unchecked")
  private S emptyResult() {
    switch (myResultLayout.kind) {
      case ARRAY:                return (S) Array.newInstance(myResultLayout.row.rowClass, 0);
      case ARRAY_OF_PRIMITIVES:  return (S) Array.newInstance(myResultLayout.row.rowClass, 0);
      case LIST:                 return (S) Collections.emptyList();
      case SET:                  return (S) Collections.emptySet();
      case MAP:                  return (S) Collections.emptyMap();
      default:                   return null;
    }
  }


  public synchronized void endFetching() {
    if (myCursor != null) {
      myCursor.close();
      myCursor = null;
    }
  }


  public synchronized void close() {
    endFetching();
    myInterSeance.close();
  }


  @Nullable
  @Override
  public <I> I getSpecificService(@NotNull final Class<I> serviceClass,
                                  @NotNull @MagicConstant(valuesFromClass = Names.class) final String serviceName) {
    if (serviceName.equalsIgnoreCase(Names.INTERMEDIATE_SERVICE)) {
      return Objects.castTo(serviceClass, myInterSeance);
    }
    else {
      return myInterSeance.getSpecificService(serviceClass, serviceName);
    }
  }
}
