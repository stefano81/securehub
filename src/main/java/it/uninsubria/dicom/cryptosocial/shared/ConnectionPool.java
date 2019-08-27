package it.uninsubria.dicom.cryptosocial.shared;

import java.sql.Connection;

public interface ConnectionPool {

	Connection getConnection() throws ConnectionPoolException;

}
