#!/bin/bash

function exists_in_hadoop() {
  $HADOOP_HOME/bin/hadoop fs -ls $1 >& /dev/null
  if [ $? -ne 0 ];then
    echo "false";
  else
    echo "true";
  fi
}

if [ $# -lt 1 ];then
  echo "Usage: ingest_data <base_dir> [num_topics]";
  exit 1
fi

if [ $# -gt 1 ];then
  NUM_TOPICS=$2
else
  NUM_TOPICS=5
  echo "Defaulting to $NUM_TOPICS topics"
fi

BASE_DIR=$1


if [ -z "$HADOOP_HOME" ];then
  echo "HADOOP_HOME is unset, assuming /usr/lib/hadoop"
  HADOOP_HOME=/usr/lib/hadoop
fi

if [ -z "$MAHOUT_HOME" ];then
  echo "MAHOUT_HOME is unset, assuming /usr/lib/mahout"
  MAHOUT_HOME=/usr/lib/mahout
fi

################################################################
#
#       Copy the data from local disk to HDFS
#
#
###############################################################
CWD=$(dirname $0)
if [ $(exists_in_hadoop $BASE_DIR/data) == "false" ];then
  echo "No input data found; expected data to be in $BASE_DIR/data";
  echo "Copying data in..."
  $CWD/copy_data.sh $BASE_DIR
  if [ $? -ne 0 ];then
    echo "Unable to copy data into hadoop...sorry, I have to bail."
    exit 2
  fi
fi

################################################################
#
#       Convert the data in HDFS to sequence file format that
#       Mahout is expecting
#
###############################################################

SEQUENCE_FILES=$BASE_DIR/ingest/seq

if [ $(exists_in_hadoop $SEQUENCE_FILES) == "true" ];then
  echo "Sequence files already exist in hadoop, skipping..."
else
  echo "Converting input documents to sequence files"
  $MAHOUT_HOME/bin/mahout seqdirectory --input $BASE_DIR/data \
                                       --output $SEQUENCE_FILES;
 if [ $? -ne 0 ];then
  echo "Unable to convert input data to sequence files...";
  exit 2
 fi
 echo "Created sequence files $BASE_DIR/data -> $SEQUENCE_FILES"
fi

################################################################
#
#       Convert the sequence files in HDFS to sparse vectors 
#       suitable for LDA
#
###############################################################

VECTOR_FILES=$BASE_DIR/ingest/vector
TF_VECTORS=$BASE_DIR/ingest/tf_vec
CUSTOM_JAR=$CWD/NLPWithMahout-1.0-SNAPSHOT.jar


CLASSPATH=${CLASSPATH}:$HADOOP_CONF_DIR

CLASSPATH=${CLASSPATH}:$JAVA_HOME/lib/tools.jar

# so that filenames w/ spaces are handled correctly in loops below
IFS=


for f in $MAHOUT_HOME/mahout-*.jar; do
  CLASSPATH=${CLASSPATH}:$f;
done

  # add dev targets if they exist
for f in $MAHOUT_HOME/examples/target/mahout-examples-*-job.jar $MAHOUT_HOME/mahout-examples-*-job.jar ; do
  CLASSPATH=${CLASSPATH}:$f;
done

# add release dependencies to CLASSPATH
for f in $MAHOUT_HOME/lib/*.jar; do
    CLASSPATH=${CLASSPATH}:$f;
done

LIBJARS=$CUSTOM_JAR
for f in $MAHOUT_HOME/mahout-*.jar;do
  LIBJARS=${LIBJARS},$f;
done

if [ $(exists_in_hadoop $VECTOR_FILES) == "true" ];then
  echo "Vector files already exist in hadoop, skipping..."
else
  echo "Converting input documents to sequence files"
  #$MAHOUT_HOME/bin/mahout seq2sparse -libjars $CUSTOM_JAR\
  export HADOOP_CLASSPATH=$MAHOUT_CONF_DIR:${HADOOP_CLASSPATH}:$CLASSPATH
  $HADOOP_HOME/bin/hadoop jar $CUSTOM_JAR com.caseystella.ingest.SparseVectorsFromSequenceFiles\
                                     -libjars $LIBJARS\
                                     -a com.caseystella.nlp.Analyzer\
                                     -i "$SEQUENCE_FILES"\
                                     -o "$VECTOR_FILES"\
                                     -x 65\
                                     -wt tf\
                                     -seq\
                                     -ow\
&&\
  $MAHOUT_HOME/bin/mahout rowid -i $VECTOR_FILES/tf-vectors\
                                -o $TF_VECTORS
 unset HADOOP_CLASSPATH
 if [ $? -ne 0 ];then
   echo "Unable to convert sequence files to vectors...";
   exit 2
 fi
 echo "Created vector files $SEQUENCE_FILES -> $VECTOR_FILES"
fi
DICTIONARY=$VECTOR_FILES/dictionary.file-0

################################################################
#
#       Run LDA
#
###############################################################

LDA_OUT=$BASE_DIR/base_lda
LDA_TOPIC_TERMS=$LDA_OUT/topic_terms
LDA_DOC_TOPIC=$LDA_OUT/doc_topic
LDA_MODEL=$LDA_OUT/model

if [ $(exists_in_hadoop $LDA_OUT) == "true" ];then
  echo "LDA Model already created in hadoop, skipping..."
else
  echo "Create the LDA model from the vectors"
  $MAHOUT_HOME/bin/mahout cvb -i $TF_VECTORS/matrix\
                              -o $LDA_TOPIC_TERMS\
                              -k $NUM_TOPICS\
                              --maxIter 20\
                              -seed 0\
                              -ow\
                              -dict $DICTIONARY\
                              -dt $LDA_DOC_TOPIC\
                              -mipd 50\
                              -ntt 1\
                              -mt $LDA_MODEL
                              #-nt $TERM_COUNT\
                              #-tf .2
 if [ $? -ne 0 ];then
   echo "Unable to create the LDA model...";
   exit 2
 fi
 echo "Created LDA model $VECTOR_FILES -> $LDA_OUT"
fi
echo "Model output:"
i=1
IFS=$'\n';for line in $($MAHOUT_HOME/bin/mahout vectordump -i $LDA_TOPIC_TERMS --dictionary $DICTIONARY --vectorSize 4 --dictionaryType sequencefile | grep "^{");do
  echo "Topic $i" 
  echo $line | ./prettyprint.sh 10
#  echo $line | ./prettyprint.sh 10 > topics/$i.dat
  echo "============================="
  echo ""
  i=`expr $i + 1`
done

