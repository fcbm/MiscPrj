package com.fcbm.frenchquiz;

import android.os.Parcel;
import android.os.Parcelable;

public class QuestionModel implements Parcelable {

	private int mId = 0;
	private String mOrgWord;
	private String[] mDstWords;
	private int mCorrectIndex = 0;
	private int mNumberOfAttempts = 0;
	private int mNumberOfSuccess = 0;
	private boolean mCorrect = false;
	private boolean mAnswered = false;
	
	public QuestionModel(Parcel in)
	{
		readFromParcel(in);
	}
	
	public QuestionModel(int questionId, String orgWord, String[] dstWord, int correctIndex)
	{
		mId = questionId;
		mOrgWord = orgWord;
		mDstWords = dstWord;
		mCorrectIndex = correctIndex;
	}

	public int getNumberOfAttempts() {
		return mNumberOfAttempts;
	}

	public void setNumberOfAttempts(int numberOfAttempts) {
		mNumberOfAttempts = numberOfAttempts;
	}

	public int getNumberOfSuccess() {
		return mNumberOfSuccess;
	}

	public void setNumberOfSuccess(int numberOfSuccess) {
		mNumberOfSuccess = numberOfSuccess;
	}

	public int getId() {
		return mId;
	}

	public void setId(int id) {
		mId = id;
	}

	public String getOrgWord() {
		return mOrgWord;
	}

	public void setOrgWord(String orgWord) {
		mOrgWord = orgWord;
	}

	public String[] getDstWords() {
		return mDstWords;
	}

	public void setDstWords(String[] dstWords) {
		mDstWords = dstWords;
	}

	public boolean isCorrect() {
		return mCorrect;
	}

	public void setCorrect(boolean correct) {
		mCorrect = correct;
	}
	
	public int getCorrectIndex() {
		return mCorrectIndex;
	}

	public void setCorrectIndex(int correctIndex) {
		mCorrectIndex = correctIndex;
	}
	
	public boolean isAnswered() {
		return mAnswered;
	}

	public void setAnswered(boolean answered) {
		mAnswered = answered;
	}
	
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		
		sb.append( " Id: ");
		sb.append( mId );
		sb.append( " word: ");
		sb.append( mOrgWord );
		sb.append( " alternatives: ");
		sb.append( mDstWords);
		sb.append( " cor idx: ");
		sb.append( mCorrectIndex );
		sb.append( " answered: ");
		sb.append( mAnswered );
		sb.append( " correct: ");
		sb.append( mCorrect );
		
		return sb.toString();
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	private void readFromParcel(Parcel in)
	{
		mId = in.readInt();
		mOrgWord = in.readString();
		int dstWordSize = in.readInt();
		mDstWords = new String[dstWordSize];
		in.readStringArray( mDstWords );
		mCorrectIndex = in.readInt();
		mNumberOfAttempts = in.readInt();
		mNumberOfSuccess = in.readInt();
		mCorrect = (in.readByte() != 0);
		mAnswered = (in.readByte() != 0);
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(mId);
		dest.writeString(mOrgWord);
		dest.writeInt( mDstWords.length );
		dest.writeStringArray( mDstWords );
		dest.writeInt(mCorrectIndex);
		dest.writeInt(mNumberOfAttempts);
		dest.writeInt(mNumberOfSuccess);
		dest.writeByte((byte)(mCorrect ? 1 : 0));
		dest.writeByte((byte)(mAnswered ? 1 : 0));
	}
	
	public static final Parcelable.Creator<QuestionModel> CREATOR = new Parcelable.Creator<QuestionModel>() {

		@Override
		public QuestionModel createFromParcel(Parcel source) {
			return new QuestionModel(source);
		}

		@Override
		public QuestionModel[] newArray(int size) {
			return new QuestionModel[size];
		}
	};
}
