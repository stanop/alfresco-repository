/*
 * #%L
 * Alfresco Repository
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
package org.alfresco.repo.action.executer;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.ActionImpl;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.dictionary.InvalidTypeException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.alfresco.test_category.BaseSpringTestsCategory;
import org.alfresco.util.BaseAlfrescoSpringTest;
import org.alfresco.util.GUID;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.transaction.annotation.Transactional;

/**
 * Specialise type action execution test
 * 
 * @author Roy Wetherall
 */
@Category(BaseSpringTestsCategory.class)
@Transactional
public class SpecialiseTypeActionExecuterTest extends BaseAlfrescoSpringTest
{    
    /**
     * The test node reference
     */
    private NodeRef nodeRef;
    
    /**
     * The specialise action executer
     */
    private SpecialiseTypeActionExecuter executer;
    
    /**
     * Id used to identify the test action created
     */
    private final static String ID = GUID.generate();
    
    private NodeRef nodeRefTest;
    
    /**
     * Called at the begining of all tests
     */
    @Before
    public void before() throws Exception
    {
        super.before();
        
        // Create the node used for tests
        this.nodeRef = this.nodeService.createNode(
                this.rootNodeRef,
                ContentModel.ASSOC_CHILDREN,
                QName.createQName("{test}testnode"),
                ContentModel.TYPE_CONTENT).getChildRef();
        
        // Get the executer instance 
        this.executer = (SpecialiseTypeActionExecuter)this.applicationContext.getBean(SpecialiseTypeActionExecuter.NAME);
    
        // Create the node used for tests
        this.nodeRefTest = this.nodeService.createNode(
                this.rootNodeRef,
                ContentModel.ASSOC_CHILDREN,
                QName.createQName("{test}testnode"),
                ContentModel.TYPE_CONTENT).getChildRef();
    
    }
    
    /**
     * Test execution
     */
    @Test
    public void testExecution()
    {
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
        
        // Check the type of the node
        assertEquals(ContentModel.TYPE_CONTENT, this.nodeService.getType(this.nodeRef));
        
        // Execute the action
        ActionImpl action = new ActionImpl(null, ID, SpecialiseTypeActionExecuter.NAME, null);
        action.setParameterValue(SpecialiseTypeActionExecuter.PARAM_TYPE_NAME, ContentModel.TYPE_FOLDER);
        this.executer.execute(action, this.nodeRef);
        
        // Check that the node's type has not been changed since it would not be a specialisation
        assertEquals(ContentModel.TYPE_CONTENT, this.nodeService.getType(this.nodeRef));
        
        // Execute the action agian
        action.setParameterValue(SpecialiseTypeActionExecuter.PARAM_TYPE_NAME, ContentModel.TYPE_DICTIONARY_MODEL);
        this.executer.execute(action, this.nodeRef);
        
        // Check that the node's type has now been changed
        assertEquals(ContentModel.TYPE_DICTIONARY_MODEL, this.nodeService.getType(this.nodeRef));
        
        //Create user as coordinator without administrator writes
        String userName = "bob" + GUID.generate();
        createUser(userName);
        PermissionService permissionService = (PermissionService)applicationContext.getBean("PermissionService");
        permissionService.setPermission(nodeRefTest, userName, PermissionService.COORDINATOR, true);
        
        AuthenticationUtil.setFullyAuthenticatedUser(userName);
        
        // Check the type of the node
        assertEquals(ContentModel.TYPE_CONTENT, this.nodeService.getType(this.nodeRefTest));
        
        // Execute the action
        action = new ActionImpl(null, ID, SpecialiseTypeActionExecuter.NAME, null);
        action.setParameterValue(SpecialiseTypeActionExecuter.PARAM_TYPE_NAME, ContentModel.TYPE_FOLDER);
        this.executer.execute(action, this.nodeRefTest);
        
        // Check that the node's type has not been changed since it would not be a specialisation
        assertEquals(ContentModel.TYPE_CONTENT, this.nodeService.getType(this.nodeRefTest));
        
        try
        {
            // Execute the action again
            action.setParameterValue(SpecialiseTypeActionExecuter.PARAM_TYPE_NAME, ContentModel.TYPE_DICTIONARY_MODEL);
            this.executer.execute(action, this.nodeRefTest);

            fail("the creation should throw InvalidTypeException");
        }
        catch (InvalidTypeException ex)
        {

        }
    }
    
   
}