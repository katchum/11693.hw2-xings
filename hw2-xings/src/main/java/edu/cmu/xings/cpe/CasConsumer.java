package edu.cmu.xings.cpe;

import org.apache.uima.util.ProcessTrace;
import org.apache.uima.cas.CAS;
import org.apache.uima.collection.CasConsumer_ImplBase;
import org.apache.uima.resource.ResourceProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;

import java.io.FileWriter;

import edu.cmu.deiis.types.Gene;
import edu.cmu.deiis.types.ID;

import org.apache.uima.cas.FSIterator;
import org.apache.uima.cas.CASException;

import java.io.File;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * 
 * CasConsumer combines the annotations produced by all member of the aggregated AE, outputs the result of annotation in the required format
 * @author xings
 * 
 * */
public class CasConsumer extends CasConsumer_ImplBase {
	
	/**
	 * 
	 * set CasProcessorId as mixed
	 * set the output file
	 * initiate a variable res that stores the result in all the JCas
	 * 
	 */
  public static final String OUTPUT = "OutputFile";
  public static final String MIXEDAE = "mixed";
  private List<String> res;
  
  /**
   * 
   * initialize() produces a new arraylist of string which stores the result
   * 
   */
  @Override
  public void initialize() {
    res = new ArrayList<String>();
  };
  /**
   * processCas(CAS) evaluates all the Gene annotation in the aCas and decide if it is a gene mention. 
   * It stores the gene mention and their ID in (ArrayList<String>)res in the required format of the homework
   *    @param aJCas 
   *      a JCAS that CasConsumer should process.
   */
  @Override
  public void processCas(CAS aCAS) throws ResourceProcessException {
    JCas jcas;
    try {
      jcas = aCAS.getJCas();
    } catch (CASException e) {
      throw new ResourceProcessException(e);
    }
    Iterator<Annotation> idIt = jcas.getAnnotationIndex(ID.type).iterator();
    Iterator<Annotation> geneIt = jcas.getAnnotationIndex(Gene.type).iterator();
    ID id = (ID) idIt.next();
    Map<String, Gene> map = new HashMap<String, Gene>();
    
    while (geneIt.hasNext()) {
      Gene gene = (Gene) geneIt.next();
      String offset = String.format("%d %d", gene.getStart(), gene.getEnd());
      if (map.containsKey(offset)){
    	  gene.setConfidence(gene.getConfidence()+map.get(offset).getConfidence());
    	  gene.setCasProcessorId(MIXEDAE);
    	  map.put(offset, gene);
      }else{
    	  map.put(offset, gene);
      }
    }
    // sort map by keys
    TreeMap<String, Gene> sortedmap = new TreeMap<String, Gene>(map);
    //output all the genes in the map
    Iterator<String> keyit = sortedmap.keySet().iterator();
    while(keyit.hasNext()){
    	Gene resgene = sortedmap.get(keyit.next());
    	if(resgene.getConfidence() > 0.4){
    	String output = String.format("%s|%d %d|%s", id.getID(), resgene.getStart(), resgene.getEnd(), resgene.getName());
    	res.add(output);
    	}
    }
  }

  /**
   * 
   * collectionProcessComplete(ProcessTrace) outputs the content in (ArrayList<String>)res to the output file
   *	@param arg0
   *		the process trace
   *
   */
  public void collectionProcessComplete(ProcessTrace arg0) throws IOException,
          UnsupportedEncodingException {
    File oFile = new File((String) getConfigParameterValue(OUTPUT));
    try {
      if (!oFile.exists())
        oFile.createNewFile();
    } catch (IOException e) {
      System.exit(1);
    }
    PrintWriter writer = new PrintWriter(oFile, "UTF-8");
    for (String geneString : res) {
      writer.println(geneString);
    }
    writer.close();
  }
  
  /**
   * 
   * @param text is the text in the JCas
   * @param start is the starting point of a Gene
   * @param end is the ending point of a Gene
   * this function returns the offset of the Gene without space. Which is required in hw2.
   * 
   */
  public int[] getTrueSE(String text, int start, int end) {
	    int whitespaceCount = 0, pointer = 0;
	    int[] pos = new int[2];
	    for (; pointer < start; pointer++) {
	      if (text.charAt(pointer) == ' ')
	        whitespaceCount++;
	    }
	    pos[0] = start - whitespaceCount;

	    for (; pointer < end; pointer++) {
	      if (text.charAt(pointer) == ' ')
	        whitespaceCount++;
	    }
	    pos[1] = end - whitespaceCount - 1;
	    return pos;
	  }
}

