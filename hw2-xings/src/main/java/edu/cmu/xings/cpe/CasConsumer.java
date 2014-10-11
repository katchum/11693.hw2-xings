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
 * CasConsumer outputs the result of annotation in the required format
 * @author xings
 * 
 * */
public class CasConsumer extends CasConsumer_ImplBase {
  public static final String OUTPUT = "OutputFile";
  public static final String MIXEDAE = "mixed";
  private List<String> res;
  
  /**
   * initialize() 
   */
  @Override
  public void initialize() {
    res = new ArrayList<String>();
  };

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
//  	String output = String.format("%s|%d %d|%s|%s", id.getID(), gene.getStart(), gene.getEnd(), gene.getName(), gene.getCasProcessorId());
//  	res.add(output);
    }
    // sort map by keys
    TreeMap<String, Gene> sortedmap = new TreeMap<String, Gene>(map);
    //output all the genes in the map
    Iterator<String> keyit = sortedmap.keySet().iterator();
    while(keyit.hasNext()){
    	Gene resgene = sortedmap.get(keyit.next());
    	if(resgene.getConfidence() > 0.5){
    	String output = String.format("%s|%d %d|%s", id.getID(), resgene.getStart(), resgene.getEnd(), resgene.getName());
    	res.add(output);
    	}
    }
  }

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
}
