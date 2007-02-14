/********************************************************************************
 * Copyright (c) 2006 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * {Name} (company) - description of contribution.
 ********************************************************************************/

package org.eclipse.rse.ui.propertypages;


import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.rse.core.IRSEPreferenceNames;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.SystemPreferencesManager;
import org.eclipse.rse.core.model.ISystemPreferenceChangeEvents;
import org.eclipse.rse.internal.model.SystemPreferenceChangeEvent;
import org.eclipse.rse.ui.ISystemPreferencesConstants;
import org.eclipse.rse.ui.Mnemonics;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemResources;
import org.eclipse.rse.ui.SystemWidgetHelpers;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;


/**
 * Root preference page for Remote Systems Plugin
 */
public class RemoteSystemsPreferencePage 
       extends FieldEditorPreferencePage implements IWorkbenchPreferencePage, 
                                                    ISystemPreferencesConstants
{
    private SystemBooleanFieldEditor showFilterPoolsEditor;
    private SystemBooleanFieldEditor qualifyConnectionNamesEditor;
    private SystemBooleanFieldEditor rememberStateEditor;
    private SystemBooleanFieldEditor useDeferredQueryEditor;

	// yantzi: artemis 60, restore from cache when available
	private SystemBooleanFieldEditor restoreFromCache;
	private Composite innerComposite;

	private SystemTypeFieldEditor systemTypesEditor;
    private SystemBooleanFieldEditor showNewConnectionPromptEditor;
    private boolean lastShowFilterPoolsValue = false;
    private boolean lastQualifyConnectionNamesValue = false;
    private boolean lastRememberStateValue = true; // changed in R2 by Phil. Not sure about migration!
//	private boolean lastRestoreFromCacheValue = true; // yantzi: new in artemis 6.0
    private boolean lastShowNewConnectionPromptValue = true;
    private boolean lastUseDeferredQueryValue = false;
	
	/**
	 * Constructor
	 */
	public RemoteSystemsPreferencePage() 
	{
		super(GRID);
		setTitle(SystemResources.RESID_PREF_ROOT_PAGE);		
		setPreferenceStore(RSEUIPlugin.getDefault().getPreferenceStore());
//		setDescription(SystemResources.RESID_PREF_ROOT_TITLE); // removed since this is not read by screen reader
	}
	/**
	 * We intercept to set the help
	 */
	public void createControl(Composite parent) 
	{
		super.createControl(parent);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), RSEUIPlugin.HELPPREFIX+"rsep0000");		 //$NON-NLS-1$
	}
	
	/**
	 * GUI widgets for preferences page
	 */
	protected void createFieldEditors() 
	{
        // DEFAULT SYSTEM TYPE		
		SystemComboBoxFieldEditor systemTypeEditor = new SystemComboBoxFieldEditor(
			IRSEPreferenceNames.SYSTEMTYPE,
			SystemResources.RESID_PREF_SYSTEMTYPE_PREFIX_LABEL,
			RSECorePlugin.getDefault().getRegistry().getSystemTypeNames(),
			true, // readonly
			getFieldEditorParent()
		);
		systemTypeEditor.setToolTipText(SystemResources.RESID_PREF_SYSTEMTYPE_PREFIX_TOOLTIP);
		addField(systemTypeEditor);

        // ENABLED STATE AND DEFAULT USERID PER SYSTEM TYPE 
        systemTypesEditor = new SystemTypeFieldEditor(
            ISystemPreferencesConstants.SYSTEMTYPE_VALUES,
            SystemResources.RESID_PREF_USERID_PERTYPE_PREFIX_LABEL,
            getFieldEditorParent()
        );
        addField(systemTypesEditor); 
        systemTypesEditor.setToolTipText(SystemResources.RESID_PREF_USERID_PERTYPE_PREFIX_TOOLTIP);
		
        // QUALIFY CONNECTION NAMES
		qualifyConnectionNamesEditor = new SystemBooleanFieldEditor(
			ISystemPreferencesConstants.QUALIFY_CONNECTION_NAMES,
			SystemResources.RESID_PREF_QUALIFYCONNECTIONNAMES_PREFIX_LABEL,
			getFieldEditorParent()
		);
		addField(qualifyConnectionNamesEditor);
		qualifyConnectionNamesEditor.setToolTipText(SystemResources.RESID_PREF_QUALIFYCONNECTIONNAMES_PREFIX_TOOLTIP);
		lastQualifyConnectionNamesValue = getPreferenceStore().getBoolean(qualifyConnectionNamesEditor.getPreferenceName());										

        // SHOW FILTER POOLS 
	    showFilterPoolsEditor = new SystemBooleanFieldEditor(
			ISystemPreferencesConstants.SHOWFILTERPOOLS,
			SystemResources.RESID_PREF_SHOWFILTERPOOLS_PREFIX_LABEL,
			getFieldEditorParent()
		);
		addField(showFilterPoolsEditor);
		showFilterPoolsEditor.setToolTipText(SystemResources.RESID_PREF_SHOWFILTERPOOLS_PREFIX_TOOLTIP);
		lastShowFilterPoolsValue = getPreferenceStore().getBoolean(showFilterPoolsEditor.getPreferenceName());

        // SHOW "NEW CONNECTION..." PROMPT INSIDE REMOTE SYSTEMS VIEW
	    showNewConnectionPromptEditor = new SystemBooleanFieldEditor(
			ISystemPreferencesConstants.SHOWNEWCONNECTIONPROMPT,
			SystemResources.RESID_PREF_SHOWNEWCONNECTIONPROMPT_PREFIX_LABEL,
			getFieldEditorParent()
		);
		addField(showNewConnectionPromptEditor);
		showNewConnectionPromptEditor.setToolTipText(SystemResources.RESID_PREF_SHOWNEWCONNECTIONPROMPT_PREFIX_TOOLTIP);
		lastShowNewConnectionPromptValue = getPreferenceStore().getBoolean(showNewConnectionPromptEditor.getPreferenceName());

        // REMEMBER STATE
		rememberStateEditor = new SystemBooleanFieldEditor(
			ISystemPreferencesConstants.REMEMBER_STATE,
			SystemResources.RESID_PREF_REMEMBERSTATE_PREFIX_LABEL,
			getFieldEditorParent()
		);
		addField(rememberStateEditor);
		rememberStateEditor.setToolTipText(SystemResources.RESID_PREF_REMEMBERSTATE_PREFIX_TOOLTIP);
		lastRememberStateValue = getPreferenceStore().getBoolean(rememberStateEditor.getPreferenceName());		

		// Restore from cache
		innerComposite = SystemWidgetHelpers.createComposite(getFieldEditorParent(), SWT.NULL);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalIndent = 20;
		innerComposite.setLayoutData(gd);
		restoreFromCache = new SystemBooleanFieldEditor(
			ISystemPreferencesConstants.RESTORE_STATE_FROM_CACHE,
			SystemResources.RESID_PREF_RESTOREFROMCACHE_PREFIX_LABEL,
			innerComposite
		);
		restoreFromCache.setEnabled(lastRememberStateValue, innerComposite);
		addField(restoreFromCache);
		restoreFromCache.setToolTipText(SystemResources.RESID_PREF_RESTOREFROMCACHE_PREFIX_TOOLTIP);
//		lastRestoreFromCacheValue = getPreferenceStore().getBoolean(ISystemPreferencesConstants.RESTORE_STATE_FROM_CACHE);


		// USE DEFERRED QUERY
		useDeferredQueryEditor = new SystemBooleanFieldEditor(
		        IRSEPreferenceNames.USE_DEFERRED_QUERIES,
		        SystemResources.RESID_PREF_USEDEFERREDQUERIES_PREFIX_LABEL,
		        getFieldEditorParent())
		        ;
		useDeferredQueryEditor.setEnabled(false); // disable this because we want it always to be true
		addField(useDeferredQueryEditor);
		useDeferredQueryEditor.setToolTipText(SystemResources.RESID_PREF_USEDEFERREDQUERIES_PREFIX_TOOLTIP);
		lastUseDeferredQueryValue = true; // enforcing this to true now
		//getPreferenceStore().getBoolean(useDeferredQueryEditor.getPreferenceName());
		
		
		/** FIXME - UDA should not be so coupled to core
		 * might need a new preference page for this
        // CASCADE USER-DEFINED ACTIONS BY PROFILE
		SystemBooleanFieldEditor cascadeUDAsEditor = new SystemBooleanFieldEditor(
			ISystemPreferencesConstants.CASCADE_UDAS_BYPROFILE,
			SystemUDAResources.RESID_PREF_UDAS_CASCADEBYPROFILE_LABEL,
			getFieldEditorParent()
		);
		addField(cascadeUDAsEditor);
		cascadeUDAsEditor.setToolTipText(SystemUDAResources.RESID_PREF_UDAS_CASCADEBYPROFILE_TOOLTIP);
		lastCascadeUDAsValue = getPreferenceStore().getBoolean(cascadeUDAsEditor.getPreferenceName());		
		**/	
		// set mnemonics
        (new Mnemonics()).setOnPreferencePage(true).setMnemonics(getFieldEditorParent());

	}
	
	public void init(IWorkbench workbench) 
	{
	}

    /**
	 * we don't use this after all because it causes an event to be fired as the
	 *  user makes each change. We prefer to wait until Apply or Defaults are pressed.
	 */
	public void propertyChange(PropertyChangeEvent event)
	{
       
        // yantzi: artemis 6.0
        if (event.getSource() == rememberStateEditor)
        {
        	Object newValue = event.getNewValue();
        	if (newValue instanceof Boolean)
        	{
        		restoreFromCache.setEnabled(((Boolean) newValue).booleanValue(), innerComposite);
        	}
        }

        super.propertyChange(event);		
	}

    /**
     * Override of parent so we can fire changes to our views
     */
    public boolean performOk() 
    {
    	boolean ok = super.performOk();
    	SystemPreferencesManager.savePreferences(); // better save to disk, just in case.
        if (!RSEUIPlugin.getDefault().isSystemRegistryActive())    	
          	return ok;
    	if (showFilterPoolsEditor != null)
    	{
    	   	boolean newValue = showFilterPoolsEditor.getBooleanValue();
    	   	if (newValue != lastShowFilterPoolsValue)
    	   	{
    	     	RSEUIPlugin.getDefault().getSystemRegistry().setShowFilterPools(newValue);
    	     	firePreferenceChangeEvent(ISystemPreferenceChangeEvents.EVENT_SHOWFILTERPOOLS,lastShowFilterPoolsValue,newValue);
    	   	}
    	   	lastShowFilterPoolsValue = newValue;
    	}
    	if (showNewConnectionPromptEditor != null)
    	{
    	   	boolean newValue = showNewConnectionPromptEditor.getBooleanValue();
    	   	if (newValue != lastShowNewConnectionPromptValue)
    	   	{
    	     	RSEUIPlugin.getDefault().getSystemRegistry().setShowNewHostPrompt(newValue);
    	   	}
    	   	lastShowNewConnectionPromptValue = newValue;    		
    	}
    	if (qualifyConnectionNamesEditor != null)
    	{
    	   	boolean newValue = qualifyConnectionNamesEditor.getBooleanValue();
    	   	if (newValue != lastQualifyConnectionNamesValue)
    	   	{
    	     	RSEUIPlugin.getDefault().getSystemRegistry().setQualifiedHostNames(newValue);
    	     	firePreferenceChangeEvent(ISystemPreferenceChangeEvents.EVENT_QUALIFYCONNECTIONNAMES,lastQualifyConnectionNamesValue,newValue);
    	   	}
    	   	lastQualifyConnectionNamesValue = newValue;    		
    	}
		if (rememberStateEditor != null)
		{
		  	boolean newValue = rememberStateEditor.getBooleanValue();
		   	if (newValue != lastRememberStateValue)
		   	{
			 	firePreferenceChangeEvent(ISystemPreferenceChangeEvents.EVENT_RESTORESTATE,lastRememberStateValue,newValue);
		   	}
		   	lastRememberStateValue = newValue;    		
		}
		if (useDeferredQueryEditor != null)
		{
		  	boolean newValue = useDeferredQueryEditor.getBooleanValue();
		   	if (newValue != lastUseDeferredQueryValue)
		   	{
			 	firePreferenceChangeEvent(ISystemPreferenceChangeEvents.EVENT_RESTORESTATE,lastUseDeferredQueryValue,newValue);
		   	}
		   	lastUseDeferredQueryValue = newValue;    		
		}

    	return ok;
    }	
    
    /**
     * Fire a preference change event
     */
    private void firePreferenceChangeEvent(int type, boolean oldValue, boolean newValue)
    {
    	RSEUIPlugin.getDefault().getSystemRegistry().fireEvent(
    	  new SystemPreferenceChangeEvent(type,
    	                                  oldValue ? Boolean.TRUE : Boolean.FALSE,
    	                                  newValue ? Boolean.TRUE : Boolean.FALSE));
    } 
}