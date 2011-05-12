package it.uninsubria.dicom.cryptosocial.shared;

import it.unisa.dia.gas.crypto.jpbc.fe.hve.ip08.generators.HVEIP08KeyPairGenerator;

import javax.crypto.KeyGenerator;

public interface CriptoInterface {
	public abstract KeyGenerator getSymmetricKeyGenerator();
	public abstract HVEIP08KeyPairGenerator getKeyPairGenerator();
}
