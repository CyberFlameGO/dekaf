package org.jetbrains.dekaf.util;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;



/**
 * Useful string functions.
 *
 * @author Leonid Bushuev from JetBrains
 */
@SuppressWarnings("SpellCheckingInspection")
public abstract class Strings {

  @SuppressWarnings({"StringEquality", "SimplifiableIfStatement"})
  public static boolean eq(final String str1, final String str2) {
    if (str1 == str2) return true;
    if (str1 == null || str2 == null) return false;
    return str1.equals(str2);
  }

  @SuppressWarnings({"StringEquality", "SimplifiableIfStatement"})
  public static boolean eq(final String str1, final String str2, final boolean caseSensitive) {
    if (str1 == str2) return true;
    if (str1 == null || str2 == null) return false;
    return caseSensitive ? str1.equals(str2) : str1.equalsIgnoreCase(str2);
  }


  @NotNull
  public static String ensureStartsWith(@NotNull final String str, final char c) {
    if (str.length() == 0) {
      return Character.toString(c);
    }
    else {
      char c1 = str.charAt(0);
      if (c1 == c) return str;
      else return c + str;
    }
  }


  @NotNull
  public static String ensureEndsWith(@NotNull final String str, final char c) {
    final int n = str.length();
    if (n > 0 && str.charAt(n - 1) == c) {
      return str;
    }
    else {
      return str + c;
    }
  }


  @NotNull
  public static String removeEnding(@NotNull final String str, @NotNull final String ending) {
    int n = str.length(),
        m = ending.length();
    if (n > m && str.endsWith(ending)) return str.substring(0, n-m);
    else return str;
  }


  @NotNull
  public static String rtrim(@NotNull final String str) {
    int n = str.length();
    int k = n;
    while (k > 0) {
      if (Character.isWhitespace(str.charAt(k - 1))) {
        k--;
      }
      else {
        break;
      }
    }

    if (k == n) {
      return str;
    }
    else {
      return str.substring(0, k);
    }
  }


  @NotNull
  public static String replace(@NotNull final String where, @NotNull final String what, @NotNull final String with) {
    // TODO make my own implementation, because Java's one is so slow
    return where.replace(what, with);
  }


  @NotNull
  public static String repeat(@NotNull final String what, @Nullable final String delimiter, int count) {
    if (count == 0) return "";
    if (count == 1) return what;

    StringBuilder b = new StringBuilder((what.length()) + (delimiter == null ? 0 : delimiter.length()) * count);
    b.append(what);
    for (int i = 1; i < count; i++) {
      if (delimiter != null) b.append(delimiter);
      b.append(what);
    }

    return b.toString();
  }


  /**
   * Checks that the given string matches the specified pattern.
   *
   * <p>
   *   Nulls are acceptable, but for nulls it alwais returns false.
   * </p>
   *
   * @param string   the string to check (nulls are acceptable).
   * @param pattern  the pattern.
   * @return         whether matches.
   */
  @Contract("null,_->false")
  public static boolean matches(@Nullable final CharSequence string, @NotNull final Pattern pattern) {
    if (string == null) return false;
    return pattern.matcher(string).matches();
  }

  /**
   * Checks that the given string matches the specified pattern.
   *
   * <p>
   *   Nulls are acceptable, but for nulls it alwais returns false.
   * </p>
   *
   * @param string   the string to check (nulls are acceptable).
   * @param pattern  the pattern.
   * @param caseSensitive  check case sensitively.
   * @return         whether matches.
   */
  @Contract("null,_,_->false")
  public static boolean matches(@Nullable final CharSequence string, @NotNull final String pattern, boolean caseSensitive) {
    if (string == null) return false;
    Pattern p = Pattern.compile(pattern, caseSensitive ? 0 : Pattern.CASE_INSENSITIVE);
    return matches(string, p);
  }


  /**
   * Converts the given string to the upper case.
   * Supports nulls.
   * @param string a string to convert, nulls are allowed.
   * @return  the given string in upper case, or null.
   * @see #lower
   */
  @Contract("!null->!null; null->null")
  public static String upper(@Nullable final String string) {
    return string == null ? null : string.toUpperCase();
  }


  /**
   * Converts the given string to the lower case.
   * Supports nulls.
   * @param string a string to convert, nulls are allowed.
   * @return  the given string in lower case, or null.
   * @see #upper
   */
  @Contract("!null->!null; null->null")
  public static String lower(@Nullable final String string) {
    return string == null ? null : string.toLowerCase();
  }


  @Contract("!null->!null; null->null")
  public static String minimizeSpaces(@Nullable final String string) {
    if (string == null) return null;
    String s = string.trim();
    if (s.isEmpty()) return "";

    Matcher m = SEVERAL_SPACES.matcher(s);
    return m.replaceAll(" ");
  }

  private static final Pattern SEVERAL_SPACES =
          Pattern.compile("[ \\s\\t\\r\\n]{2,}");

  /**
   * Returns the last part of the string. Parts deleimited with the specificed <tt>delimiter</tt>.
   * @param string     the string.
   * @param delimiter  the parts delimiter.
   * @return           the last part;
   *                   or the whole string if the <tt>delimiter</tt> doesn't present in the string.
   */
  @Contract("!null,_->!null; null,_->null")
  public static String lastPart(@Nullable final String string, final char delimiter) {
    if (string == null) return null;

    int pos = string.lastIndexOf(delimiter);
    if (pos >= 0) {
      return string.substring(pos+1);
    }
    else {
      return string;
    }
  }

}
