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
import java.util.Iterator;
import java.util.List;

/**
 * 
 * 
 * CasConsumer outputs the result of annotation in the required format
 * @author xings
 * 
 * */
public class CasConsumer extends CasConsumer_ImplBase {
  public static final String PARAM_OUTPUTFILE = "OutputFile";

  private List<String> res;

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
    String text = jcas.getDocumentText();
    Iterator<Annotation> idIt = jcas.getAnnotationIndex(ID.type).iterator();
    Iterator<Annotation> geneIt = jcas.getAnnotationIndex(Gene.type).iterator();
    ID id = (ID) idIt.next();
    while (geneIt.hasNext()) {
      Gene gene = (Gene) geneIt.next();
      int[] pos = getTrueSE(text, gene.getStart(), gene.getEnd());
      String output = String.format("%s|%d %d|%s", id.getID(), pos[0], pos[1], gene.getName());
      res.add(output);
    }
  }

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

  public void collectionProcessComplete(ProcessTrace arg0) throws IOException,
          UnsupportedEncodingException {
    File oFile = new File((String) getConfigParameterValue(PARAM_OUTPUTFILE));
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
