package com.caseystella.talks.util;

import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: cstella
 * Date: 5/20/13
 * Time: 3:32 PM
 * To change this template use File | Settings | File Templates.
 */
public class ToSequenceFiles extends Configured implements Tool
{
    public static class CorpusText implements Writable
    {
        public static enum PERSPECTIVE
        {
            ISRAELI,
            PALESTINIAN;
            public static PERSPECTIVE fileNameToPerspective(Path path)
            {
                if(path.getName().endsWith("pal1") || path.getName().endsWith("pal2"))
                {
                   return PALESTINIAN;
                }
                else if(path.getName().endsWith("is1") || path.getName().endsWith("is2"))
                {
                    return ISRAELI;
                }
                else
                {
                    return null;
                }
            }
        }

        private PERSPECTIVE perspective;
        private Text content;

        public CorpusText()
        {
            content = new Text();
            perspective = null;
        }

        public PERSPECTIVE getPerspective() {
            return perspective;
        }

        public Text getContent() {
            return content;
        }

        public void setPerspective(PERSPECTIVE perspective) {
            this.perspective = perspective;
        }

        public void setContent(Text content) {
            this.content = content;
        }

        @Override
        public void write(DataOutput dataOutput) throws IOException {
            dataOutput.writeShort(perspective.ordinal());
            content.write(dataOutput);
        }

        @Override
        public void readFields(DataInput dataInput) throws IOException {
            perspective = PERSPECTIVE.values()[dataInput.readShort()];
            content.readFields(dataInput);
        }
    }


    public static class Map extends Mapper<Text, BytesWritable, Text, CorpusText>
    {
        @Override
        protected void setup(Context context) throws IOException, InterruptedException {
            super.setup(context);    //To change body of overridden methods use File | Settings | File Templates.
        }

        @Override
        protected void map(Text key, BytesWritable value, Context context) throws IOException, InterruptedException {
            Path path = new Path(key.toString());
            CorpusText.PERSPECTIVE perspective = CorpusText.PERSPECTIVE.fileNameToPerspective(path);
            if(perspective == null)
            {
                //skip it if we can't figure out the perspective from the filename
                return;
            }
            Text outputKey = getKey(perspective,path.getName());
            CorpusText outputValue = new CorpusText();
            outputValue.setPerspective(perspective);
            outputValue.setContent(new Text(value.getBytes()));
        }

        private Text getKey(CorpusText.PERSPECTIVE perspective, String filename)
        {
            int suffixLength = -1;
            if(perspective == CorpusText.PERSPECTIVE.ISRAELI)
            {
                //cut off the is?
                suffixLength = "is1".length();
            }
            else
            {
                suffixLength = "pal1".length();
            }
            return new Text(filename.substring(filename.length() - suffixLength));
        }

    }
    @Override
    public int run(String[] strings) throws Exception {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public static void main(String... argv) throws Exception {
        ToolRunner.run(new ToSequenceFiles(), argv);
    }
}
