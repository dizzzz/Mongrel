/*
 *  eXist Open Source Native XML Database
 *  Copyright (C) 2014 The eXist Project
 *  http://exist-db.org
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package org.exist.mongodb.xquery.mongodb;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.MongoException;
import java.net.UnknownHostException;
import java.util.UUID;
import org.exist.dom.QName;
import org.exist.mongodb.shared.Constants;
import org.exist.mongodb.shared.MongodbClientStore;
import org.exist.mongodb.xquery.MongodbModule;
import org.exist.xquery.BasicFunction;
import org.exist.xquery.Cardinality;
import org.exist.xquery.FunctionSignature;
import org.exist.xquery.XPathException;
import org.exist.xquery.XQueryContext;
import org.exist.xquery.value.FunctionParameterSequenceType;
import org.exist.xquery.value.FunctionReturnSequenceType;
import org.exist.xquery.value.Sequence;
import org.exist.xquery.value.SequenceType;
import org.exist.xquery.value.StringValue;
import org.exist.xquery.value.Type;

/**
 * Implementation of the gridfs:connect() function
 * 
 * @author Dannes Wessels
 */

public class Connect extends BasicFunction {
    
    
    
    public final static FunctionSignature signatures[] = {

        new FunctionSignature(
        new QName("connect", MongodbModule.NAMESPACE_URI, MongodbModule.PREFIX),
        "Connect to GridFS server",
        new SequenceType[]{
                new FunctionParameterSequenceType("url", Type.STRING, Cardinality.ONE, "URI to server")
            },
            new FunctionReturnSequenceType(Type.STRING, Cardinality.ONE, "MongoDB client id")
        ),
        
    };

    public Connect(XQueryContext context, FunctionSignature signature) {
        super(context, signature);
    }

    @Override
    public Sequence eval(Sequence[] args, Sequence contextSequence) throws XPathException {

        // User must either be DBA or in the JMS group
        if (!context.getSubject().hasDbaRole() && !context.getSubject().hasGroup(Constants.MONGODB_GROUP)) {
            String txt = String.format("Permission denied, user '%s' must be a DBA or be in group '%s'",
                    context.getSubject().getName(), Constants.MONGODB_GROUP);
            LOG.error(txt);
            throw new XPathException(this, txt);
        }

        // Get connection URL
        String url = args[0].itemAt(0).getStringValue();

        try {
            // Construct client
            MongoClientURI uri = new MongoClientURI(url);         
            MongoClient client = new MongoClient(uri);
            
            LOG.debug(String.format("client=%s", client));

            // Create unique identifier
            String mongodbClientId = UUID.randomUUID().toString();
            
            // Store Client
            MongodbClientStore.getInstance().add(mongodbClientId, client);
            
            // Report identifier
            return new StringValue(mongodbClientId);

        } catch (UnknownHostException ex) {
            LOG.error(ex.getMessage());
            throw new XPathException(this, ex);
            
        } catch (MongoException ex) {
            LOG.error(ex);
            throw new XPathException(this, ex);       
            
        } catch (Throwable ex) {
            LOG.error(ex);
            throw new XPathException(this, ex);
        }


    }    
}
