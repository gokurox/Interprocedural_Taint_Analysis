package inter_taint_analysis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import soot.Pack;
import soot.PackManager;
import soot.Scene;
import soot.SootClass;
import soot.Transform;
import soot.options.Options;

/**
 * @author Gursimran Singh
 * @rollno 2014041
 */

public class DriverClass {
		final private static String[] TESTFILES = {
										"TestFiles.TC4" 
									};

	public static void main(String[] args) throws InterruptedException {
		/* 
		 * Set Command line Options
		 */
		// Output Format = JIMPLE
		Options.v().set_output_format (Options.output_format_jimple);
		// Keep original variable names
		Options.v().setPhaseOption ("jb", "use-original-names:true");
		// Keep a mapping to Java Source Code Line Numbers
		Options.v().set_keep_line_number (true);
		// Whole Program Mode (-w)
		Options.v().set_whole_program (true);
		// Call Graph
		Options.v().setPhaseOption ("cg", "enabled:true,verbose:false");
		// Use Class Hierarchy Analysis to generate Call Graph
		// Options.v().setPhaseOption ("cg.spark", "enabled:true,verbose:false");
		// Use Spark to generate Call Graph
		Options.v().setPhaseOption ("cg.spark", "enabled:true,verbose:false");

		String INPUT_FILENAME;
		
		if (args.length > 0) {
			INPUT_FILENAME = args[0];
		}
		else if (TESTFILES.length > 0) {
			INPUT_FILENAME = TESTFILES[0];
		}
		else {
			System.err.println ("No input arguments provided ...");
			System.err.println ("Use either:");
			System.err.println ("\t" + "1. command line args");
			System.err.println ("\t" + "2. static field TESTFILES");
			return;
		}
		
		SootClass class_to_analyze = Scene.v().loadClassAndSupport(INPUT_FILENAME);
		Scene.v().loadBasicClasses();
		Scene.v().loadNecessaryClasses();
		Scene.v().loadDynamicClasses();

		// Set main-class as the class-to-analyze
		Options.v().set_main_class (class_to_analyze.getName());

		PackManager.v().runPacks();

		Pack jtp = PackManager.v().getPack ("jtp");
		jtp.add (new Transform ("jtp.instrumenter", new InterTaintAnalysisWrapper (class_to_analyze)));

		args = new String[] {};
		List<String> argsList = new ArrayList<String> (Arrays.asList(args));
		argsList.addAll (Arrays.asList (new String[]{
				class_to_analyze.getName()		// argument-class
		}));
		args = argsList.toArray (new String[0]);
		
		// Disable Call Graph
		Options.v().setPhaseOption ("cg", "enabled:false");
		// Disable Spark Call Graph
		Options.v().setPhaseOption ("cg.spark", "enabled:false");
		soot.Main.main(args);

		/*if (args.length > 0) {
			soot.Main.main (args);
		}
		else if (TESTFILES.length > 0) {
			soot.Main.main (TESTFILES);
		}
		else {
			System.err.println ("No input arguments provided ...");
			System.err.println ("Use either:");
			System.err.println ("\t" + "1. command line args");
			System.err.println ("\t" + "2. static field TESTFILES");
			return;
		}*/
	}
}
