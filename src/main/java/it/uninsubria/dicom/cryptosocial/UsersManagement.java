package it.uninsubria.dicom.cryptosocial;

import it.uninsubria.dicom.cryptosocial.engine.CryptoProvider;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class UsersManagement extends HttpServlet {
	private static Logger 	logger = Logger.getLogger(UsersManagement.class.toString());
	
	private ConnectionPool	dbPool;
	private CryptoProvider crypto; 
	

	@Override
	protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		File f = new File("owls/diagnosi.owl");
		
		if (f.exists()) {
			File bkp = new File("owls/bkps/diagnosi.2011.05.06.15.47.22.owl");
			f.renameTo(bkp);
			
			f = new File("owls/diagnosi.owl");
		}
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// TODO Auto-generated method stub
		super.doPost(req, resp);
	}

	@Override
	protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
	}

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		
		dbPool = DatabasePoolImplPostgres.getInstance();
		crypto = CryptoProvider.getInstance();
	}
}
