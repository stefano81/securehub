package it.uninsubria.dicom.cryptosocial.shared;

import com.sun.org.apache.xerces.internal.impl.dv.xs.YearDV;
import it.unisa.dia.gas.crypto.jpbc.fe.hve.ip08.params.HVEIP08Parameters;
import it.unisa.dia.gas.crypto.jpbc.fe.hve.ip08.params.HVEIP08PublicKeyParameters;
import it.unisa.dia.gas.crypto.jpbc.fe.ibe.dip10.params.AHIBEDIP10PublicKeyParameters;
import it.unisa.dia.gas.jpbc.CurveParameters;
import it.unisa.dia.gas.jpbc.Element;
//import it.unisa.dia.gas.plaf.jpbc.util.io.ElementObjectInput;
//import it.unisa.dia.gas.plaf.jpbc.util.io.ElementObjectOutput;
import org.bouncycastle.crypto.CipherParameters;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.annotation.ElementType;
import java.util.ArrayList;
import java.util.List;

//import static it.unisa.dia.gas.plaf.jpbc.util.io.ElementObjectInput.FieldType.G1;
//import static it.unisa.dia.gas.plaf.jpbc.util.io.ElementObjectInput.FieldType.GT;

public class DBRAPublicKeyParameters implements CipherParameters, Externalizable {
    protected CipherParameters hvePk;
    protected CipherParameters hibePk;


    public DBRAPublicKeyParameters() {
    }

    public DBRAPublicKeyParameters(CipherParameters hvePk, CipherParameters hibePk) {
        this.hvePk = hvePk;
        this.hibePk = hibePk;
    }


    public CipherParameters getHvePk() {
        return hvePk;
    }

    public CipherParameters getHibePk() {
        return hibePk;
    }


    public void writeExternal(ObjectOutput out) throws IOException {
        /*ElementObjectOutput eout = new ElementObjectOutput(out);

        // Write HVE key
        HVEIP08PublicKeyParameters hve = (HVEIP08PublicKeyParameters) hvePk;

        // --- write common part
        eout.writeObject(hve.getParameters().getCurveParameters());
        eout.writeElement(hve.getParameters().getG());
        eout.writeInts(hve.getParameters().getAttributeLengths());

        // --- write public key
        eout.writeElement(hve.getY());

        eout.writeInt(hve.getParameters().getN());
        for (int i = 0; i < hve.getParameters().getN(); i++) {
            eout.writeInt(hve.getParameters().getAttributeNumAt(i));
            for (int j = 0; j < hve.getParameters().getAttributeNumAt(i); j++) {
                eout.writeElement(hve.getTAt(i, j));
                eout.writeElement(hve.getVAt(i, j));
            }
        }


        // Write HIBE key
        AHIBEDIP10PublicKeyParameters hibe = (AHIBEDIP10PublicKeyParameters) hibePk;
        eout.writeObject(hibe.getCurveParameters());
        eout.writeElement(hibe.getY1());
        eout.writeElement(hibe.getY3());
        eout.writeElement(hibe.getY4());
        eout.writeElement(hibe.getT());
        eout.writeElements(hibe.getUs());
        eout.writeElement(hibe.getOmega());*/
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        /*ElementObjectInput ein = new ElementObjectInput(in);

        // Read HVE
        CurveParameters curveParameters = ein.readCurveParameters();
        Element g = ein.readElement(G1);
        int[] attributeLengths = ein.readInts();

        HVEIP08Parameters hveParameters = new HVEIP08Parameters(curveParameters, g, attributeLengths);

        Element Y = ein.readElement(GT);

        List<List<Element>> Ts = new ArrayList<List<Element>>();
        List<List<Element>> Vs = new ArrayList<List<Element>>();

        int n = ein.readInt();
        for (int i = 0; i < n; i++) {
            int attributeNum = ein.readInt();
            List<Element> tList = new ArrayList<Element>(attributeNum);
            List<Element> vList = new ArrayList<Element>(attributeNum);

            for (int j = 0; j < attributeNum; j++) {
                tList.add(ein.readElement(G1));
                vList.add(ein.readElement(G1));
            }

            Ts.add(tList);
            Vs.add(vList);
        }

        HVEIP08PublicKeyParameters hve = new HVEIP08PublicKeyParameters(hveParameters, Y, Ts, Vs);
        this.hvePk = hve;

        // Read HIBE
        curveParameters = ein.readCurveParameters();
        Element Y1 = ein.readElement(G1);
        Element Y3 = ein.readElement(G1);
        Element Y4 = ein.readElement(G1);
        Element T = ein.readElement(G1);
        Element[] Us = ein.readElements(G1);
        Element Omega = ein.readElement(GT);

        AHIBEDIP10PublicKeyParameters hibe = new AHIBEDIP10PublicKeyParameters(curveParameters, Y1, Y3, Y4, T, Us, Omega);
        this.hibePk = hibe;*/
    }
}
