package metrics;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

/**
 * This class represents method of a class. It maintains method name & metrics
 * @author Shashidar Ette - se146
 *
 */
public class MethodInfo {
	// Name of the method/entity under consideration
	private String name;
	
	// Metrics of the method/entity under consideration
	protected Map<Metric, Double> metrics;
	
	public MethodInfo(String name) {
 		this.name = name;
 		metrics = new HashMap<Metric, Double>();
 	}
 	
 	public String getName() {
 		return name;
 	}
 	
 	// Add the metric if its not present
 	public void addMetric(Metric metric, Double value) {
 		metrics.putIfAbsent(metric, value);
 	}
 	
 	// get a specific metric value if present
 	public double getMetric(Metric metric) {
 		double value = 0.0;
 		if (metrics.containsKey(metric)) {
 			value = metrics.get(metric);
 		}
 		
 		return value;
 	}
 	
 	// Generate a CSV string
 	public String toString() {
 		StringJoiner joiner = new StringJoiner(",");
 		joiner.add(name);
 		if (metrics.containsKey(Metric.LOC)) {
 			joiner.add(Double.toString(metrics.get(Metric.LOC)));
 		}
 		if (metrics.containsKey(Metric.MethodCount)) {
 			joiner.add(Double.toString(metrics.get(Metric.MethodCount)));
 		}
 		if (metrics.containsKey(Metric.WMC)) {
 			joiner.add(Double.toString(metrics.get(Metric.WMC)));
 		}
 		if (metrics.containsKey(Metric.CC)) {
 			joiner.add(Double.toString(metrics.get(Metric.CC)));
 		}
 		if (metrics.containsKey(Metric.AttributeCount)) {
 			joiner.add(Double.toString(metrics.get(Metric.AttributeCount)));
 		}
 		if (metrics.containsKey(Metric.FieldCount)) {
 			joiner.add(Double.toString(metrics.get(Metric.FieldCount)));
 		}
 		if (metrics.containsKey(Metric.DepthOfInheritance)) {
 			joiner.add(Double.toString(metrics.get(Metric.DepthOfInheritance)));
 		}
 		return joiner.toString();
 	}
}
