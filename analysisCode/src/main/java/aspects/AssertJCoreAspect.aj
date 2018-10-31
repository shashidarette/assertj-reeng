package aspects;

import java.io.File;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * AssertJCoreAspect aspect
 * 
 * @author Shashidar Ette - se146 Reference: the basic idea is from Trace.java
 *         created as part of DynamicAnalysis lab of System Re-engineering
 */
public aspect AssertJCoreAspect {
	// This member variable is used to filter the methods being traced to
	// org.assertj.core package
	// To trace all function either assign it as empty or "*"
	// This can be further improved by having config.properties which could hold
	// a parameter and control the behavior
	// of the packages to be considered or filtered externally. Intentionally
	// this is kept simple for the assignment.
	private String filterPackage = "org.assertj.core.api";

	// Logger object to capture the execution trace
	private static Logger logger = Logger.getLogger("Tracing");

	// Stack-depth to compute the stack depth at any instance of a method call
	private static int stackDepth = 0;

	public AssertJCoreAspect() {
		try {
			FileHandler handler = new FileHandler("trace.csv", false);

			logger.addHandler(handler);

			handler.setFormatter(new Formatter() {
				public String format(LogRecord record) {
					return record.getMessage() + "\r\n";
				}
			});

			logHeader();
		}

		catch (Exception e) {
		}

	}

	// Header for Trace
	private void logHeader() {
		// Capturing TimeStamp, ClassName, FunctionName, Parameters of the
		// function and StackDepth
		Logger.getLogger("Tracing").log(Level.INFO, "DateTime\tClassName\tFunctionName\tParameters\tLine\tStackDepth");
	}

	// Point-cut to capture all method execution
	pointcut traceMethods() : (execution(* *(..))&& !cflow(within(AssertJCoreAspect)));

	// Increments a call or stack depth - invoked when a method is called for
	// execution
	private static void incrementDepth() {
		stackDepth++;
	}

	// Decrements a call or stack depth - invoked when a method is returned
	// after execution
	private static void decrementDepth() {
		stackDepth--;
	}

	private boolean isFilterPackage(String className) {
		boolean consider = filterPackage == null || filterPackage.isEmpty() || filterPackage.equals("*");

		if (!consider) {

			String packageName = className.substring(0, className.lastIndexOf("."));
			consider = packageName.contains(filterPackage);
		}
		return consider;
	}

	// before() pointcut to consider execution trace before getting into a
	// method call
	before(): traceMethods(){
		String className = thisJoinPointStaticPart.getSignature().getDeclaringTypeName();
		String functionName = thisJoinPointStaticPart.getSignature().getName();
		String longString = thisJoinPointStaticPart.toLongString();

		// from long string extract parameters ()
		String parameters = longString.subSequence(longString.lastIndexOf("("), longString.lastIndexOf(")")).toString();
		parameters = parameters.replace(",", " ");
		String line = thisJoinPointStaticPart.getSourceLocation().getLine() + " ";

		// consider the trace if it originates from required package
		if (isFilterPackage(className)) {
			// increment call or stack depth
			incrementDepth();

			// Log the required values of the method call
			String trace = LocalDateTime.now() + "\t" + className + "\t" + functionName + "\t" + parameters + "\t"
					+ line + "\t" + stackDepth;
			// Log the information required
			Logger.getLogger("Tracing").log(Level.INFO, trace);
		}
	}

	// after() pointcut to consider execution trace return from a method call
	after() : traceMethods(){
		String className = thisJoinPointStaticPart.getSignature().getDeclaringTypeName();

		// consider the trace if it originates from required package
		if (isFilterPackage(className)) {
			// decrement call or stack depth
			decrementDepth();
		}
	}
}
