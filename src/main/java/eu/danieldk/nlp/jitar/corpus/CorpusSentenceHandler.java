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

package eu.danieldk.nlp.jitar.corpus;

import java.util.List;

/**
 * Classes implementing this interface can handle sentences, for instance
 * those provided by a corpus reader.
 * @param <WordType>
 */
public interface CorpusSentenceHandler<WordType> {
	public void handleSentence(List<WordType> sentence);
}
