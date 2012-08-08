package org.apache.vxquery.datamodel.accessors.nodes;

import org.apache.vxquery.datamodel.accessors.atomic.CodedQNamePointable;

import edu.uci.ics.hyracks.api.dataflow.value.ITypeTraits;
import edu.uci.ics.hyracks.data.std.api.AbstractPointable;
import edu.uci.ics.hyracks.data.std.api.IPointable;
import edu.uci.ics.hyracks.data.std.api.IPointableFactory;
import edu.uci.ics.hyracks.data.std.primitive.IntegerPointable;
import edu.uci.ics.hyracks.data.std.primitive.VoidPointable;

/*
 * Attribute {
 *  NamePtr namePtr;
 *  NamePtr typePtr?;
 *  NodeId nodeId?;
 *  TaggedValue value;
 * }
 */
public class AttributeNodePointable extends AbstractPointable {
    private static final int LOCAL_NODE_ID_SIZE = 4;
    public static final IPointableFactory FACTORY = new IPointableFactory() {
        private static final long serialVersionUID = 1L;

        @Override
        public ITypeTraits getTypeTraits() {
            return VoidPointable.TYPE_TRAITS;
        }

        @Override
        public IPointable createPointable() {
            return new AttributeNodePointable();
        }
    };

    public void getName(CodedQNamePointable name) {
        name.set(bytes, getNameOffset(), getNameSize());
    }

    public void getTypeName(NodeTreePointable nodeTree, CodedQNamePointable typeName) {
        if (nodeTree.typeExists()) {
            typeName.set(bytes, getTypeOffset(), getTypeSize(nodeTree));
        } else {
            typeName.set(null, -1, -1);
        }
    }

    public int getLocalNodeId(NodeTreePointable nodeTree) {
        return nodeTree.nodeIdExists() ? IntegerPointable.getInteger(bytes, getLocalNodeIdOffset(nodeTree)) : -1;
    }

    public void getValue(NodeTreePointable nodeTree, IPointable value) {
        value.set(bytes, getValueOffset(nodeTree), getValueSize(nodeTree));
    }

    private int getNameOffset() {
        return start;
    }

    private int getNameSize() {
        return CodedQNamePointable.SIZE;
    }

    private int getTypeOffset() {
        return getNameOffset() + getNameSize();
    }

    private int getTypeSize(NodeTreePointable nodeTree) {
        return nodeTree.typeExists() ? CodedQNamePointable.SIZE : 0;
    }

    private int getLocalNodeIdOffset(NodeTreePointable nodeTree) {
        return getTypeOffset() + getTypeSize(nodeTree);
    }

    private int getLocalNodeIdSize(NodeTreePointable nodeTree) {
        return nodeTree.nodeIdExists() ? LOCAL_NODE_ID_SIZE : 0;
    }

    private int getValueOffset(NodeTreePointable nodeTree) {
        return getLocalNodeIdOffset(nodeTree) + getLocalNodeIdSize(nodeTree);
    }

    private int getValueSize(NodeTreePointable nodeTree) {
        return length - (getValueOffset(nodeTree) - start);
    }
}