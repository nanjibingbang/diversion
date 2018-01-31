package com.liou.diversion.element.execute;

import com.liou.diversion.node.DiversionNode;

public class ExecuteContext {

    public ExecuteContext(int sign, DiversionNode diversionNode) {
        if (sign == 0 || diversionNode == null) {
            throw new IllegalArgumentException(String.format("uuid:%s, diversionNode:%s", sign, diversionNode));
        }
        this.sign = sign;
        this.diversionNode = diversionNode;
    }

    public final int sign;

    public final DiversionNode diversionNode;

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof ExecuteContext) {
            ExecuteContext other = (ExecuteContext) obj;
            return other.sign == sign && other.diversionNode.equals(diversionNode);
        }
        return super.equals(obj);
    }
}
