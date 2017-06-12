package org.opencds.cqf.cql.elm.execution;

import org.opencds.cqf.cql.execution.Context;
import org.opencds.cqf.cql.runtime.Interval;
import org.opencds.cqf.cql.runtime.Value;

/*
meets after(left Interval<T>, right Interval<T>) Boolean

The meets after operator returns true if the first interval starts immediately after the second interval ends.
If either argument is null, the result is null.
*/

/**
* Created by Chris Schuler on 6/8/2016
*/
public class MeetsAfterEvaluator extends org.cqframework.cql.elm.execution.MeetsAfter {

  public static Object meetsAfter(Interval left, Interval right) {
    if (left == null || right == null) {
      return null;
    }

    Object leftStart = left.getStart();
    Object rightEnd = right.getEnd();

    if (leftStart == null || rightEnd == null) {
      return null;
    }

    return EqualEvaluator.equal(leftStart, Value.successor(rightEnd));
  }

  @Override
  public Object evaluate(Context context) {
    Interval left = (Interval)getOperand().get(0).evaluate(context);
    Interval right = (Interval)getOperand().get(1).evaluate(context);

    return context.logTrace(this.getClass(), meetsAfter(left, right), left, right);
  }
}
