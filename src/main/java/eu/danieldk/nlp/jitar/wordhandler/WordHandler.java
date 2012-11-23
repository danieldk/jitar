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

package eu.danieldk.nlp.jitar.wordhandler;

import java.util.Map;

/**
 * Classes implementing the <i>wordHandler</i> interface provide the
 * <i>tagProbs</i> method to estimate the probability of a word given
 * a tag.
 */
public interface WordHandler {
	/**
	 * Return the logprobs of <i>word</i>, given one or more tags (log(p(w|t)).
	 * @param word The word.
	 * @return <i>log(p(w|t))</i> for all possible tags <i>t</i> for that word.
	 */
	Map<Integer, Double> tagProbs(String word);
}
