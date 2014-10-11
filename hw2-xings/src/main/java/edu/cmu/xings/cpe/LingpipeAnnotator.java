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
 * LingpipeAnnotator finds the named entity Gene in CAS text using the lingpipe gene-tag model
 * @author xings
 * 
 * 
 * */

public class LingpipeAnnotator extends JCasAnnotator_ImplBase {

  /** The Chunker instance */
  private Chunker chunker;
  private static final String LINGPIPE_ID = "LingPipe";

  @Override
  /**
   * 
   * This method will initialize one instance for private variable chunker
   * , which is one of the components of Lingpipe. It also loads the Genome Model into
   * the annotator.
   * 
   * 
   */
  public void initialize(UimaContext aContext) throws ResourceInitializationException {
    try {
      chunker = (Chunker) AbstractExternalizable.readResourceObject("/ne-en-bio-genia.TokenShapeChunker");
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
   *      a JCAS that LingpipeAnnotator should process.
   */
  public void process(JCas aJCas) throws AnalysisEngineProcessException {
    // get the text and chunk it
    String text = aJCas.getDocumentText();
    Chunking chunking = chunker.chunk(text);
    Set<Chunk> chunkSet = chunking.chunkSet();
    Iterator<Chunk> it = chunkSet.iterator();
    while (it.hasNext()) {
      // store the gene in gene
      Chunk chunk = it.next();
      Gene gene = new Gene(aJCas);
      gene.setCasProcessorId(LINGPIPE_ID);
      int pos[] = getTrueSE(text, chunk.start(), chunk.end());
      gene.setStart(pos[0]);
      gene.setEnd(pos[1]);
      gene.setName(text.substring(chunk.start(), chunk.end()));
      gene.setConfidence(0.25);
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

