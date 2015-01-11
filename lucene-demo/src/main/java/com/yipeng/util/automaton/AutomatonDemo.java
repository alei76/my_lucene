package com.yipeng.util.automaton;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.automaton.Automata;
import org.apache.lucene.util.automaton.Automaton;
import org.apache.lucene.util.automaton.CharacterRunAutomaton;

public class AutomatonDemo {
	public void testBasicAutomaton(){
		String[] words = {"hockey", "hawk", "puck", "text", "textual", "anachronism", "anarchy"};
		Collection<BytesRef> strings = new ArrayList<BytesRef>();
		for (String word : words) {
		  strings.add(new BytesRef(word));

		}
		//build up a simple automaton out of several words
		Automaton automaton = Automata.makeStringUnion(strings);
		CharacterRunAutomaton run = new CharacterRunAutomaton(automaton);
		System.out.println("Match: " + run.run("hockey"));
		System.out.println("Match: " + run.run("text"));
	}
	
	public static void main(String[] args) {
		AutomatonDemo demo = new AutomatonDemo();
		demo.testBasicAutomaton();
	}
}
