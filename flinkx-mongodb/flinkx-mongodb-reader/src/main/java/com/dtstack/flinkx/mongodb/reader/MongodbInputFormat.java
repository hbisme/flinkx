package com.dtstack.flinkx.mongodb.reader;

import com.dtstack.flinkx.inputformat.RichInputFormat;
import com.dtstack.flinkx.mongodb.Column;
import com.dtstack.flinkx.mongodb.MongodbUtil;
import com.dtstack.flinkx.util.StringUtil;
import com.mongodb.BasicDBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import org.apache.commons.lang.StringUtils;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.core.io.InputSplit;
import org.apache.flink.types.Row;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.dtstack.flinkx.mongodb.MongodbConfigKeys.*;

/**
 * @author jiangbo
 * @date 2018/6/5 10:28
 */
public class MongodbInputFormat extends RichInputFormat {

    protected String hostPorts;

    protected String username;

    protected String password;

    protected String database;

    protected String collectionName;

    protected List<Column> columns;

    protected String filterJson;

    private Bson filter;

    private MongoCollection<Document> collection;

    private MongoCursor<Document> cursor;

    @Override
    public void configure(Configuration parameters) {
        Map<String,String> config = new HashMap<>(4);
        config.put(KEY_HOST_PORTS,hostPorts);
        config.put(KEY_USERNAME,username);
        config.put(KEY_PASSWORD,password);
        config.put(KEY_DATABASE,database);

        collection = MongodbUtil.getCollection(config,database,collectionName);
        buildFilter();
    }

    @Override
    protected void openInternal(InputSplit inputSplit) throws IOException {
        MongodbInputSplit split = (MongodbInputSplit) inputSplit;
        FindIterable<Document> findIterable;

        if(filter == null){
            findIterable = collection.find();
        } else {
            findIterable = collection.find(filter);
        }

        findIterable = findIterable.skip(split.getSkip()).limit(split.getLimit());
        cursor = findIterable.iterator();
    }

    @Override
    public Row nextRecordInternal(Row row) throws IOException {
        return MongodbUtil.convertDocTORow(cursor.next(),columns);
    }

    @Override
    protected void closeInternal() throws IOException {
        if (cursor != null){
            cursor.close();
            MongodbUtil.close();
        }
    }

    @Override
    public InputSplit[] createInputSplits(int minNumSplits) throws IOException {
        ArrayList<MongodbInputSplit> splits = new ArrayList<>();

        long docNum = filter == null ? collection.count() : collection.count(filter);
        if(docNum <= minNumSplits){
            splits.add(new MongodbInputSplit(0,(int)docNum));
            return splits.toArray(new MongodbInputSplit[splits.size()]);
        }

        long size = Math.floorDiv(docNum,(long)minNumSplits);
        for (int i = 0; i < minNumSplits; i++) {
            splits.add(new MongodbInputSplit((int)(i * size), (int)size));
        }

        if(size * minNumSplits < docNum){
            splits.add(new MongodbInputSplit((int)(size * minNumSplits), (int)(docNum - size * minNumSplits)));
        }

        return splits.toArray(new MongodbInputSplit[splits.size()]);
    }

    @Override
    public boolean reachedEnd() throws IOException {
        return !cursor.hasNext();
    }

    private void buildFilter(){
        if(StringUtils.isNotEmpty(filterJson)){
            filter = BasicDBObject.parse(filterJson);
        }
    }
}
