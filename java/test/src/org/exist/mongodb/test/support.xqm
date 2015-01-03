xquery version "3.0";

module namespace support="http://exist-db.org/ext/mongodb/test/support";

import module namespace mongodb = "http://exist-db.org/xquery/mongodb" 
                at "java:org.exist.mongodb.xquery.MongodbModule";

declare variable $support:mongoUrl := "mongodb://miniserver.local";
declare variable $support:database := "mydatabase";
declare variable $support:testCollection := "/db/mongodbTest";
declare variable $support:tokenStore := "token.xml";

(:  
 : Connect to mongodb, store token 
 :)
declare function support:setup() {
    let $foo :=  xmldb:create-collection("/db", "mongodbTest")
    let $token := mongodb:connect($support:mongoUrl)
    return xmldb:store($support:testCollection, $support:tokenStore, <token>{$token}</token>)
};

(: 
 : Disconnect from mongodb, cleanup token
 :)
declare function support:cleanup() {
    let $mongodbClientId := support:getToken()
    let $logout := mongodb:close($mongodbClientId)
    return
    xmldb:remove($support:testCollection)
};

(:  
 : Get token from store
 :)
declare function support:getToken() as xs:string {
    doc($support:testCollection || "/" || $support:tokenStore )//token/text()
};
