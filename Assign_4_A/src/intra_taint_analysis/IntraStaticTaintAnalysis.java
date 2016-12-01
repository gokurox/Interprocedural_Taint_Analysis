package intra_taint_analysis;

import java.util.ArrayList;
import java.util.Iterator;

import soot.Body;
import soot.Local;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.jimple.AssignStmt;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.ReturnStmt;
import soot.tagkit.AbstractHost;
import soot.tagkit.LineNumberTag;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.ArraySparseSet;
import soot.toolkits.scalar.FlowSet;
import soot.toolkits.scalar.ForwardFlowAnalysis;

/**
 * @author Gursimran Singh
 * @rollno 2014041
 */

public class IntraStaticTaintAnalysis extends ForwardFlowAnalysis<Unit, FlowSet> {
	final private Body methodBody;
	
	public IntraStaticTaintAnalysis(UnitGraph graph) {
		super(graph);
		
		methodBody = graph.getBody();
		doAnalysis();
	}

	@SuppressWarnings({ "unchecked" })
	@Override
	protected void flowThrough(FlowSet in, Unit unit, FlowSet out) {
		LineNumberTag lineNumberTag = (LineNumberTag)((AbstractHost) unit).getTag ("LineNumberTag");
		
		// Copy inFlow to outFlow
		in.copy (out);
		
		if (LocalSettings.VERBOSE) {
			System.out.println ("Unit: " + unit);
			System.out.println ("Java_src line number: " + lineNumberTag.getLineNumber());
			System.out.println ("InFlow: " + in);
		}

		// For return Statements (return sink)
		if (unit instanceof ReturnStmt) {
			if (LocalSettings.VERBOSE) {
				System.out.println ("RETURN SINK at line " + lineNumberTag.getLineNumber() + ":");
				System.out.println ("UnitStatement: " + unit.toString());
			}
			
			ArrayList<String> potentiallyTainted = new ArrayList<>();
			ArrayList<String> localTainted = new ArrayList<>();
			
			Iterator<String> outIt = out.iterator();
			while (outIt.hasNext()) {
				String varname = outIt.next();
				if (!LocalSettings.VERBOSE && varname.charAt(0) == '$')
					continue;
				potentiallyTainted.add (varname);
			}
			
			Iterator<ValueBox> useIt = unit.getUseBoxes().iterator();
			while (useIt.hasNext()) {
				Value usebox = useIt.next().getValue();
				if (usebox instanceof Local) {
					if (potentiallyTainted.contains(usebox.toString())) {
						localTainted.add (usebox.toString());
					}
				}
			}
			
			if (LocalSettings.VERBOSE) {
				System.out.print ("Tainted Variables being returned: ");
				System.out.println (localTainted);
				System.out.print ("Potentially Tainted Variables at this point: ");
				System.out.println (potentiallyTainted);
			}
			
			if (!LocalSettings.VERBOSE) {
				System.out.println ("RETURN SINK at line " + lineNumberTag.getLineNumber() + ": Returning "
						+ (localTainted.size() > 0 ? "tainted":"untainted") + " value.");
			}
			
			if (LocalSettings.VERBOSE) {
				System.out.println ("outFlow: " + out);
				System.out.println ();
			}
			
			return;
		}

		// For print Statements (print sink)
		if (unit instanceof InvokeStmt) {
			if (unit.toString().contains("java.io.PrintStream")) {
				if (LocalSettings.VERBOSE) {
					System.out.println ("PRINT SINK at line " + lineNumberTag.getLineNumber() + ":");
					System.out.println ("UnitStatement: " + unit.toString());
				}
				
				ArrayList<String> potentiallyTainted = new ArrayList<>();
				ArrayList<String> localTainted = new ArrayList<>();
				
				Iterator<String> outIt = out.iterator();
				while (outIt.hasNext()) {
					String varname = outIt.next();
					if (!LocalSettings.VERBOSE && varname.charAt(0) == '$')
						continue;
					potentiallyTainted.add (varname);
				}
				
				Iterator<ValueBox> useIt = unit.getUseBoxes().iterator();
				while (useIt.hasNext()) {
					Value usebox = useIt.next().getValue();
					if (usebox instanceof Local) {
						if (potentiallyTainted.contains(usebox.toString())) {
							localTainted.add (usebox.toString());
						}
					}
				}
				
				if (LocalSettings.VERBOSE) {
					System.out.print ("Tainted Variables being printed: ");
					System.out.println (localTainted);
					System.out.print ("Potentially Tainted Variables at this point: ");
					System.out.println (potentiallyTainted);
				}
				
				if (!LocalSettings.VERBOSE) {
					System.out.println ("PRINT SINK at line " + lineNumberTag.getLineNumber() + ": Printing "
							+ (localTainted.size() > 0 ? "tainted":"untainted") + " value.");
				}
				
				if (LocalSettings.VERBOSE) {
					System.out.println ("outFlow: " + out);
					System.out.println ();
				}
				
				return;
			}
		}
		
		// For assignment Statements
		if (unit instanceof AssignStmt) {
			Iterator<ValueBox> defBoxesIt, useBoxesIt;
			defBoxesIt = unit.getDefBoxes().iterator();
			useBoxesIt = unit.getUseBoxes().iterator();
			
			while (defBoxesIt.hasNext()) {
				ValueBox defbox = defBoxesIt.next();
				
				if (LocalSettings.VERBOSE) {
					System.out.println ("Removing: " + defbox.getValue());
				}
				out.remove(defbox.getValue().toString());

				boolean valueTainted = false;
				while (useBoxesIt.hasNext()) {
					ValueBox usebox = useBoxesIt.next();
					Value value = usebox.getValue();
					
					if (value instanceof Local) {
						if (in.contains(value.toString())) {
							if (LocalSettings.VERBOSE) {
								System.out.println ("Tainted Var: " + value);
							}
							valueTainted = true;
						}
					}
					if (value instanceof InvokeExpr) {
						Iterator<ValueBox> invokeValuesIt = value.getUseBoxes().iterator();
						boolean parameterTainted = false;
						
						while (invokeValuesIt.hasNext()) {
							ValueBox box = invokeValuesIt.next();
							Value invokeValue = box.getValue();
							if (in.contains (invokeValue.toString())) {
								if (LocalSettings.VERBOSE) {
									System.out.println ("Tainted Parameter: " + invokeValue);
								}
								parameterTainted = true;
								valueTainted = true;
							}
						}
						
						System.out.println ("Summary of " + ((InvokeExpr) value).getMethod().getName() + " at line " + lineNumberTag.getLineNumber() + ": " 
									+ "Returns " + (parameterTainted ? "tainted":"untainted") + " value.");
					}
				}
				
				if (valueTainted) {
					String defValue = defbox.getValue().toString();
				
					if (LocalSettings.VERBOSE) {
						System.out.println ("Adding: " + defValue);
					}
					out.add (defbox.getValue().toString());
				}
			}
		}
		
		if (LocalSettings.VERBOSE) {
			System.out.println ("outFlow: " + out);
			System.out.println ();
		}
	}

	@Override
	protected void copy(FlowSet src, FlowSet dest) {
		src.copy (dest);
	}

	@Override
	protected FlowSet entryInitialFlow() {
		ArraySparseSet entry = new ArraySparseSet();
		
		int paraCount = methodBody.getMethod().getParameterCount();
		for (int i=0; i<paraCount; i++) {
			entry.add (methodBody.getParameterLocal(i).toString());
		}
		
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
