package structuralAnalysis;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

import metrics.ClassInfo;
import metrics.ClassMetrics;
import metrics.Metric;

/**
 * Created by neilwalkinshaw on 24/10/2017.
 * This solution is extended further to consider metrics derived for a class.
 * Depth of inheritance, LOC and Number of methods are used for visualizing the class in an intuitive way 
 */
public class ClassDiagramSolution {
	String mainJarPath = 
			"C:\\Users\\Shashi\\Documents\\MS\\SEM2\\SRE\\Assignment3\\assignment-3-shashidarette\\assertj-core\\target" +
					"\\assertj-core-3.9.0-SNAPSHOT.jar";

	// Class loader is used along with main jar to resolve and find required classes
	static URLClassLoader classLoader;
	
    protected Map<String,String> inheritance;
    protected Map<String,Set<String>> associations;

    //include classes in the JDK etc? Can produce crowded diagrams.
    protected boolean includeLibraryClasses = true;

    protected Set<String> allClassNames;
    
    protected Set<String> desiredClasses = new HashSet<String>();

//   hard-coded class name list
//   Arrays.asList(
//    "org.assertj.core.api.AbstractIterableAssert",
//    "org.assertj.core.internal.Arrays",
//    "org.assertj.core.internal.Iterables",
//    "org.assertj.core.api.AbstractObjectArrayAssert",
//    "org.assertj.core.api.AtomicReferenceArrayAssert",
//    "org.assertj.core.internal.Strings",
//    "org.assertj.core.internal.Objects",
//    "org.assertj.core.internal.DeepDifference",
//    "org.assertj.core.presentation.StandardRepresentation",
//    "org.assertj.core.api.AbstractDateAssert",
//    "org.assertj.core.internal.Maps",
//    "org.assertj.core.internal.Dates",
//    "org.assertj.core.internal.Classes",
//    "org.assertj.core.api.AbstractAssert",
//    "org.assertj.core.api.AbstractMapAssert",
//    "org.assertj.core.api.Assertions",
//    "org.assertj.core.api.WithAssertions",
//    "org.assertj.core.util.diff.DiffUtils",
//    "org.assertj.core.internal.Paths",
//    "org.assertj.core.api.Java6Assertions",
//    "org.assertj.core.api.Assumptions",
//    "org.assertj.core.internal.ObjectArrays",
//    "org.assertj.core.api.AbstractCharSequenceAssert",
//    "org.assertj.core.api.ListAssert",
//    "org.assertj.core.api.AbstractFloatAssert",
//    "org.assertj.core.api.AbstractDoubleAssert",
//    "org.assertj.core.api.IterableAssert",
//    "org.assertj.core.api.AbstractZonedDateTimeAssert",
//    "org.assertj.core.api.AssertionsForClassTypes",
//    "org.assertj.core.api.AbstractOffsetDateTimeAssert",
//    "org.assertj.core.api.AbstractListAssert",
//    "org.assertj.core.util.Files",
//    "org.assertj.core.api.AbstractOffsetTimeAssert",
//    "org.assertj.core.api.AbstractByteArrayAssert"));


    List<ClassInfo> desiredClassInfoList = new ArrayList<ClassInfo>();
    
    public static List<Class<?>> processDirectory(File directory, String pkgname) {

        ArrayList<Class<?>> classes = new ArrayList<Class<?>>();
        String prefix = pkgname+".";
        if(pkgname.equals(""))
            prefix = "";

        // Get the list of the files contained in the package
        String[] files = directory.list();
        for (int i = 0; i < files.length; i++) {
            String fileName = files[i];
            String className = null;

            // we are only interested in .class files
            if (fileName.endsWith(".class")) {
                // removes the .class extension
                className = prefix+fileName.substring(0, fileName.length() - 6);
            }


            if (className != null) {
                Class loaded = loadClass(className);
                if(loaded!=null)
                    classes.add(loaded);
            }

            //If the file is a directory recursively class this method.
            File subdir = new File(directory, fileName);
            if (subdir.isDirectory()) {

                classes.addAll(processDirectory(subdir, prefix + fileName));
            }
        }
        return classes;
    }

    private static Class<?> loadClass(String className) {
        try {
            return Class.forName(className);
            //return Class.forName(className, true, classLoader);
        }
        catch (ClassNotFoundException e) {
            //throw new RuntimeException("Unexpected ClassNotFoundException loading class '" + className + "'");
        	return null;
        }
        catch (Error e){
            return null;
        }
    }


    /**
     * Instantiating the class will populate the inheritance and association relations.
     * @param root
     * @throws MalformedURLException 
     */
    public ClassDiagramSolution(String root, boolean includeLibs) throws MalformedURLException{
    	
    	try {
			desiredClassInfoList = ClassMetrics.getClasses(300);
			
			for (ClassInfo cinfo : desiredClassInfoList) {
				String className = cinfo.getName().replaceAll("/", ".");
				desiredClasses.add(className);
			}
		} catch (IOException e) {
			// error occurred during class diagram
			e.printStackTrace();
		}
    	
    	File file = new File(mainJarPath);
        classLoader = URLClassLoader.newInstance(new URL[]{file.toURI().toURL()});
        
        this.includeLibraryClasses = includeLibs;
        File dir = new File(root);

        List<Class<?>> classes = processDirectory(dir,"");
        inheritance = new HashMap<String, String>();
        associations = new HashMap<String, Set<String>>();

        allClassNames = new HashSet<String>();
        for(Class cl : classes){
        	if (desiredClasses.contains(cl.getName())) {
        		allClassNames.add(cl.getName());
        	}
        }

        for(Class cl : classes){
            if(cl.isInterface())
                continue;
            if (desiredClasses.contains(cl.getName())) {
	            inheritance.put(cl.getName(),cl.getSuperclass().getName());
	            Set<String> fields = new HashSet<String>();
	            for(Field fld : cl.getDeclaredFields()){
	                //Do not want to include associations to primitive types such as ints or doubles.
	                if(!fld.getType().isPrimitive()) {
	                    fields.add(fld.getType().getName());
	                }
	            }
	            associations.put(cl.getName(),fields);
            }
        }
    }
    
    /**
     * Write out the class diagram to a specified file.
     * @param target
     */
    public void writeDot(File target) throws IOException {
        BufferedWriter fw = new BufferedWriter(new FileWriter(target));
        StringBuffer dotGraph = new StringBuffer();
        Collection<String> dotGraphClasses = new HashSet<String>(); //need this to specify that shape of each class should be a square.
        dotGraph.append("digraph classDiagram{\n"
        + "graph [splines=ortho]\n\n");

        //Add inheritance relations
        for(String childClass : inheritance.keySet()){
            String from = "\""+childClass +"\"";
            dotGraphClasses.add(from);
            String to = "\""+inheritance.get(childClass)+"\"";
            if(!includeLibraryClasses){
                if(!allClassNames.contains(inheritance.get(childClass)))
                    continue;
            }
            dotGraphClasses.add(to);
            dotGraph.append(from+ " -> "+to+"[arrowhead = onormal];\n");
        }

        //Add associations
        for(String cls : associations.keySet()){
            Set<String> fields = associations.get(cls);
            for(String field : fields) {
                String from = "\""+cls +"\"";
                dotGraphClasses.add(from);
                String to = "\""+field+"\"";
                if(!includeLibraryClasses){
                    if(!allClassNames.contains(field))
                        continue;
                }
                dotGraphClasses.add(to);
                dotGraph.append(from + " -> " +to + "[arrowhead = diamond];\n");
            }
        }

        int maxLOC = (int) desiredClassInfoList.get(0).getMetric(Metric.LOC);
        int maxMethod = (int) desiredClassInfoList.get(0).getMetric(Metric.MethodCount);
        int maxheight = 5;
        int maxWidth = 5;
        
        if (desiredClassInfoList == null || desiredClassInfoList.size() <= 0) {
	        for(String node : dotGraphClasses){
	            dotGraph.append(node+ "[shape = box];\n");
	        }
        } else {
	        for(ClassInfo node : desiredClassInfoList){
	        	// Depth of inheritance - FontSize, 
	        	// LOC - Height of Box and 
	        	// Number of methods - Width of the box
	        	// are used for visualizing the class in an intuitive way 
	        	double w = (double) (maxWidth * node.getMetric(Metric.MethodCount)) / (double) maxMethod;
	        	double h = (double) (maxheight * node.getMetric(Metric.LOC)) / (double) maxLOC;
	        	double fontsize = (node.getMetric(Metric.DepthOfInheritance) + 1) * 8.0;
	        	String className = "\"" + node.getName().replaceAll("/", ".") + "\"";
	        	dotGraph.append(className + "[shape = box"
	        	+ ", fontsize=" + fontsize 
	        	+  ", width=" + w 
	        	+ ", height=" + h + "]" + "\n");
	        }
        }
        
        dotGraph.append("}");
        fw.write(dotGraph.toString());
        fw.flush();
        fw.close();
    }


}
