package org.jetbrains.dekaf.util;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.math.BigInteger;



/**
 * @author Leonid Bushuev from JetBrains
 */
public abstract class Numbers {


  /**
   * Checks whether tow numbers have equal values (even they are instances of different classes).
   *
   * @param x  the first number.
   * @param y  the second number.
   * @return   whether they're equal.
   */
  public static boolean valuesAreEqual(@Nullable final Number x, @Nullable final Number y) {
    //noinspection NumberEquality
    if (x == y) return true;
    //noinspection SimplifiableIfStatement
    if (x == null || y == null) return false;
    return x.getClass() == y.getClass() ? x.equals(y) : x.toString().equals(y.toString());
  }


  /**
   * Converts an arbitrary number to the specified class of Number.
   * @param <N>            desired type of number.
   * @param numberClass    desired class of number.
   * @param number         the number to convert (nulls are possible).
   * @return               the converted number.
   */
  @SuppressWarnings("unchecked")
  @Contract(value = "_,!null -> !null; _,null -> null", pure = true)
  public static <N extends Number> N convertNumber(final Class<N> numberClass, final Number number) {
    if (number == null) return null;
    if (numberClass.isAssignableFrom(number.getClass())) return (N) number;

    if (numberClass == Byte.class || numberClass == byte.class) return (N) new Byte(number.byteValue());
    if (numberClass == Short.class || numberClass == short.class) return (N) new Short(number.shortValue());
    if (numberClass == Integer.class || numberClass == int.class) return (N) new Integer(number.intValue());
    if (numberClass == Long.class || numberClass == long.class) return (N) new Long(number.longValue());
    if (numberClass == Float.class || numberClass == float.class) return (N) new Float(number.floatValue());
    if (numberClass == Double.class || numberClass == double.class) return (N) new Double(number.doubleValue());
    if (numberClass == BigInteger.class) return (N) new BigInteger(number.toString());
    if (numberClass == BigDecimal.class) return (N) new BigDecimal(number.toString());

    String message =
        String.format("Unknown how to convert value (%s) of type %s to %s.",
                      number.toString(),
                      number.getClass().getCanonicalName(),
                      numberClass.getCanonicalName());
    throw new IllegalArgumentException(message);
  }

  @Contract(value = "!null -> !null; null -> null", pure = true)
  public static Number convertNumberSmartly(final BigDecimal decimal) {
    if (decimal == null) return null;
    if (decimal.equals(Numbers.DECIMAL_ZERO)) return BYTE_ZERO;

    final int precision = decimal.precision();
    final int scale = decimal.scale();

    Number num;
    if (scale == 0) {
      try {
        if (precision <= 19 && decimal.compareTo(Numbers.DECIMAL_MIN_LONG) >= 0
                            && decimal.compareTo(Numbers.DECIMAL_MAX_LONG) <= 0) {
          long v = decimal.longValueExact();
          if (-128 <= v && v <= 127) num = (byte) v;
          else if (-32768 <= v && v <= 32767) num = (short) v;
          else if (Integer.MIN_VALUE <= v && v <= Integer.MAX_VALUE) num = (int) v;
          else num = v;
        }
        else {
          num = decimal.toBigIntegerExact();
        }
      }
      catch (ArithmeticException ae) {
        num = decimal;
      }
    }
    else {
      if (precision <= 6) {
        num = decimal.floatValue();
      }
      else if (precision <= 15) {
        num = decimal.doubleValue();
      }
      else {
        num = decimal;
      }
    }

    return num;
  }

  public static int parseIntSafe(@Nullable final String str) {
    if (str == null) {
      return 0;
    }
    try {
      final String s = str.trim();
      if (s.isEmpty()) return 0;
      if (s.charAt(0) == '+') return Integer.parseInt(s.substring(1));
      return Integer.parseInt(s);
    }
    catch (NumberFormatException e) {
      return 0;
    }
  }


  public static long parseLongSafe(@Nullable final String str) {
    if (str == null) {
      return 0;
    }
    try {
      final String s = str.trim();
      if (s.isEmpty()) return 0L;
      if (s.charAt(0) == '+') return Long.parseLong(s.substring(1));
      return Long.parseLong(s);
    }
    catch (NumberFormatException e) {
      return 0;
    }
  }


  public static final Byte       BYTE_ZERO        = (byte) 0;
  public static final BigDecimal DECIMAL_ZERO     = new BigDecimal(0);
  public static final BigDecimal DECIMAL_MIN_LONG = new BigDecimal(Long.MIN_VALUE);
  public static final BigDecimal DECIMAL_MAX_LONG = new BigDecimal(Long.MAX_VALUE);

}
