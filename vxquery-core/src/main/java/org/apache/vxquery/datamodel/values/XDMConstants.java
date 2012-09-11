package org.apache.vxquery.datamodel.values;

import java.io.IOException;
import java.util.Arrays;

import org.apache.vxquery.datamodel.builders.sequence.SequenceBuilder;

import edu.uci.ics.hyracks.data.std.api.IPointable;
import edu.uci.ics.hyracks.data.std.primitive.BooleanPointable;
import edu.uci.ics.hyracks.data.std.util.ArrayBackedValueStorage;

public class XDMConstants {
    private static final byte[] BOOLEAN_TRUE_CONSTANT;

    private static final byte[] BOOLEAN_FALSE_CONSTANT;

    private static final byte[] EMPTY_SEQUENCE;

    static {
        BOOLEAN_TRUE_CONSTANT = new byte[2];
        BOOLEAN_TRUE_CONSTANT[0] = ValueTag.XS_BOOLEAN_TAG;
        BooleanPointable.setBoolean(BOOLEAN_TRUE_CONSTANT, 1, true);

        BOOLEAN_FALSE_CONSTANT = new byte[2];
        BOOLEAN_FALSE_CONSTANT[0] = ValueTag.XS_BOOLEAN_TAG;
        BooleanPointable.setBoolean(BOOLEAN_FALSE_CONSTANT, 1, false);

        ArrayBackedValueStorage abvs = new ArrayBackedValueStorage();
        SequenceBuilder sb = new SequenceBuilder();
        sb.reset(abvs);
        try {
            sb.finish();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        EMPTY_SEQUENCE = Arrays.copyOf(abvs.getByteArray(), abvs.getLength());
    }

    public static void setTrue(IPointable p) {
        set(p, BOOLEAN_TRUE_CONSTANT);
    }

    public static void setFalse(IPointable p) {
        set(p, BOOLEAN_FALSE_CONSTANT);
    }

    public static void setEmptySequence(IPointable p) {
        set(p, EMPTY_SEQUENCE);
    }

    private static void set(IPointable p, byte[] array) {
        p.set(array, 0, array.length);
    }

    private XDMConstants() {
    }
}