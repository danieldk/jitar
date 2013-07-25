// Copyright 2008, 2009 Daniel de Kok
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package eu.danieldk.nlp.jitar.cli;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import eu.danieldk.nlp.jitar.corpus.BrownCorpusReader;
import eu.danieldk.nlp.jitar.corpus.CorpusReader;
import eu.danieldk.nlp.jitar.corpus.CorpusReaderException;
import eu.danieldk.nlp.jitar.corpus.CorpusSentenceHandler;
import eu.danieldk.nlp.jitar.corpus.TaggedWord;

public class Train {
	private static class TrainHandler implements CorpusSentenceHandler<TaggedWord> {
		private final Map<String, Map<String, Integer>> d_lexicon;
		private final Map<String, Integer> d_uniGrams;
		private final Map<String, Integer> d_biGrams;
		private final Map<String, Integer> d_triGrams;
		
		public TrainHandler() {
			d_lexicon = new HashMap<String, Map<String,Integer>>();
			d_uniGrams = new HashMap<String, Integer>();
			d_biGrams = new HashMap<String, Integer>();
			d_triGrams = new HashMap<String, Integer>();
		}
		
		public Map<String, Integer> biGrams() {
			return d_biGrams;
		}
		
		public void handleSentence(List<TaggedWord> sentence) {
			for (int i = 0; i < sentence.size(); ++i) {
				addLexiconEntry(sentence.get(i));
				addUniGram(sentence, i);
				if (i > 0)
					addBiGram(sentence, i);
				if (i > 1)
					addTriGram(sentence, i);
			}
		}
		
		public Map<String, Map<String, Integer>> lexicon() {
			return d_lexicon;
		}
		
		public Map<String, Integer> triGrams() {
			return d_triGrams;
		}

		public Map<String, Integer> uniGrams() {
			return d_uniGrams;
		}
		
		private void addLexiconEntry(TaggedWord taggedWord) {
			String word = taggedWord.word();
			String tag = taggedWord.tag();
			
			if (!d_lexicon.containsKey(word))
				d_lexicon.put(word, new HashMap<String, Integer>());
			
			if (!d_lexicon.get(word).containsKey(tag))
				d_lexicon.get(word).put(tag, 1);
			else
				d_lexicon.get(word).put(tag, d_lexicon.get(word).get(tag) + 1);			
		}
		
		private void addUniGram(List<TaggedWord> sentence, int index) {
			String uniGram = sentence.get(index).tag();
			
			if (!d_uniGrams.containsKey(uniGram))
				d_uniGrams.put(uniGram, 1);
			else
				d_uniGrams.put(uniGram, d_uniGrams.get(uniGram) + 1);
		}
		
		private void addBiGram(List<TaggedWord> sentence, int index) {
			String biGram = sentence.get(index - 1).tag() + " " +
					sentence.get(index).tag();
			
			if (!d_biGrams.containsKey(biGram))
				d_biGrams.put(biGram, 1);
			else
				d_biGrams.put(biGram, d_biGrams.get(biGram) + 1);
		}

		private void addTriGram(List<TaggedWord> sentence, int index) {
			String triGram = sentence.get(index - 2).tag() + " " +
					sentence.get(index - 1).tag() + " " +
					sentence.get(index).tag();
			
			if (!d_triGrams.containsKey(triGram))
				d_triGrams.put(triGram, 1);
			else
				d_triGrams.put(triGram, d_triGrams.get(triGram) + 1);
		}
	}
	
	private static void writeNGrams(Map<String, Integer> uniGrams,
			Map<String, Integer> biGrams, Map<String, Integer> triGrams,
			BufferedWriter writer) throws IOException {
		for (Entry<String, Integer> entry: uniGrams.entrySet())
			writer.write(entry.getKey() + " " + entry.getValue() + "\n");
		
		for (Entry<String, Integer> entry: biGrams.entrySet())
			writer.write(entry.getKey() + " " + entry.getValue() + "\n");

		for (Entry<String, Integer> entry: triGrams.entrySet())
			writer.write(entry.getKey() + " " + entry.getValue() + "\n");

		writer.flush();
	}
	
	private static void writeLexicon(Map<String, Map<String, Integer>> lexicon,
			BufferedWriter writer) throws IOException {
		for (Entry<String, Map<String, Integer>> wordEntry: lexicon.entrySet()) {
			String word = wordEntry.getKey();
			
			writer.write(word);
			
			for (Entry<String, Integer> tagEntry: lexicon.get(word).entrySet()) {
				writer.write(" ");
				writer.write(tagEntry.getKey());
				writer.write(" ");
				writer.write(tagEntry.getValue().toString());
			}
			
			writer.newLine();
		}
		
		writer.flush();
	}
		
	public static void main(String[] args) {
		if (args.length != 3) {
			System.out.println("Train corpus lexicon ngrams");
			System.exit(1);
		}
		
		List<TaggedWord> startMarkers = new ArrayList<TaggedWord>();
		startMarkers.add(new TaggedWord("<START>", "<START>"));
		startMarkers.add(new TaggedWord("<START>", "<START>"));
		List<TaggedWord> endMarkers = new ArrayList<TaggedWord>();
		endMarkers.add(new TaggedWord("<END>", "<END>"));
		
		CorpusReader<TaggedWord> corpusReader = new BrownCorpusReader(startMarkers,
				endMarkers, true);
		
		TrainHandler trainHandler = new TrainHandler();
		corpusReader.addHandler(trainHandler);

		try {
			corpusReader.parse(new BufferedReader(new FileReader(args[0])));
		} catch (IOException e) {
			System.out.println("Could not read corpus!");
			e.printStackTrace();
			System.exit(1);
		} catch (CorpusReaderException e) {
			e.printStackTrace();
			System.exit(1);
		}

		try {
			writeLexicon(trainHandler.lexicon(), new BufferedWriter(new FileWriter(args[1])));
			writeNGrams(trainHandler.uniGrams(), trainHandler.biGrams(),
					trainHandler.triGrams(), new BufferedWriter(new FileWriter(args[2])));
		} catch (IOException e) {
			System.out.println("Could not write training data!");
			e.printStackTrace();
			System.exit(1);
		}
	}
}
