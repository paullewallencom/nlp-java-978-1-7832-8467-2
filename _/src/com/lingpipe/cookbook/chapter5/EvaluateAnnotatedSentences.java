package com.lingpipe.cookbook.chapter5;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.management.RuntimeErrorException;

import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.ChunkAndCharSeq;
import com.aliasi.chunk.ChunkFactory;
import com.aliasi.chunk.Chunking;
import com.aliasi.chunk.ChunkingEvaluation;
import com.aliasi.chunk.ChunkingImpl;
import com.aliasi.sentences.IndoEuropeanSentenceModel;
import com.aliasi.sentences.SentenceChunker;
import com.aliasi.sentences.SentenceEvaluation;
import com.aliasi.sentences.SentenceEvaluator;
import com.aliasi.sentences.SentenceModel;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;
import com.aliasi.util.Files;
import com.aliasi.util.Strings;

public class EvaluateAnnotatedSentences {

	public static void main(String[] args) throws IOException {
		String annotatedSentPath = args.length > 0 ? args[0] : "data/hitchHikersGuide.sentDetected";
		char[] chars = Files.readCharsFromFile(new File(annotatedSentPath), Strings.UTF8);
		StringBuilder rawChars = new StringBuilder();
		int start = -1;
		int end = -1;
		Set<Chunk> sentChunks = new HashSet<Chunk>();
		for (int i=0; i < chars.length; ++i) {
			if (chars[i] == '[') {
				start = rawChars.length();
			}
			else if (chars[i] == ']') {
				end = rawChars.length();
				Chunk chunk = ChunkFactory.createChunk(start,end,SentenceChunker.SENTENCE_CHUNK_TYPE);
				sentChunks.add(chunk);
			}
			else {
				rawChars.append(chars[i]);
			}
		}
		String originalText = rawChars.toString();
		ChunkingImpl sentChunking = new ChunkingImpl(originalText);
		for (Chunk chunk : sentChunks) {
			sentChunking.add(chunk);
		}
		boolean eosIsSentBoundary = false;
		boolean balanceParens = true;
		SentenceModel sentenceModel 
			= new MySentenceModel();
			//= new IndoEuropeanSentenceModel(eosIsSentBoundary,balanceParens);
		TokenizerFactory tokFactory = IndoEuropeanTokenizerFactory.INSTANCE;
		SentenceChunker sentenceChunker 
			= new SentenceChunker(tokFactory,sentenceModel);	

		SentenceEvaluator evaluator = new SentenceEvaluator(sentenceChunker);
		evaluator.handle(sentChunking);
		SentenceEvaluation eval = evaluator.evaluation();
		ChunkingEvaluation chunkEval = eval.chunkingEvaluation();
		for (ChunkAndCharSeq truePos : chunkEval.truePositiveSet()) {
			System.out.println("TruePos: " + truePos);
		}
		for (ChunkAndCharSeq falsePos : chunkEval.falsePositiveSet()) {
			System.out.println("FalsePos: " + falsePos);
		}
		for (ChunkAndCharSeq falseNeg : chunkEval.falseNegativeSet()){
			System.out.println("FalseNeg: " + falseNeg);
		}		
	}
}
