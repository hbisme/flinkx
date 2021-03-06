package com.dtstack.flinkx.mongodb.reader;

import com.dtstack.flinkx.inputformat.RichInputFormatBuilder;
import com.dtstack.flinkx.mongodb.Column;

import java.util.List;
import java.util.Map;

/**
 * @author jiangbo
 * @date 2018/6/5 10:28
 */
public class MongodbInputFormatBuilder extends RichInputFormatBuilder {

    private MongodbInputFormat format;

    public MongodbInputFormatBuilder() {
        super.format = format = new MongodbInputFormat();
    }

    public void setHostPorts(String hostPorts){
        format.hostPorts = hostPorts;
    }

    public void setUsername(String username){
        format.username = username;
    }

    public void setPassword(String password){
        format.password = password;
    }

    public void setDatabase(String database){
        format.database = database;
    }

    public void setCollection(String collection){
        format.collectionName = collection;
    }

    public void setColumns(List<Column> columns){
        format.columns = columns;
    }

    public void setFilter(String filter){
        format.filterJson = filter;
    }

    @Override
    protected void checkFormat() {
        if(format.hostPorts == null){
            throw new IllegalArgumentException("No host supplied");
        }

        if(format.database == null){
            throw new IllegalArgumentException("No database supplied");
        }

        if(format.collectionName == null){
            throw new IllegalArgumentException("No collection supplied");
        }
    }
}
