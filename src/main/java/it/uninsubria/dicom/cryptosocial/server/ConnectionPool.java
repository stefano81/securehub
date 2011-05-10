package it.uninsubria.dicom.cryptosocial.server;

import java.sql.Connection;

public interface ConnectionPool {
	public abstract Connection getConnection() throws ConnectionPoolException;
}