package com.fcbm.frenchquiz;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

public class MatchModel implements Parcelable {

	public static final int QUIZ_TYPE_MULTIPLE_CHOICE = 1;
	public static final int QUIZ_TYPE_SINGLE_CHOICE = 2;
	public static final int QUIZ_TYPE_MIXED_CHOICE = 3;
	
	int mNbOfQuestions = 0;
	int mMaxNbOfAlternatives = 3;
	int mCurrentQuestion = 0;
	int mTypeOfQuestions = 0;
	boolean mMixSwitchLang = false;
	String mOrgLang, mDstLang;
	QuestionModel[] mQuestions = null;
	Random mRandomGen = null;
	
	public MatchModel(Cursor c, int numberOfQuestions, String origLang, String destLang, int typeOfQuestion, boolean mixSwitchLang)
	{
		if (c == null)
			return;
		
		c.moveToFirst();
		mRandomGen = new Random( System.currentTimeMillis() );
		mTypeOfQuestions = typeOfQuestion;
		mNbOfQuestions = numberOfQuestions;
		mMixSwitchLang = mixSwitchLang;
		mOrgLang = origLang;
		mDstLang = destLang;
		mCurrentQuestion = 0;
		mQuestions = new QuestionModel[mNbOfQuestions];
	
		switch(typeOfQuestion)
		{
		case QUIZ_TYPE_SINGLE_CHOICE :
			buildSingleChoiceQuestions(c);
			break;
		case QUIZ_TYPE_MULTIPLE_CHOICE :
			buildMultipleChoiceQuestions(c);
			break;
		case QUIZ_TYPE_MIXED_CHOICE :
			buildMixedChoiceQuestions(c);
			break;
		}
	}
	
	public MatchModel(Parcel in)
	{
		mRandomGen = new Random( System.currentTimeMillis() );
		readFromParcel(in);
	}
	
	public int getNumberOfQuestion()
	{
		return mQuestions.length;
	}
	
	public void updateCurrentQuestion( boolean correct)
	{
		QuestionModel qm = getCurrentQuestion();
		qm.setCorrect(correct);
		qm.setAnswered( true );
	}
	
	public QuestionModel getQuestionAt(int index)
	{
		// TODO : handle index out of bound exception
		return mQuestions[ index ];
	}

	public QuestionModel getCurrentQuestion()
	{
		// TODO : handle index out of bound exception
		return mQuestions[ mCurrentQuestion ];
	}

	public QuestionModel getNextQuestion()
	{
		QuestionModel result = null;
		if (mCurrentQuestion < (mQuestions.length-1))
		{
			mCurrentQuestion++;
		 	result = getCurrentQuestion();
		}
		return result;
	}
	
	public void moveToFirst()
	{
		mCurrentQuestion = 0;
	}
	
	private void buildMixedChoiceQuestions(Cursor c)
	{
		
		for (int i = 0; i < mNbOfQuestions && !c.isAfterLast(); i++, c.moveToNext())
		{
			int mixedType = mRandomGen.nextInt( QUIZ_TYPE_MIXED_CHOICE );
			QuestionModel qm = null;
			if (mixedType == QUIZ_TYPE_SINGLE_CHOICE)
			{
				qm = buildSingleChoiceQuestion(c);
			}
			else
			{
				qm = buildMultipleChoiceQuestion(c);
			}
			mQuestions[i] = qm ;
		}
	}
	
	private void buildSingleChoiceQuestions(Cursor c)
	{
		for (int i = 0; i < mNbOfQuestions && !c.isAfterLast(); i++, c.moveToNext())
		{
			QuestionModel qm = buildSingleChoiceQuestion(c);
			mQuestions[i] = qm ;
		}
	}

	private QuestionModel buildSingleChoiceQuestion(Cursor c)
	{
		QuestionModel qm = new QuestionModel( readTranslationId( c) , readWord( c ), new String[] { readTranslation( c )}, 0);
		return qm;
	}
	
	private void buildMultipleChoiceQuestions(Cursor c)
	{
		for (int i = 0; i < mNbOfQuestions && !c.isAfterLast(); i++, c.moveToNext())
		{
			QuestionModel qm = buildMultipleChoiceQuestion(c);
			mQuestions[i] = qm ;
		}
	}
	
	public String[] getSummary()
	{
		String[] results = new String[mQuestions.length];
		
		for (int i = 0; i < mQuestions.length ; i++)
		{
			QuestionModel qm = mQuestions[i];
			String str = 
					qm.getOrgWord() + "|" + 
					qm.getDstWords()[qm.getCorrectIndex()] + "|" + 
					qm.isCorrect() + "|" +
					qm.getNumberOfAttempts() + "|" +
					qm.getNumberOfSuccess() + "|" ;
			results[i] = str ;
		}
		return results;
	}
	
	public int getCorrectAnswers()
	{
		int result = 0;
	
		for (QuestionModel qm : mQuestions)
		{
			if (qm.isCorrect())
				result++;
		}
		
		return result;
	}

	private QuestionModel buildMultipleChoiceQuestion(Cursor c)
	{
		int id = readTranslationId( c );
		String word = readWord( c );
		String[] translations = new String[mMaxNbOfAlternatives];
		int correctIndex = mRandomGen.nextInt( mMaxNbOfAlternatives );
		
		for (int i = 0; i < mMaxNbOfAlternatives && !c.isAfterLast(); i++, c.moveToNext())
		{
			translations[i] = readTranslation( c );
		}
		
		if (correctIndex > 0)
		{
			String tmp = translations[0];
			translations[0] = translations[correctIndex];
			translations[correctIndex] = tmp;
		}
		QuestionModel qm = new QuestionModel( id, word, translations, correctIndex);
		
		qm.setNumberOfAttempts( readNumberOfAttempts( c ));
		qm.setNumberOfSuccess( readNumberOfSuccess(c ));
		return qm;
	}
	
	//private int readGlossaryId(Cursor c)
	//{
		//return c.getInt( c.getColumnIndex( LanguagesProvider.GLOSSARY_COL_ID ));
	//}

	private int readTranslationId(Cursor c)
	{
		return c.getInt( c.getColumnIndex( LanguagesProvider.TRANS_COL_ID_CLIENT ));
	}

	private int readNumberOfSuccess(Cursor c)
	{
		return c.getInt( c.getColumnIndex( LanguagesProvider.TRANS_COL_NUMBER_OF_SUCCESS ));
	}

	private int readNumberOfAttempts(Cursor c)
	{
		return c.getInt( c.getColumnIndex( LanguagesProvider.TRANS_COL_NUMBER_OF_ATTEMTS ));
	}
	
	private String readWord(Cursor c)
	{
		return c.getString( c.getColumnIndex( LanguagesProvider.GLOSSARY_COL_WORD ));
	}

	private String readTranslation(Cursor c)
	{
		return c.getString( c.getColumnIndex( LanguagesProvider.GLOSSARY_COL_WORD_TRANSLATION ));
	}
	
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		
		sb.append("Number of questions " + mNbOfQuestions + " initialized: " + mQuestions.length + "\n");
		sb.append("TypeOfQuestions " + mTypeOfQuestions + " MixLang " + mMixSwitchLang + "\n");
		sb.append("Lang: org " + mOrgLang + " dst " + mDstLang + "\n");
		sb.append("Index " + mCurrentQuestion + "\n");
		
		for (QuestionModel qm : mQuestions)
		{
			sb.append( "Q: " + qm.toString() + "\n");
		}
		
		return sb.toString();
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(mNbOfQuestions);
		dest.writeInt(mMaxNbOfAlternatives);
		dest.writeInt(mCurrentQuestion);
		dest.writeInt(mTypeOfQuestions);
		dest.writeByte((byte) (mMixSwitchLang ? 1 : 0));
		dest.writeString(mOrgLang);
		dest.writeString(mDstLang);
		dest.writeInt(mQuestions.length);
		for (QuestionModel qm : mQuestions)
			dest.writeParcelable(qm, 0);
	}
	
	private void readFromParcel(Parcel in)
	{
		mNbOfQuestions = in.readInt();
		mMaxNbOfAlternatives = in.readInt();
		mCurrentQuestion = in.readInt();
		mTypeOfQuestions = in.readInt();
		mMixSwitchLang = (in.readByte() != 0);
		mOrgLang = in.readString();
		mDstLang = in.readString();
		int questionsSize = in.readInt();
		mQuestions = new QuestionModel[questionsSize];
		for (int i = 0; i < questionsSize; i++)
		{
			QuestionModel qm = in.readParcelable( QuestionModel.class.getClassLoader() );
			mQuestions[i] = qm;
		}
		// TODO: check this
		//mQuestions = (QuestionModel[]) in.readParcelableArray( QuestionModel.class.getClassLoader() );
	}
	
	public static final Parcelable.Creator<MatchModel> CREATOR = new Parcelable.Creator<MatchModel>() {

		@Override
		public MatchModel createFromParcel(Parcel source) {
			return new MatchModel(source);
		}

		@Override
		public MatchModel[] newArray(int size) {
			return new MatchModel[size];
		}
	};
}
