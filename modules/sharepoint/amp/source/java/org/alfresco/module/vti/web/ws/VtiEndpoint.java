/*
 * #%L
 * Alfresco Sharepoint Protocol
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

package org.alfresco.module.vti.web.ws;

import org.alfresco.module.vti.handler.alfresco.UrlHelper;


/**
* Interface that must implement all the Vti endpoints realizations
*   
* @author Stas Sokolovsky
*
*/
public interface VtiEndpoint
{
    /**
     * Executes target endpoint method
     * 
     * @param soapRequest Vti Soap Request ({@link VtiSoapRequest})
     * @param soapResponse Vti Soap Response ({@link VtiSoapResponse})
     */
    public void execute(VtiSoapRequest soapRequest, VtiSoapResponse soapResponse) throws Exception;
    
    /**
     * @return the name of the endpoint
     */
    public String getName();
    
    /**
     * @return the namespace of the endpoint
     */
    public String getNamespace();
    
    public UrlHelper getUrlHelper();

    public String getResponseTagName();

    public String getResultTagName();
}
