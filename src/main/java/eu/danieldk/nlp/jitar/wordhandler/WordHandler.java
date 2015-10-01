//
// Copyright 2008, 2015 Daniël de Kok
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
//

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
