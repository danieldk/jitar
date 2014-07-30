//
// Copyright 2008, 2014 Daniël de Kok
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
