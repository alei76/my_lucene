package org.yipeng.test.analysis;

import org.apache.lucene.util.Attribute;

/*
 * 自定义的Attribute
 */
public interface PartOfSpeechAttribute extends Attribute{
	public static enum PartOfSpeech {
		Noun,Verb,Adjective,Adverb,Pronoun,Preposition,
		Conjunction,Article,Unknown
	}
	
	public void setPartOfSpeech(PartOfSpeech pos);
	
	public PartOfSpeech getPartOfSpeech();
}
