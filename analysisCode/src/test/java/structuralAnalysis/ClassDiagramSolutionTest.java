package structuralAnalysis;

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

/**
 * Created by neilwalkinshaw on 25/10/2017.
 */
public class ClassDiagramSolutionTest {

    File dotFile = new File("outputs"+File.separator+"classDiagram.dot");
    String rootDir = 
    		"C:\\Users\\Shashi\\Documents\\MS\\SEM2\\SRE\\Assignment3\\assignment-3-shashidarette\\assertj-core\\target\\classes";

    /**
     * If a CSVFile exists from a previous test, delete it.
     *
     * We do not clean this file up *after* running the test because you might want to inspect it /
     * analyse it with Excel etc.
     *
     */
    @Before
    public void cleanUpAndPrepare(){
        if(dotFile.exists()){
            dotFile.delete();
        }
        File outputDir = new File("outputs");
        if(!outputDir.exists()){
            outputDir.mkdir();
        }
    }

    @Test
    public void ClassDiagramTest() throws IOException {
        //ClassDiagramSolution testSubject = new ClassDiagramSolution("target/classes", false);
    	ClassDiagramSolution testSubject = new ClassDiagramSolution(rootDir, false);
        testSubject.writeDot(dotFile);
    }


}