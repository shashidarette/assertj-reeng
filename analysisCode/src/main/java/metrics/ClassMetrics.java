package metrics;
import dependenceAnalysis.util.cfg.CFGExtractor;
import dependenceAnalysis.util.cfg.Graph;
import dependenceAnalysis.util.cfg.Node;
import structuralAnalysis.ClassDiagram;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.AnalyzerException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This class will write a single CSV file of metrics for a class.
 *
 *
 * Created by neilwalkinshaw on 24/10/2017.
 * SE146 - Extended this class to accept a directory containing .class files and generate a CSV file
 */
public class ClassMetrics {
	
	private static String rootDir = 
			"C:\\Users\\Shashi\\Documents\\MS\\SEM2\\SRE\\Assignment3\\assignment-3-shashidarette\\assertj-core\\target\\classes";
	private static String rootPackageName = "";
	
	private static String mainJarPath = 
			"C:\\Users\\Shashi\\Documents\\MS\\SEM2\\SRE\\Assignment3\\assignment-3-shashidarette\\assertj-core\\target\\assertj-core-3.9.0-SNAPSHOT.jar";

	// Class loader is used along with main jar to resolve and find required classes
	private static URLClassLoader classLoader;
	
    /**
     * First argument is the class name, e.g. /java/awt/geom/Area.class"
     * The second argument is the name of the target csv file, e.v. "classMetrics.csv"
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {        
    	List<ClassInfo> classInfoList = getClasses(-1);
    	
    	// Generates class metrics in CSV format
    	System.out.println("Class information");
    	for (ClassInfo cInfo : classInfoList) {
    		System.out.println(cInfo);
    	}
    	
    	// Generates class method metrics in CSV format
    	System.out.println("Classes methods information");
    	for (ClassInfo cInfo : classInfoList) {
    		System.out.println(cInfo.toDetailedString());
    	}
    }

    public static List<ClassInfo> getClasses(int numberOfTopClasses) throws IOException {
    	ArrayList<String> classes = new ArrayList<String>();
        
    	classes.addAll(processDirectory(new File(rootDir), rootPackageName));
    	
    	rootDir = "file:/C:/"; // + rootDir.replace("\\", "/");
    	URL[] urls = new URL[] { new URL(rootDir) }; 
    	classLoader = new URLClassLoader(urls); 
        
    	List<ClassInfo> classInfoList = new ArrayList<ClassInfo>();
    	for (String classFile : classes) {
    		classInfoList.add(getClassInfo(classFile));
    	}
    	
    	if (numberOfTopClasses > 0) {
    		// if the number of top classes
    		// get top classes sorted based on LOC
    		Collections.sort(classInfoList, new SortByLOC());
    	}
    	
    	if (numberOfTopClasses < 0 || classInfoList.size() < numberOfTopClasses) {
    		numberOfTopClasses = classInfoList.size();
		}
    	return classInfoList.subList(0, numberOfTopClasses-1);
    }
    
    // referred List<Class<?>> processDirectory() from ClassDiagram
    public static List<String> processDirectory(File directory, String pkgname) {
        ArrayList<String> classes = new ArrayList<String>();
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
                classes.add(directory.getAbsolutePath() + "\\" + fileName);
            	//classes.add(fileName);
            	//className = prefix + fileName.substring(0, fileName.length() - 6);
            	//classes.add(className);
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
     * Gets depth of inheritance of a given class
     * @param c
     * @return
     */
    private static int getDepthOfInheritance(Class c) {
    	int doi = 0;
    	
    	// continue incrementing doi until you reach object class or premitive type
    	// i.e. getSuperclass() is returned as null
    	while (c != null) {
    		c = c.getSuperclass();
    		if (c != null) {
    			doi += 1;
    		}
    	}
    	
    	return doi;
    }
    
    /**
     * Gets a class info object along with metrics for a given class file
     * @param classFile
     * @return
     * @throws IOException
     */
    private static ClassInfo getClassInfo(String classFile) throws IOException {
    	ClassInfo record;
    	
    	ClassNode cn = new ClassNode(Opcodes.ASM4);
    	classFile = classFile.substring(classFile.indexOf('\\')+1);
    	classFile = classFile.replace("\\", "/");
    	//classFile = classFile.replace(".", "/") + ".class";
        InputStream in = classLoader.getResourceAsStream(classFile); //  CFGExtractor.class.getResourceAsStream(classFile); //
        ClassReader classReader = new ClassReader(in);
        classReader.accept(cn, 0);
        
        record = new ClassInfo(cn.name);
        
        // Depth of inheritance
        Class loaded = loadClass(cn.name.replaceAll("/", "."));
        double doi = -1;
        if (loaded != null) {
        	doi = (double) getDepthOfInheritance(loaded);
        }
        record.addMetric(Metric.DepthOfInheritance, doi);
        
        int cc = 0;
        int loc = 0;
        
        for(MethodNode mn : (List<MethodNode>) cn.methods) {       	
            int numNodes = -1;
            int cyclomaticComplexity = 0; // both values default to -1 if they cannot be computed.
            try {
                Graph cfg = CFGExtractor.getCFG(cn.name, mn);
                numNodes = getNodeCount(cfg);
                cyclomaticComplexity = getCyclomaticComplexity(cfg);

            } catch (AnalyzerException e) {
                e.printStackTrace();
            }

            //Write the method details and metrics to the CSV record.
            MethodInfo method = new MethodInfo(cn.name + "." + mn.name); // + mn.desc); //Add method signature in first column.
            method.addMetric(Metric.LOC, (double) numNodes);
            method.addMetric(Metric.CC, (double) cyclomaticComplexity);
            method.addMetric(Metric.AttributeCount, (double) (mn.attrs != null ? mn.attrs.size() : 0));
            
            loc += numNodes;
            cc += cyclomaticComplexity;
            record.addMethod(method);
        }
        record.addMetric(Metric.LOC, (double) loc);
        record.addMetric(Metric.MethodCount, (double) cn.methods.size());
        record.addMetric(Metric.WMC, (double) cc);
        record.addMetric(Metric.AttributeCount, (double) (cn.attrs != null ? cn.attrs.size() : 0));
        record.addMetric(Metric.FieldCount, (double) (cn.fields != null ? cn.fields.size() : 0));
        
        return record;
    }
    
    /**
     * Returns the number of nodes in the CFG.
     * @param cfg
     * @return
     */
    private static int getNodeCount(Graph cfg){
        return cfg.getNodes().size();
    }

    /**
     * Returns the Cyclomatic Complexity by counting the number of branches and adding 1.
     * @param cfg
     * @return
     */
    private static int getCyclomaticComplexity(Graph cfg) {
        int branchCount = 0;
        for(Node n : cfg.getNodes()){
            if(cfg.getSuccessors(n).size() > 1){
                branchCount ++;
            }
        }
        return branchCount + 1;
    }
}
