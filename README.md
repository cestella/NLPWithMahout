NLP With Mahout
=============
This repository is intended to provide a working example of using
the Latent Dirichlet Allocation library from Mahout.  This work was
presented at the Hadoop Summit 2013 in San Jose, California.  You can
find the presentation
[here](https://github.com/cestella/NLPWithMahout/blob/master/src/main/presentation/NLP_with_Mahout.pdf?raw=true).

Details
=========
The data presented here comes from the [bitterlemons
corpus](https://sites.google.com/site/weihaolinatcmu/data).  This data
contains blog post entries about the Israeli and Palestinian crisis from
the early 2000s.

Running the "mvn clean verify" command will prep the data and code in
the target/landing directory.  You should copy this directory to your
gateway node (i.e. a node where the hadoop client is installed).

The [driver
program](https://github.com/cestella/NLPWithMahout/blob/master/src/main/bash/ingest_data.sh) will upload the data from
the access node to HDFS as well as running the training.  On the
Hortonworks Sandbox VM instance running in VMWare on my Macbook 
pro, this takes approximately 30 minutes.  At the end, it will output
the topics.

Caveats
========
Currently, there are two caveats worth mentioning.  The driver program
for seq2sparse had to be ripped out of Mahout and modified as of the
time of this writing due to the inability to pass in the -libjars arg.
This was necessary because a custom analyzer was used (to filter not
only by stop words, but also by part of speech tag).  I will be
submitting a ticket to Mahout shortly with my patch.

Also, this ran on a patched version of Mahout 0.7.  I would suggest
waiting until 0.8 to let the patch that I applied for the LDA
implementation to make its way into the release.


