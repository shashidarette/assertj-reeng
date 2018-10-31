package metrics;

import java.util.Comparator;

/**
 * Utility Comparator class used for comparing of 2 MethodInfo object based on LOC
 * @author Shashidar Ette - se146
 *
 */
public class SortByLOC implements Comparator<MethodInfo>
{
	private final static Metric metric = Metric.LOC;
	
    public int compare(MethodInfo a, MethodInfo b)
    {
    	return (int) (b.getMetric(metric)-  a.getMetric(metric));
    }
}
