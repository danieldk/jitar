/*
 * Copyright 2008, 2009 Daniel de Kok
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package eu.danieldk.nlp.jitar.corpus;

/**
 * Instances of <i>TaggedWord</i> represent a word/tag combination.
 */
public class TaggedWord {
	private final String d_word;
	private final String d_tag;
	
	public TaggedWord(String word, String tag) {
		d_word = word;
		d_tag = tag;
	}
	
	public String tag() {
		return d_tag;
	}
	
	public String word() {
		return d_word;
	}
}
