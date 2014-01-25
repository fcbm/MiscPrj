package com.fcbm.frenchquiz;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;

public class TermsCreatorUtil {

	private static ContentValues buildContentValue(String origWord, String dstWord, String origLang, String dstLang)
	{
		ContentValues cv = new ContentValues();
		cv.put( LanguagesProvider.GLOSSARY_COL_WORD, origWord);
		cv.put( LanguagesProvider.GLOSSARY_COL_LANG, origLang);
		cv.put( LanguagesProvider.TRANS_COL_DEST_LANG, dstLang);
		cv.put( LanguagesProvider.TRANS_COL_DEST_WORDS, dstWord);
		return cv;
	}
	
	public static List<ContentValues> buildTerms()
	{
		List<ContentValues> cvGlossary = new ArrayList<ContentValues>();

		cvGlossary.add( buildContentValue("femme", "moglie,donna", "fr", "it") );
		/*cvGlossary.add( buildContentValue("bonjour", "buongiorno", "fr", "it") );
		cvGlossary.add( buildContentValue("bonsoire", "buonasera", "fr", "it") );*/
		cvGlossary.add( buildContentValue("beaucoup", "molto", "fr", "it") );
		/*cvGlossary.add( buildContentValue("tot", "presto", "fr", "it") );
		cvGlossary.add( buildContentValue("salut", "ciao", "fr", "it") );

		cvGlossary.add( buildContentValue("toit", "tetto", "fr", "it") );
		cvGlossary.add( buildContentValue("toujours", "sempre", "fr", "it") );
		cvGlossary.add( buildContentValue("touriste", "turista", "fr", "it") );
		cvGlossary.add( buildContentValue("tournée", "giro", "fr", "it") );
		cvGlossary.add( buildContentValue("tract", "volantino", "fr", "it") );
		cvGlossary.add( buildContentValue("tradition", "tradizione", "fr", "it") );

		
		cvGlossary.add( buildContentValue("train", "treno", "fr", "it") );
		cvGlossary.add( buildContentValue("trajet", "tragitto", "fr", "it") );
		cvGlossary.add( buildContentValue("tranquille", "tranquillo", "fr", "it") );
		cvGlossary.add( buildContentValue("tranquillement", "tranqulllamente", "fr", "it") );
		cvGlossary.add( buildContentValue("travail", "lavoro", "fr", "it") );
		cvGlossary.add( buildContentValue("travailler", "lavorare", "fr", "it") );*/

		
		cvGlossary.add( buildContentValue("treize", "tredici", "fr", "it") );
		cvGlossary.add( buildContentValue("très", "molto", "fr", "it") );
		/*cvGlossary.add( buildContentValue("trois", "tre", "fr", "it") );
		cvGlossary.add( buildContentValue("tromper (se -)" , "sbagliarsi", "fr", "it") );
		cvGlossary.add( buildContentValue("tronc", "tronco", "fr", "it") );
		cvGlossary.add( buildContentValue("trop", "troppo", "fr", "it") );

		cvGlossary.add( buildContentValue("vacances", "ferie,vacanze", "fr", "it") );
		cvGlossary.add( buildContentValue("vache", "mucca,vacca", "fr", "it") );
		cvGlossary.add( buildContentValue("vachement", "terribilmente,maledettamente", "fr", "it") );
		cvGlossary.add( buildContentValue("vague", "onda", "fr", "it") );
		cvGlossary.add( buildContentValue("valise", "valigia", "fr", "it") );
		cvGlossary.add( buildContentValue("veinard", "fortunato", "fr", "it") );

		
		cvGlossary.add( buildContentValue("veau", "vitello", "fr", "it") );
		cvGlossary.add( buildContentValue("vendeur", "venditore", "fr", "it") );
		cvGlossary.add( buildContentValue("vendre", "vendere", "fr", "it") );
		cvGlossary.add( buildContentValue("vendredi" , "venerdi", "fr", "it") );
		cvGlossary.add( buildContentValue("venir", "venire", "fr", "it") );
		cvGlossary.add( buildContentValue("vers", "verso", "fr", "it") );
		
		cvGlossary.add( buildContentValue("femme", "wife,woman", "fr", "en") );
		cvGlossary.add( buildContentValue("salut", "hello", "fr", "en") );
		cvGlossary.add( buildContentValue("donna", "woman", "it", "en") );
		cvGlossary.add( buildContentValue("moglie", "wife", "it", "en") );
		cvGlossary.add( buildContentValue("ciao", "hello", "it", "en") );*/

		
		return cvGlossary;
	}
}
