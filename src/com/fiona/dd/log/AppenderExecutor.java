package com.fiona.dd.log ;

import com.mongodb.DBCollection;
import com.mongodb.WriteConcern;

/**
 * @author Prajwal Tuladhar <praj@infynyxx.com>
 */
public class AppenderExecutor<E> {

    private final MongoConverter<E> mongoConverter;
    private final DBCollection dbCollection;
    private final boolean safeInsert;

    public AppenderExecutor(MongoConverter<E> converter, DBCollection dbCollection, boolean safeInsert) {
        this.mongoConverter = converter;
        this.dbCollection = dbCollection;
        this.safeInsert = safeInsert;
    }

    @SuppressWarnings("deprecation")
	public void append(E logEvent) {
        if (!safeInsert) {
            dbCollection.insert(mongoConverter.toBSON(logEvent));
        } else {
            dbCollection.insert(mongoConverter.toBSON(logEvent), WriteConcern.SAFE);
        }
    }
}
