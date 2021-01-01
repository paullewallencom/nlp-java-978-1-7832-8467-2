package com.lingpipe.cookbook.chapter7;

import com.aliasi.chunk.Chunk;

class CorefChunk implements Chunk {
	String mType;
	int mId;
	int mStart;
	int mEnd;

	public CorefChunk(int id, String type, int start, int end) {
		mType = type;
		mId = id;
		mStart = start;
		mEnd = end;
	}
	@Override
	public int start() {
		// TODO Auto-generated method stub
		return mStart;
	}
	@Override
	public int end() {
		// TODO Auto-generated method stub
		return mEnd;
	}
	@Override
	public String type() {
		// TODO Auto-generated method stub
		return mType;
	}
	@Override
	public double score() {
		// TODO Auto-generated method stub
		return 0.0d;				
	}
	@Override
	public String toString() {
		return mType + " " + mId + " " + mStart + " " + mEnd + " ";
	}
}