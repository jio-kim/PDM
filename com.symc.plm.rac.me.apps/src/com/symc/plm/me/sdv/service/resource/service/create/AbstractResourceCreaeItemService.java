/**
 * 
 */
package com.symc.plm.me.sdv.service.resource.service.create;

import org.sdv.core.common.data.IDataMap;

import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.util.Registry;

/**
 * Class Name : AbstractResourceCreaeItemService
 * Class Description :
 * 
 * @date 2013. 12. 16.
 * 
 */
public abstract class AbstractResourceCreaeItemService {
    IDataMap datamap = null;
    public Registry registry;

    public AbstractResourceCreaeItemService(IDataMap datamap) throws Exception {
        this.datamap = datamap;
        registry = Registry.getRegistry("com.symc.plm.me.sdv.view.resource.resource");
        validate();
    }

    abstract void validate() throws Exception;

    abstract public TCComponentItemRevision create() throws Exception;

}
