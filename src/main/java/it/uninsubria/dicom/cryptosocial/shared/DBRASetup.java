package it.uninsubria.dicom.cryptosocial.shared;

import it.unisa.dia.gas.crypto.jpbc.fe.hve.ip08.generators.HVEIP08KeyPairGenerator;
import it.unisa.dia.gas.crypto.jpbc.fe.hve.ip08.generators.HVEIP08ParametersGenerator;
import it.unisa.dia.gas.crypto.jpbc.fe.hve.ip08.params.HVEIP08KeyGenerationParameters;
import it.unisa.dia.gas.crypto.jpbc.fe.ibe.dip10.generators.AHIBEDIP10KeyPairGenerator;
import it.unisa.dia.gas.crypto.jpbc.fe.ibe.dip10.params.AHIBEDIP10KeyPairGenerationParameters;
import it.unisa.dia.gas.jpbc.CurveParameters;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;

import java.security.SecureRandom;

/**
 * @author Angelo De Caro (angelo.decaro@gmail.com)
 */
public class DBRASetup {

    public static DBRAKeyPairParameters setup(int hveLength, String hveCurve,
                                              int hibeLength, int hibeBitLenght) {
        HVEIP08ParametersGenerator hveParamGen = new HVEIP08ParametersGenerator();
        PairingFactory factory = PairingFactory.getInstance();
        CurveParameters curve = factory.loadCurveParameters(hveCurve);
        
        hveParamGen.init(hveLength, curve);

        HVEIP08KeyPairGenerator hveKeyPairGen = new HVEIP08KeyPairGenerator();
        hveKeyPairGen.init(new HVEIP08KeyGenerationParameters(new SecureRandom(), hveParamGen.generateParameters()));

        AsymmetricCipherKeyPair hve = hveKeyPairGen.generateKeyPair();

        AHIBEDIP10KeyPairGenerator hibeKeyPairGen = new AHIBEDIP10KeyPairGenerator();
        hibeKeyPairGen.init(new AHIBEDIP10KeyPairGenerationParameters(hibeBitLenght, hibeLength));

        AsymmetricCipherKeyPair hibe = hibeKeyPairGen.generateKeyPair();

        return new DBRAKeyPairParameters(hve, hibe);
    }

}
