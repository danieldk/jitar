Jitar - A simple Trigram HMM part-of-speech tagger

== Introduction ==

Jitar is a simple part-of-speech tagger, based on a trigram Hidden
Markov Model (HMM). It (partly) implements the ideas set forth in
[1]. Jitar is written in Java, so it should be easy to use in other
Java programs, or languages that run on the JVM.

== Warning ==

The Jitar API will be highly unstable for the first few versions!

== Training ==

A model can be created from a corpus that includes part of speech
tags, such as the Brown corpus. The model can be created easily with
the training program:

java -cp dist/jitar.jar org.langkit.tagger.cli.Train corpus lexicon ngrams

Where 'corpus' is the corpus to use for training. The 'lexicon'
and 'ngrams' files will be created and form the model.

Sample models are included in the 'models' directory.

== Tagging ==

Usually, you will want to call the tagger from your own program, but
we have included a simple command line tagger as a sample. This
tagger reads pretokenized sentences from the standard input (one
sentence per line), and will print the best scoring tag sequence to
the standard output. For example:

---
$ echo "The cat is on the mat ." | java -cp dist/jitar.jar \
  org.langkit.tagger.cli.Tag lexicon ngrams
AT NN BEZ IN AT NN .
---

A model based on the Brown corpus is included in models/brown.

== Authors ==

Daniel de Kok <me@danieldk.org>

== FAQ ==

- "What's up with the name?"

  This is a Java port of a C++ tagger that I previously wrote,
  named Sitar. Jitar, it is not an abbreviation. If you do prefer
  abbreviations, let's make it "JavaIsh TAgging Redux" :).

- "Can I use Jitar, or parts thereof in closed-source software?"

  No, Jitar is licensed under the Affero GNU General Public License.
  Even use in a closed-source network application is disallowed.

  A perpetual non-exclusive license for the use of Jitar in
  proprietary software is available from the author.

[1] TnT - a statistical part-of-speech tagger, Thorsten Brants, 2000
