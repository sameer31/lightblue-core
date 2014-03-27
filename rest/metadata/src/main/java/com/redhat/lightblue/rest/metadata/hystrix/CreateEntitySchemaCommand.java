/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redhat.lightblue.rest.metadata.hystrix;

import com.redhat.lightblue.config.metadata.MetadataManager;
import com.redhat.lightblue.metadata.EntityInfo;
import com.redhat.lightblue.metadata.EntityMetadata;
import com.redhat.lightblue.metadata.EntitySchema;
import com.redhat.lightblue.metadata.Metadata;
import com.redhat.lightblue.metadata.parser.JSONMetadataParser;
import com.redhat.lightblue.rest.metadata.RestMetadataConstants;
import com.redhat.lightblue.util.Error;
import com.redhat.lightblue.util.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author nmalik
 */
public class CreateEntitySchemaCommand extends AbstractRestCommand {
    private static final Logger LOGGER = LoggerFactory.getLogger(GetEntityRolesCommand.class);
    private final String entity;
    private final String version;
    private final String schema;

    public CreateEntitySchemaCommand(String clientKey, String entity, String version, String schema) {
        this(clientKey, null, entity, version, schema);
    }

    public CreateEntitySchemaCommand(String clientKey, Metadata metadata, String entity, String version, String schema) {
        super(CreateEntitySchemaCommand.class.getSimpleName(), CreateEntitySchemaCommand.class.getSimpleName(), clientKey, metadata);
        this.entity = entity;
        this.version = version;
        this.schema = schema;
    }

    @Override
    protected String run() throws Exception {
        LOGGER.debug("run: entity={}, version={}", entity, version);
        Error.reset();
        Error.push(getClass().getSimpleName());
        Error.push(entity);
        Error.push(version);
        try {
            JSONMetadataParser parser = MetadataManager.getJSONParser();
            EntitySchema sch = parser.parseEntitySchema(JsonUtils.json(schema));
            if (!sch.getName().equals(entity)) {
                throw Error.get(RestMetadataConstants.ERR_NO_NAME_MATCH, entity);
            }
            if (!sch.getVersion().getValue().equals(version)) {
                throw Error.get(RestMetadataConstants.ERR_NO_VERSION_MATCH, version);
            }

            Metadata md = MetadataManager.getMetadata();
            EntityInfo ei = md.getEntityInfo(entity);
            if (ei == null) {
                throw Error.get(RestMetadataConstants.ERR_NO_ENTITY_NAME, entity);
            }

            EntityMetadata emd = new EntityMetadata(ei, sch);
            md.createNewSchema(emd);
            emd = md.getEntityMetadata(entity, version);
            return parser.convert(emd).toString();
        } catch (Error e) {
            return e.toString();
        } catch (Exception e) {
            LOGGER.error("Failure: {}", e);
            return Error.get(RestMetadataConstants.ERR_REST_ERROR, e.toString()).toString();
        } finally {
            Error.reset();
        }
    }
}
