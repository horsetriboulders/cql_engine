package org.opencds.cqf.cql.engine.elm.execution;

import java.util.Iterator;

import org.opencds.cqf.cql.engine.exception.InvalidOperatorArgument;
import org.opencds.cqf.cql.engine.execution.Context;

/*
Max(argument List<Integer>) Integer
Max(argument List<Long>) Long
Max(argument List<Decimal>) Decimal
Max(argument List<Quantity>) Quantity
Max(argument List<Date>) Date
Max(argument List<DateTime>) DateTime
Max(argument List<Time>) Time
Max(argument List<String>) String

The Max operator returns the maximum element in the source. Comparison semantics are defined by the
    Comparison Operators for the type of value being aggregated.

If the source contains no non-null elements, null is returned.

If the source is null, the result is null.
*/

public class MaxEvaluator extends org.cqframework.cql.elm.execution.Max {

    public static Object max(Object source) {
        if (source == null) {
            return null;
        }

        if (source instanceof Iterable) {
            Iterable<?> element = (Iterable<?>)source;
            Iterator<?> itr = element.iterator();

            if (!itr.hasNext()) { // empty list
                return null;
            }

            Object max = itr.next();
            while (max == null && itr.hasNext()) {
                max = itr.next();
            }

            while (itr.hasNext()) {
                Object value = itr.next();

                if (value == null) { // skip null
                    continue;
                }

                Boolean greater = GreaterEvaluator.greater(value, max);
                if (greater != null && greater) {
                    max = value;
                }
            }
            return max;
        }

        throw new InvalidOperatorArgument(
            "Max(List<Integer>), Max(List<Long>, Max(List<Decimal>, Max(List<Quantity>), Max(List<Date>), Max(List<DateTime>), Max(List<Time>) or Max(List<String>))",
            String.format("Max(%s)", source.getClass().getName())
        );
    }

    @Override
    protected Object internalEvaluate(Context context) {
        Object source = getSource().evaluate(context);
        return max(source);
    }
}
