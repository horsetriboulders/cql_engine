package org.cqframework.cql.poc.translator.expressions;

/**
 * Created by bobd on 7/24/14.
 */
public class NullLiteral extends Expression{

    public NullLiteral(){
    }

    public Object getValue(){
        return null;
    }

    @Override
    public String toCql() {
        return "null";
    }
}
