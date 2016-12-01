package inter_taint_analysis;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import soot.Body;
import soot.BodyTransformer;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.jimple.toolkits.annotation.purity.DirectedCallGraph;
import soot.jimple.toolkits.annotation.purity.SootMethodFilter;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.graph.PseudoTopologicalOrderer;
import soot.toolkits.graph.UnitGraph;

/**
 * @author Gursimran Singh
 * @rollno 2014041
 */

public class InterTaintAnalysisWrapper extends BodyTransformer {
	private SootClass class_to_analyze;
	private HashMap<String, HashMap<Integer, Boolean>> funcName_to_summary;
	
	public InterTaintAnalysisWrapper (SootClass class_to_analyze) {
		super();
		
		this.class_to_analyze = class_to_analyze;
		this.funcName_to_summary = new HashMap<>();
		build_summary();
	}
	
	@SuppressWarnings("unchecked")
	private void build_summary () {
		// Get all methods belonging to class_to_analyze
		final List<SootMethod> methodList = class_to_analyze.getMethods ();
		Iterator<SootMethod> methodListIt = methodList.iterator();
		
		// Soot Method Filter for Directed Call Graph
		SootMethodFilter methodFilter = new SootMethodFilter() {
			
			@Override
			public boolean want(SootMethod arg0) {
				if (methodList.contains (arg0) 
						&& arg0.getName().compareTo("main") != 0
							&& arg0.getName().compareTo("<init>") != 0)
					return true;
				return false;
			}
		};
		
		CallGraph callGraph = Scene.v().getCallGraph();
		DirectedCallGraph directedCallGraph = new DirectedCallGraph (callGraph,
																	 methodFilter,
																	 methodListIt,
																	 false);
		PseudoTopologicalOrderer<SootMethod> pseudoTopSorter = new PseudoTopologicalOrderer<>();
		List<SootMethod> orderedMethodList = pseudoTopSorter.newList(directedCallGraph, true);
		
		System.out.println ();
		System.out.println ("Analyzing Methods in Reverse Topological Order:");
		Iterator<SootMethod> methodIt = orderedMethodList.iterator();
		while (methodIt.hasNext()) {
			SootMethod method = methodIt.next();
			System.out.println ("Method: " + method.toString() + ", Name: " + method.getName());
			
			HashMap<Integer, Boolean> param_to_tainted = new HashMap<>();
			Body methodBody = method.retrieveActiveBody();
			UnitGraph method_CFG = new BriefUnitGraph (methodBody);
			int param_count = method.getParameterCount();
			
			if (param_count == 0) {
				IntraTaintAnalysis intraAnalysis = new IntraTaintAnalysis (method_CFG, funcName_to_summary, -1);
				param_to_tainted.put (-1, intraAnalysis.is_returnValueTainted());
			}
			else {
				for (int i = 0; i < param_count; i++) {
					IntraTaintAnalysis intraAnalysis = new IntraTaintAnalysis (method_CFG, funcName_to_summary, i);
					param_to_tainted.put (i, intraAnalysis.is_returnValueTainted());
				}
			}
			
			System.out.println ("Detailed Summary:");
			Set<Integer> keySet = param_to_tainted.keySet();
			Iterator<Integer> keyIt = keySet.iterator();
			while (keyIt.hasNext()) {
				int key = keyIt.next();
				System.out.println (key + " : " + param_to_tainted.get(key));
			}
			funcName_to_summary.put (method.getName(), param_to_tainted);
		}
		System.out.println ();
	}

	@Override
	protected void internalTransform(Body arg0, String arg1, @SuppressWarnings("rawtypes") Map arg2) {
		SootMethod sootMethod = arg0.getMethod();
		if (sootMethod.getName().compareTo("main") != 0)
			return;
		
		System.out.println ();
		System.out.println ("Analyzing Method: " + sootMethod.getName());
		UnitGraph controlFlowGraph = new BriefUnitGraph (sootMethod.getActiveBody());
		new InterTaintAnalysis (controlFlowGraph, funcName_to_summary);
	}
	
}
