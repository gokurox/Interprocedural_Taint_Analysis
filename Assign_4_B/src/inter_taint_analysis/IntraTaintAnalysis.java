package inter_taint_analysis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import soot.Body;
import soot.Local;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.jimple.AssignStmt;
import soot.jimple.InvokeExpr;
import soot.jimple.ReturnStmt;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.ArraySparseSet;
import soot.toolkits.scalar.FlowSet;
import soot.toolkits.scalar.ForwardFlowAnalysis;

/**
 * @author Gursimran Singh
 * @rollno 2014041
 */

public class IntraTaintAnalysis extends ForwardFlowAnalysis<Unit, FlowSet> {

	final private HashMap<String, HashMap<Integer, Boolean>> funcName_to_summary;
	final private Body methodBody;
	final private int taintedParameter_idx;
	private boolean returnValueTainted;
	
	public IntraTaintAnalysis (UnitGraph graph, HashMap<String, HashMap<Integer, Boolean>> funcName_to_summary, int taintedParameter_idx) {
		super(graph);
		
		this.funcName_to_summary = funcName_to_summary;
		this.methodBody = graph.getBody();
		this.taintedParameter_idx = taintedParameter_idx;
		this.returnValueTainted = false;
		
		doAnalysis();
	}
	
	public boolean is_returnValueTainted () {
		return returnValueTainted;
	}

	@Override
	protected void flowThrough(FlowSet in, Unit unit, FlowSet out) {
		// Copy inFlow to outFlow
		in.copy (out);
		
		// For return Statements (return sink)
		if (unit instanceof ReturnStmt) {
			ArrayList<String> localTainted = new ArrayList<>();
			
			Iterator<ValueBox> useIt = unit.getUseBoxes().iterator();
			while (useIt.hasNext()) {
				Value usebox = useIt.next().getValue();
				if (usebox instanceof Local) {
					if (in.contains(usebox.toString())) {
						localTainted.add (usebox.toString());
					}
				}
			}
			
			if (localTainted.size() > 0)
				returnValueTainted = returnValueTainted || true;
			
			return;
		}

		// For assignment Statements
		if (unit instanceof AssignStmt) {
			Iterator<ValueBox> defBoxesIt, useBoxesIt;
			defBoxesIt = unit.getDefBoxes().iterator();
			useBoxesIt = unit.getUseBoxes().iterator();
			
			while (defBoxesIt.hasNext()) {
				ValueBox defbox = defBoxesIt.next();
				out.remove(defbox.getValue().toString());

				boolean unitContainsInvokeExpr = false;
				while (useBoxesIt.hasNext()) {
					ValueBox usebox = useBoxesIt.next();
					Value value = usebox.getValue();
					
					if (value instanceof InvokeExpr) {
						unitContainsInvokeExpr = true;
						break;
					}
				}
				useBoxesIt = null;
				useBoxesIt = unit.getUseBoxes().iterator();
				
				boolean valueTainted = false;
				
				if (unitContainsInvokeExpr) {
					while (useBoxesIt.hasNext()) {
						ValueBox usebox = useBoxesIt.next();
						Value value = usebox.getValue();
						
						if (value instanceof InvokeExpr) {
							String functionName = ((InvokeExpr) value).getMethod().getName();
							int functionArgumentCount = ((InvokeExpr) value).getArgCount();
							List<Value> functionArguments = ((InvokeExpr) value).getArgs();
							Iterator<Value> argIt = functionArguments.iterator();
							HashMap<Integer, Boolean> pIdx_to_taint = funcName_to_summary.get (functionName);
							boolean funcReturnsTainted = false;
							int paramIdx = 0;
							
							if (pIdx_to_taint == null) {
								while (argIt.hasNext()) {
									Value arg = argIt.next();
									if (arg instanceof Local) {
										if (in.contains(arg.toString())) {
											funcReturnsTainted = true;
										}
									}
									paramIdx ++;
									assert (paramIdx <= functionArgumentCount);
								}
							}
							else {
								if (functionArgumentCount == 0) {
									funcReturnsTainted = funcReturnsTainted || pIdx_to_taint.get (-1);
								}
								else if (functionArgumentCount > 0) {
									while (argIt.hasNext()) {
										Value arg = argIt.next();
										if (arg instanceof Local) {
											if (in.contains(arg.toString())) {
												funcReturnsTainted = funcReturnsTainted || pIdx_to_taint.get (paramIdx);
											}
										}
										paramIdx ++;
										assert (paramIdx <= functionArgumentCount);
									}
								}
							}
							
							if (funcReturnsTainted)
								valueTainted = true;
						}
					}
				}
				else {
					while (useBoxesIt.hasNext()) {
						ValueBox usebox = useBoxesIt.next();
						Value value = usebox.getValue();
						
						if (value instanceof Local) {
							if (in.contains(value.toString())) {
								valueTainted = true;
							}
						}
					}
				}
				
				if (valueTainted) {
					String defValue = defbox.getValue().toString();
					out.add (defValue);
				}
			}
		}
	}

	@Override
	protected void copy(FlowSet src, FlowSet dest) {
		src.copy (dest);
	}

	@Override
	protected FlowSet entryInitialFlow() {
		ArraySparseSet entry = new ArraySparseSet();
		if (taintedParameter_idx != -1)
			entry.add (methodBody.getParameterLocal (taintedParameter_idx).toString());
		return entry;
	}

	@Override
	protected void merge(FlowSet in1, FlowSet in2, FlowSet out) {
		in1.union (in2, out);
	}

	@Override
	protected FlowSet newInitialFlow() {
		return new ArraySparseSet();
	}
}
