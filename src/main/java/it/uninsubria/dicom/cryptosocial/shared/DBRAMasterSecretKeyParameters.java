package it.uninsubria.dicom.cryptosocial.shared;

import it.unisa.dia.gas.crypto.jpbc.fe.hve.ip08.params.HVEIP08MasterSecretKeyParameters;
import it.unisa.dia.gas.crypto.jpbc.fe.hve.ip08.params.HVEIP08Parameters;
import it.unisa.dia.gas.crypto.jpbc.fe.hve.ip08.params.HVEIP08PublicKeyParameters;
import it.unisa.dia.gas.crypto.jpbc.fe.ibe.dip10.params.AHIBEDIP10MasterSecretKeyParameters;
import it.unisa.dia.gas.crypto.jpbc.fe.ibe.dip10.params.AHIBEDIP10PublicKeyParameters;
import it.unisa.dia.gas.jpbc.CurveParameters;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.plaf.jpbc.util.io.ElementObjectInput;
import it.unisa.dia.gas.plaf.jpbc.util.io.ElementObjectOutput;
import org.bouncycastle.crypto.CipherParameters;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.List;

import static it.unisa.dia.gas.plaf.jpbc.util.io.ElementObjectInput.FieldType.G1;
import static it.unisa.dia.gas.plaf.jpbc.util.io.ElementObjectInput.FieldType.Zr;

public class DBRAMasterSecretKeyParameters implements CipherParameters, Externalizable {
    protected CipherParameters hveSk;
    protected CipherParameters hibeSk;


    public DBRAMasterSecretKeyParameters() {
    }

    public DBRAMasterSecretKeyParameters(CipherParameters hveSk, CipherParameters hibeSk) {
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
        ElementObjectOutput eout = new ElementObjectOutput(out);

        // Write HVE key
        HVEIP08MasterSecretKeyParameters hve = (HVEIP08MasterSecretKeyParameters) hveSk;

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
        AHIBEDIP10MasterSecretKeyParameters hibe = (AHIBEDIP10MasterSecretKeyParameters) hibeSk;
        eout.writeObject(hibe.getCurveParameters());
        eout.writeElement(hibe.getX1());
        eout.writeElement(hibe.getAlpha());
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        ElementObjectInput ein = new ElementObjectInput(in);

        // Read HVE
        CurveParameters curveParameters = ein.readCurveParameters();
        Element g = ein.readElement(G1);
        int[] attributeLengths = ein.readInts();

        HVEIP08Parameters hveParameters = new HVEIP08Parameters(curveParameters, g, attributeLengths);

        Element Y = ein.readElement(Zr);
        List<List<Element>> Ts = new ArrayList<List<Element>>();
        List<List<Element>> Vs = new ArrayList<List<Element>>();

        int n = ein.readInt();
        for (int i = 0; i < n; i++) {
            int attributeNum = ein.readInt();
            List<Element> tList = new ArrayList<Element>(attributeNum);
            List<Element> vList = new ArrayList<Element>(attributeNum);

            for (int j = 0; j < attributeNum; j++) {
                tList.add(ein.readElement(Zr));
                vList.add(ein.readElement(Zr));
            }

            Ts.add(tList);
            Vs.add(vList);
        }

        HVEIP08MasterSecretKeyParameters hve = new HVEIP08MasterSecretKeyParameters(hveParameters, Y, Ts, Vs);
        this.hveSk = hve;

        // Read HIBE
        curveParameters = ein.readCurveParameters();
        Element X1 = ein.readElement(G1);
        Element alpha = ein.readElement(Zr);

        AHIBEDIP10MasterSecretKeyParameters hibe = new AHIBEDIP10MasterSecretKeyParameters(curveParameters, X1, alpha);
        this.hibeSk = hibe;
    }
}
