package metrics;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

/**
 * This class represents a class. It maintains methods within it
 * @author Shashidar Ette - se146
 *
 */
public class ClassInfo extends MethodInfo {
	private List<MethodInfo> methods;
	
 	public ClassInfo(String name) {
 		super(name);
 		methods = new ArrayList<MethodInfo>();
 	}
 	
 	// add method
 	public void addMethod(MethodInfo method) {
 		methods.add(method);
 	}
 	
 	// detailed CSV with method info
 	public String toDetailedString() {
 		StringJoiner joiner = new StringJoiner("\r\n");
 		for (MethodInfo method : methods) {
 			joiner.add(method.toString());
 		}
 		return joiner.toString();
 	}
 	
 	// class info with its metrics
 	public String toString() {
 		return super.toString();
 	}
}
