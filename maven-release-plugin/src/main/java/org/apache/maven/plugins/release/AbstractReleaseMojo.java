package org.apache.maven.plugins.release;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.release.helpers.ReleaseProgressTracker;
import org.apache.maven.plugins.release.helpers.ScmHelper;
import org.apache.maven.scm.manager.ScmManager;
import org.apache.maven.scm.provider.starteam.repository.StarteamScmProviderRepository;
import org.apache.maven.scm.repository.ScmRepository;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.Settings;
import org.codehaus.plexus.components.interactivity.InputHandler;

/**
 * @author <a href="mailto:jdcasey@apache.org">John Casey</a>
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public abstract class AbstractReleaseMojo
    extends AbstractMojo
{
    /**
     * @component
     */
    private ScmManager scmManager;

    /**
     * @component
     */
    private InputHandler inputHandler;

    /**
     * @parameter expression="${settings}"
     * @required
     * @readonly
     */
    private Settings settings;

    
    private ScmHelper scmHelper;

    protected abstract ReleaseProgressTracker getReleaseProgress()
        throws MojoExecutionException;
    
    protected InputHandler getInputHandler()
    {
        return inputHandler;
    }

    protected Settings getSettings()
    {
        return settings;
    }
    
    protected ScmHelper getScm( String directory )
        throws MojoExecutionException
    {
        if ( scmHelper == null )
        {
            scmHelper = new ScmHelper();

            scmHelper.setScmManager( scmManager );

            ReleaseProgressTracker releaseProgress = getReleaseProgress();

            scmHelper.setUrl( releaseProgress.getScmUrl() );

            scmHelper.setTag( releaseProgress.getScmTag() );

            scmHelper.setTagBase( releaseProgress.getScmTagBase() );

            scmHelper.setUsername( releaseProgress.getUsername() );

            scmHelper.setPassword( releaseProgress.getPassword() );
        }

        scmHelper.setWorkingDirectory( directory );
        
        loadStarteamUsernamePassword( scmHelper );

        return scmHelper;
    }
    
    private ScmManager getScmManager()
    {
        return this.scmManager;
    }
    
    /**
     * Load starteam username/password from settings if needed
     * @param scmHelper
     * @throws MojoExecutionException
     */
    private void loadStarteamUsernamePassword( ScmHelper scmHelper )
        throws MojoExecutionException
    {
        if ( scmHelper.getUsername() == null || scmHelper.getPassword() == null )
        {
            ScmRepository repository = null;
     
            try
            {
                repository = getScmManager().makeScmRepository( scmHelper.getUrl() );
            }
            catch ( Exception e )
            {
                throw new MojoExecutionException ( "Can't load the scm provider.", e );
            }

            if ( repository.getProvider().equals( "starteam" ) )
            {
                StarteamScmProviderRepository starteamRepo = (StarteamScmProviderRepository) repository.getProviderRepository();
                    
                String starteamAddress = starteamRepo.getHost();
                    
                int starteamPort = starteamRepo.getPort();
                    
                if ( starteamPort != 0 )
                {
                    starteamAddress += ":" + starteamPort;
                }
                    
                Server server = this.settings.getServer( starteamAddress );
                    
                if ( server != null )
                {
                    if ( scmHelper.getUsername() == null )
                    {
                        scmHelper.setUsername( server.getUsername() );
                    }
                
                    if ( scmHelper.getPassword() == null )
                    {
                        scmHelper.setPassword( server.getPassword() );
                    }
                 }
              }
        }
        
    }
}
