package edu.cmu.xings.cpe;

import edu.cmu.deiis.types.Gene;

import java.io.File;
import java.io.IOException;

import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.UimaContext;
import org.apache.uima.cas.FSIterator;

import abner.Tagger;

public class AbnerAnnotator extends JCasAnnotator_ImplBase {
	private static final String TYPE_DNA = "DNA";
	private static final String ABNER_ID = "abner";
	
	Tagger tagger = null;
	
	public void initialize(UimaContext aContext){
		this.tagger = new Tagger();
    }

	
	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
	    String text = aJCas.getDocumentText();
	    String textNoSpaces = text.replaceAll("\\s", "");
	    String[] entities = tagger.getEntities(text, TYPE_DNA);
	    for (String entity : entities) {
	      String entityNoSpaces = entity.replaceAll("\\s", "");
	      Gene gene = new Gene(aJCas);
	      gene.setCasProcessorId(ABNER_ID);
	      int startIndex = textNoSpaces.indexOf(entityNoSpaces);
	      int endIndex = startIndex + entityNoSpaces.length() - 1;
	      gene.setStart(startIndex);
	      gene.setEnd(endIndex);
	      gene.setName(entity);
	      gene.setConfidence(0.25);
	      gene.addToIndexes();
	    }
	}

}
