/*
 * Copyright 2008 Daniel de Kok
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

package eu.danieldk.nlp.jitar.languagemodel;

import eu.danieldk.nlp.jitar.data.TriGram;

/**
 * Classes that implement the <i>LanguageModel</i> interface provide the
 * <i>triGramProb</i> method that estimates the probability of a trigram,
 * p(t3|t1,t2).
 * 
 */
public interface LanguageModel {
	/**
	 * Estimate the probability of a trigram, p(t3|t1,t2), and return
	 * the logprob.
	 * @param triGram t1,t2,t3
	 * @return log(p(t3|t1,t2))
	 */
	double triGramProb(TriGram triGram);
}
