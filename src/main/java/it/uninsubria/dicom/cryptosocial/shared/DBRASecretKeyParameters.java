package it.uninsubria.dicom.cryptosocial.shared;

import it.unisa.dia.gas.crypto.jpbc.fe.hve.ip08.params.HVEIP08MasterSecretKeyParameters;
import it.unisa.dia.gas.crypto.jpbc.fe.hve.ip08.params.HVEIP08Parameters;
import it.unisa.dia.gas.crypto.jpbc.fe.hve.ip08.params.HVEIP08SecretKeyParameters;
import it.unisa.dia.gas.crypto.jpbc.fe.ibe.dip10.params.AHIBEDIP10MasterSecretKeyParameters;
import it.unisa.dia.gas.crypto.jpbc.fe.ibe.dip10.params.AHIBEDIP10SecretKeyParameters;
import it.unisa.dia.gas.jpbc.CurveParameters;
import it.unisa.dia.gas.jpbc.Element;
//import it.unisa.dia.gas.plaf.jpbc.util.io.ElementObjectInput;
//import it.unisa.dia.gas.plaf.jpbc.util.io.ElementObjectOutput;
import org.bouncycastle.crypto.CipherParameters;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

//import static it.unisa.dia.gas.plaf.jpbc.util.io.ElementObjectInput.FieldType.G1;
//import static it.unisa.dia.gas.plaf.jpbc.util.io.ElementObjectInput.FieldType.Zr;

public class DBRASecretKeyParameters implements CipherParameters, Externalizable {
    protected CipherParameters hveSk;
    protected CipherParameters hibeSk;


    public DBRASecretKeyParameters() {
    }

    public DBRASecretKeyParameters(CipherParameters hveSk, CipherParameters hibeSk) {
        this.hveSk = hveSk;
        this.hibeSk = hibeSk;
    }


    public CipherParameters getHveSk() {
        return hveSk;
    }

    public CipherParameters getHibeSk() {
        return hibeSk;
    }

    public void writeExternal(ObjectOutput out) throws IOException {
       /* ElementObjectOutput eout = new ElementObjectOutput(out);

        // Write HVE key
        HVEIP08SecretKeyParameters hve = (HVEIP08SecretKeyParameters) hveSk;

        // --- write common part
        eout.writeObject(hve.getParameters().getCurveParameters());
        eout.writeElement(hve.getParameters().getG());
        eout.writeInts(hve.getParameters().getAttributeLengths());

        // --- write public key
        eout.writeBoolean(hve.isAllStar());
        if (hve.isAllStar()) {
            eout.writeElement(hve.getK());
        } else {
            eout.writeInts(hve.getPattern());
            eout.writeElements(hve.getYs());
            eout.writeElements(hve.getLs());
        }

        // Write HIBE key
        AHIBEDIP10SecretKeyParameters hibe = (AHIBEDIP10SecretKeyParameters) hibeSk;
        eout.writeObject(hibe.getCurveParameters());
        eout.writeElement(hibe.getK11());
        eout.writeElement(hibe.getK12());
        eout.writeElement(hibe.getK21());
        eout.writeElement(hibe.getK22());
        eout.writeElements(hibe.getE1s());
        eout.writeElements(hibe.getE2s());
        eout.writeElements(hibe.getIds());*/
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        /*ElementObjectInput ein = new ElementObjectInput(in);

        // Read HVE
        CurveParameters curveParameters = ein.readCurveParameters();
        Element g = ein.readElement(G1);
        int[] attributeLengths = ein.readInts();

        HVEIP08Parameters hveParameters = new HVEIP08Parameters(curveParameters, g, attributeLengths);
        HVEIP08SecretKeyParameters hve;

        boolean isAllStar = ein.readBoolean();
        if (isAllStar) {
            Element K = ein.readElement(G1);
            hve = new HVEIP08SecretKeyParameters(hveParameters, K);
        } else {
            int[] pattern = ein.readInts();
            Element[] Y = ein.readElements(G1);
            Element[] L = ein.readElements(G1);

            hve = new HVEIP08SecretKeyParameters(hveParameters, pattern, Y, L);
        }

        this.hveSk = hve;

        // Read HIBE
        curveParameters = ein.readCurveParameters();
        Element k11 = ein.readElement(G1);
        Element k12 = ein.readElement(G1);
        Element k21 = ein.readElement(G1);
        Element k22 = ein.readElement(G1);
        Element[] e1s = ein.readElements(G1);
        Element[] e2s = ein.readElements(G1);
        Element[] ids = ein.readElements(G1);

        AHIBEDIP10SecretKeyParameters hibe = new AHIBEDIP10SecretKeyParameters(curveParameters, k11, k12, e1s, k21, k22, e2s, ids);
        this.hibeSk = hibe;*/
    }
}
