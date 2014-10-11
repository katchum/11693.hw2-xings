package edu.cmu.xings.cpe;

import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.Chunking;
import com.aliasi.chunk.Chunker;
import com.aliasi.chunk.ConfidenceChunker;
import com.aliasi.util.AbstractExternalizable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.cas.text.AnnotationIndex;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.jcas.tcas.Annotation;

import edu.cmu.deiis.types.Gene;

/**
 * 
 * GeneFinder Finds the named entity Gene in CAS text 
 * @author xings
 * 
 * 
 * */

public class LingpipeConfidenceAnnotator extends JCasAnnotator_ImplBase {

  /** The Chunker instance */
  private ConfidenceChunker chunker;
  private static final String LINGPIPECONF_ID = "LingPipeConfidence";
  private static final int MAX_N_BEST_CHUNKS = 5;

  @Override
  /**
   * This method will initialize one instance for private variable chunker
   * , which is one of the components of Lingpipe. It also loads the Genetag Model into
   * the annotator.
   * 
   * @param aContext
   * 
   */
  public void initialize(UimaContext aContext) throws ResourceInitializationException {
    try {
    	//need change
      chunker = (ConfidenceChunker) AbstractExternalizable.readResourceObject("/ne-en-bio-genetag.HmmChunker");
    } catch (IOException e) {
      e.printStackTrace();
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }

  }

  @Override
  /** 
   * process(JCas aJCas) will call annoGene(JCas aJCas, String text) to process JCAS with 
   * annotation of noun/phrase, adding gene annotation to them.
   * 
   * @param aJCas 
   *      a JCAS that GeneAnnotator should process.
   */
  public void process(JCas aJCas) throws AnalysisEngineProcessException {
    // get the text and chunk it
    String text = aJCas.getDocumentText();
    char[] cs = text.toCharArray();
    Iterator<Chunk> it = chunker.nBestChunks(cs,0,cs.length,MAX_N_BEST_CHUNKS);
    while (it.hasNext()) {
      // store the gene in gene
      Chunk chunk = it.next();
      double conf = Math.pow(2.0,chunk.score());
      Gene gene = new Gene(aJCas);
      gene.setCasProcessorId(LINGPIPECONF_ID);
      int pos[] = getTrueSE(text, chunk.start(), chunk.end());
      gene.setStart(pos[0]);
      gene.setEnd(pos[1]);
      gene.setName(text.substring(chunk.start(), chunk.end()));
      gene.setConfidence(0.5*conf);
      gene.addToIndexes();
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
}

