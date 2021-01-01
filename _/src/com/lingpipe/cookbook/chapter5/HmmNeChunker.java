package com.lingpipe.cookbook.chapter5;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.Iterator;

import com.aliasi.chunk.CharLmRescoringChunker;
import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.Chunker;
import com.aliasi.chunk.NBestChunker;
import com.aliasi.chunk.RescoringChunker;
import com.aliasi.hmm.HmmDecoder;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;
import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.Strings;


public class HmmNeChunker {

	static void trainHMMChunker(String modelFilename, String trainFilename) throws IOException{
		File modelFile = new File(modelFilename);
		File trainFile = new File(trainFilename);
		int numChunkingsRescored = 64;
		int maxNgram = 12;
		int numChars = 256;
		double lmInterpolation = maxNgram; 
		TokenizerFactory factory
			= IndoEuropeanTokenizerFactory.INSTANCE;
		CharLmRescoringChunker chunkerEstimator
			= new CharLmRescoringChunker(factory,numChunkingsRescored,
				maxNgram,numChars,
				lmInterpolation);
		Conll2002ChunkTagParser parser = new Conll2002ChunkTagParser();
		parser.setHandler(chunkerEstimator);
		parser.parse(trainFile);
		AbstractExternalizable.compileTo(chunkerEstimator,modelFile);
	}

	public static void main(String[] args) throws IOException, ClassNotFoundException {
		String modelFilename = "models/Conll2002_ESP.RescoringChunker";
		String trainFilename = "data/ner/data/esp.train";
		
		File modelFile = new File(modelFilename);
		if(!modelFile.exists()){
			System.out.println("Training HMM Chunker on data from: " + trainFilename);
			trainHMMChunker(modelFilename, trainFilename);
			System.out.println("Output written to : " + modelFilename);
		}
		@SuppressWarnings("unchecked")
		RescoringChunker<CharLmRescoringChunker> chunker 
			= (RescoringChunker<CharLmRescoringChunker>) AbstractExternalizable.readObject(modelFile);
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
	    String text = "";
	    int MAX_N_BEST_CHUNKS = 10;
	    while (true) {
	    	System.out.println("Enter text, . to quit:");
	    	text = reader.readLine();
	    	
	    	if(text.equals(".")){
	    		break;
	    	}
	    	char[] cs = text.toCharArray();
	        Iterator<Chunk> it = chunker.nBestChunks(cs,0,cs.length,MAX_N_BEST_CHUNKS);
	        System.out.println(text);
	        System.out.println("Rank          Conf      Span    Type     Phrase");
	        DecimalFormat df = new DecimalFormat("0.0000");
	        for (int n = 0; it.hasNext(); ++n) {
	            Chunk chunk = it.next();
	            double conf = chunk.score();
	            int start = chunk.start();
	            int end = chunk.end();
	            String phrase = text.substring(start,end);
	            
	            System.out.println(n + " "
	            					+ "            " + df.format(conf)
	                               + "       (" + start
	                               + ", " + end
	                               + ")       " + chunk.type()
	                               + "         " + phrase);
	         }
	    	
	    }

	}






} 

