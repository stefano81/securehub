package it.uninsubria.dicom.cryptosocial.shared;

import java.sql.Connection;

public interface ClientDatabase {
	public abstract Connection getConnection() throws ConnectionPoolException;
}