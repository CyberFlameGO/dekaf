package org.jetbrains.dekaf;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;

import static org.jetbrains.dekaf.util.Objects.notNull;



/**
 * Type of RDBMS.
 *
 * <p>
 *   Assumed that there is only one instance of Rdbms class for each RDBMS type.
 *   So, it's OK to use <b>==</b> for comparing instances.
 * </p>
 *
 * @author Leonid Bushuev from JetBrains
 */
public final class Rdbms implements Serializable {

  //// STATE \\\\


  /**
   * A unique short code that is used when
   * the RDBMS type is serialized/deserialized.
   */
  @NotNull
  public final String code;



  //// METHODS \\\\


  public static Rdbms of(@NotNull final String code) {
    String theCode = code.intern();
    Rdbms newRdbms = new Rdbms(theCode);
    Rdbms oldRdbms = RdbmsMarkersCache.cache.putIfAbsent(theCode, newRdbms);
    return notNull(oldRdbms, newRdbms);
  }


  private Rdbms(@NotNull final String code) {
    this.code = code.intern();
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Rdbms that = (Rdbms)o;

    return this.code.equals(that.code);
  }


  @Override
  public int hashCode() {
    return code.hashCode();
  }


  @Override
  public String toString() {
    return code;
  }


  //// SERIALIZATION \\\\


  @SuppressWarnings("unused")
  private Object writeReplace() {
    return new RdbmsProxy(code);
  }

}


class RdbmsProxy implements Serializable {

  private final String code;

  RdbmsProxy(final String code) {
    this.code = code;
  }

  Object readResolve() {
    return Rdbms.of(code);
  }

}


abstract class RdbmsMarkersCache {

  static final ConcurrentHashMap<String,Rdbms> cache = new ConcurrentHashMap<String,Rdbms>();

}

