package org.jetbrains.dekaf.core;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.dekaf.util.Version;

import java.io.Serializable;



/**
 * Brief connection info.
 *
 * Value object.
 *
 * @author Leonid Bushuev from JetBrains
 **/
public final class ConnectionInfo implements Serializable {

  @NotNull
  public final String rdbmsName;

  @Nullable
  public final String databaseName;

  @Nullable
  public final String schemaName;

  @Nullable
  public final String userName;

  @NotNull
  public final Version serverVersion;

  @NotNull
  public final Version driverVersion;


  public ConnectionInfo(@NotNull final String rdbmsName,
                        @Nullable final String databaseName,
                        @Nullable final String schemaName,
                        @Nullable final String userName,
                        @NotNull final Version serverVersion,
                        @NotNull final Version driverVersion) {
    this.rdbmsName = rdbmsName;
    this.databaseName = databaseName;
    this.schemaName = schemaName;
    this.userName = userName;
    this.serverVersion = serverVersion;
    this.driverVersion = driverVersion;
  }


  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    ConnectionInfo that = (ConnectionInfo) o;

    return rdbmsName.equals(that.rdbmsName)
        && !(databaseName != null ? !databaseName.equals(that.databaseName) : that.databaseName != null)
        && !(schemaName != null ? !schemaName.equals(that.schemaName) : that.schemaName != null)
        && !(userName != null ? !userName.equals(that.userName) : that.userName != null)
        && serverVersion.equals(that.serverVersion)
        && driverVersion.equals(that.driverVersion);
  }

  @Override
  public int hashCode() {
    int result = databaseName != null ? databaseName.hashCode() : 0;
    result = 31 * result + (schemaName != null ? schemaName.hashCode() : 0);
    result = 31 * result + (userName != null ? userName.hashCode() : 0);
    result = 31 * result + serverVersion.hashCode();
    result = 31 * result + rdbmsName.hashCode();
    return result;
  }
}
