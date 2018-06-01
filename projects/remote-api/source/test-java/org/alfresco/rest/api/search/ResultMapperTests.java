/*-
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
 * 
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
package org.alfresco.rest.api.search;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;
import static junit.framework.TestCase.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.notNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.alfresco.repo.search.EmptyResultSet;
import org.alfresco.repo.search.impl.lucene.SolrJSONResultSet;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.rest.api.impl.NodesImpl;
import org.alfresco.rest.api.model.Node;
import org.alfresco.rest.api.model.UserInfo;
import org.alfresco.rest.api.search.context.FacetFieldContext;
import org.alfresco.rest.api.search.context.FacetQueryContext;
import org.alfresco.rest.api.search.context.SpellCheckContext;
import org.alfresco.rest.api.search.impl.ResultMapper;
import org.alfresco.rest.api.search.model.SearchQuery;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.api.search.context.SearchContext;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.LimitBy;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.GUID;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Tests the ResultMapper class
 *
 * @author Gethin James
 */
public class ResultMapperTests
{
    static ResultMapper mapper;
    public static final String JSON_REPONSE = "{\"responseHeader\":{\"status\":0,\"QTime\":9},\"_original_parameters_\":\"org.apache.solr.common.params.DefaultSolrParams:{params(df=TEXT&alternativeDic=DEFAULT_DICTIONARY&fl=DBID,score&start=0&fq={!afts}AUTHORITY_FILTER_FROM_JSON&fq={!afts}TENANT_FILTER_FROM_JSON&rows=1000&locale=en_US&wt=json),defaults(carrot.url=id&spellcheck.collateExtendedResults=true&carrot.produceSummary=true&spellcheck.maxCollations=3&spellcheck.maxCollationTries=5&spellcheck.alternativeTermCount=2&spellcheck.extendedResults=false&defType=afts&spellcheck.maxResultsForSuggest=5&spellcheck=false&carrot.outputSubClusters=false&spellcheck.count=5&carrot.title=mltext@m___t@{http://www.alfresco.org/model/content/1.0}title&carrot.snippet=content@s___t@{http://www.alfresco.org/model/content/1.0}content&spellcheck.collate=true)}\",\"_field_mappings_\":{},\"_date_mappings_\":{},\"_range_mappings_\":{},\"_pivot_mappings_\":{},\"_interval_mappings_\":{},\"_stats_field_mappings_\":{},\"_stats_facet_mappings_\":{},\"_facet_function_mappings_\":{},\"response\":{\"numFound\":6,\"start\":0,\"maxScore\":0.7849362,\"docs\":[{\"DBID\":565,\"score\":0.7849362},{\"DBID\":566,\"score\":0.7849362},{\"DBID\":521,\"score\":0.3540957},{\"DBID\":514,\"score\":0.33025497},{\"DBID\":420,\"score\":0.32440513},{\"DBID\":415,\"score\":0.2780319}]},"
                + "\"facet_counts\":{\"facet_queries\":{\"small\":0,\"large\":0,\"xtra small\":3,\"xtra large\":0,\"medium\":8,\"XX large\":0},"
                + "\"facet_fields\":{\"content.size\":[\"Big\",8,\"Brown\",3,\"Fox\",5,\"Jumped\",2,\"somewhere\",3]},\"facet_dates\":{},\"facet_ranges\":{},\"facet_intervals\":{}\n" + "    },"
                + "\"spellcheck\":{\"searchInsteadFor\":\"alfresco\"},"
                + "\"processedDenies\":true, \"lastIndexedTx\":34}";

    @BeforeClass
    public static void setupTests() throws Exception
    {
        Map<String, UserInfo> mapUserInfo = new HashMap<>();
        mapUserInfo.put(AuthenticationUtil.getSystemUserName(), new UserInfo(AuthenticationUtil.getSystemUserName(), "sys", "sys"));
        Map<QName, Serializable> nodeProps = new HashMap<>();

        NodesImpl nodes = mock(NodesImpl.class);
        ServiceRegistry sr = mock(ServiceRegistry.class);
        nodes.setServiceRegistry(sr);

        when(nodes.getFolderOrDocument(notNull(NodeRef.class), any(), any(), any(), any())).thenAnswer(new Answer<Node>() {
            @Override
            public Node answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                NodeRef aNode = (NodeRef)args[0];
                if (StoreRef.STORE_REF_ARCHIVE_SPACESSTORE.equals(aNode.getStoreRef()))
                {
                    //Return NULL if its from the archive store.
                    return null;
                }
                return new Node(aNode, (NodeRef)args[1], nodeProps, mapUserInfo, sr);
            }
        });
        mapper = new ResultMapper(nodes);
    }

    @Test
    public void testNoResults() throws Exception
    {
        CollectionWithPagingInfo<Node> collection =  mapper.toCollectionWithPagingInfo(SearchQuery.EMPTY,new EmptyResultSet());
        assertNotNull(collection);
        assertFalse(collection.hasMoreItems());
        assertTrue(collection.getTotalItems() < 1);
        assertNull(collection.getContext());
    }

    @Test
    public void testToCollectionWithPagingInfo() throws Exception
    {
        ResultSet results = mockResultset(Arrays.asList(514l));
        CollectionWithPagingInfo<Node> collectionWithPage =  mapper.toCollectionWithPagingInfo(SearchQuery.EMPTY,results);
        assertNotNull(collectionWithPage);
        Long found = results.getNumberFound();
        assertEquals(found.intValue(), collectionWithPage.getTotalItems().intValue());
        Node firstNode = collectionWithPage.getCollection().stream().findFirst().get();
        assertNotNull(firstNode.getSearch().getScore());
    }

    @Test
    public void testToSearchContext() throws Exception
    {
        ResultSet results = mockResultset(Collections.emptyList());
        SearchContext searchContext = mapper.toSearchContext((SolrJSONResultSet) results);
        assertEquals(34l, searchContext.getConsistency().getlastTxId());
        assertEquals(6, searchContext.getFacetQueries().size());
   //     assertEquals("{!afts}creator:admin",searchContext.getFacetQueries().get(0).getLabel());
     //   assertEquals(1,searchContext.getFacetQueries().get(0).getCount());
        assertEquals("searchInsteadFor",searchContext.getSpellCheck().getType());
        assertEquals(1,searchContext.getSpellCheck().getSuggestions().size());
        assertEquals("alfresco",searchContext.getSpellCheck().getSuggestions().get(0));
        assertEquals("content.size",searchContext.getFacetsFields().getLabel());
        assertEquals(5,searchContext.getFacetsFields().getBuckets().size());
    }

    @Test
    public void testIsNullContext() throws Exception
    {
        assertTrue(mapper.isNullContext(new SearchContext(0l,null,null,null)));
        assertFalse(mapper.isNullContext(new SearchContext(1l,null,null,null)));
        assertFalse(mapper.isNullContext(new SearchContext(0l,null,null,new SpellCheckContext(null, null))));
        assertFalse(mapper.isNullContext(new SearchContext(0l,Arrays.asList(new FacetQueryContext(null, 0)),null,null)));
        assertFalse(mapper.isNullContext(new SearchContext(0l,null,new FacetFieldContext(null, null),null)));
    }

    private ResultSet mockResultset(List<Long> archivedNodes) throws JSONException
    {

        NodeService nodeService = mock(NodeService.class);
        when(nodeService.getNodeRef(any())).thenAnswer(new Answer<NodeRef>() {
            @Override
            public NodeRef answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                //If the DBID is in the list archivedNodes, instead of returning a noderef return achivestore noderef
                if (archivedNodes.contains(args[0])) return new NodeRef(StoreRef.STORE_REF_ARCHIVE_SPACESSTORE, GUID.generate());;
                return new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, GUID.generate());
            }
        });

        SearchParameters sp = new SearchParameters();
        sp.setBulkFetchEnabled(false);
        JSONObject json = new JSONObject(new JSONTokener(JSON_REPONSE));
        ResultSet results = new SolrJSONResultSet(json,sp,nodeService, null, LimitBy.FINAL_SIZE, 10);
        return results;
    }
/**
    private Params mockParams(SearchQuery searchQuery)
    {
        Params params = mock(Params.class);
        when(params.getInclude()).thenReturn(new ArrayList<String>());
        when(params.getPassedIn()).thenReturn(searchQuery);
        return params;
    }
**/
}
